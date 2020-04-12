package br.com.douglas444.minas.core;

import br.com.douglas444.minas.config.MicroClusterPredictor;
import br.com.douglas444.minas.config.SamplePredictor;
import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private boolean incrementallyUpdate;
    private MicroClusterPredictor microClusterPredictor;
    private SamplePredictor samplePredictor;
    private List<MicroCluster> microClusters;

    public DecisionModel(boolean incrementallyUpdate, MicroClusterPredictor microClusterPredictor,
                         SamplePredictor samplePredictor) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.microClusterPredictor = microClusterPredictor;
        this.samplePredictor = samplePredictor;
        this.microClusters = new ArrayList<>();
    }

    public DecisionModel(boolean incrementallyUpdate, MicroClusterPredictor microClusterPredictor,
                         SamplePredictor samplePredictor, List<MicroCluster> microClusters) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.microClusterPredictor = microClusterPredictor;
        this.samplePredictor = samplePredictor;
        this.microClusters = new ArrayList<>(microClusters);
    }

    Prediction predict(Sample sample) {

        Prediction prediction = this.samplePredictor.predict(sample, this.microClusters);

        prediction.ifExplained((closestMicroCluster) -> {
            closestMicroCluster.setTimestamp(sample.getT());
            if (incrementallyUpdate) {
                closestMicroCluster.update(sample);
            }
        });

        return prediction;
    }

    Prediction predict(MicroCluster microCluster) {
        return this.microClusterPredictor.predict(microCluster, this.microClusters);
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
                .filter(microCluster -> timestamp - microCluster.getTimestamp() >= lifespan)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }

    public List<MicroCluster> getMicroClusters() {
        return microClusters;
    }

}
