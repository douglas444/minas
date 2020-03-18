package br.com.douglas444.minas.feedback;

import br.com.douglas444.minas.core.Category;
import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import br.com.douglas444.mltk.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Feedback {

    public static boolean validateConceptEvolution(Prediction prediction, MicroCluster concept, List<Sample> samples,
                                             List<MicroCluster> modelMicroClusters) {

        if (estimateBayesError(concept.calculateCenter(), modelMicroClusters) < 0.5) {

            Sample sample = getMostInformativeSample(samples, modelMicroClusters);

            if (prediction.getClosestMicroCluster().isPresent()) {
                MicroCluster closest = prediction.getClosestMicroCluster().get();
                Integer label = Oracle.label(sample);
                return !(closest.getCategory() == Category.KNOWN && label == closest.getLabel());
            }
        }
        return true;
    }

    private static Sample getMostInformativeSample(List<Sample> samples, List<MicroCluster> microClusters) {

        Sample maxRiskSample = samples.get(0);
        double maxRisk = 0;

        for (Sample sample : samples) {
            double risk = estimateBayesError(sample, microClusters);
            if (risk > maxRisk) {
                maxRiskSample = sample;
                maxRisk = risk;
            }
        }

        return maxRiskSample;
    }


    private static double estimateBayesError(Sample target, List<MicroCluster> microClusters) {

        HashMap<Integer, List<MicroCluster>> microClustersByLabel = new HashMap<>();
        microClusters.forEach(microCluster -> {
            microClustersByLabel.putIfAbsent(microCluster.getLabel(), new ArrayList<>());
            microClustersByLabel.get(microCluster.getLabel()).add(microCluster);
        });

        HashMap<Integer, MicroCluster> closestMicroClusterByLabel = new HashMap<>();
        microClustersByLabel.forEach((key, value) -> {
            MicroCluster.calculateClosestMicroCluster(target, value)
                    .ifPresent(closest -> closestMicroClusterByLabel.put(key, closest));
        });

        double n = 1.0 / closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> microCluster.calculateCenter().distance(target))
                .min(Double::compare)
                .orElse(0.0);

        double d = closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> 1.0 / (microCluster.calculateCenter().distance(target)))
                .reduce(0.0, Double::sum);

        return 1 - (n/d);

    }
}
