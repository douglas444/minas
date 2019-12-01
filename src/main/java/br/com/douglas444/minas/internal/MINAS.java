package br.com.douglas444.minas.internal;

import br.com.douglas444.minas.internal.config.ClusteringAlgorithm;
import br.com.douglas444.minas.internal.config.Configuration;
import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DynamicConfusionMatrix;
import br.com.douglas444.mltk.Sample;
import java.util.*;
import java.util.stream.Collectors;

public class MINAS {

    private int timestamp;
    private DecisionModel decisionModel;
    private List<Sample> temporaryMemory;
    private DecisionModel sleepMemory;
    private int noveltyCount;
    private int unexplainedSamplesCount;
    private int delayedPredictionsCount;
    private int realTimePredictionsCount;
    private DynamicConfusionMatrix confusionMatrix;

    private Configuration configuration;


    public MINAS(List<Sample> trainSet, Configuration configuration) {

        this.timestamp = 0;
        this.decisionModel = buildDecisionModel(trainSet, configuration.getClusteringAlgorithm());
        this.temporaryMemory = new ArrayList<>();
        this.sleepMemory = new DecisionModel();

        this.noveltyCount = 0;
        this.unexplainedSamplesCount = 0;
        this.delayedPredictionsCount = 0;
        this.realTimePredictionsCount = 0;

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
            microClusters.addAll(
                    clusters
                            .stream()
                            .map(cluster -> new MicroCluster(cluster, key))
                            .collect(Collectors.toList()));
        });

        microClusters.forEach(microCluster -> microCluster.setCategory(Category.KNOWN));

        return new DecisionModel(microClusters);
    }


    private void detectNoveltyAndUpdate() {

        List<Cluster> clusters = configuration.getClusteringAlgorithm().execute(this.temporaryMemory);
        List<MicroCluster> microClusters = new ArrayList<>();
        Map<MicroCluster, List<Sample>> samplesByMicroCluster = new HashMap<>();

        for (Cluster cluster : clusters) {
            double silhouette = this.decisionModel.calculateSilhouette(cluster);
            if (silhouette > 0 && cluster.getSize() > configuration.getMinClusterSize()) {
                this.temporaryMemory.removeAll(cluster.getSamples());
                MicroCluster microCluster = new MicroCluster(cluster);
                microClusters.add(microCluster);
                samplesByMicroCluster.put(microCluster, cluster.getSamples());
            }
        }

        List<MicroCluster> awakenedMicroClusters = new ArrayList<>();

        microClusters.forEach(microCluster -> {

            Optional<MicroCluster> extended;
            if ((extended = this.decisionModel.predict(microCluster, configuration.getVl())).isPresent()) {

                microCluster.setCategory(extended.get().getCategory());
                microCluster.setLabel(extended.get().getLabel());

            } else if ((extended = this.sleepMemory.predict(microCluster, configuration.getVl())).isPresent()) {

                microCluster.setCategory(extended.get().getCategory());
                microCluster.setLabel(extended.get().getLabel());
                awakenedMicroClusters.add(extended.get());
                this.sleepMemory.remove(extended.get());

            } else {

                microCluster.setCategory(Category.NOVELTY);
                microCluster.setLabel(this.noveltyCount);
                ++this.noveltyCount;

            }

            samplesByMicroCluster.get(microCluster).forEach(sample -> {

                this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                        microCluster.getCategory() == Category.NOVELTY);
                --this.unexplainedSamplesCount;
                ++this.delayedPredictionsCount;
            });

        });

        this.decisionModel.merge(microClusters);
        this.decisionModel.merge(awakenedMicroClusters);
    }


    public Optional<MicroCluster> predictAndUpdate(Sample sample) {

        Optional<MicroCluster> microCluster = this.decisionModel.predictAndUpdate(sample);

        if (!microCluster.isPresent()) {
            ++this.unexplainedSamplesCount;
            this.temporaryMemory.add(sample);
            if (this.temporaryMemory.size() >= this.configuration.getMinSizeDN()) {
                this.detectNoveltyAndUpdate();
            }
            this.confusionMatrix.addUnknown(sample.getY());
        } else {
            ++this.realTimePredictionsCount;
            this.confusionMatrix.addPrediction(sample.getY(), microCluster.get().getLabel(),
                    microCluster.get().getCategory() == Category.NOVELTY);
        }

        ++this.timestamp;
        if (this.timestamp % configuration.getWindowSize()== 0) {

            List<MicroCluster> inactiveMicroClusters = this.decisionModel
                    .extractInactiveMicroClusters(this.timestamp, configuration.getClusterLifespan());

            this.sleepMemory.merge(inactiveMicroClusters);
            this.temporaryMemory.removeIf(p -> (this.timestamp - p.getT()) > configuration.getSampleLifespan());
        }

        return microCluster;

    }

    public int getTimestamp() {
        return timestamp;
    }

    public DynamicConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public int getUnexplainedSamplesCount() {
        return unexplainedSamplesCount;
    }

    public int getDelayedPredictionsCount() {
        return delayedPredictionsCount;
    }

    public int getRealTimePredictionsCount() {
        return realTimePredictionsCount;
    }
}
