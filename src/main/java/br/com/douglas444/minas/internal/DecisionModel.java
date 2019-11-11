package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class DecisionModel {

    private List<MicroCluster> microClusters;

    DecisionModel() {
        this.microClusters = new ArrayList<>();
    }

    DecisionModel(List<MicroCluster> microClusters) {
        this.microClusters = new ArrayList<>(microClusters);
    }

    private Optional<MicroCluster> predict(Sample sample) {

        Optional<MicroCluster> closestMicroCluster = calculateClosestMicroCluster(sample);
        if (!closestMicroCluster.isPresent()) {
            return closestMicroCluster;
        }

        Sample center = closestMicroCluster.get().calculateCenter();
        double distance = center.distance(sample);
        double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

        if (distance > microClusterStandardDeviation * Hyperparameter.THRESHOLD_MULTIPLIER) {
            return Optional.empty();
        }

        return closestMicroCluster;

    }

    Optional<MicroCluster> predict(MicroCluster microCluster) {

        Sample center = microCluster.calculateCenter();
        Optional<MicroCluster> closestMicroCluster = calculateClosestMicroCluster(center);

        if (!closestMicroCluster.isPresent()) {
            return closestMicroCluster;
        }

        double distance = closestMicroCluster.get().calculateCenter().distance(center);
        double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

        if (distance > microClusterStandardDeviation * Hyperparameter.THRESHOLD_MULTIPLIER) {
            return Optional.empty();
        }

        return closestMicroCluster;

    }

    Optional<MicroCluster> predictAndUpdate(Sample sample) {

        Optional<MicroCluster> closestMicroCluster = predict(sample);
        closestMicroCluster.ifPresent(microCluster -> microCluster.update(sample));
        return closestMicroCluster;

    }



    private Optional<MicroCluster> calculateClosestMicroCluster(Sample sample) {


        HashMap<Sample, MicroCluster> microClusterByCenter = new HashMap<>();

        List<Sample> decisionModelCenters = this
                .microClusters
                .stream()
                .map(microCluster -> {
                    Sample center = microCluster.calculateCenter();
                    microClusterByCenter.put(center, microCluster);
                    return center;
                })
                .sorted(new DistanceComparator(sample))
                .collect(Collectors.toList());

        if (decisionModelCenters.size() > 0) {
            Sample closestCenter = decisionModelCenters.get(0);
            return Optional.of(microClusterByCenter.get(closestCenter));
        } else {
            return Optional.empty();
        }
    }

    double calculateSilhouette(Cluster cluster) {

        Sample center = cluster.calculateCenter();

        List<Sample> decisionModelCenters = this
                .microClusters
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

    void merge(List<MicroCluster> microClusters) {
        this.microClusters.addAll(microClusters);
    }

    void remove(MicroCluster microCluster) {
        this.microClusters.remove(microCluster);
    }

    List<MicroCluster> extractInactiveMicroClusters(int timestamp) {

        List<MicroCluster> inactiveMicroClusters = this
                .microClusters
                .stream()
                .filter(microCluster -> timestamp - microCluster.getTimestamp() > Hyperparameter.P)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }
}
