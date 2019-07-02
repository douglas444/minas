package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Point;

import java.util.List;

public class MicroCluster {

    private int timestamp;
    double label;
    private Category category;
    private int n;
    private double[] ls;
    private double[] ss;

    public MicroCluster(Cluster cluster) {

        int dimensions = cluster.getPoints().get(0).getX().length;
        List<Point> points = cluster.getPoints();

        this.n = points.size();
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        points.forEach(this::update);
    }

    public MicroCluster(int dimensions, List<Point> points) {

        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        points.forEach(this::update);

    }

    public void update(Point point) {
        for (int i = 0; i < point.getX().length; ++i) {
            this.ls[i] += point.getX()[i];
            this.ss[i] += point.getX()[i] * point.getX()[i];
            ++this.n;
        }
    }

    public Point calculateCenter() {
        double[] x = this.ls.clone();
        for (int i = 0; i < x.length; ++i) {
            x[i] /= n;
        }
        return new Point(x, label);
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

    public double getLabel() {
        return label;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
