package br.com.douglas444.minas.heater;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Heater {

    private final int k;
    private final Random random;
    private final int initialBufferSize;
    private final HashMap<Integer, AgglomerativeBuffer> agglomerativeBufferByLabel;

    public Heater(int initialBufferSize, int k, Random random) {

        this.initialBufferSize = initialBufferSize;
        this.k = k;
        this.random = random;
        this.agglomerativeBufferByLabel = new HashMap<>();

    }

    public void process(final Sample sample) {

        final int label = sample.getY();

        this.agglomerativeBufferByLabel.putIfAbsent(label,
                new AgglomerativeBuffer(label, this.initialBufferSize, this.k, this.random));

        final AgglomerativeBuffer ab = this.agglomerativeBufferByLabel.get(label);
        ab.add(sample);

    }

    public List<MicroCluster> getResult() {

        final List<MicroCluster> microClusters = new ArrayList<>();
        this.agglomerativeBufferByLabel.forEach((label, ab) -> microClusters.addAll(ab.getBuffer()));
        return microClusters;

    }

}
