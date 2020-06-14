package br.com.douglas444.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSClassifierExecutor;
import br.com.douglas444.minas.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    private static final int MIN_SIZE_DN = 2000;
    private static final int MIN_CLUSTER_SIZE = 20;
    private static final int WINDOW_SIZE = 4000;
    private static final int MICRO_CLUSTER_LIFESPAN = 4000;
    private static final int SAMPLE_LIFESPAN = 4000;
    private static final int ONLINE_PHASE_START_TIME = 10000;
    private static final boolean INCREMENTALLY_UPDATE_DECISION_MODEL = false;
    private static final boolean FEEDBACK_ENABLED = false;

    private static final int HEATER_INITIAL_BUFFER_SIZE = 1000;
    private static final int HEATER_NUMBER_OF_CLUSTER_PER_LABEL = 100;
    private static final int HEATER_AGGLOMERATIVE_BUFFER_THRESHOLD = 100;

    private static final int NOVELTY_DETECTION_NUMBER_OF_CLUSTERS = 100;
    private static final int RANDOM_GENERATOR_SEED = 1;


    public static void main(String[] args) throws IOException {

        final MINASBuilder minasBuilder = new MINASBuilder(
                MIN_SIZE_DN,
                MIN_CLUSTER_SIZE,
                WINDOW_SIZE,
                MICRO_CLUSTER_LIFESPAN,
                SAMPLE_LIFESPAN,
                ONLINE_PHASE_START_TIME,
                INCREMENTALLY_UPDATE_DECISION_MODEL,
                FEEDBACK_ENABLED,
                HEATER_INITIAL_BUFFER_SIZE,
                HEATER_NUMBER_OF_CLUSTER_PER_LABEL,
                HEATER_AGGLOMERATIVE_BUFFER_THRESHOLD,
                NOVELTY_DETECTION_NUMBER_OF_CLUSTERS,
                RANDOM_GENERATOR_SEED,
                MAIN_MICRO_CLUSTER_PREDICTOR,
                SLEEP_MICRO_CLUSTER_PREDICTOR,
                SAMPLE_PREDICTOR);

        final MINASController minasController = minasBuilder.build();

        FileReader fileReader = new FileReader(new File("/home/douglas444/Dropbox/workspace/MOA3_fold1_ini"));
        DSFileReader dsFileReader = new DSFileReader(",", fileReader);
        DSClassifierExecutor.start(minasController, dsFileReader, true, 1000);

        fileReader = new FileReader(new File("/home/douglas444/Dropbox/workspace/MOA3_fold1_onl"));
        dsFileReader = new DSFileReader(",", fileReader);
        DSClassifierExecutor.start(minasController, dsFileReader, true, 1000);

    }

    public static final MicroClusterClassifier MAIN_MICRO_CLUSTER_PREDICTOR = (microCluster, microClusters) -> {

        if (microClusters.isEmpty()) {
            return new ClassificationResult(null, false);
        }

        final MicroCluster closestMicroCluster = microCluster.calculateClosestMicroCluster(microClusters);
        final double distance = microCluster.distance(closestMicroCluster);

        if (distance < closestMicroCluster.calculateStandardDeviation() * 1.1) {

            return new ClassificationResult(closestMicroCluster, true);

        } else if (distance < closestMicroCluster.calculateStandardDeviation()
                + microCluster.calculateStandardDeviation()) {

            return new ClassificationResult(closestMicroCluster, true);

        }

        return new ClassificationResult(closestMicroCluster, false);
    };

    public static final MicroClusterClassifier SLEEP_MICRO_CLUSTER_PREDICTOR = (microCluster, microClusters) -> {

        if (microClusters.isEmpty()) {
            return new ClassificationResult(null, false);
        }

        final MicroCluster closestMicroCluster = microCluster.calculateClosestMicroCluster(microClusters);
        final double distance = microCluster.distance(closestMicroCluster);

        if (distance < closestMicroCluster.calculateStandardDeviation() * 1.1) {
            return new ClassificationResult(closestMicroCluster, true);
        }

        return new ClassificationResult(closestMicroCluster, false);
    };

    public static final SampleClassifier SAMPLE_PREDICTOR = (sample, microClusters) -> {

        if (microClusters.isEmpty()) {
            return new ClassificationResult(null, false);
        }

        final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample, microClusters);
        final double distance = sample.distance(closestMicroCluster.calculateCentroid());

        if (distance < closestMicroCluster.calculateStandardDeviation() * 2) {
            return new ClassificationResult(closestMicroCluster, true);
        }

        return new ClassificationResult(closestMicroCluster, false);
    };

}