package br.com.douglas444.minas.feedback;

import br.com.douglas444.minas.core.Category;
import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.mltk.Sample;

import java.util.*;
import java.util.stream.Collectors;

public class Feedback {

    public static boolean on = false;

    private static boolean checkPreConditions(Context context) {

        return on && context.getPrediction().getClosestMicroCluster().isPresent() &&
                context.getPrediction().getClosestMicroCluster().get().getCategory() != Category.NOVELTY;

    }

    public static boolean validateConceptDrift(Context context) {

        if (!checkPreConditions(context)) {
            return true;
        }

        Set<MicroCluster> knownConcepts = new HashSet<>(context.getKnownConcepts());
        knownConcepts.add(context.getPrediction().getClosestMicroCluster().get());

        if (estimateBayesError(context.getConcept().calculateCenter(), knownConcepts) > 0.5) {

            final Sample sample = getLastInformativeSample(context.getSamples(), knownConcepts);

            if (context.getPrediction().getClosestMicroCluster().isPresent()) {
                final MicroCluster closest = context.getPrediction().getClosestMicroCluster().get();
                final Integer label = Oracle.label(sample);
                return label == closest.getLabel();
            }
        }
        return true;
    }

    public static boolean validateConceptEvolution(Context context) {

        if (!checkPreConditions(context)) {
            return true;
        }

        Set<MicroCluster> knownConcepts = new HashSet<>(context.getKnownConcepts());
        knownConcepts.add(context.getPrediction().getClosestMicroCluster().get());

        if (estimateBayesError(context.getConcept().calculateCenter(), knownConcepts) < 0.8) {

            final Sample sample = getMostInformativeSample(context.getSamples(), knownConcepts);

            if (context.getPrediction().getClosestMicroCluster().isPresent()) {
                final MicroCluster closest = context.getPrediction().getClosestMicroCluster().get();
                final Integer label = Oracle.label(sample);
                return label != closest.getLabel();
            }
        }
        return true;
    }

    private static Sample getMostInformativeSample(List<Sample> samples, Set<MicroCluster> microClusters) {

        assert samples != null && samples.size() > 0;

        return samples.stream().sorted((sample1, sample2) -> {
            double e1 = estimateBayesError(sample1, microClusters);
            double e2 = estimateBayesError(sample2, microClusters);
            return Double.compare(e1, e2);
        }).collect(Collectors.toList()).get(samples.size() - 1);

    }

    private static Sample getLastInformativeSample(List<Sample> samples, Set<MicroCluster> microClusters) {

        assert samples != null && samples.size() > 0;

        return samples.stream().sorted((sample1, sample2) -> {
            double e1 = estimateBayesError(sample1, microClusters);
            double e2 = estimateBayesError(sample2, microClusters);
            return Double.compare(e1, e2);
        }).collect(Collectors.toList()).get(0);

    }

    private static double estimateBayesError(Sample target, Set<MicroCluster> microClusters) {

        final HashMap<Integer, List<MicroCluster>> microClustersByLabel = new HashMap<>();
        microClusters.forEach(microCluster -> {
            microClustersByLabel.putIfAbsent(microCluster.getLabel(), new ArrayList<>());
            microClustersByLabel.get(microCluster.getLabel()).add(microCluster);
        });

        final HashMap<Integer, MicroCluster> closestMicroClusterByLabel = new HashMap<>();
        microClustersByLabel.forEach((key, value) -> {
            MicroCluster.calculateClosestMicroCluster(target, value)
                    .ifPresent(closest -> closestMicroClusterByLabel.put(key, closest));
        });

        final double n = 1.0 / closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> microCluster.calculateCenter().distance(target))
                .min(Double::compare)
                .orElse(0.0);

        final double d = closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> 1.0 / (microCluster.calculateCenter().distance(target)))
                .reduce(0.0, Double::sum);

        return 1 - (n/d);

    }
}
