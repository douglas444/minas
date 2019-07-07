package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Point;

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

        int dimensions = cluster.getPoints().get(0).getX().length;
        List<Point> points = cluster.getPoints();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        points.forEach(this::update);
    }


    MicroCluster(Cluster cluster, int label) {

        this.label = label;

        int dimensions = cluster.getPoints().get(0).getX().length;
        List<Point> points = cluster.getPoints();

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        points.forEach(this::update);
    }

    MicroCluster(int dimensions, List<Point> points) {

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        points.forEach(this::update);

    }

    void update(Point point) {
        for (int i = 0; i < point.getX().length; ++i) {
            this.timestamp = point.getT();
            this.ls[i] += point.getX()[i];
            this.ss[i] += point.getX()[i] * point.getX()[i];
        }
        ++this.n;
    }

    Point calculateCenter() {
        double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= this.n;
        }
        return new Point(x, this.label);
    }

    double calculateStandardDeviation() {

        Point center = this.calculateCenter();

        double sum = 0;

        for (int i = 0; i < this.ss.length; ++i) {
            sum += this.ss[i] - (2 * this.ls[i] * center.getX()[i]) + (center.getX()[i] * center.getX()[i]);
        }

        return Math.sqrt(sum / n);

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
