package br.com.douglas444.minas.core;

import br.com.douglas444.minas.type.Category;
import br.com.douglas444.minas.type.MicroCluster;
import br.com.douglas444.minas.type.MicroClusterPredictor;
import br.com.douglas444.minas.type.SamplePredictor;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.util.SampleDistanceComparator;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private boolean incrementallyUpdate;
    private MicroClusterPredictor microClusterPredictor;
    private SamplePredictor samplePredictor;
    private List<MicroCluster> microClusters;

    DecisionModel(boolean incrementallyUpdate, MicroClusterPredictor microClusterPredictor,
                         SamplePredictor samplePredictor) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.microClusterPredictor = microClusterPredictor;
        this.samplePredictor = samplePredictor;
        this.microClusters = new ArrayList<>();
    }

    Category.Prediction predict(Sample sample) {

        Category.Prediction prediction = this.samplePredictor.predict(sample, this.microClusters);

        prediction.ifExplained((closestMicroCluster) -> {
            closestMicroCluster.setTimestamp(sample.getT());
            if (incrementallyUpdate) {
                closestMicroCluster.update(sample);
            }
        });

        return prediction;
    }

    Category.Prediction predict(MicroCluster microCluster) {
        return this.microClusterPredictor.predict(microCluster, this.microClusters);
    }

    double calculateSilhouette(Cluster cluster) {

        Sample center = cluster.calculateCenter();

        List<Sample> decisionModelCenters = this.microClusters
                .stream()
                .map(MicroCluster::calculateCenter)
                .sorted(new SampleDistanceComparator(center))
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
                .filter(microCluster -> timestamp - microCluster.getTimestamp() >= lifespan)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }

    List<MicroCluster> getMicroClusters() {
        return microClusters;
    }

}
