package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MicroCluster {

    private int timestamp;
    private int label;
    private Category category;
    private int n;
    private double[] ls;
    private double[] ss;

    MicroCluster(Cluster cluster) {

        int dimensions = cluster.getSamples().get(0).getX().length;
        List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }


    MicroCluster(Cluster cluster, int label) {

        this.label = label;

        int dimensions = cluster.getSamples().get(0).getX().length;
        List<Sample> samples = cluster.getSamples();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);
    }

    MicroCluster(int dimensions, List<Sample> samples) {

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        samples.forEach(this::update);

    }

    void update(Sample sample) {
        for (int i = 0; i < sample.getX().length; ++i) {
            this.timestamp = sample.getT();
            this.ls[i] += sample.getX()[i];
            this.ss[i] += sample.getX()[i] * sample.getX()[i];
        }
        ++this.n;
    }

    Sample calculateCenter() {
        double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= this.n;
        }
        return new Sample(x, this.label);
    }

    double calculateStandardDeviation() {

        double sum = 0;

        for (int i = 0; i < this.ss.length; ++i) {
            sum += Math.sqrt(this.ss[i]/this.n - (this.ls[i]/this.n * this.ls[i]/this.n));
        }

        return sum;

    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public double[] getLs() {
        return ls;
    }

    public void setLs(double[] ls) {
        this.ls = ls;
    }

    public double[] getSs() {
        return ss;
    }

    public void setSs(double[] ss) {
        this.ss = ss;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
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
