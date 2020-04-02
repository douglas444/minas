package br.com.douglas444.minas.config;

public class Configuration {

    private int minSizeDN;
    private int minClusterSize;
    private int windowSize;
    private int clusterLifespan;
    private int sampleLifespan;

    private ClusteringAlgorithmController clusteringAlgorithmController;
    private VL vl;
    private VL vlSleep;

    public Configuration(int minSizeDN, int minClusterSize, int windowSize, int clusterLifespan, int sampleLifespan,
                         ClusteringAlgorithmController clusteringAlgorithmController, VL vl, VL vlSleep) {
        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.clusterLifespan = clusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.clusteringAlgorithmController = clusteringAlgorithmController;
        this.vl = vl;
        this.vlSleep = vlSleep;
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

    public ClusteringAlgorithmController getClusteringAlgorithmController() {
        return clusteringAlgorithmController;
    }

    public void setClusteringAlgorithmController(ClusteringAlgorithmController clusteringAlgorithmController) {
        this.clusteringAlgorithmController = clusteringAlgorithmController;
    }

    public VL getVl() {
        return vl;
    }

    public void setVl(VL vl) {
        this.vl = vl;
    }

    public VL getVlSleep() {
        return vlSleep;
    }

    public void setVlSleep(VL vlSleep) {
        this.vlSleep = vlSleep;
    }
}
