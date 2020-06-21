package br.com.douglas444.minas;

import br.com.douglas444.minas.heater.Heater;
import br.com.douglas444.minas.interceptor.MINASInterceptor;
import br.com.douglas444.minas.interceptor.context.NoveltyDetectionContext;
import br.com.douglas444.mltk.clustering.kmeans.KMeansPlusPlus;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.DynamicConfusionMatrix;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

public class MINAS {

    private long timestamp;
    private int noveltyCount;
    private boolean warmed;
    private int heaterCapacity;

    private final DecisionModel decisionModel;
    private final List<Sample> temporaryMemory;
    private final DecisionModel sleepMemory ;
    private final DynamicConfusionMatrix confusionMatrix;

    private final int temporaryMemoryMaxSize;
    private final int minimumClusterSize;
    private final int windowSize;
    private final int microClusterLifespan;
    private final int sampleLifespan;
    private final long randomGeneratorSeed;
    private final int noveltyDetectionNumberOfClusters;
    private final Heater heater;

    private final MINASInterceptor interceptorCollection;

    public MINAS(int temporaryMemoryMaxSize,
                 int minimumClusterSize,
                 int windowSize,
                 int microClusterLifespan,
                 int sampleLifespan,
                 int heaterCapacity,
                 boolean incrementallyUpdateDecisionModel,
                 int heaterInitialBufferSize,
                 int heaterNumberOfClustersPerLabel,
                 int noveltyDetectionNumberOfClusters,
                 long randomGeneratorSeed,
                 MINASInterceptor interceptorCollection) {

        this.timestamp = 1;
        this.noveltyCount = 0;
        this.warmed = false;
        this.temporaryMemory = new ArrayList<>();

        this.heaterCapacity = heaterCapacity;
        this.temporaryMemoryMaxSize = temporaryMemoryMaxSize;
        this.minimumClusterSize = minimumClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.randomGeneratorSeed = randomGeneratorSeed;

        this.interceptorCollection = interceptorCollection;
        this.heater = new Heater(heaterInitialBufferSize, heaterNumberOfClustersPerLabel, this.randomGeneratorSeed);
        this.decisionModel = new DecisionModel(incrementallyUpdateDecisionModel, interceptorCollection);
        this.sleepMemory = new DecisionModel(incrementallyUpdateDecisionModel, interceptorCollection);
        this.confusionMatrix = new DynamicConfusionMatrix();

    }

    private void warmUp(final Sample sample) {

        assert !warmed;

        if (!this.confusionMatrix.isLabelKnown(sample.getY())) {
            this.confusionMatrix.addKnownLabel(sample.getY());
        }

        this.heater.process(sample);

        if (this.heaterCapacity > 1) {
            this.heaterCapacity--;
        } else {

            this.warmed = true;

            final List<MicroCluster> microClusters = this.heater.getResult().stream()
                    .filter(microCluster -> microCluster.getN() >= 3)
                    .collect(Collectors.toCollection(ArrayList::new));

            microClusters.forEach(microCluster -> microCluster.setTimestamp(this.timestamp));
            this.decisionModel.merge(microClusters);
        }

    }

