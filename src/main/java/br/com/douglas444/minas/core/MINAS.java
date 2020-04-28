package br.com.douglas444.minas.core;

import br.com.douglas444.minas.type.*;
import br.com.douglas444.minas.feedback.Feedback;
import br.com.douglas444.mltk.clustering.kmeans.KMeans;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.DynamicConfusionMatrix;
import br.com.douglas444.mltk.datastructure.Sample;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private final int runningPhaseStartTime;
    private final long randomGeneratorSeed ;
    private final int noveltyDetectionNumberOfClusters;
    private final Heater heater;

    private boolean feedbackDisabled;

    public MINAS(int minSizeDN,
                 int minClusterSize,
                 int windowSize,
                 int microClusterLifespan,
                 int sampleLifespan,
                 int runningPhaseStartTime,
                 boolean incrementallyUpdateDecisionModel,
                 boolean feedbackEnabled,
                 HeaterType heaterType,
                 int heaterNumberOfClusters,
                 int noveltyDetectionNumberOfClusters,
                 int randomGeneratorSeed,
                 MicroClusterPredictor mainMicroClusterPredictor,
                 MicroClusterPredictor sleepMemoryMicroClusterPredictor,
                 SamplePredictor samplePredictor) {

        this.timestamp = 0;
        this.noveltyCount = 0;
        this.warmed = false;
        this.temporaryMemory = new ArrayList<>();

        this.runningPhaseStartTime = runningPhaseStartTime;
        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.feedbackDisabled = !feedbackEnabled;
        this.randomGeneratorSeed = randomGeneratorSeed;

        if (heaterType.equals(HeaterType.KMEANS)) {
            this.heater = new KMeansHeater(heaterNumberOfClusters, this.randomGeneratorSeed);
        } else {
           throw new NotImplementedException();
        }

        this.decisionModel = new DecisionModel(incrementallyUpdateDecisionModel,
                mainMicroClusterPredictor,
                samplePredictor);

        this.sleepMemory = new DecisionModel(incrementallyUpdateDecisionModel,
                sleepMemoryMicroClusterPredictor,
                samplePredictor);

        this.confusionMatrix = new DynamicConfusionMatrix();

    }

    private void warmUp(final Sample sample) {

        if(this.warmed) {
            throw new IllegalStateException();
        }

        if (this.timestamp < this.runningPhaseStartTime) {
            this.heater.process(sample);
        } else {
            this.warmed = true;
            final List<MicroCluster> microClusters = this.heater.close();
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
            final Prediction prediction = this.decisionModel.predict(microCluster);

            prediction.ifExplainedOrElse((closestMicroCluster) -> {

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

                final Prediction sleepPrediction = this.sleepMemory.predict(microCluster);

                sleepPrediction.ifExplainedOrElse((closestMicroCluster) -> {

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

    public Prediction process(final Sample sample) {

        sample.setT(this.timestamp);
        ++this.timestamp;

        if (!this.warmed) {
            this.warmUp(sample);
            if (!this.confusionMatrix.isLabelKnown(sample.getY())) {
                this.confusionMatrix.addKnownLabel(sample.getY());
            }
            return new Prediction(null, false);
        }

        final Prediction prediction = this.decisionModel.predict(sample);

        prediction.ifExplainedOrElse((closestMicroCluster) -> {

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

        return prediction;

    }

    public int getTimestamp() {
        return timestamp;
    }

    public DynamicConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public double cer() {
        return this.confusionMatrix.cer();
    }

    public double unkR() {
        return this.confusionMatrix.unkR();
    }

}