package br.com.douglas444.minas.core;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.minas.ClassificationResult;
import br.com.douglas444.minas.interceptor.DecisionModelContext;
import br.com.douglas444.minas.interceptor.MINASInterceptor;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.util.SampleDistanceComparator;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private final boolean incrementallyUpdate;
    private final MINASInterceptor interceptorCollection;
    private final List<MicroCluster> microClusters;

    DecisionModel(boolean incrementallyUpdate, MINASInterceptor interceptorCollection) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.interceptorCollection = interceptorCollection;
        this.microClusters = new ArrayList<>();
    }

    ClassificationResult classify(final Sample sample) {

        final ClassificationResult classificationResult = this.interceptorCollection.SAMPLE_CLASSIFIER_INTERCEPTOR
                .with(new DecisionModelContext()
                        .sampleTarget(sample)
                        .decisionModel(this.microClusters))
                .executeOrDefault(() -> {

                    if (microClusters.isEmpty()) {
                        return new ClassificationResult(null, false);
                    }

                    final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample,
                            microClusters);

                    final double distance = sample.distance(closestMicroCluster.calculateCentroid());

                    if (distance <= closestMicroCluster.calculateStandardDeviation() * 2) {
                        return new ClassificationResult(closestMicroCluster, true);
                    }

                    return new ClassificationResult(closestMicroCluster, false);
                });

        classificationResult.ifExplained((closestMicroCluster) -> {
            closestMicroCluster.setTimestamp(sample.getT());
            if (this.incrementallyUpdate) {
                closestMicroCluster.update(sample);
            }
        });

        return classificationResult;
    }

    ClassificationResult classify(final MicroCluster microCluster) {

        return this.interceptorCollection.MICRO_CLUSTER_CLASSIFIER_INTERCEPTOR
                .with(new DecisionModelContext()
                        .microClusterTarget(microCluster)
                        .decisionModel(this.microClusters))
                .executeOrDefault(() -> {

                    if (microClusters.isEmpty()) {
                        return new ClassificationResult(null, false);
                    }

                    final MicroCluster closestMicroCluster = microCluster.calculateClosestMicroCluster(microClusters);
                    final double distance = microCluster.distance(closestMicroCluster);

                    if (distance <= closestMicroCluster.calculateStandardDeviation() + microCluster.calculateStandardDeviation()) {
                        return new ClassificationResult(closestMicroCluster, true);
                    }

                    return new ClassificationResult(closestMicroCluster, false);
                });
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

    List<MicroCluster> extractInactiveMicroClusters(final long timestamp, final int lifespan) {

        final List<MicroCluster> inactiveMicroClusters = this.microClusters
                .stream()
                .filter(microCluster -> microCluster.getTimestamp()  < timestamp - lifespan)
                .collect(Collectors.toList());

        this.microClusters.removeAll(inactiveMicroClusters);

        return inactiveMicroClusters;
    }

    public List<MicroCluster> getMicroClusters() {
        return microClusters;
    }

    void remove(final MicroCluster microCluster) {
        this.microClusters.remove(microCluster);
    }
}
