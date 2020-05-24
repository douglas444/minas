package br.com.douglas444.minas.core;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.minas.MicroClusterClassifier;
import br.com.douglas444.minas.ClassificationResult;
import br.com.douglas444.minas.SampleClassifier;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.util.SampleDistanceComparator;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private final boolean incrementallyUpdate;
    private final MicroClusterClassifier microClusterClassifier;
    private final SampleClassifier sampleClassifier;
    private final List<MicroCluster> microClusters;

    DecisionModel(boolean incrementallyUpdate, MicroClusterClassifier microClusterClassifier,
                  SampleClassifier sampleClassifier) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.microClusterClassifier = microClusterClassifier;
        this.sampleClassifier = sampleClassifier;
        this.microClusters = new ArrayList<>();
    }

    ClassificationResult classify(final Sample sample) {

        final ClassificationResult classificationResult = this.sampleClassifier.classify(sample, this.microClusters);

        classificationResult.ifExplained((closestMicroCluster) -> {
            closestMicroCluster.setTimestamp(sample.getT());
            if (incrementallyUpdate) {
                closestMicroCluster.update(sample);
            }
        });

        return classificationResult;
    }

    ClassificationResult classify(final MicroCluster microCluster) {
        return this.microClusterClassifier.classify(microCluster, this.microClusters);
    }

    double calculateSilhouette(final Cluster cluster) {

        final Sample centroid = cluster.calculateCentroid();

        final List<Sample> decisionModelCentroids = this.microClusters
                .stream()
                .map(MicroCluster::calculateCentroid)
                .sorted(new SampleDistanceComparator(centroid))
                .collect(Collectors.toList());

        final double a = cluster.calculateStandardDeviation();

        final double b;
        if (decisionModelCentroids.size() > 0) {
            final Sample closestCentroid = decisionModelCentroids.get(0);
            b = centroid.distance(closestCentroid);
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
