package br.com.douglas444.minas;

import br.com.douglas444.minas.config.VL;
import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private List<MicroCluster> microClusters;

    DecisionModel() {
        this.microClusters = new ArrayList<>();
    }

    DecisionModel(List<MicroCluster> microClusters) {
        this.microClusters = new ArrayList<>(microClusters);
    }

    private Prediction predict(Sample sample) {

        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample,
                this.microClusters);

        if (closestMicroCluster.isPresent()) {

            Sample center = closestMicroCluster.get().calculateCenter();
            double distance = center.distance(sample);
            double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

            if (distance <= microClusterStandardDeviation) {
                return new Prediction(closestMicroCluster.get());
            }
        }

        return new Prediction(null);

    }

    Prediction predict(MicroCluster microCluster, VL vl) {
        return vl.predict(microCluster, this.microClusters);
    }

    Prediction predictAndUpdate(Sample sample) {
        Prediction prediction = this.predict(sample);
        prediction.ifExplained(microCluster -> microCluster.update(sample));
        return prediction;
    }

    double calculateSilhouette(Cluster cluster) {

        Sample center = cluster.calculateCenter();

        List<Sample> decisionModelCenters = this.microClusters
                .stream()
                .map(MicroCluster::calculateCenter)
                .sorted(new DistanceComparator(center))
                .collect(Collectors.toList());

        double a = cluster.calculateStandardDeviation();

        double b;
        if (decisionModelCenters.size() > 0) {
            Sample closestCenter = decisionModelCenters.get(0);
            b = center.distance(closestCenter);
        } else {
            b = Double.MAX_VALUE;
        }


        return (b - a) / Math.max(b, a);


    }

    void merge(MicroCluster microCluster) {
        this.microClusters.add(microCluster);
    }

    void merge(List<MicroCluster> microClusters) {
        this.microClusters.addAll(microClusters);
    }

    void remove(MicroCluster microCluster) {
        this.microClusters.remove(microCluster);
    }

    List<MicroCluster> extractInactiveMicroClusters(int timestamp, int lifespan) {

        List<MicroCluster> inactiveMicroClusters = this.microClusters
                .stream()
                .filter(microCluster -> timestamp - microCluster.getTimestamp() > lifespan)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }

    public double estimateBayesError(Sample target) {

        HashMap<Integer, List<MicroCluster>> microClustersByLabel = new HashMap<>();
        microClusters.forEach(microCluster -> {
            microClustersByLabel.putIfAbsent(microCluster.getLabel(), new ArrayList<>());
            microClustersByLabel.get(microCluster.getLabel()).add(microCluster);
        });

        HashMap<Integer, MicroCluster> closestMicroClusterByLabel = new HashMap<>();
        microClustersByLabel.forEach((key, value) -> {
            MicroCluster.calculateClosestMicroCluster(target, value)
                    .ifPresent(closest -> closestMicroClusterByLabel.put(key, closest));
        });

        double n = 1.0 / closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> microCluster.calculateCenter().distance(target))
                .min(Double::compare)
                .orElse(1.0);

        double d = closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> 1.0 / (1 + microCluster.calculateCenter().distance(target)))
                .reduce(0.0, Double::sum);

        return 1 - (n/ (d + 1));

    }

    public Sample getMostInformativeSample(List<Sample> samples) {

        Sample maxRiskSample = samples.get(0);
        double maxRisk = 0;

        for (Sample sample : samples) {
            double risk = estimateBayesError(sample);
            if (risk > maxRisk) {
                maxRiskSample = sample;
                maxRisk = risk;
            }
        }

        return maxRiskSample;
    }

    public Optional<MicroCluster> getClosestMicroCluster(Sample sample) {
        return MicroCluster.calculateClosestMicroCluster(sample, this.microClusters);
    }

}
