package br.com.douglas444.minas.core;

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

    public MicroCluster(Cluster cluster, int timestamp) {

        this.timestamp = timestamp;
        final int dimensions = cluster.getSamples().get(0).getX().length;
        final List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }


    public MicroCluster(Cluster cluster, int label, int timestamp) {

        this.timestamp = timestamp;
        this.label = label;

        final int dimensions = cluster.getSamples().get(0).getX().length;
        final List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }

    public MicroCluster(Cluster cluster, int label, int timestamp, Category category) {

        this.timestamp = timestamp;
        this.label = label;
        this.category = category;

        final int dimensions = cluster.getSamples().get(0).getX().length;
        final List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }

    public void update(Sample sample) {

        for (int i = 0; i < sample.getX().length; ++i) {
            this.ls[i] += sample.getX()[i];
            this.ss[i] += sample.getX()[i] * sample.getX()[i];
        }

        ++this.n;
    }

    public Sample calculateCenter() {
        final double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= this.n;
        }
        return new Sample(x, this.label);
    }

    public double calculateStandardDeviation() {

        double sum = 0;

        for (int i = 0; i < this.ss.length; ++i) {
            sum += (this.ss[i] / this.n) - Math.pow(this.ls[i] / this.n, 2);
        }

        return Math.sqrt(sum);

    }

    public static Optional<MicroCluster> calculateClosestMicroCluster(Sample sample, List<MicroCluster> microClusters) {

        final HashMap<Sample, MicroCluster> microClusterByCenter = new HashMap<>();

        final List<Sample> decisionModelCenters = microClusters.stream()
                .map(microCluster -> {
                    Sample center = microCluster.calculateCenter();
                    microClusterByCenter.put(center, microCluster);
                    return center;
                })
                .sorted(new DistanceComparator(sample))
                .collect(Collectors.toList());

        if (decisionModelCenters.size() > 0) {
            final Sample closestCenter = decisionModelCenters.get(0);
            return Optional.of(microClusterByCenter.get(closestCenter));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MicroCluster that = (MicroCluster) o;
        return timestamp == that.timestamp &&
                label == that.label &&
                n == that.n &&
                category == that.category &&
                Arrays.equals(ls, that.ls) &&
                Arrays.equals(ss, that.ss);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, label, category, n);
        result = 31 * result + Arrays.hashCode(ls);
        result = 31 * result + Arrays.hashCode(ss);
        return result;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
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
