package br.com.douglas444.minas.config;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;
import br.com.douglas444.mltk.kmeans.KMeansPlusPlus;
import java.util.List;

public class KMeans implements ClusteringAlgorithm {

    private int k;

    public KMeans(int k) {
        this.k = k;
    }

    @Override
    public List<Cluster> execute(List<Sample> samples) {
        KMeansPlusPlus kMeansPlusPlus = new KMeansPlusPlus(samples, k);
        return kMeansPlusPlus.fit();
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
