package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierBuilder;
import br.com.douglas444.minas.core.MINAS;

public class MINASBuilder implements DSClassifierBuilder {

    private final int temporaryMemoryMaxSize;
    private final int minimumClusterSize;
    private final int windowSize;
    private final int microClusterLifespan;
    private final int sampleLifespan;
    private final int heaterCapacity;
    private final boolean incrementallyUpdateDecisionModel;
    private final boolean feedbackEnabled;
    private final int heaterInitialBufferSize;
    private final int heaterNumberOfClustersPerLabel;
    private final int noveltyDetectionNumberOfClusters;
    private final int randomGeneratorSeed;
    private final MicroClusterClassifier mainMicroClusterPredictor;
    private final MicroClusterClassifier sleepMicroClusterPredictor;
    private final SampleClassifier samplePredictor;

    public MINASBuilder(int temporaryMemoryMaxSize,
                        int minimumClusterSize,
                        int windowSize,
                        int microClusterLifespan,
                        int sampleLifespan,
                        int heaterCapacity,
                        boolean incrementallyUpdateDecisionModel,
                        boolean feedbackEnabled,
                        int heaterInitialBufferSize,
                        int heaterNumberOfClustersPerLabel,
                        int noveltyDetectionNumberOfClusters,
                        int randomGeneratorSeed,
                        MicroClusterClassifier mainMicroClusterPredictor,
                        MicroClusterClassifier sleepMicroClusterPredictor,
                        SampleClassifier samplePredictor) {

        this.temporaryMemoryMaxSize = temporaryMemoryMaxSize;
        this.minimumClusterSize = minimumClusterSize;
        this.windowSize = windowSize;
        this.microClusterLifespan = microClusterLifespan;
        this.sampleLifespan = sampleLifespan;
        this.heaterCapacity = heaterCapacity;
        this.incrementallyUpdateDecisionModel = incrementallyUpdateDecisionModel;
        this.feedbackEnabled = feedbackEnabled;
        this.heaterInitialBufferSize = heaterInitialBufferSize;
        this.heaterNumberOfClustersPerLabel = heaterNumberOfClustersPerLabel;
        this.noveltyDetectionNumberOfClusters = noveltyDetectionNumberOfClusters;
        this.randomGeneratorSeed = randomGeneratorSeed;
        this.mainMicroClusterPredictor = mainMicroClusterPredictor;
        this.sleepMicroClusterPredictor = sleepMicroClusterPredictor;
        this.samplePredictor = samplePredictor;

    }

    public MINASController build() {

        final MINAS minas = new MINAS(this.temporaryMemoryMaxSize,
                this.minimumClusterSize,
                this.windowSize,
                this.microClusterLifespan,
                this.sampleLifespan,
                this.heaterCapacity,
                this.incrementallyUpdateDecisionModel,
                this.feedbackEnabled,
                this.heaterInitialBufferSize,
                this.heaterNumberOfClustersPerLabel,
                this.noveltyDetectionNumberOfClusters,
                this.randomGeneratorSeed,
                this.mainMicroClusterPredictor,
                this.sleepMicroClusterPredictor,
                this.samplePredictor);

        return new MINASController(minas);

    }
}
