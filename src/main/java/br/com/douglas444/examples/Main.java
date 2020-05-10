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
    private static final int ONLINE_PHASE_START_TIME = 9000;
    private static final boolean INCREMENTALLY_UPDATE_DECISION_MODEL = false;
    private static final boolean FEEDBACK_ENABLED = true;
    private static final int HEATER_INITIAL_BUFFER_SIZE = 1000;
    private static final int HEATER_NUMBER_OF_CLUSTER_PER_LABEL = 100;
    private static final int HEATER_AGGLOMERATIVE_BUFFER_THRESHOLD = 2000;
    private static final int NOVELTY_DETECTION_NUMBER_OF_CLUSTERS = 100;
    private static final int RANDOM_GENERATOR_SEED = 1;
    private static final String DATA_FILE = "./datasets/moa_fold1_on.data";
    private static final String SEPARATOR = ",";


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
        final FileReader fileReader = new FileReader(new File(DATA_FILE));
        final DSFileReader dsFileReader = new DSFileReader(SEPARATOR, fileReader);

        DSClassifierExecutor.start(minasController, dsFileReader);

    }

    public static final MicroClusterPredictor MAIN_MICRO_CLUSTER_PREDICTOR = (microCluster, microClusters) -> {

        final MicroCluster closestMicroCluster = microCluster.calculateClosestMicroCluster(microClusters);
        final double distance = microCluster.distance(closestMicroCluster);

        if (distance < closestMicroCluster.calculateStandardDeviation() +
                microCluster.calculateStandardDeviation()) {
            return new Prediction(closestMicroCluster, true);
        }
        return new Prediction(closestMicroCluster, false);
    };

    public static final MicroClusterPredictor SLEEP_MICRO_CLUSTER_PREDICTOR = (microCluster, microClusters) -> {

        final MicroCluster closestMicroCluster = microCluster.calculateClosestMicroCluster(microClusters);
        final double distance = microCluster.distance(closestMicroCluster);

        if (distance < closestMicroCluster.calculateStandardDeviation() +
                microCluster.calculateStandardDeviation()) {
            return new Prediction(closestMicroCluster, true);
        }
        return new Prediction(closestMicroCluster, false);
    };

    public static final SamplePredictor SAMPLE_PREDICTOR = (sample, microClusters) -> {

        final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample, microClusters);
        final double distance = sample.distance(closestMicroCluster.calculateCenter());

        if (distance <= closestMicroCluster.calculateStandardDeviation() * 2) {
            return new Prediction(closestMicroCluster, true);
        }
        return new Prediction(closestMicroCluster, false);
    };

}