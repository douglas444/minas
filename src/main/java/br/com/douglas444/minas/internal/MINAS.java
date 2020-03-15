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
    private DynamicConfusionMatrix confusionMatrix;

    private Configuration configuration;

    private int al;
    private boolean useAL;

    public MINAS(List<Sample> trainSet, Configuration configuration) {

        this.useAL = true;

        this.timestamp = 0;
        this.decisionModel = buildDecisionModel(trainSet, configuration.getClusteringAlgorithm());
        this.temporaryMemory = new ArrayList<>();
        this.sleepMemory = new DecisionModel();

        this.noveltyCount = 0;

        Set<Integer> knownLabels = new HashSet<>();
        trainSet.forEach(sample -> knownLabels.add(sample.getY()));
        this.confusionMatrix = new DynamicConfusionMatrix(new ArrayList<>(knownLabels));

        this.configuration = configuration;
        this.al = 0;
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


        microClusters.forEach(microCluster -> {

            Optional<MicroCluster> extended;
            if ((extended = this.decisionModel.predict(microCluster, configuration.getVl())).isPresent()) {

                microCluster.setCategory(extended.get().getCategory());
                microCluster.setLabel(extended.get().getLabel());

            } else if ((extended = this.sleepMemory.predict(microCluster, configuration.getVl())).isPresent()) {

                microCluster.setCategory(extended.get().getCategory());
                microCluster.setLabel(extended.get().getLabel());

                this.sleepMemory.remove(extended.get());
                this.decisionModel.merge(extended.get());

            } else {

                List<MicroCluster> decisionModelMicroClusters = this.decisionModel.getMicroClusters();
                List<MicroCluster> sleepMemoryMicroClusters = this.sleepMemory.getMicroClusters();
                List<MicroCluster> microClusters1;

                double dist = Double.MAX_VALUE;
                double distSleep = Double.MAX_VALUE;

                if (decisionModelMicroClusters.size() > 0) {
                    dist = MicroCluster.calculateClosestMicroCluster(microCluster.calculateCenter(), decisionModelMicroClusters).get().calculateCenter().distance(microCluster.calculateCenter());
                }
                if (decisionModelMicroClusters.size() > 0) {
                    distSleep = MicroCluster.calculateClosestMicroCluster(microCluster.calculateCenter(), sleepMemoryMicroClusters).get().calculateCenter().distance(microCluster.calculateCenter());
                }

                boolean sleep = false;

                if (dist < distSleep) {
                    microClusters1 = decisionModelMicroClusters;
                } else {
                    microClusters1 = sleepMemoryMicroClusters;
                    sleep = true;
                }

                if (useAL && estimateBayesError(microCluster.calculateCenter(), microClusters1) < 0.5) {

                    List<Sample> samples = samplesByMicroCluster.get(microCluster);
                    HashMap<Sample, Double> riskBySample = new HashMap<>();

                    samples.forEach(sample -> {
                        riskBySample.put(sample, estimateBayesError(sample, microClusters1));
                    });

                    Sample maxRiskSample = Objects.requireNonNull(riskBySample
                            .entrySet()
                            .stream()
                            .max(Map.Entry.comparingByValue())
                            .orElse(null)).getKey();

                    MicroCluster closest = MicroCluster.calculateClosestMicroCluster(maxRiskSample, microClusters1).orElse(null);

                    if (closest != null
                            && closest.getCategory() != Category.NOVELTY
                            && maxRiskSample.getY() == closest.getLabel()) {

                        ++this.al;

                        microCluster.setCategory(closest.getCategory());
                        microCluster.setLabel(closest.getLabel());


                        if (sleep) {

                            this.sleepMemory.remove(closest);
                            this.decisionModel.merge(closest);
                        }

                    } else {

                        microCluster.setCategory(Category.NOVELTY);
                        microCluster.setLabel(this.noveltyCount);
                        ++this.noveltyCount;

                        if (closest != null && closest.getCategory() != Category.NOVELTY && maxRiskSample.getY() != closest.getLabel()) {
                            ++this.al;
                        }


                    }


                } else {

                    microCluster.setCategory(Category.NOVELTY);
                    microCluster.setLabel(this.noveltyCount);
                    ++this.noveltyCount;
                }



            }

            this.decisionModel.merge(microCluster);

            samplesByMicroCluster.get(microCluster).forEach(sample -> {

                this.confusionMatrix.updatedDelayed(sample.getY(), microCluster.getLabel(),
                        microCluster.getCategory() == Category.NOVELTY);

            });

        });

    }


    public Optional<MicroCluster> predictAndUpdate(Sample sample) {

        Optional<MicroCluster> microCluster = this.decisionModel.predictAndUpdate(sample);

        if (!microCluster.isPresent()) {

            this.temporaryMemory.add(sample);
            this.confusionMatrix.addUnknown(sample.getY());

            if (this.temporaryMemory.size() >= this.configuration.getMinSizeDN()) {
                this.detectNoveltyAndUpdate();
            }

        } else {

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
        System.out.println(this.al);
        return microCluster;

    }

    public static double estimateBayesError(Sample target, List<MicroCluster> microClusters) {

        HashMap<Integer, List<MicroCluster>> microClustersByLabel = new HashMap<>();
        microClusters.forEach(microCluster -> {

            microClustersByLabel.putIfAbsent(microCluster.getLabel(), new ArrayList<>());
            microClustersByLabel.get(microCluster.getLabel()).add(microCluster);

        });

        HashMap<Integer, MicroCluster> closestMicroClusterByLabel = new HashMap<>();
        microClustersByLabel.forEach((key, value) -> {

            MicroCluster closest = MicroCluster.calculateClosestMicroCluster(target, value).orElse(null);
            closestMicroClusterByLabel.put(key, closest);

        });

        double n = 1.0 / closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> microCluster.calculateCenter().distance(target))
                .min(Double::compare)
                .orElse(0.0);

        double d = closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> 1.0 / microCluster.calculateCenter().distance(target))
                .reduce(0.0, Double::sum);

        return 1 - (n/d);

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
