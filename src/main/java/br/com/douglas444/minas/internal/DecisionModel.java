package br.com.douglas444.minas.internal;

import br.com.douglas444.minas.internal.config.VL;
import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Sample;

import java.util.ArrayList;
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

        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample,
                this.microClusters);

        if (!closestMicroCluster.isPresent()) {
            return closestMicroCluster;
        }

        Sample center = closestMicroCluster.get().calculateCenter();
        double distance = center.distance(sample);
        double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

        if (distance >= microClusterStandardDeviation) {
            return Optional.empty();
        }

        return closestMicroCluster;

    }

    Optional<MicroCluster> predict(MicroCluster microCluster, VL vl) {

        return vl.predict(microCluster, this.microClusters);

    }

    Optional<MicroCluster> predictAndUpdate(Sample sample) {

        Optional<MicroCluster> closestMicroCluster = predict(sample);
        closestMicroCluster.ifPresent(microCluster -> microCluster.update(sample));
        return closestMicroCluster;

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

        List<MicroCluster> inactiveMicroClusters = this
                .microClusters
                .stream()
                .filter(microCluster -> timestamp - microCluster.getTimestamp() > lifespan)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }

    public List<MicroCluster> getMicroClusters() {
        return microClusters;
    }

    public void setMicroClusters(List<MicroCluster> microClusters) {
        this.microClusters = microClusters;
    }
}
