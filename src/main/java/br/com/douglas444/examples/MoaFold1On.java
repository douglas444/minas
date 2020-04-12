package br.com.douglas444.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSRunnable;
import br.com.douglas444.minas.MINASController;
import br.com.douglas444.minas.config.*;
import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import br.com.douglas444.mltk.Sample;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MoaFold1On {

    private static final int MIN_SIZE_DN = 2000;
    private static final int MIN_CLUSTER_SIZE = 20;
    private static final int WINDOW_SIZE = 4000;
    private static final int MICRO_CLUSTER_LIFESPAN = 4000;
    private static final int SAMPLE_LIFESPAN = 4000;
    private static final boolean INCREMENTALLY_UPDATE_DECISION_MODEL = false;
    private static final boolean TURN_FEEDBACK_ON = false;
    private static final KMeansController OFFLINE_PHASE_CLUSTERING_ALG = new KMeansController(100);
    private static final KMeansController ONLINE_PHASE_CLUSTERING_ALG = new KMeansController(100);
    private static final double THRESHOLD_MULTIPLIER = 1.1;
    private static final double THRESHOLD_MULTIPLIER_SLEEP = 1.1;
    private static final double THRESHOLD_MULTIPLIER_SAMPLE = 2;
    private static final int TRAIN_SET_SIZE = 9000;
    private static final String DATA_FILE = "./datasets/moa_fold1_on.data";
    private static final String SEPARATOR = ",";


    public static void main(String[] args) throws IOException {

        Configuration configuration = new Configuration(
                MIN_SIZE_DN,
                MIN_CLUSTER_SIZE,
                WINDOW_SIZE,
                MICRO_CLUSTER_LIFESPAN,
                SAMPLE_LIFESPAN,
                INCREMENTALLY_UPDATE_DECISION_MODEL,
                TURN_FEEDBACK_ON,
                OFFLINE_PHASE_CLUSTERING_ALG,
                ONLINE_PHASE_CLUSTERING_ALG,
                MAIN_MICRO_CLUSTER_PREDICTOR,
                SLEEP_MICRO_CLUSTER_PREDICTOR,
                SAMPLE_PREDICTOR);

        DSFileReader fileReader = loadDSFileReader();
        List<Sample> trainSet = fileReader.next(TRAIN_SET_SIZE);
        MINASController minasController = new MINASController(trainSet, configuration);

        DSRunnable.run(minasController, fileReader, true,1);

    }

    static DSFileReader loadDSFileReader() throws IOException {
        FileReader fileReader = new FileReader(new File(DATA_FILE));
        return new DSFileReader(SEPARATOR, fileReader);
    }

    //---------------------------------------------------------------------------------------------

    public static final MicroClusterPredictor MAIN_MICRO_CLUSTER_PREDICTOR = (microCluster, microClusters) -> {

        Sample center = microCluster.calculateCenter();
        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(center, microClusters);

        if (closestMicroCluster.isPresent()) {

            double distance = closestMicroCluster.get().calculateCenter().distance(center);
            double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

            if (distance <= microClusterStandardDeviation * THRESHOLD_MULTIPLIER) {
                return new Prediction(closestMicroCluster.get(), true);
            }

        }

        return new Prediction(closestMicroCluster.orElse(null), false);

    };

    public static final MicroClusterPredictor SLEEP_MICRO_CLUSTER_PREDICTOR = (microCluster, microClusters) -> {

        Sample center = microCluster.calculateCenter();
        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(center, microClusters);

        if (closestMicroCluster.isPresent()) {

            double distance = closestMicroCluster.get().calculateCenter().distance(center);
            double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

            if (distance <= microClusterStandardDeviation * THRESHOLD_MULTIPLIER_SLEEP) {
                return new Prediction(closestMicroCluster.get(), true);
            }

        }

        return new Prediction(closestMicroCluster.orElse(null), false);

    };

    public static final SamplePredictor SAMPLE_PREDICTOR = (sample, microClusters) -> {

        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample,
                microClusters);

        if (closestMicroCluster.isPresent()) {

            Sample center = closestMicroCluster.get().calculateCenter();
            double distance = center.distance(sample);
            double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

            if (distance <= microClusterStandardDeviation * THRESHOLD_MULTIPLIER_SAMPLE) {
                return new Prediction(closestMicroCluster.get(), true);
            }
        }

        return new Prediction(closestMicroCluster.orElse(null), false);
    };

}