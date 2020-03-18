package br.com.douglas444.minas.config;

public class Configuration {

    private int minSizeDN;
    private int minClusterSize;
    private int windowSize;
    private int clusterLifespan;
    private int sampleLifespan;

    private ClusteringAlgorithm clusteringAlgorithm;
    private VL vl;

    public Configuration(int minSizeDN, int minClusterSize, int windowSize, int clusterLifespan, int sampleLifespan,
                         ClusteringAlgorithm clusteringAlgorithm, VL vl) {
        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.clusterLifespan = clusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.clusteringAlgorithm = clusteringAlgorithm;
        this.vl = vl;
    }

    public int getMinSizeDN() {
        return minSizeDN;
    }

    public void setMinSizeDN(int minSizeDN) {
        this.minSizeDN = minSizeDN;
    }

    public int getMinClusterSize() {
        return minClusterSize;
    }

    public void setMinClusterSize(int minClusterSize) {
        this.minClusterSize = minClusterSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getClusterLifespan() {
        return clusterLifespan;
    }

    public void setClusterLifespan(int clusterLifespan) {
        this.clusterLifespan = clusterLifespan;
    }

    public int getSampleLifespan() {
        return sampleLifespan;
    }

    public void setSampleLifespan(int sampleLifespan) {
        this.sampleLifespan = sampleLifespan;
    }

    public ClusteringAlgorithm getClusteringAlgorithm() {
        return clusteringAlgorithm;
    }

    public void setClusteringAlgorithm(ClusteringAlgorithm clusteringAlgorithm) {
        this.clusteringAlgorithm = clusteringAlgorithm;
    }

    public VL getVl() {
        return vl;
    }

    public void setVl(VL vl) {
        this.vl = vl;
    }
}
