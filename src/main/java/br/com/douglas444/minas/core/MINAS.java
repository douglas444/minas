package br.com.douglas444.minas.core;

import br.com.douglas444.minas.*;
import br.com.douglas444.minas.heater.Heater;
import br.com.douglas444.minas.feedback.Feedback;
import br.com.douglas444.mltk.clustering.kmeans.KMeans;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.DynamicConfusionMatrix;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MINAS {

    private int timestamp;
    private int noveltyCount;
    private boolean warmed;

    private final DecisionModel decisionModel;
    private final List<Sample> temporaryMemory;
    private final DecisionModel sleepMemory ;
    private final DynamicConfusionMatrix confusionMatrix;

    private final int minSizeDN;
    private final int minClusterSize;
    private final int windowSize;
    private final int microClusterLifespan;
    private final int sampleLifespan;
    private final int onlinePhaseStartTime;
    private final long randomGeneratorSeed ;
    private final int noveltyDetectionNumberOfClusters;
    private final Heater heater;

    private boolean feedbackDisabled;

    public MINAS(int minSizeDN,
                 int minClusterSize,
                 int windowSize,
                 int microClusterLifespan,
                 int sampleLifespan,
                 int onlinePhaseStartTime,
                 boolean incrementallyUpdateDecisionModel,
                 boolean feedbackEnabled,
                 int heaterInitialBufferSize,
                 int heaterNumberOfClustersPerLabel,
                 int heaterAgglomerativeBufferThreshold,
                 int noveltyDetectionNumberOfClusters,
                 int randomGeneratorSeed,
                 MicroClusterClassifier mainMicroClusterClassifier,
                 MicroClusterClassifier sleepMemoryMicroClusterClassifier,
                 SampleClassifier sampleClassifier) {

        this.timestamp = 0;
        this.noveltyCount = 0;
        this.warmed = false;
        this.temporaryMemory = new ArrayList<>();

        this.onlinePhaseStartTime = onlinePhaseStartTime;
        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.feedbackDisabled = !feedbackEnabled;
        this.randomGeneratorSeed = randomGeneratorSeed;
        this.heater = new Heater(heaterInitialBufferSize, heaterNumberOfClustersPerLabel,
                heaterAgglomerativeBufferThreshold, this.randomGeneratorSeed);

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

        if (this.timestamp < this.onlinePhaseStartTime) {
            this.heater.process(sample);
        } else {
            this.warmed = true;
            final List<MicroCluster> microClusters = this.heater.getResult();
            microClusters.forEach(microCluster -> microCluster.setTimestamp(this.timestamp));
            this.decisionModel.merge(microClusters);
        }

    }

    private void detectNoveltyAndUpdate() {

        final Predicate<Cluster> isCohesive = cluster -> this.decisionModel.calculateSilhouette(cluster) > 0
                && cluster.getSize() >= this.minClusterSize;

        final List<Cluster> cohesiveClusters = KMeans
                .execute(this.temporaryMemory, this.noveltyDetectionNumberOfClusters, this.randomGeneratorSeed)
                .stream()
                .filter(isCohesive)
                .peek(cluster -> this.temporaryMemory.removeAll(cluster.getSamples()))
                .collect(Collectors.toList());

        for (Cluster cohesiveCluster : cohesiveClusters) {

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

        sample.setT(this.timestamp++);

        if (!this.warmed) {
            this.warmUp(sample);
            return new ClassificationResult(null, false);
        }

        final ClassificationResult classificationResult = this.decisionModel.classify(sample);

        classificationResult.ifExplainedOrElse((closestMicroCluster) -> {

            this.confusionMatrix.addPrediction(sample.getY(), closestMicroCluster.getLabel(),
                    closestMicroCluster.getMicroClusterCategory() == MicroClusterCategory.NOVELTY);

        }, () -> {

            this.temporaryMemory.add(sample);
            this.confusionMatrix.addUnknown(sample.getY());
            if (this.temporaryMemory.size() >= this.minSizeDN) {
                this.detectNoveltyAndUpdate();
            }

        });

        if (this.timestamp % this.windowSize == 0) {

            final List<MicroCluster> inactiveMicroClusters = this.decisionModel
                    .extractInactiveMicroClusters(this.timestamp, microClusterLifespan);

            this.sleepMemory.merge(inactiveMicroClusters);
            this.temporaryMemory.removeIf(p -> (this.timestamp - p.getT()) >= sampleLifespan);
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