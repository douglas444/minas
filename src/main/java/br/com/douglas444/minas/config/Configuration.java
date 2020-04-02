package br.com.douglas444.minas.config;

public class Configuration {

    private int minSizeDN;
    private int minClusterSize;
    private int windowSize;
    private int microClusterLifespan;
    private int sampleLifespan;
    private boolean incrementallyUpdatable;
    private boolean feedbackNeeded;

    private ClusteringAlgorithmController mainClusteringAlgorithm;
    private ClusteringAlgorithmController decisionModelBuilderClusteringAlgorithm;

    private MicroClusterPredictor mainMicroClusterPredictor;
    private MicroClusterPredictor sleepMemoryMicroClusterPredictor;
    private SamplePredictor samplePredictor;

    public Configuration(int minSizeDN, int minClusterSize, int windowSize, int microClusterLifespan,
                         int sampleLifespan, boolean incrementallyUpdatable, boolean feedbackNeeded,
                         ClusteringAlgorithmController mainClusteringAlgorithm,
                         ClusteringAlgorithmController decisionModelBuilderClusteringAlgorithm,
                         MicroClusterPredictor mainMicroClusterPredictor,
                         MicroClusterPredictor sleepMemoryMicroClusterPredictor,
                         SamplePredictor samplePredictor) {

        this.feedbackNeeded = feedbackNeeded;
        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.mainClusteringAlgorithm = mainClusteringAlgorithm;
        this.mainMicroClusterPredictor = mainMicroClusterPredictor;
        this.sleepMemoryMicroClusterPredictor = sleepMemoryMicroClusterPredictor;
        this.samplePredictor = samplePredictor;
        this.incrementallyUpdatable = incrementallyUpdatable;
        this.decisionModelBuilderClusteringAlgorithm = decisionModelBuilderClusteringAlgorithm;
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

    public ClusteringAlgorithmController getMainClusteringAlgorithm() {
        return mainClusteringAlgorithm;
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

    public ClusteringAlgorithmController getDecisionModelBuilderClusteringAlgorithm() {
        return decisionModelBuilderClusteringAlgorithm;
    }

    public boolean isFeedbackNeeded() {
        return feedbackNeeded;
    }
}
