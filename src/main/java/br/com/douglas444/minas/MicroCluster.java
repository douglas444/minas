package br.com.douglas444.minas;

import br.com.douglas444.streams.datastructures.Cluster;
import br.com.douglas444.streams.datastructures.Sample;
import br.com.douglas444.streams.datastructures.SampleDistanceComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MicroCluster {

    private Long timestamp;
    private Integer label;
    private MicroClusterCategory microClusterCategory;
    private int n;
    private final double[] ls;
    private final double[] ss;

    public MicroCluster(final Long timestamp,
                        final Integer label,
                        final MicroClusterCategory microClusterCategory,
                        final int n,
                        final double[] ls,
                        final double[] ss) {

        this.timestamp = timestamp;
        this.label = label;
        this.microClusterCategory = microClusterCategory;
        this.n = n;
        this.ls = ls;
        this.ss = ss;
    }

    public MicroCluster(final Sample sample) {

        final int dimensions = sample.getX().length;

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        this.update(sample);
    }

    public MicroCluster(final Cluster cluster, final Long timestamp) {

        if (cluster.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.timestamp = timestamp;
        final int dimensions = cluster.getSamples().get(0).getX().length;

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        cluster.getSamples().forEach(this::update);
    }

    public MicroCluster(final Cluster cluster,
                        final Integer label,
                        final MicroClusterCategory microClusterCategory) {

        if (cluster.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.label = label;
        this.microClusterCategory = microClusterCategory;
        final int dimensions = cluster.getSamples().get(0).getX().length;

        this.n = 0;
        this.ls = new double[dimensions];
        this.ss = new double[dimensions];

        cluster.getSamples().forEach(this::update);
    }

    public void update(final Sample sample) {

        for (int i = 0; i < sample.getX().length; ++i) {
            this.ls[i] += sample.getX()[i];
            this.ss[i] += sample.getX()[i] * sample.getX()[i];
        }

        ++this.n;
    }

    public Sample calculateCentroid() {

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

    public double distance(final MicroCluster microCluster) {

        return this.calculateCentroid().distance(microCluster.calculateCentroid());

    }

    public MicroCluster calculateClosestMicroCluster(final List<MicroCluster> microClusters) {

        if (microClusters.isEmpty()) {
            throw new IllegalArgumentException();
        }

        final HashMap<Sample, MicroCluster> microClusterByCentroid = new HashMap<>();

        final List<Sample> decisionModelCentroids = microClusters.stream()
                .map(microCluster -> {
                    Sample microClusterCentroid = microCluster.calculateCentroid();
                    microClusterByCentroid.put(microClusterCentroid, microCluster);
                    return microClusterCentroid;
                })
                .sorted(new SampleDistanceComparator(this.calculateCentroid()))
                .collect(Collectors.toList());

        final Sample closestCentroid = decisionModelCentroids.get(0);
        return microClusterByCentroid.get(closestCentroid);

    }

    public static MicroCluster calculateClosestMicroCluster(final Sample sample,
                                                            final List<MicroCluster> microClusters) {

        if (microClusters.isEmpty()) {
            throw new IllegalArgumentException();
        }

        final HashMap<Sample, MicroCluster> microClusterByCentroid = new HashMap<>();

        final List<Sample> decisionModelCentroids = microClusters.stream()
                .map(microCluster -> {
                    Sample microClusterCentroid = microCluster.calculateCentroid();
                    microClusterByCentroid.put(microClusterCentroid, microCluster);
                    return microClusterCentroid;
                })
                .sorted(new SampleDistanceComparator(sample))
                .collect(Collectors.toList());

        final Sample closestCentroid = decisionModelCentroids.get(0);
        return microClusterByCentroid.get(closestCentroid);
    }

    public static MicroCluster merge(MicroCluster m1, MicroCluster m2) {

        final int n = m1.n + m2.n;
        final double[] ss = m1.ss.clone();
        final double[] ls = m1.ls.clone();

        final Long timestamp =
                (m1.timestamp != null && m2.timestamp != null) ?
                        Long.valueOf(Math.max(m1.timestamp, m2.timestamp))
                        : (m1.timestamp != null ? m1.timestamp : m2.timestamp);

        final Integer label = m1.label;
        final MicroClusterCategory microClusterCategory = MicroClusterCategory
                .valueOf(m1.microClusterCategory.name());

        for (int i = 0; i < ss.length; ++i) {
            ss[i] += m2.ss[i];
            ls[i] += m2.ls[i];
        }

        return new MicroCluster(timestamp, label, microClusterCategory, n, ls, ss);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MicroCluster that = (MicroCluster) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(label, that.label) &&
                n == that.n &&
                microClusterCategory == that.microClusterCategory &&
                Arrays.equals(ls, that.ls) &&
                Arrays.equals(ss, that.ss);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, label, microClusterCategory, n);
        result = 31 * result + Arrays.hashCode(ls);
        result = 31 * result + Arrays.hashCode(ss);
        return result;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public MicroClusterCategory getMicroClusterCategory() {
        return microClusterCategory;
    }

    public void setMicroClusterCategory(MicroClusterCategory microClusterCategory) {
        this.microClusterCategory = microClusterCategory;
    }

    public int getN() {
        return n;
    }
}
