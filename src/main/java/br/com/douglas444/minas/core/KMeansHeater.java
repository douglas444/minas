package br.com.douglas444.minas.core;

import br.com.douglas444.minas.type.MicroClusterCategory;
import br.com.douglas444.minas.type.MicroCluster;
import br.com.douglas444.mltk.clustering.kmeans.KMeans;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class KMeansHeater implements Heater {

    private final int k;
    private final long seed;
    private final List<Sample> buffer;

    private int currentTimestamp;

    KMeansHeater(int k, long seed) {
        this.k = k;
        this.seed = seed;
        this.buffer = new ArrayList<>();
    }

    @Override
    public void process(final Sample sample) {
        this.currentTimestamp = sample.getT();
        this.buffer.add(sample);
    }

    @Override
    public List<MicroCluster> close() {

        final List<MicroCluster> microClusters = new ArrayList<>();
        final HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        this.buffer.forEach(storedSample -> {
            samplesByLabel.putIfAbsent(storedSample.getY(), new ArrayList<>());
            samplesByLabel.get(storedSample.getY()).add(storedSample);
        });

        this.buffer.clear();

        samplesByLabel.forEach((label, samples) -> {

            final List<Cluster> clusters = KMeans.execute(samples, this.k, this.seed);

            clusters.stream()
                    .map(cluster -> new MicroCluster(cluster, label, this.currentTimestamp, MicroClusterCategory.KNOWN))
                    .forEach(microClusters::add);
        });

        return microClusters;

    }
}
