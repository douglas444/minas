package br.com.douglas444.minas.core;

import br.com.douglas444.minas.*;
import br.com.douglas444.minas.heater.Heater;
import br.com.douglas444.minas.feedback.Feedback;
import br.com.douglas444.mltk.clustering.kmeans.KMeans;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.DynamicConfusionMatrix;
import br.com.douglas444.mltk.datastructure.Sample;
import java.util.*;
import java.util.stream.Collectors;

public class MINAS {

    private int timestamp;
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
                 int randomGeneratorSeed,
                 MicroClusterClassifier mainMicroClusterClassifier,
                 MicroClusterClassifier sleepMemoryMicroClusterClassifier,
                 SampleClassifier sampleClassifier) {

        this.timestamp = 0;
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

            microClusters.forEach(microCluster -> microCluster.setTimestamp(1));
            this.decisionModel.merge(microClusters);
        }

    }

    private void detectNoveltyAndUpdate() {

         List<Cluster> cohesiveClusters = KMeans
                .execute(this.temporaryMemory, this.noveltyDetectionNumberOfClusters, this.randomGeneratorSeed)
                .stream()
                .filter(cluster -> cluster.getSize() >= this.minimumClusterSize)
                .collect(Collectors.toList());

        for (Cluster cohesiveCluster : cohesiveClusters) {

            if (this.decisionModel.calculateSilhouette(cohesiveCluster) <= 0) {
                continue;
            }

            this.temporaryMemory.removeAll(cohesiveCluster.getSamples());
            final MicroCluster microCluster = new MicroCluster(cohesiveCluster, this.timestamp);
            final ClassificationResult classificationResult = this.decisionModel.classify(microCluster);

            classificationResult.ifExplainedOrElse((closestMicroCluster) -> {

                if (this.feedbackDisabled || Feedback.validateConceptDrift(closestMicroCluster, microCluster,
                        cohesiveCluster.getSamples(), this.decisionModel.getMicroClusters())) {

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
                            cohesiveCluster.getSamples(), this.decisionModel.getMicroClusters())) {

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
                            optionalClosestMicroCluster.get(), microCluster, cohesiveCluster.getSamples(),
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

            for (Sample sample : cohesiveCluster.getSamples()) {
                this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                        microCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);
            }

            this.decisionModel.merge(microCluster);
        }
    }

    public ClassificationResult process(final Sample sample) {

        if (!this.warmed) {
            this.warmUp(sample);
            return new ClassificationResult(null, false);
        }

        sample.setT(++this.timestamp);

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

        return classificationResult;

    }

    public int getTimestamp() {
        return timestamp;
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
}