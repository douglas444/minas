package br.com.douglas444.minas.config;

public class Configuration {

    private int minSizeDN;
    private int minClusterSize;
    private int windowSize;
    private int microClusterLifespan;
    private int sampleLifespan;
    private boolean incrementallyUpdatable;
    private boolean turnFeedbackOn;

    private ClusteringAlgorithmController onlineClusteringAlgorithm;
    private ClusteringAlgorithmController offlineClusteringAlgorithm;

    private MicroClusterPredictor mainMicroClusterPredictor;
    private MicroClusterPredictor sleepMemoryMicroClusterPredictor;
    private SamplePredictor samplePredictor;

    public Configuration(int minSizeDN, int minClusterSize, int windowSize, int microClusterLifespan,
                         int sampleLifespan, boolean incrementallyUpdatable, boolean turnFeedbackOn,
                         ClusteringAlgorithmController onlineClusteringAlgorithm,
                         ClusteringAlgorithmController offlineClusteringAlgorithm,
                         MicroClusterPredictor mainMicroClusterPredictor,
                         MicroClusterPredictor sleepMemoryMicroClusterPredictor,
                         SamplePredictor samplePredictor) {

        this.turnFeedbackOn = turnFeedbackOn;
        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.onlineClusteringAlgorithm = onlineClusteringAlgorithm;
        this.mainMicroClusterPredictor = mainMicroClusterPredictor;
        this.sleepMemoryMicroClusterPredictor = sleepMemoryMicroClusterPredictor;
        this.samplePredictor = samplePredictor;
        this.incrementallyUpdatable = incrementallyUpdatable;
        this.offlineClusteringAlgorithm = offlineClusteringAlgorithm;
    }

    public int getMinSizeDN() {
        return minSizeDN;
    }

    public int getMinClusterSize() {
        return minClusterSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getMicroClusterLifespan() {
        return microClusterLifespan;
    }

    public int getSampleLifespan() {
        return sampleLifespan;
    }

    public ClusteringAlgorithmController getOnlineClusteringAlgorithm() {
        return onlineClusteringAlgorithm;
    }

    public MicroClusterPredictor getMainMicroClusterPredictor() {
        return mainMicroClusterPredictor;
    }

    public MicroClusterPredictor getSleepMemoryMicroClusterPredictor() {
        return sleepMemoryMicroClusterPredictor;
    }

    public boolean isIncrementallyUpdatable() {
        return incrementallyUpdatable;
    }


    public SamplePredictor getSamplePredictor() {
        return samplePredictor;
    }

    public ClusteringAlgorithmController getOfflineClusteringAlgorithm() {
        return offlineClusteringAlgorithm;
    }

    public boolean getTurnFeedbackOn() {
        return turnFeedbackOn;
    }
}
