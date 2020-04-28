package br.com.douglas444.minas.core;

import br.com.douglas444.minas.type.*;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.util.SampleDistanceComparator;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private final boolean incrementallyUpdate;
    private final MicroClusterPredictor microClusterPredictor;
    private final SamplePredictor samplePredictor;
    private final List<MicroCluster> microClusters;

    DecisionModel(boolean incrementallyUpdate, MicroClusterPredictor microClusterPredictor,
                  SamplePredictor samplePredictor) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.microClusterPredictor = microClusterPredictor;
        this.samplePredictor = samplePredictor;
        this.microClusters = new ArrayList<>();
    }

    Prediction predict(final Sample sample) {

        final Prediction prediction = this.samplePredictor.predict(sample, this.microClusters);

        prediction.ifExplained((closestMicroCluster) -> {
            closestMicroCluster.setTimestamp(sample.getT());
            if (incrementallyUpdate) {
                closestMicroCluster.update(sample);
            }
        });

        return prediction;
    }

    Prediction predict(final MicroCluster microCluster) {
        return this.microClusterPredictor.predict(microCluster, this.microClusters);
    }

    double calculateSilhouette(final Cluster cluster) {

        final Sample center = cluster.calculateCenter();

        final List<Sample> decisionModelCenters = this.microClusters
                .stream()
                .map(MicroCluster::calculateCenter)
                .sorted(new SampleDistanceComparator(center))
                .collect(Collectors.toList());

        final double a = cluster.calculateStandardDeviation();

        final double b;
        if (decisionModelCenters.size() > 0) {
            final Sample closestCenter = decisionModelCenters.get(0);
            b = center.distance(closestCenter);
        } else {
            b = Double.MAX_VALUE;
        }

        return (b - a) / Math.max(b, a);

    }

    void merge(final MicroCluster microCluster) {
        this.microClusters.add(microCluster);
    }

    void merge(final List<MicroCluster> microClusters) {
        this.microClusters.addAll(microClusters);
    }

    void remove(final MicroCluster microCluster) {
        this.microClusters.remove(microCluster);
    }

    List<MicroCluster> extractInactiveMicroClusters(final int timestamp, final int lifespan) {


        final List<MicroCluster> inactiveMicroClusters = this.microClusters
                .stream()
                .filter(microCluster -> timestamp - microCluster.getTimestamp() >= lifespan)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }

    List<MicroCluster> getMicroClusters() {
        return microClusters;
    }

}
