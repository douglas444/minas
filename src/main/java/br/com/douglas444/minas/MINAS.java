package br.com.douglas444.minas;

import br.com.douglas444.minas.config.ClusteringAlgorithm;
import br.com.douglas444.minas.config.Configuration;
import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DynamicConfusionMatrix;
import br.com.douglas444.mltk.Sample;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MINAS {

    private int timestamp;
    private DecisionModel decisionModel;
    private List<Sample> temporaryMemory;
    private DecisionModel sleepMemory;
    private int noveltyCount;
    private DynamicConfusionMatrix confusionMatrix;
    private Configuration configuration;

    public MINAS(List<Sample> trainSet, Configuration configuration) {

        this.timestamp = 0;
        this.decisionModel = buildDecisionModel(trainSet, configuration.getClusteringAlgorithm());
        this.temporaryMemory = new ArrayList<>();
        this.sleepMemory = new DecisionModel();

        this.noveltyCount = 0;

        Set<Integer> knownLabels = new HashSet<>();
        trainSet.forEach(sample -> knownLabels.add(sample.getY()));
        this.confusionMatrix = new DynamicConfusionMatrix(new ArrayList<>(knownLabels));

        this.configuration = configuration;
    }


    private static DecisionModel buildDecisionModel(List<Sample> trainSet, ClusteringAlgorithm clusteringAlgorithm) {

        List<MicroCluster> microClusters = new ArrayList<>();
        HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        trainSet.forEach(sample -> {
            samplesByLabel.putIfAbsent(sample.getY(), new ArrayList<>());
            samplesByLabel.get(sample.getY()).add(sample);
        });

        samplesByLabel.forEach((key, value) -> {

            List<Cluster> clusters = clusteringAlgorithm.execute(value);

            microClusters.addAll(clusters.stream()
                    .map(cluster -> new MicroCluster(cluster, key))
                    .collect(Collectors.toList()));

        });

        microClusters.forEach(microCluster -> microCluster.setCategory(Category.KNOWN));

        return new DecisionModel(microClusters);
    }

    private void detectNoveltyAndUpdate() {

        final Predicate<Cluster> isCohesive = cluster -> this.decisionModel.calculateSilhouette(cluster) > 0
                && cluster.getSize() > configuration.getMinClusterSize();

        List<Cluster> cohesiveClusters = configuration
                .getClusteringAlgorithm()
                .execute(this.temporaryMemory)
                .stream()
                .filter(isCohesive)
                .peek(cluster -> this.temporaryMemory.removeAll(cluster.getSamples()))
                .collect(Collectors.toList());

        for (Cluster cohesiveCluster : cohesiveClusters) {

            MicroCluster microCluster = new MicroCluster(cohesiveCluster);
            Prediction prediction = this.decisionModel.predict(microCluster, configuration.getVl());

            if (!prediction.isExplained()) {
                prediction = this.sleepMemory.predict(microCluster, configuration.getVl());
                if (prediction.isExplained()) {
                    this.sleepMemory.remove(prediction.getExplainedBy());
                    this.decisionModel.merge(prediction.getExplainedBy());
                }
            }

            if (prediction.isExplained()) {

                microCluster.setCategory(prediction.getExplainedBy().getCategory());
                microCluster.setLabel(prediction.getExplainedBy().getLabel());

            } else if (this.decisionModel.estimateBayesError(microCluster.calculateCenter()) < 0.5) {

                Sample sample = this.decisionModel.getMostInformativeSample(cohesiveCluster.getSamples());
                this.decisionModel.getClosestMicroCluster(sample)
                        .ifPresent((closest) -> {
                            Integer label = Oracle.label(sample);
                            if (closest.getCategory() == Category.KNOWN && label == closest.getLabel()) {
                                microCluster.setCategory(closest.getCategory());
                                microCluster.setLabel(closest.getLabel());
                            } else {
                                microCluster.setCategory(Category.NOVELTY);
                                microCluster.setLabel(this.noveltyCount);
                                ++this.noveltyCount;
                            }
                        });

            } else {
                microCluster.setCategory(Category.NOVELTY);
                microCluster.setLabel(this.noveltyCount);
                ++this.noveltyCount;
            }

            for (Sample sample : cohesiveCluster.getSamples()) {
                this.confusionMatrix.updatedDelayed(
                        sample.getY(),
                        microCluster.getLabel(),
                        microCluster.getCategory() == Category.NOVELTY);
            }

            this.decisionModel.merge(microCluster);
        }

    }


    public Prediction predictAndUpdate(Sample sample) {

        sample.setT(this.timestamp);
        Prediction prediction = this.decisionModel.predictAndUpdate(sample);

        if(prediction.isExplained()) {

            this.confusionMatrix.addPrediction(sample.getY(), prediction.getExplainedBy().getLabel(),
                    prediction.getExplainedBy().getCategory() == Category.NOVELTY);

        } else {
            this.temporaryMemory.add(sample);
            this.confusionMatrix.addUnknown(sample.getY());
            if (this.temporaryMemory.size() >= this.configuration.getMinSizeDN()) {
                this.detectNoveltyAndUpdate();
            }
        }

        ++this.timestamp;

        if (this.timestamp % configuration.getWindowSize()== 0) {

            List<MicroCluster> inactiveMicroClusters = this.decisionModel
                    .extractInactiveMicroClusters(this.timestamp, configuration.getClusterLifespan());

            this.sleepMemory.merge(inactiveMicroClusters);
            this.temporaryMemory.removeIf(p -> (this.timestamp - p.getT()) > configuration.getSampleLifespan());

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
