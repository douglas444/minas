package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierBuilder;
import br.com.douglas444.minas.core.MINAS;

public class MINASBuilder implements DSClassifierBuilder {

    private int minSizeDN;
    private int minClusterSize;
    private int windowSize;
    private int microClusterLifespan;
    private int sampleLifespan;
    private int onlinePhaseStartTime;
    private boolean incrementallyUpdateDecisionModel;
    private boolean feedbackEnabled;

    private int heaterInitialBufferSize;
    private int heaterNumberOfClustersPerLabel;
    private int heaterAgglomerativeBufferThreshold;
    private int noveltyDetectionNumberOfClusters;
    private int randomGeneratorSeed;

    private MicroClusterPredictor mainMicroClusterPredictor;
    private MicroClusterPredictor sleepMemoryMicroClusterPredictor;
    private SamplePredictor samplePredictor;

    public MINASBuilder(int minSizeDN,
                        int minClusterSize,
                        int windowSize,
                        int microClusterLifespan,
                        int sampleLifespan,
                        int onlinePhaseStartTime,
                        boolean incrementallyUpdateDecisionModel,
                        boolean feedbackEnabled,
                        int heaterInitialBufferSize,
                        int heaterNumberOfClustersPerLabel,
                        int heaterAgglomerativeBufferThreshold,
                        int noveltyDetectionNumberOfClusters,
                        int randomGeneratorSeed,
                        MicroClusterPredictor mainMicroClusterPredictor,
                        MicroClusterPredictor sleepMemoryMicroClusterPredictor,
                        SamplePredictor samplePredictor) {

        this.minSizeDN = minSizeDN;
        this.minClusterSize = minClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.onlinePhaseStartTime = onlinePhaseStartTime;
        this.incrementallyUpdateDecisionModel = incrementallyUpdateDecisionModel;
        this.feedbackEnabled = feedbackEnabled;
        this.heaterInitialBufferSize = heaterInitialBufferSize;
        this.heaterNumberOfClustersPerLabel = heaterNumberOfClustersPerLabel;
        this.heaterAgglomerativeBufferThreshold = heaterAgglomerativeBufferThreshold;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.randomGeneratorSeed = randomGeneratorSeed;
        this.mainMicroClusterPredictor = mainMicroClusterPredictor;
        this.sleepMemoryMicroClusterPredictor = sleepMemoryMicroClusterPredictor;
        this.samplePredictor = samplePredictor;
    }

    public MINASController build() {

        final MINAS minas = new MINAS(this.minSizeDN,
                this.minClusterSize,
                this.windowSize,
                this.microClusterLifespan,
                this.sampleLifespan,
                this.onlinePhaseStartTime,
                this.incrementallyUpdateDecisionModel,
                this.feedbackEnabled,
                this.heaterInitialBufferSize,
                this.heaterNumberOfClustersPerLabel,
                this.heaterAgglomerativeBufferThreshold,
                this.noveltyDetectionNumberOfClusters,
                this.randomGeneratorSeed,
                this.mainMicroClusterPredictor,
                this.sleepMemoryMicroClusterPredictor,
                this.samplePredictor);

        return new MINASController(minas);

    }
}
