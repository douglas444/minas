package br.com.douglas444.minas;

import br.com.douglas444.minas.interceptor.context.DecisionModelContext;
import br.com.douglas444.minas.interceptor.MINASInterceptor;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.util.SampleDistanceComparator;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

class DecisionModel {

    private final boolean incrementallyUpdate;
    private final MINASInterceptor interceptor;
    private final List<MicroCluster> microClusters;

    DecisionModel(boolean incrementallyUpdate, MINASInterceptor interceptor) {

        this.incrementallyUpdate = incrementallyUpdate;
        this.interceptor = interceptor;
        this.microClusters = new ArrayList<>();
    }

    Classification classify(final Sample sample) {

        final DecisionModelContext context = new DecisionModelContext();
        context.setDecisionModel(this.microClusters);
        context.setSampleTarget(sample);

        final Classification classification = this.interceptor.SAMPLE_CLASSIFIER.with(context).executeOrDefault(() -> {

                    if (microClusters.isEmpty()) {
                        return new Classification(null, false);
                    }

                    final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample,
                            microClusters);
                    final double distance = sample.distance(closestMicroCluster.calculateCentroid());

                    if (distance <= closestMicroCluster.calculateStandardDeviation() * 2) {
                        return new Classification(closestMicroCluster, true);
                    }

                    return new Classification(closestMicroCluster, false);
                });

        classification.ifExplained((closestMicroCluster) -> {
            closestMicroCluster.setTimestamp(sample.getT());
            if (this.incrementallyUpdate) {
                closestMicroCluster.update(sample);
            }
        });

        return classification;
    }

    Classification classify(final MicroCluster microCluster) {

        final DecisionModelContext context = new DecisionModelContext();
        context.setDecisionModel(this.microClusters);
        context.setMicroClusterTarget(microCluster);

        return this.interceptor.MICRO_CLUSTER_CLASSIFIER.with(context).executeOrDefault(() -> {

                    if (microClusters.isEmpty()) {
                        return new Classification(null, false);
                    }

                    final MicroCluster closestMicroCluster = microCluster.calculateClosestMicroCluster(microClusters);
                    final double distance = microCluster.distance(closestMicroCluster);

                    if (distance <= closestMicroCluster.calculateStandardDeviation()
                            + microCluster.calculateStandardDeviation()) {
                        return new Classification(closestMicroCluster, true);
                    }

                    return new Classification(closestMicroCluster, false);
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
