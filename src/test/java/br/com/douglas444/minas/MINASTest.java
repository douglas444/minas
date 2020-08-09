package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierExecutor;
import br.com.douglas444.dsframework.DSFileReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MINASTest {

    private static final int TEMPORARY_MEMORY_MAX_SIZE = 2000;
    private static final int MINIMUM_CLUSTER_SIZE = 20;
    private static final int WINDOW_SIZE = 4000;
    private static final int MICRO_CLUSTER_LIFESPAN = 4000;
    private static final int SAMPLE_LIFESPAN = 4000;
    private static final int HEATER_CAPACITY = 10000;
    private static final boolean INCREMENTALLY_UPDATE_DECISION_MODEL = false;
    private static final int HEATER_INITIAL_BUFFER_SIZE = 1000;
    private static final int HEATER_NUMBER_OF_CLUSTERS_PER_LABEL = 100;
    private static final int NOVELTY_DETECTION_NUMBER_OF_CLUSTERS = 100;
    private static final long RANDOM_GENERATOR_SEED = 0;

    @Test
    public void execute() throws IOException {

        final MINASBuilder minasBuilder = new MINASBuilder(
                TEMPORARY_MEMORY_MAX_SIZE,
                MINIMUM_CLUSTER_SIZE,
                WINDOW_SIZE,
                MICRO_CLUSTER_LIFESPAN,
                SAMPLE_LIFESPAN,
                HEATER_CAPACITY,
                INCREMENTALLY_UPDATE_DECISION_MODEL,
                HEATER_INITIAL_BUFFER_SIZE,
                HEATER_NUMBER_OF_CLUSTERS_PER_LABEL,
                NOVELTY_DETECTION_NUMBER_OF_CLUSTERS,
                RANDOM_GENERATOR_SEED,
                null);

        final MINASController minasController = minasBuilder.build();

        URL url = getClass().getClassLoader().getResource("MOA3_fold1_ini");
        assert url != null;
        File file = new File(url.getFile());

        FileReader fileReader = new FileReader(file);
        DSFileReader dsFileReader = new DSFileReader(",", fileReader);
        DSClassifierExecutor.start(minasController, dsFileReader, true, 10000);

        url = getClass().getClassLoader().getResource("MOA3_fold1_onl");
        assert url != null;
        file = new File(url.getFile());

        fileReader = new FileReader(file);
        dsFileReader = new DSFileReader(",", fileReader);
        DSClassifierExecutor.start(minasController, dsFileReader, true, 10000);

        System.out.println(minasController.getDynamicConfusionMatrixString().toString());

        //Asserting UnkR
        double unkR = minasController.getDynamicConfusionMatrixString().unkR();
        unkR = (double) Math.round(unkR * 10000) / 10000;
        assertEquals(0.1106, unkR, "The final value of UnkR differs from the expected " +
                "for the dataset MOA3_fold1 with the following parameters configuration:\n" + parameters());

        //Asserting CER
        double cer = minasController.getDynamicConfusionMatrixString().cer();
        cer = (double) Math.round(cer * 10000) / 10000;
        assertEquals(0.0, cer, "The final value of CER differs from the expected for the " +
                "dataset MOA3_fold1 with the following parameters configuration:\n" + parameters());

        //Asserting number of novelties
        assertEquals(157, minasController.getNoveltyCount(),
                "The final value of Novelty Count differs from the expected for the dataset " +
                        "MOA3_fold1 with the following parameters configuration:\n" + parameters());

    }

    static String parameters() {
        return
                "\nTEMPORARY_MEMORY_MAX_SIZE = 2000" +
                "\nMINIMUM_CLUSTER_SIZE = 20" +
                "\nWINDOW_SIZE = 4000" +
                "\nMICRO_CLUSTER_LIFESPAN = 4000" +
                "\nSAMPLE_LIFESPAN = 4000" +
                "\nHEATER_CAPACITY = 10000" +
                "\nINCREMENTALLY_UPDATE_DECISION_MODEL = false" +
                "\nHEATER_INITIAL_BUFFER_SIZE = 1000" +
                "\nHEATER_NUMBER_OF_CLUSTERS_PER_LABEL = 100" +
                "\nNOVELTY_DETECTION_NUMBER_OF_CLUSTERS = 100" +
                "\nRANDOM_GENERATOR_SEED = 0";
    }

}