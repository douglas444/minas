package br.com.douglas444.minas.config;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;
import br.com.douglas444.mltk.kmeans.KMeans;
import java.util.List;

public class KMeansController implements ClusteringAlgorithmController {

    private int k;
    private int seed;

    public KMeansController(int k, int seed) {
        this.k = k;
        this.seed = seed;
    }

    @Override
    public List<Cluster> execute(List<Sample> samples) {
        KMeans kMeans = new KMeans(samples, this.k, this.seed);
        return kMeans.fit();
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
