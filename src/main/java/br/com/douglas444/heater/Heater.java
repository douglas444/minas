package br.com.douglas444.heater;

import br.com.douglas444.minas.type.MicroClusterCategory;
import br.com.douglas444.minas.type.MicroCluster;
import br.com.douglas444.mltk.clustering.kmeans.KMeans;
import br.com.douglas444.mltk.datastructure.Cluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Heater {

    private int k;
    private long seed;
    private int threshold;
    private boolean initialized;
    private final int initialBufferSize;
    private final List<Sample> initialBuffer;
    private final HashMap<Integer, AgglomerativeBuffer> agglomerativeBufferByLabel;

    public Heater(int initialBufferSize, int k, int threshold, long seed) {

        this.k = k;
        this.seed = seed;
        this.threshold = threshold;
        this.initialized = false;
        this.initialBufferSize = initialBufferSize;
        this.initialBuffer = new ArrayList<>();
        this.agglomerativeBufferByLabel = new HashMap<>();

    }

    public void process(final Sample sample) {

        if (!this.initialized) {

            this.initialBuffer.add(sample);

            if (this.initialBuffer.size() >= this.initialBufferSize) {
                this.initialize();
            }

        } else if (this.agglomerativeBufferByLabel.containsKey(sample.getY())) {

            int label = sample.getY();
            AgglomerativeBuffer ab = this.agglomerativeBufferByLabel.get(label);
            ab.add(sample);

        }

    }

    private void initialize() {

        this.initialized = true;

        final HashMap<Integer, List<Sample>> samplesByLabel = new HashMap<>();

        this.initialBuffer.forEach(storedSample -> {
            samplesByLabel.putIfAbsent(storedSample.getY(), new ArrayList<>());
            samplesByLabel.get(storedSample.getY()).add(storedSample);
        });

        this.initialBuffer.clear();

        samplesByLabel.forEach((label, samples) -> {

            if (samples.size() < this.k) {
                throw new IllegalStateException("not enough samples for label");
            }

            final List<Cluster> clusters = KMeans.execute(samples, this.k, this.seed);

            final List<MicroCluster> microClusters = new ArrayList<>();

            clusters.stream()
                    .map(cluster -> new MicroCluster(cluster, label, 0, MicroClusterCategory.KNOWN))
                    .forEach(microClusters::add);

            AgglomerativeBuffer ab = new AgglomerativeBuffer(microClusters, this.threshold);

            this.agglomerativeBufferByLabel.put(label, ab);

        });

    }

    public List<MicroCluster> getResult() {

        List<MicroCluster> microClusters = new ArrayList<>();
        this.agglomerativeBufferByLabel.forEach((label, ab) -> microClusters.addAll(ab.getBuffer()));
        return microClusters;

    }
}
