package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Point;

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

    private Optional<MicroCluster> predict(Point point) {

        Optional<MicroCluster> closestMicroCluster = calculateClosestMicroCluster(point);
        if (!closestMicroCluster.isPresent()) {
            return closestMicroCluster;
        }

        Point center = closestMicroCluster.get().calculateCenter();
        double distance = center.distance(point);
        double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

        if (distance > microClusterStandardDeviation * Hyperparameter.THRESHOLD_MULTIPLIER) {
            return Optional.empty();
        }

        return closestMicroCluster;

    }

    Optional<MicroCluster> predict(MicroCluster microCluster) {

        Point center = microCluster.calculateCenter();
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

    Optional<MicroCluster> predictAndUpdate(Point point) {

        Optional<MicroCluster> closestMicroCluster = predict(point);
        closestMicroCluster.ifPresent(microCluster -> microCluster.update(point));
        return closestMicroCluster;

    }



    private Optional<MicroCluster> calculateClosestMicroCluster(Point point) {


        HashMap<Point, MicroCluster> microClusterByCenter = new HashMap<>();

        List<Point> decisionModelCenters = this
                .microClusters
                .stream()
                .map(microCluster -> {
                    Point center = microCluster.calculateCenter();
                    microClusterByCenter.put(center, microCluster);
                    return center;
                })
                .sorted(new DistanceComparator(point))
                .collect(Collectors.toList());

        if (decisionModelCenters.size() > 0) {
            Point closestCenter = decisionModelCenters.get(0);
            return Optional.of(microClusterByCenter.get(closestCenter));
        } else {
            return Optional.empty();
        }
    }

    double calculateSilhouette(Cluster cluster) {

        Point center = cluster.calculateCenter();

        List<Point> decisionModelCenters = this
                .microClusters
                .stream()
                .map(MicroCluster::calculateCenter)
                .sorted(new DistanceComparator(center))
                .collect(Collectors.toList());

        double a = cluster.calculateStandardDeviation();

        double b;
        if (decisionModelCenters.size() > 0) {
            Point closestCenter = decisionModelCenters.get(0);
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
