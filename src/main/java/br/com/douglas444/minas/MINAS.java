package br.com.douglas444.minas;

import br.com.douglas444.minas.heater.Heater;
import br.com.douglas444.streams.algorithms.KMeansPlusPlus;
import br.com.douglas444.streams.datastructures.Cluster;
import br.com.douglas444.streams.datastructures.DynamicConfusionMatrix;
import br.com.douglas444.streams.datastructures.Sample;
import br.ufu.facom.pcf.core.Context;
import br.ufu.facom.pcf.core.Interceptor;
import br.ufu.facom.pcf.core.ResponseContext;

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
    private final Random random;
    private final int noveltyDetectionNumberOfClusters;
    private final boolean pcfTightIntegration;
    private final Heater heater;

    private Interceptor interceptor;

    public MINAS(final int temporaryMemoryMaxSize,
                 final int minimumClusterSize,
                 final int windowSize,
                 final int microClusterLifespan,
                 final int sampleLifespan,
                 final int heaterCapacity,
                 final boolean incrementallyUpdateDecisionModel,
                 final int heaterInitialBufferSize,
                 final int heaterNumberOfClustersPerLabel,
                 final int noveltyDetectionNumberOfClusters,
                 final boolean pcfTightIntegration,
                 final long randomGeneratorSeed) {

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
        this.pcfTightIntegration = pcfTightIntegration;
        this.random = new Random(randomGeneratorSeed);

        this.heater = new Heater(heaterInitialBufferSize, heaterNumberOfClustersPerLabel, this.random);
        this.decisionModel = new DecisionModel(incrementallyUpdateDecisionModel);
        this.sleepMemory = new DecisionModel(incrementallyUpdateDecisionModel);
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
                .execute(this.temporaryMemory, this.noveltyDetectionNumberOfClusters, this.random)
                .stream()
                .filter(cluster -> cluster.getSize() >= this.minimumClusterSize)
                .sorted(Comparator.comparing(cluster -> cluster.getMostRecentSample().getT()))
                .collect(Collectors.toList());

        for (Cluster cluster : clusters) {

            if (this.decisionModel.calculateSilhouette(cluster) <= 0) {
                continue;
            }

            this.temporaryMemory.removeAll(cluster.getSamples());
            final MicroCluster microCluster = new MicroCluster(cluster, cluster.getMostRecentSample().getT());

            this.decisionModel.classify(microCluster).ifExplainedOrElse((closest) -> {

                ResponseContext responseContext = null;

                if (this.interceptor != null) {

                    final Context context = PCF.buildContext(
                            microCluster,
                            cluster.getSamples(),
                            closest.getMicroClusterCategory(),
                            this.decisionModel.getMicroClusters(),
                            this.sleepMemory.getMicroClusters());

                    if (context.getKnownLabels().size() > 1) {
                        responseContext = this.interceptor.intercept(context);
                    }
                }

                if (responseContext != null && this.pcfTightIntegration) {
                    tightIntegration(cluster, responseContext);
                } else {
                    this.addExtension(microCluster, closest.getMicroClusterCategory(), closest.getLabel());
                    for (Sample sample : cluster.getSamples()) {
                        this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                                microCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
                    }
                }

            }, () -> {

                this.sleepMemory.classify(microCluster).ifExplainedOrElse((closest) -> {

                    ResponseContext responseContext = null;

                    if (this.interceptor != null) {

                        final Context context = PCF.buildContext(
                                microCluster,
                                cluster.getSamples(),
                                closest.getMicroClusterCategory(),
                                this.decisionModel.getMicroClusters(),
                                this.sleepMemory.getMicroClusters());

                        if (context.getKnownLabels().size() > 1) {
                            responseContext = this.interceptor.intercept(context);
                        }
                    }

                    if (responseContext != null && this.pcfTightIntegration) {
                        tightIntegration(cluster, responseContext);
                    } else {
                        this.awake(closest);
                        this.addExtension(microCluster, closest.getMicroClusterCategory(), closest.getLabel());
                        for (Sample sample : cluster.getSamples()) {
                            this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                                    microCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
                        }
                    }

                }, (optionalClosest) -> {

                    ResponseContext responseContext = null;

                    if (this.interceptor != null) {

                        final Context context = PCF.buildContext(
                                microCluster,
                                cluster.getSamples(),
                                MicroClusterCategory.NOVELTY,
                                this.decisionModel.getMicroClusters(),
                                this.sleepMemory.getMicroClusters());

                        if (context.getKnownLabels().size() > 1) {
                            responseContext = this.interceptor.intercept(context);
                        }
                    }

                    if (responseContext != null && this.pcfTightIntegration) {
                        tightIntegration(cluster, responseContext);
                    } else {
                        this.addNovelty(microCluster);
                        for (Sample sample : cluster.getSamples()) {
                            this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                                    microCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
                        }
                    }

                });

            });


        }
    }

    private void tightIntegration(Cluster cluster, ResponseContext responseContext) {
        for (Cluster subCluster : handleImpurity(responseContext, cluster.getSamples())) {
            final MicroCluster subMicroCluster = new MicroCluster(subCluster, subCluster.getMostRecentSample().getT());
            this.addExtension(subMicroCluster, MicroClusterCategory.KNOWN, subCluster.getLabel());
            for (Sample sample : subCluster.getSamples()) {
                this.confusionMatrix.updatedDelayed(sample.getY(), subCluster.getLabel(),
                        subMicroCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
            }
        }
    }

    public List<Cluster> handleImpurity(final ResponseContext responseContext, final List<Sample> samples) {

        final List<Cluster> clusters = new ArrayList<>();

        if (responseContext.getLabeledSamplesAttributes().length == 1) {
            final Cluster cluster = new Cluster(samples);
            cluster.setLabel(responseContext.getLabeledSamplesLabels()[0]);
            clusters.add(cluster);
            return clusters;
        }

        final List<Sample> centroids = new ArrayList<>();

        for (int i = 0; i < responseContext.getLabeledSamplesAttributes().length; ++i) {
            centroids.add(new Sample(
                    responseContext.getLabeledSamplesAttributes()[i],
                    responseContext.getLabeledSamplesLabels()[i]));
        }

        final HashMap<Sample, List<Sample>> samplesByCentroid = new HashMap<>();

        centroids.forEach(centroid -> samplesByCentroid.put(centroid, new ArrayList<>()));

        samples.forEach(sample -> {
            final Sample closestCentroid = sample.calculateClosestSample(centroids);
            samplesByCentroid.get(closestCentroid).add(sample);
        });


        samplesByCentroid.forEach((key, value) -> {
            if (!value.isEmpty()) {
                final Cluster cluster = new Cluster(value);
                cluster.setLabel(key.getY());
                clusters.add(cluster);
            }
        });

        return clusters;

    }

    public Classification process(final Sample sample) {

        if (!this.warmed) {
            this.warmUp(sample);
            return new Classification(null, false);
        }

        sample.setT(this.timestamp);

        final Classification classification = this.decisionModel.classify(sample);

        classification.ifExplainedOrElse((closestMicroCluster) -> {

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

        return classification;

    }

    private void awake(MicroCluster microCluster) {
        this.sleepMemory.remove(microCluster);
        this.decisionModel.merge(microCluster);
    }

    private void addNovelty(MicroCluster microCluster) {
        microCluster.setMicroClusterCategory(MicroClusterCategory.NOVELTY);
        microCluster.setLabel(this.noveltyCount++);
        this.decisionModel.merge(microCluster);
    }

    private void addExtension(MicroCluster microCluster, MicroClusterCategory category, Integer label) {
        microCluster.setMicroClusterCategory(category);
        microCluster.setLabel(label);
        this.decisionModel.merge(microCluster);
    }

    public long getTimestamp() {
        return timestamp - 1;
    }

    public DynamicConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public double calculateCER() {
        return this.confusionMatrix.measureCER();
    }

    public double calculateUnkR() {
        return this.confusionMatrix.measureUnkR();
    }

    public int getNoveltyCount() {
        return noveltyCount;
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }
}