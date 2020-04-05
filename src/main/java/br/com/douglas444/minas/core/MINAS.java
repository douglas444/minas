package br.com.douglas444.minas.core;

import br.com.douglas444.minas.config.ClusteringAlgorithmController;
import br.com.douglas444.minas.config.Configuration;
import br.com.douglas444.minas.config.MicroClusterPredictor;
import br.com.douglas444.minas.config.SamplePredictor;
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

    private final boolean feedbackNeeded;
    private final int minSizeDN;
    private final int minClusterSize;
    private final int windowSize;
    private final int clusterLifespan;
    private final int sampleLifespan;
    private final ClusteringAlgorithmController clusteringAlgorithm;

    public MINAS(List<Sample> trainSet, Configuration configuration) {

        this.timestamp = 0;
        this.noveltyCount = 0;
        this.temporaryMemory = new ArrayList<>();

        this.feedbackNeeded = configuration.isFeedbackNeeded();
        this.minSizeDN = configuration.getMinSizeDN();
        this.minClusterSize = configuration.getMinClusterSize();
        this.windowSize = configuration.getWindowSize();
        this.clusterLifespan = configuration.getMicroClusterLifespan();
        this.sampleLifespan = configuration.getSampleLifespan();
        this.clusteringAlgorithm = configuration.getMainClusteringAlgorithm();

        this.decisionModel = buildDecisionModel(trainSet,
                configuration.isIncrementallyUpdatable(),
                configuration.getDecisionModelBuilderClusteringAlgorithm(),
                configuration.getMainMicroClusterPredictor(),
                configuration.getSamplePredictor());

        this.sleepMemory = new DecisionModel(configuration.isIncrementallyUpdatable(),
                configuration.getSleepMemoryMicroClusterPredictor(),
                configuration.getSamplePredictor());

        this.confusionMatrix = buildConfusionMatrix(trainSet);

    }

    private static DynamicConfusionMatrix buildConfusionMatrix(List<Sample> trainSet) {

        final Set<Integer> knownLabels = new HashSet<>();
        trainSet.forEach(sample -> knownLabels.add(sample.getY()));
        return new DynamicConfusionMatrix(new ArrayList<>(knownLabels));

    }


    private static DecisionModel buildDecisionModel(List<Sample> trainSet, boolean incrementallyUpdatable,
                                                    ClusteringAlgorithmController clusteringAlgorithm,
                                                    MicroClusterPredictor microClusterPredictor,
                                                    SamplePredictor samplePredictor) {

        final List<MicroCluster> microClusters = new ArrayList<>();
        final HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        trainSet.forEach(sample -> {
            samplesByLabel.putIfAbsent(sample.getY(), new ArrayList<>());
            samplesByLabel.get(sample.getY()).add(sample);
        });

        samplesByLabel.forEach((label, samples) -> {

            final List<Cluster> clusters = clusteringAlgorithm.execute(samples);

            clusters.stream()
                    .map(cluster -> new MicroCluster(cluster, label, 0))
                    .forEach(microClusters::add);

        });

        microClusters.forEach(microCluster -> microCluster.setCategory(Category.KNOWN));

        return new DecisionModel(incrementallyUpdatable, microClusterPredictor, samplePredictor, microClusters);
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
            Prediction prediction = this.decisionModel.predict(microCluster);

            if (!prediction.isExplained()) {
                prediction = this.sleepMemory.predict(microCluster);
                if (prediction.getClosestMicroCluster().isPresent() && prediction.isExplained()) {
                    this.sleepMemory.remove(prediction.getClosestMicroCluster().get());
                    this.decisionModel.merge(prediction.getClosestMicroCluster().get());
                }
            }

            if (prediction.isExplained()) {

                microCluster.setCategory(prediction.getClosestMicroCluster().get().getCategory());
                microCluster.setLabel(prediction.getClosestMicroCluster().get().getLabel());

            } else {

                boolean isNovel = true;

                if (feedbackNeeded) {
                    isNovel = Feedback.validateConceptEvolution(prediction, microCluster,
                            cohesiveCluster.getSamples(), decisionModel.getMicroClusters());
                }

                if (isNovel) {
                    microCluster.setCategory(Category.NOVELTY);
                    microCluster.setLabel(this.noveltyCount);
                    ++this.noveltyCount;
                } else if (prediction.getClosestMicroCluster().isPresent()) {
                    microCluster.setCategory(prediction.getClosestMicroCluster().get().getCategory());
                    microCluster.setLabel(prediction.getClosestMicroCluster().get().getLabel());
                }
            }

            for (Sample sample : cohesiveCluster.getSamples()) {

                this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                        microCluster.getCategory() == Category.NOVELTY);

            }

            this.decisionModel.merge(microCluster);
        }

    }


    public Prediction predict(Sample sample) {

        sample.setT(this.timestamp);
        final Prediction prediction = this.decisionModel.predict(sample);

        if(prediction.getClosestMicroCluster().isPresent() && prediction.isExplained()) {

            this.confusionMatrix.addPrediction(sample.getY(), prediction.getClosestMicroCluster().get().getLabel(),
                    prediction.getClosestMicroCluster().get().getCategory() == Category.NOVELTY);

        } else {
            this.temporaryMemory.add(sample);
            this.confusionMatrix.addUnknown(sample.getY());
            if (this.temporaryMemory.size() >= this.minSizeDN) {
                this.detectNoveltyAndUpdate();
            }
        }

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
