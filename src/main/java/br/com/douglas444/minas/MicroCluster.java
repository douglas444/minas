package br.com.douglas444.minas;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DistanceComparator;
import br.com.douglas444.mltk.Sample;

import java.util.*;
import java.util.stream.Collectors;

public class MicroCluster {

    private int timestamp;
    private int label;
    private Category category;
    private int n;
    private double[] ls;
    private double[] ss;

    public MicroCluster(Cluster cluster) {

        int dimensions = cluster.getSamples().get(0).getX().length;
        List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }


    public MicroCluster(Cluster cluster, int label) {

        this.label = label;

        int dimensions = cluster.getSamples().get(0).getX().length;
        List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }

    public void update(Sample sample) {
        for (int i = 0; i < sample.getX().length; ++i) {
            this.timestamp = sample.getT();
            this.ls[i] += sample.getX()[i];
            this.ss[i] += sample.getX()[i] * sample.getX()[i];
        }
        ++this.n;
    }

    public Sample calculateCenter() {
        double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= this.n;
        }
        return new Sample(x, this.label);
    }

    public double calculateStandardDeviation() {

        double sum = 0;

        for (int i = 0; i < this.ss.length; ++i) {
            sum += Math.sqrt(this.ss[i]/this.n - (this.ls[i]/this.n * this.ls[i]/this.n));
        }

        return sum;

    }

    public static Optional<MicroCluster> calculateClosestMicroCluster(Sample sample, List<MicroCluster> microClusters) {


        HashMap<Sample, MicroCluster> microClusterByCenter = new HashMap<>();

        List<Sample> decisionModelCenters = microClusters.stream()
                .map(microCluster -> {
                    Sample center = microCluster.calculateCenter();
                    microClusterByCenter.put(center, microCluster);
                    return center;
                })
                .sorted(new DistanceComparator(sample))
                .collect(Collectors.toList());

        if (decisionModelCenters.size() > 0) {
            Sample closestCenter = decisionModelCenters.get(0);
            return Optional.of(microClusterByCenter.get(closestCenter));
        } else {
            return Optional.empty();
        }
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
