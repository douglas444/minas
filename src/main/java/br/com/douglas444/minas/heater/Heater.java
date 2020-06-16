package br.com.douglas444.minas.heater;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Heater {

    private final int k;
    private final long seed;
    private final int initialBufferSize;
    private final HashMap<Integer, AgglomerativeBuffer> agglomerativeBufferByLabel;

    public Heater(int initialBufferSize, int k, long seed) {

        this.initialBufferSize = initialBufferSize;
        this.k = k;
        this.seed = seed;
        this.agglomerativeBufferByLabel = new HashMap<>();

    }

    public void process(final Sample sample) {

        final int label = sample.getY();

        this.agglomerativeBufferByLabel.putIfAbsent(label,
                new AgglomerativeBuffer(label, this.initialBufferSize, this.k, this.seed));

        final AgglomerativeBuffer ab = this.agglomerativeBufferByLabel.get(label);
        ab.add(sample);

    }

    public List<MicroCluster> getResult() {

        final List<MicroCluster> microClusters = new ArrayList<>();
        this.agglomerativeBufferByLabel.forEach((label, ab) -> microClusters.addAll(ab.getBuffer()));
        return microClusters;

    }

}
