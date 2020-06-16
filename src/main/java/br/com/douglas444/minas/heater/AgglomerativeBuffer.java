package br.com.douglas444.minas.heater;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.minas.MicroClusterCategory;
import br.com.douglas444.mltk.clustering.kmeans.KMeans;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

public class AgglomerativeBuffer {

    private boolean isActive;
    private final List<Sample> initialData;
    private final List<MicroCluster> buffer;
    private final int initialDataSize;
    private final int bufferSize;
    private final long seed;
    private final int label;

    public AgglomerativeBuffer(int label, int initialDataSize, int bufferSize, long seed) {

        this.label = label;
        this.isActive = false;
        this.seed = seed;
        this.initialDataSize = initialDataSize;
        this.bufferSize = bufferSize;
        this.initialData = new ArrayList<>();
        this.buffer = new ArrayList<>();
    }

    public void add(final Sample sample) {

        if (!this.isActive) {

            this.initialData.add(sample);

            if (this.initialData.size() < this.initialDataSize) {
                return;
            }

            this.isActive = true;

            if (this.initialData.size() < this.bufferSize) {
                throw new IllegalStateException("not enough samples for agglomerative buffer");
            }

            final List<Cluster> clusters = KMeans.execute(this.initialData, this.bufferSize, this.seed);

            this.initialData.clear();

            clusters.stream()
                    .map(cluster -> new MicroCluster(cluster, this.label, 0, MicroClusterCategory.KNOWN))
                    .forEach(this.buffer::add);


            return;

        }

        final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample, this.buffer);
        double distance = sample.distance(closestMicroCluster.calculateCentroid());

        final double radius;

        if (closestMicroCluster.getN() > 1) {

            radius = closestMicroCluster.calculateStandardDeviation() * 2;

        } else {

             final List<MicroCluster> bufferSubSet = this.buffer.stream()
                    .filter(microCluster -> microCluster != closestMicroCluster)
                    .collect(Collectors.toCollection(ArrayList::new));

            radius = MicroCluster
                    .calculateClosestMicroCluster(closestMicroCluster.calculateCentroid(), bufferSubSet)
                    .distance(closestMicroCluster);
        }

        if (distance < radius) {
            closestMicroCluster.update(sample);
            closestMicroCluster.setTimestamp(sample.getT());
        } else {
            MicroCluster microCluster = new MicroCluster(sample);
            microCluster.setLabel(sample.getY());
            microCluster.setMicroClusterCategory(MicroClusterCategory.KNOWN);
            this.add(microCluster);
        }

    }

    private void add(final MicroCluster microCluster) {

        MicroCluster m1 = null;
        MicroCluster m2 = null;

        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < this.buffer.size(); i++) {

            MicroCluster a = this.buffer.get(i);

            for (int j = i + 1; j < this.buffer.size(); j++) {

                MicroCluster b = this.buffer.get(j);

                double distance = a.distance(b);
                if (distance < minDistance) {
                    minDistance = distance;
                    m1 = a;
                    m2 = b;
                }
            }
        }

        if (m1 != null) {
            this.buffer.remove(m1);
            this.buffer.remove(m2);
            this.buffer.add(MicroCluster.merge(m1, m2));
        } else {
            throw new IllegalStateException("could not decrease agglomerative buffer size");
        }

        this.buffer.add(microCluster);

    }

    public List<MicroCluster> getBuffer() {
        if (!this.isActive) {
            throw new IllegalStateException("agglomerative buffer not active");
        }
        return this.buffer;
    }

}
