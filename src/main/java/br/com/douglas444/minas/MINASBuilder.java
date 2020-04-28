package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierBuilder;
import br.com.douglas444.minas.type.HeaterType;
import br.com.douglas444.minas.type.MicroClusterPredictor;
import br.com.douglas444.minas.type.SamplePredictor;
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

    private HeaterType heaterType;

    private int heaterNumberOfClusters;
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
                        HeaterType heaterType,
                        int heaterNumberOfClusters,
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
        this.heaterType = heaterType;
        this.heaterNumberOfClusters = heaterNumberOfClusters;
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
                this.heaterType,
                this.heaterNumberOfClusters,
                this.noveltyDetectionNumberOfClusters,
                this.randomGeneratorSeed,
                this.mainMicroClusterPredictor,
                this.sleepMemoryMicroClusterPredictor,
                this.samplePredictor);

        return new MINASController(minas);

    }
}
