package br.com.douglas444.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSClassifierExecutor;
import br.com.douglas444.minas.*;
import br.com.douglas444.minas.feedback.Feedback;
import br.com.douglas444.minas.interceptor.MINASInterceptor;
import br.com.douglas444.mltk.datastructure.DynamicConfusionMatrix;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

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

    private static final MINASInterceptor INTERCEPTOR_COLLECTION = new MINASInterceptor();


    public static void main(String[] args) throws IOException {

        INTERCEPTOR_COLLECTION
                .MICRO_CLUSTER_EXPLAINED_INTERCEPTOR.define((context) -> {

            if (Feedback.validateConceptDrift(
                    context.getClosestMicroCluster(),
                    context.getTargetMicroCluster(),
                    context.getTargetSamples(),
                    context.getMinas().getDecisionModel())) {

                context.getMinas().addExtension(context.getTargetMicroCluster(), context.getClosestMicroCluster());
            } else {
                context.getMinas().addNovelty(context.getTargetMicroCluster());
            }

        });

        INTERCEPTOR_COLLECTION
                .MICRO_CLUSTER_EXPLAINED_BY_ASLEEP_INTERCEPTOR.define((context) -> {

            if (Feedback.validateConceptDrift(
                    context.getClosestMicroCluster(),
                    context.getTargetMicroCluster(),
                    context.getTargetSamples(),
                    context.getMinas().getDecisionModel())) {

                context.getMinas().awake(context.getClosestMicroCluster());
                context.getMinas().addExtension(context.getTargetMicroCluster(), context.getClosestMicroCluster());
            } else {
                context.getMinas().addNovelty(context.getTargetMicroCluster());
            }

        });

        INTERCEPTOR_COLLECTION
                .MICRO_CLUSTER_UNEXPLAINED_INTERCEPTOR.define((context) -> {

            if (context.getClosestMicroCluster() == null) {

                context.getMinas().addNovelty(context.getTargetMicroCluster());

            } else if (Feedback.validateConceptEvolution(
                    context.getClosestMicroCluster(),
                    context.getTargetMicroCluster(),
                    context.getTargetSamples(),
                    context.getMinas().getDecisionModel())) {

                context.getMinas().addNovelty(context.getTargetMicroCluster());
            } else {
                context.getMinas().awake(context.getClosestMicroCluster());
                context.getMinas().addExtension(context.getTargetMicroCluster(), context.getClosestMicroCluster());
            }

        });

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
                INTERCEPTOR_COLLECTION);

        final MINASController minasController = minasBuilder.build();

        FileReader fileReader = new FileReader(new File("/home/douglas444/Dropbox/workspace/MOA3_fold1_ini"));
        DSFileReader dsFileReader = new DSFileReader(",", fileReader);
        DSClassifierExecutor.start(minasController, dsFileReader, true, 1000);

        fileReader = new FileReader(new File("/home/douglas444/Dropbox/workspace/MOA3_fold1_onl"));
        dsFileReader = new DSFileReader(",", fileReader);
        DSClassifierExecutor.start(minasController, dsFileReader, true, 1000);

        DynamicConfusionMatrix dcm = minasController.getDynamicConfusionMatrixString();
        System.out.println("\n" + dcm.toString());

    }


}