package br.com.douglas444.minas;

import br.com.douglas444.minas.util.FileUtil;
import br.com.douglas444.streams.processor.StreamsFileReader;
import br.com.douglas444.streams.processor.StreamsProcessorExecutor;
import br.ufu.facom.pcf.core.Configurable;
import br.ufu.facom.pcf.core.Interceptable;
import br.ufu.facom.pcf.core.Interceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class MINASInterceptable implements Interceptable, Configurable {

    private StreamsProcessorExecutor executor;

    private static final String TEMPORARY_MEMORY_MAX_SIZE = "Temporary memory max size";
    private static final String MINIMUM_CLUSTER_SIZE = "Minimum cluster size";
    private static final String WINDOW_SIZE = "Window size";
    private static final String MICRO_CLUSTER_LIFESPAN = "Micro cluster lifespan";
    private static final String SAMPLE_LIFESPAN = "Sample lifespan";
    private static final String HEATER_CAPACITY = "Heater capacity";
    private static final String INCREMENTALLY_UPDATE_DECISION_MODEL = "Incrementally update {0,1}";
    private static final String HEATER_INITIAL_BUFFER_SIZE = "Heater initial buffer size";
    private static final String HEATER_NUMBER_OF_CLUSTERS_PER_LABEL = "Heater clusters per label";
    private static final String NOVELTY_DETECTION_NUMBER_OF_CLUSTERS = "Number of cluster for ND";
    private static final String RANDOM_GENERATOR_SEED = "Seed";
    private static final String DATASET_FILE_PATH = "Dataset CSV's (separated by ';')";
    private static final String LOG_INTERVAL = "Log interval";
    private static final String PCF_TIGHT_INTEGRATION = "PCF tight integration {0,1}";

    private static final double DEFAULT_TEMPORARY_MEMORY_MAX_SIZE = 2000;
    private static final double DEFAULT_MINIMUM_CLUSTER_SIZE = 20;
    private static final double DEFAULT_WINDOW_SIZE = 4000;
    private static final double DEFAULT_MICRO_CLUSTER_LIFESPAN = 4000;
    private static final double DEFAULT_SAMPLE_LIFESPAN = 4000;
    private static final double DEFAULT_HEATER_CAPACITY = 10000;
    private static final double DEFAULT_INCREMENTALLY_UPDATE_DECISION_MODEL = 0;
    private static final double DEFAULT_HEATER_INITIAL_BUFFER_SIZE = 1000;
    private static final double DEFAULT_HEATER_NUMBER_OF_CLUSTERS_PER_LABEL = 100;
    private static final double DEFAULT_NOVELTY_DETECTION_NUMBER_OF_CLUSTERS = 100;
    private static final double DEFAULT_RANDOM_GENERATOR_SEED = 0;
    private static final double DEFAULT_LOG_INTERVAL = 1000;
    private static final double DEFAULT_PCF_TIGHT_INTEGRATION = 0;


    final private HashMap<String, Double> numericParameters;
    final private HashMap<String, String> nominalParameters;

    public MINASInterceptable() {

        this.numericParameters = new HashMap<>();

        this.numericParameters.put(TEMPORARY_MEMORY_MAX_SIZE, DEFAULT_TEMPORARY_MEMORY_MAX_SIZE);
        this.numericParameters.put(MINIMUM_CLUSTER_SIZE, DEFAULT_MINIMUM_CLUSTER_SIZE);
        this.numericParameters.put(WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
        this.numericParameters.put(MICRO_CLUSTER_LIFESPAN, DEFAULT_MICRO_CLUSTER_LIFESPAN);
        this.numericParameters.put(SAMPLE_LIFESPAN, DEFAULT_SAMPLE_LIFESPAN);
        this.numericParameters.put(HEATER_CAPACITY, DEFAULT_HEATER_CAPACITY);
        this.numericParameters.put(INCREMENTALLY_UPDATE_DECISION_MODEL, DEFAULT_INCREMENTALLY_UPDATE_DECISION_MODEL);
        this.numericParameters.put(HEATER_INITIAL_BUFFER_SIZE, DEFAULT_HEATER_INITIAL_BUFFER_SIZE);
        this.numericParameters.put(NOVELTY_DETECTION_NUMBER_OF_CLUSTERS, DEFAULT_NOVELTY_DETECTION_NUMBER_OF_CLUSTERS);
        this.numericParameters.put(HEATER_NUMBER_OF_CLUSTERS_PER_LABEL, DEFAULT_HEATER_NUMBER_OF_CLUSTERS_PER_LABEL);
        this.numericParameters.put(RANDOM_GENERATOR_SEED, DEFAULT_RANDOM_GENERATOR_SEED);
        this.numericParameters.put(PCF_TIGHT_INTEGRATION, DEFAULT_PCF_TIGHT_INTEGRATION);
        this.numericParameters.put(LOG_INTERVAL, DEFAULT_LOG_INTERVAL);

        this.nominalParameters = new HashMap<>();
        this.nominalParameters.put(DATASET_FILE_PATH, "");
    }

    @Override
    public boolean execute(Interceptor interceptor) {

        final boolean incrementallyUpdate;
        if (this.numericParameters.get(INCREMENTALLY_UPDATE_DECISION_MODEL).equals(0d)) {
            incrementallyUpdate = false;
        } else if (this.numericParameters.get(INCREMENTALLY_UPDATE_DECISION_MODEL).equals(1d)) {
            incrementallyUpdate = true;
        } else {
            throw new IllegalArgumentException();
        }

        final boolean pcfTightIntegration;
        if (this.numericParameters.get(PCF_TIGHT_INTEGRATION).equals(0d)) {
            pcfTightIntegration = false;
        } else if (this.numericParameters.get(PCF_TIGHT_INTEGRATION).equals(1d)) {
            pcfTightIntegration = true;
        } else {
            throw new IllegalArgumentException();
        }

        final MINASBuilder minasBuilder = new MINASBuilder(
                this.numericParameters.get(TEMPORARY_MEMORY_MAX_SIZE).intValue(),
                this.numericParameters.get(MINIMUM_CLUSTER_SIZE).intValue(),
                this.numericParameters.get(WINDOW_SIZE).intValue(),
                this.numericParameters.get(MICRO_CLUSTER_LIFESPAN).intValue(),
                this.numericParameters.get(SAMPLE_LIFESPAN).intValue(),
                this.numericParameters.get(HEATER_CAPACITY).intValue(),
                incrementallyUpdate,
                this.numericParameters.get(HEATER_INITIAL_BUFFER_SIZE).intValue(),
                this.numericParameters.get(HEATER_NUMBER_OF_CLUSTERS_PER_LABEL).intValue(),
                this.numericParameters.get(NOVELTY_DETECTION_NUMBER_OF_CLUSTERS).intValue(),
                this.numericParameters.get(RANDOM_GENERATOR_SEED).intValue(),
                pcfTightIntegration,
                interceptor);

        final MINASController controller = minasBuilder.build();
        this.executor = new StreamsProcessorExecutor();

        final String[] files = Arrays.stream(this.nominalParameters.get(DATASET_FILE_PATH).split(";"))
                .map(file -> file.replace(" ", ""))
                .filter(file -> !file.isEmpty())
                .toArray(String[]::new);

        final StreamsFileReader[] fileReaders = new StreamsFileReader[files.length];

        for (int i = 0; i < files.length; i++) {
            fileReaders[i] = new StreamsFileReader(",", FileUtil.getFileReader(files[i]));
        }

        boolean success;
        try {

            success = executor.start(
                    controller,
                    this.getNumericParameters().get(LOG_INTERVAL).intValue(),
                    fileReaders);

        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        if (success) {
            System.out.println(controller.getDynamicConfusionMatrix());
        }

        return success;

    }


    @Override
    public void stop() {
        if (this.executor != null) {
            this.executor.interrupt();
        }
    }

    @Override
    public HashMap<String, String> getNominalParameters() {
        return this.nominalParameters;
    }

    @Override
    public HashMap<String, Double> getNumericParameters() {
        return this.numericParameters;
    }
}
