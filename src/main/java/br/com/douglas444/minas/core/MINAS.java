package br.com.douglas444.minas.core;

import br.com.douglas444.minas.*;
import br.com.douglas444.minas.heater.Heater;
import br.com.douglas444.minas.feedback.Feedback;
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
    private final boolean feedbackDisabled;

    public MINAS(int temporaryMemoryMaxSize,
                 int minimumClusterSize,
                 int windowSize,
                 int microClusterLifespan,
                 int sampleLifespan,
                 int heaterCapacity,
                 boolean incrementallyUpdateDecisionModel,
                 boolean feedbackEnabled,
                 int heaterInitialBufferSize,
                 int heaterNumberOfClustersPerLabel,
                 int noveltyDetectionNumberOfClusters,
                 long randomGeneratorSeed,
                 MicroClusterClassifier mainMicroClusterClassifier,
                 MicroClusterClassifier sleepMemoryMicroClusterClassifier,
                 SampleClassifier sampleClassifier) {

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
        this.feedbackDisabled = !feedbackEnabled;
        this.randomGeneratorSeed = randomGeneratorSeed;

        this.heater = new Heater(heaterInitialBufferSize, heaterNumberOfClustersPerLabel, this.randomGeneratorSeed);

        this.decisionModel = new DecisionModel(incrementallyUpdateDecisionModel,
                mainMicroClusterClassifier,
                sampleClassifier);

        this.sleepMemory = new DecisionModel(incrementallyUpdateDecisionModel,
                sleepMemoryMicroClusterClassifier,
                sampleClassifier);

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
            final MicroCluster microCluster = new MicroCluster(cluster, cluster.getMostRecentSample().getT());
            final ClassificationResult classificationResult = this.decisionModel.classify(microCluster);

            classificationResult.ifExplainedOrElse((closestMicroCluster) -> {

                if (this.feedbackDisabled || Feedback.validateConceptDrift(closestMicroCluster, microCluster,
                        cluster.getSamples(), this.decisionModel.getMicroClusters())) {

                    microCluster.setMicroClusterCategory(closestMicroCluster.getMicroClusterCategory());
                    microCluster.setLabel(closestMicroCluster.getLabel());

                } else {
                    microCluster.setMicroClusterCategory(MicroClusterCategory.NOVELTY);
                    microCluster.setLabel(this.noveltyCount);
                    ++this.noveltyCount;
                }

            }, () -> {

                final ClassificationResult sleepClassificationResult = this.sleepMemory.classify(microCluster);

                sleepClassificationResult.ifExplainedOrElse((closestMicroCluster) -> {

                    if (this.feedbackDisabled || Feedback.validateConceptDrift(closestMicroCluster, microCluster,
                            cluster.getSamples(), this.decisionModel.getMicroClusters())) {

                        this.sleepMemory.remove(closestMicroCluster);
                        this.decisionModel.merge(closestMicroCluster);
                        microCluster.setMicroClusterCategory(closestMicroCluster.getMicroClusterCategory());
                        microCluster.setLabel(closestMicroCluster.getLabel());

                    } else {
                        microCluster.setMicroClusterCategory(MicroClusterCategory.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;
                    }

                }, (optionalClosestMicroCluster) -> {

                    if (!optionalClosestMicroCluster.isPresent()) {

                        microCluster.setMicroClusterCategory(MicroClusterCategory.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;

                    } else if (this.feedbackDisabled || Feedback.validateConceptEvolution(
                            optionalClosestMicroCluster.get(), microCluster, cluster.getSamples(),
                            this.decisionModel.getMicroClusters())) {

                        microCluster.setMicroClusterCategory(MicroClusterCategory.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;

                    } else {
                        this.sleepMemory.remove(optionalClosestMicroCluster.get());
                        this.decisionModel.merge(optionalClosestMicroCluster.get());
                        microCluster.setMicroClusterCategory(optionalClosestMicroCluster.get().getMicroClusterCategory());
                        microCluster.setLabel(optionalClosestMicroCluster.get().getLabel());
                    }

                });

            });

            this.decisionModel.merge(microCluster);

            for (Sample sample : cluster.getSamples()) {
                this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                        microCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
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