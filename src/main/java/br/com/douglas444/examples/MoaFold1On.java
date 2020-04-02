package br.com.douglas444.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSRunnable;
import br.com.douglas444.minas.MINASController;
import br.com.douglas444.minas.config.Configuration;
import br.com.douglas444.minas.config.KMeansController;
import br.com.douglas444.minas.config.MicroClusterPredictorType1;
import br.com.douglas444.minas.config.SamplePredictorType1;
import br.com.douglas444.mltk.Sample;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MoaFold1On {

    private static final int MIN_SIZE_DN = 2000;
    private static final int MIN_CLUSTER_SIZE = 20;
    private static final int WINDOW_SIZE = 4000;
    private static final int MICRO_CLUSTER_LIFESPAN = 4000;
    private static final int SAMPLE_LIFESPAN = 4000;
    private static final boolean INCREMENTALLY_UPDATABLE = false;
    private static final boolean FEEDBACK_NEEDED = false;
    private static final int K = 100;
    private static final double THRESHOLD_MULTIPLIER = 1.1;
    private static final double THRESHOLD_MULTIPLIER_SLEEP = 1.1;
    private static final double THRESHOLD_MULTIPLIER_SAMPLE = 2.0;
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
                INCREMENTALLY_UPDATABLE,
                FEEDBACK_NEEDED,
                new KMeansController(K),
                new KMeansController(K),
                new MicroClusterPredictorType1(THRESHOLD_MULTIPLIER),
                new MicroClusterPredictorType1(THRESHOLD_MULTIPLIER_SLEEP),
                new SamplePredictorType1(THRESHOLD_MULTIPLIER_SAMPLE));

        DSFileReader fileReader = loadDSFileReader();
        List<Sample> trainSet = fileReader.next(TRAIN_SET_SIZE);
        MINASController minasController = new MINASController(trainSet, configuration);

        DSRunnable.run(minasController, fileReader);

    }

    static DSFileReader loadDSFileReader() throws IOException {
        FileReader fileReader = new FileReader(new File(DATA_FILE));
        return new DSFileReader(SEPARATOR, fileReader);
    }

}