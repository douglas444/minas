package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DynamicConfusionMatrix;
import br.com.douglas444.mltk.Sample;
import br.com.douglas444.mltk.kmeans.KMeansPlusPlus;
import java.util.*;
import java.util.stream.Collectors;

public class MINAS {

    private int timestamp;
    private DecisionModel decisionModel;
    private List<Sample> temporaryMemory;
    private DecisionModel sleepMemory;
    private int noveltyCount;
    private int unexplainedSamplesCount;
    private DynamicConfusionMatrix confusionMatrix;


    public MINAS(List<Sample> trainSet) {

        this.timestamp = 0;
        this.decisionModel = buildDecisionModel(trainSet);
        this.temporaryMemory = new ArrayList<>();
        this.sleepMemory = new DecisionModel();

        this.noveltyCount = 0;
        this.unexplainedSamplesCount = 0;

        Set<Integer> knownLabels = new HashSet<>();
        trainSet.forEach(sample -> knownLabels.add(sample.getY()));
        this.confusionMatrix = new DynamicConfusionMatrix(new ArrayList<>(knownLabels));

    }


    private static DecisionModel buildDecisionModel(List<Sample> trainSet) {

        List<MicroCluster> microClusters = new ArrayList<>();
        HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        trainSet.forEach(sample -> {
            samplesByLabel.putIfAbsent(sample.getY(), new ArrayList<>());
            samplesByLabel.get(sample.getY()).add(sample);
        });

        samplesByLabel.forEach((key, value) -> {
            KMeansPlusPlus kMeansPlusPlus = new KMeansPlusPlus(value, Hyperparameter.K);
            List<Cluster> clusters = kMeansPlusPlus.fit();
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

        KMeansPlusPlus kMeansPlusPlus = new KMeansPlusPlus(this.temporaryMemory, Hyperparameter.K);
        List<Cluster> clusters = kMeansPlusPlus.fit();
        List<MicroCluster> microClusters = new ArrayList<>();

        for (Cluster cluster : clusters) {
            double silhouette = this.decisionModel.calculateSilhouette(cluster);
            if (silhouette > 0 && cluster.getSize() > Hyperparameter.MICRO_CLUSTER_MIN_SIZE) {
                this.temporaryMemory.removeAll(cluster.getSamples());
                MicroCluster microCluster = new MicroCluster(cluster);
                microClusters.add(microCluster);
            }
        }

        List<MicroCluster> awakenedMicroClusters = new ArrayList<>();

        microClusters.forEach(microCluster -> {

            Optional<MicroCluster> extended;
            if ((extended = this.decisionModel.predict(microCluster)).isPresent()) {

                microCluster.setCategory(extended.get().getCategory());
                microCluster.setLabel(extended.get().getLabel());

            } else if ((extended = this.sleepMemory.predict(microCluster)).isPresent()) {

                microCluster.setCategory(extended.get().getCategory());
                microCluster.setLabel(extended.get().getLabel());
                awakenedMicroClusters.add(extended.get());
                this.sleepMemory.remove(extended.get());

            } else {

                microCluster.setCategory(Category.NOVELTY);
                microCluster.setLabel(this.noveltyCount);
                ++this.noveltyCount;

            }

        });

        this.decisionModel.merge(microClusters);
        this.decisionModel.merge(awakenedMicroClusters);
    }


    public Optional<MicroCluster> predictAndUpdate(Sample sample) {

        Optional<MicroCluster> microCluster = this.decisionModel.predictAndUpdate(sample);

        if (!microCluster.isPresent()) {
            ++this.unexplainedSamplesCount;
            this.temporaryMemory.add(sample);
            if (this.temporaryMemory.size() >= Hyperparameter.TEMPORARY_MEMORY_MIN_SIZE) {
                detectNoveltyAndUpdate();
            }
        } else {
            this.confusionMatrix.addPrediction(sample.getY(), microCluster.get().getLabel());
        }

        ++this.timestamp;
        if (this.timestamp % Hyperparameter.WINDOW_MAX_SIZE == 0) {
            this.sleepMemory.merge(this.decisionModel.extractInactiveMicroClusters(this.timestamp));
            this.temporaryMemory.removeIf(p -> (this.timestamp - p.getT()) > Hyperparameter.TS);
        }

        return microCluster;

    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public DynamicConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public void setConfusionMatrix(DynamicConfusionMatrix confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
    }

    public int getUnexplainedSamplesCount() {
        return unexplainedSamplesCount;
    }

    public void setUnexplainedSamplesCount(int unexplainedSamplesCount) {
        this.unexplainedSamplesCount = unexplainedSamplesCount;
    }
}
