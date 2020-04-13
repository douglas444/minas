package br.com.douglas444.minas.core;

import br.com.douglas444.minas.config.ClusteringAlgorithmController;
import br.com.douglas444.minas.config.Configuration;
import br.com.douglas444.minas.feedback.Feedback;
import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DynamicConfusionMatrix;
import br.com.douglas444.mltk.Sample;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MINAS {

    private int timestamp;
    private int noveltyCount;

    private final DecisionModel decisionModel;
    private final List<Sample> temporaryMemory;
    private final DecisionModel sleepMemory;
    private final DynamicConfusionMatrix confusionMatrix;

    private final int minSizeDN;
    private final int minClusterSize;
    private final int windowSize;
    private final int clusterLifespan;
    private final int sampleLifespan;
    private final ClusteringAlgorithmController clusteringAlgorithm;

    private boolean feedbackDisabled;

    public MINAS(List<Sample> trainSet, Configuration configuration) {

        this.timestamp = 0;
        this.noveltyCount = 0;
        this.temporaryMemory = new ArrayList<>();

        this.minSizeDN = configuration.getMinSizeDN();
        this.minClusterSize = configuration.getMinClusterSize();
        this.windowSize = configuration.getWindowSize();
        this.clusterLifespan = configuration.getMicroClusterLifespan();
        this.sampleLifespan = configuration.getSampleLifespan();
        this.clusteringAlgorithm = configuration.getOnlineClusteringAlgorithm();
        this.feedbackDisabled = !configuration.getFeedbackEnabled();

        this.decisionModel = new DecisionModel(configuration.isIncrementallyUpdateDecisionModel(),
                configuration.getMainMicroClusterPredictor(),
                configuration.getSamplePredictor());

        this.sleepMemory = new DecisionModel(configuration.isIncrementallyUpdateDecisionModel(),
                configuration.getSleepMemoryMicroClusterPredictor(),
                configuration.getSamplePredictor());

        final Set<Integer> knownLabels = new HashSet<>();
        trainSet.forEach(sample -> knownLabels.add(sample.getY()));
        this.confusionMatrix = new DynamicConfusionMatrix(new ArrayList<>(knownLabels));

        this.warmUp(trainSet);
    }

    private void warmUp(List<Sample> trainSet) {

        final List<MicroCluster> microClusters = new ArrayList<>();
        final HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        trainSet.forEach(sample -> {
            samplesByLabel.putIfAbsent(sample.getY(), new ArrayList<>());
            samplesByLabel.get(sample.getY()).add(sample);
        });

        samplesByLabel.forEach((label, samples) -> {

            final List<Cluster> clusters = this.clusteringAlgorithm.execute(samples);

            clusters.stream()
                    .map(cluster -> new MicroCluster(cluster, label, 0, Category.KNOWN))
                    .forEach(microClusters::add);
        });

        this.decisionModel.merge(microClusters);
    }

    private void detectNoveltyAndUpdate() {

        final Predicate<Cluster> isCohesive = cluster -> this.decisionModel.calculateSilhouette(cluster) > 0
                && cluster.getSize() >= this.minClusterSize;

        final List<Cluster> cohesiveClusters = clusteringAlgorithm
                .execute(this.temporaryMemory)
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

                    microCluster.setCategory(closestMicroCluster.getCategory());
                    microCluster.setLabel(closestMicroCluster.getLabel());

                } else {
                    microCluster.setCategory(Category.NOVELTY);
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
                        microCluster.setCategory(closestMicroCluster.getCategory());
                        microCluster.setLabel(closestMicroCluster.getLabel());

                    } else {
                        microCluster.setCategory(Category.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;
                    }

                }, (optionalClosestMicroCluster) -> {

                    if (!optionalClosestMicroCluster.isPresent()) {

                        microCluster.setCategory(Category.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;

                    } else if (this.feedbackDisabled || Feedback.validateConceptEvolution(
                            optionalClosestMicroCluster.get(), microCluster, cohesiveCluster.getSamples(),
                            this.decisionModel.getMicroClusters())) {

                        microCluster.setCategory(Category.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;

                    } else {
                        microCluster.setCategory(optionalClosestMicroCluster.get().getCategory());
                        microCluster.setLabel(optionalClosestMicroCluster.get().getLabel());
                    }

                });

            });

            for (Sample sample : cohesiveCluster.getSamples()) {

                this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                        microCluster.getCategory() == Category.NOVELTY);

            }

            this.decisionModel.merge(microCluster);
        }

    }


    public Prediction process(Sample sample) {

        sample.setT(this.timestamp);
        final Prediction prediction = this.decisionModel.predict(sample);

        prediction.ifExplainedOrElse((closestMicroCluster) -> {

            this.confusionMatrix.addPrediction(sample.getY(), closestMicroCluster.getLabel(),
                    closestMicroCluster.getCategory() == Category.NOVELTY);

        }, () -> {

            this.temporaryMemory.add(sample);
            this.confusionMatrix.addUnknown(sample.getY());
            if (this.temporaryMemory.size() >= this.minSizeDN) {
                this.detectNoveltyAndUpdate();
            }

        });

        ++this.timestamp;

        if (this.timestamp % this.windowSize == 0) {

            final List<MicroCluster> inactiveMicroClusters = this.decisionModel
                    .extractInactiveMicroClusters(this.timestamp, clusterLifespan);

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
