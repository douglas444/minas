package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DecisionModel {

    private List<MicroCluster> microClusters;

    public DecisionModel() {
        this.microClusters = new ArrayList<>();
    }

    public DecisionModel(List<MicroCluster> microClusters) {
        this.microClusters = microClusters;
    }

    public Optional<MicroCluster> predictAndUpdate(Point point) {

        Optional<MicroCluster> closestMicroCluster = calculateClosestMicroCluster(point);

        if (closestMicroCluster.isPresent() &&
                closestMicroCluster.get().calculateCenter().distance(point) < Hyperparameter.T) {

            closestMicroCluster.get().update(point);
            return closestMicroCluster;

        } else {
            return Optional.empty();
        }

    }

    public Optional<MicroCluster> predict(MicroCluster microCluster) {

        Point center = microCluster.calculateCenter();
        Optional<MicroCluster> closestMicroCluster = calculateClosestMicroCluster(center);


        if (closestMicroCluster.isPresent() &&
                closestMicroCluster.get().calculateCenter().distance(center) < Hyperparameter.T) {

            return closestMicroCluster;
        } else {
            return Optional.empty();
        }

    }

    public Optional<MicroCluster> calculateClosestMicroCluster(Point point) {


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

    public double calculateSilhouette(Cluster cluster) {

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

    public void merge(List<MicroCluster> microClusters) {
        this.microClusters.addAll(microClusters);
    }

    public List<MicroCluster> extractInactiveMicroClusters(int timestamp) {

        List<MicroCluster> inactiveMicroClusters = this
                .microClusters
                .stream()
                .filter(microCluster -> timestamp - microCluster.getTimestamp() > Hyperparameter.P)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }
}