    private void detectNoveltyAndUpdate() {

        final List<Cluster> clusters = KMeansPlusPlus
                .execute(this.temporaryMemory, this.noveltyDetectionNumberOfClusters, this.randomGeneratorSeed)
                .stream()
                .filter(cluster -> cluster.getSize() >= this.minimumClusterSize)
                .sorted(Comparator.comparing(cluster -> cluster.getMostRecentSample().getT()))
                .collect(Collectors.toList());

        for (Cluster cluster : clusters) {

            if (this.decisionModel.calculateSilhouette(cluster) <= 0) {
                continue;
            }

            this.temporaryMemory.removeAll(cluster.getSamples());
            final MicroCluster micro = new MicroCluster(cluster, cluster.getMostRecentSample().getT());

            this.decisionModel.classify(micro).ifExplainedOrElse((closest) -> {

                this.interceptorCollection.MICRO_CLUSTER_EXPLAINED_INTERCEPTOR
                        .with(new NoveltyDetectionContext()
                                .minas(this)
                                .closestMicroCluster(closest)
                                .targetMicroCluster(micro)
                                .targetSamples(cluster.getSamples()))
                        .executeOrDefault(() -> {
                            this.addExtension(micro, closest);
                        });

            }, () -> {

                this.sleepMemory.classify(micro).ifExplainedOrElse((closest) -> {

                    this.interceptorCollection.MICRO_CLUSTER_EXPLAINED_BY_ASLEEP_INTERCEPTOR
                            .with(new NoveltyDetectionContext()
                                    .minas(this)
                                    .closestMicroCluster(closest)
                                    .targetMicroCluster(micro)
                                    .targetSamples(cluster.getSamples()))
                            .executeOrDefault(() -> {
                                this.awake(micro);
                                this.addExtension(micro, closest);
                            });

                }, (optionalClosest) -> {

                    this.interceptorCollection.MICRO_CLUSTER_UNEXPLAINED_INTERCEPTOR
                            .with(new NoveltyDetectionContext()
                                    .minas(this)
                                    .closestMicroCluster(optionalClosest.orElse(null))
                                    .targetMicroCluster(micro)
                                    .targetSamples(cluster.getSamples()))
                            .executeOrDefault(() -> {
                                this.addNovelty(micro);
                            });
                });
            });

            for (Sample sample : cluster.getSamples()) {
                this.confusionMatrix.updatedDelayed(sample.getY(), micro.getLabel(),
                        micro.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
            }

        }
    }

    public ClassificationResult process(final Sample sample) {

        if (!this.warmed) {
            this.warmUp(sample);
            return new ClassificationResult(null, false);
        }

        sample.setT(this.timestamp);

        final ClassificationResult classificationResult = this.decisionModel.classify(sample);

        classificationResult.ifExplainedOrElse((closestMicroCluster) -> {

            this.confusionMatrix.addPrediction(sample.getY(), closestMicroCluster.getLabel(),
                    closestMicroCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);

        }, () -> {
            this.temporaryMemory.add(sample);
            this.confusionMatrix.addUnknown(sample.getY());
            if (this.temporaryMemory.size() >= this.temporaryMemoryMaxSize) {
                this.detectNoveltyAndUpdate();
            }
        });

        if (this.timestamp % this.windowSize == 0) {
            final List<MicroCluster> inactiveMicroClusters = this.decisionModel
                    .extractInactiveMicroClusters(this.timestamp, microClusterLifespan);
            this.sleepMemory.merge(inactiveMicroClusters);
            this.temporaryMemory.removeIf(p -> p.getT() < this.timestamp - this.sampleLifespan);
        }

        ++this.timestamp;

        return classificationResult;

    }

    public void awake(MicroCluster microCluster) {
        this.sleepMemory.remove(microCluster);
        this.decisionModel.merge(microCluster);
    }

    public void addNovelty(MicroCluster microCluster) {
        microCluster.setMicroClusterCategory(MicroClusterCategory.NOVELTY);
        microCluster.setLabel(this.noveltyCount++);
        this.decisionModel.merge(microCluster);
    }

    public void addExtension(MicroCluster microCluster, MicroCluster closestMicroCluster) {
        microCluster.setMicroClusterCategory(closestMicroCluster.getMicroClusterCategory());
        microCluster.setLabel(closestMicroCluster.getLabel());
        this.decisionModel.merge(microCluster);
    }

    public List<MicroCluster> getDecisionModel() {
        return this.decisionModel.getMicroClusters();
    }

    public long getTimestamp() {
        return timestamp - 1;
    }

    public DynamicConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public double calculateCER() {
        return this.confusionMatrix.cer();
    }

    public double calculateUnkR() {
        return this.confusionMatrix.unkR();
    }

    public boolean isWarmed() {
        return warmed;
    }

    public int getNoveltyCount() {
        return noveltyCount;
    }

}