package br.com.douglas444.minas.feedback;

import br.com.douglas444.minas.MicroClusterCategory;
import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;
import java.util.stream.Collectors;

public class Feedback {

    public static boolean validateConceptDrift(final MicroCluster closestConcept, final MicroCluster concept,
                                               final List<Sample> samples,
                                               final List<MicroCluster> decisionModelConcepts) {

        if (closestConcept.getMicroClusterCategory() == MicroClusterCategory.NOVELTY) {
            return true;
        }

        final Set<MicroCluster> concepts = new HashSet<>(decisionModelConcepts);
        concepts.add(closestConcept);

        if (estimateBayesError(concept.calculateCentroid(), concepts) > 0.5) {
            final Sample sample = getMostInformativeSample(samples, concepts);
            final Integer label = Oracle.label(sample);
            return label.equals(closestConcept.getLabel());
        }

        return true;
    }

    public static boolean validateConceptEvolution(final MicroCluster closestConcept, final MicroCluster concept,
                                                   final List<Sample> samples,
                                                   final List<MicroCluster> decisionModelConcepts) {

        if (closestConcept.getMicroClusterCategory() == MicroClusterCategory.NOVELTY) {
            return true;
        }

        final Set<MicroCluster> concepts = new HashSet<>(decisionModelConcepts);
        concepts.add(closestConcept);

        if (estimateBayesError(concept.calculateCentroid(), concepts) < 0.8) {
            final Sample sample = getLessInformativeSample(samples, concepts);
            final Integer label = Oracle.label(sample);
            return !label.equals(closestConcept.getLabel());
        }

        return true;

    }

    private static Sample getMostInformativeSample(final List<Sample> samples, final Set<MicroCluster> microClusters) {

        if (samples == null || samples.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return samples.stream().sorted((sample1, sample2) -> {
            double e1 = estimateBayesError(sample1, microClusters);
            double e2 = estimateBayesError(sample2, microClusters);
            return Double.compare(e1, e2);
        }).collect(Collectors.toList()).get(samples.size() - 1);

    }

    private static Sample getLessInformativeSample(final List<Sample> samples, final Set<MicroCluster> microClusters) {

        if (samples == null || samples.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return samples.stream().sorted((sample1, sample2) -> {
            double e1 = estimateBayesError(sample1, microClusters);
            double e2 = estimateBayesError(sample2, microClusters);
            return Double.compare(e1, e2);
        }).collect(Collectors.toList()).get(0);

    }

    private static double estimateBayesError(final Sample target, final Set<MicroCluster> microClusters) {

        final HashMap<Integer, List<MicroCluster>> microClustersByLabel = new HashMap<>();
        microClusters.forEach(microCluster -> {
            microClustersByLabel.putIfAbsent(microCluster.getLabel(), new ArrayList<>());
            microClustersByLabel.get(microCluster.getLabel()).add(microCluster);
        });

        final HashMap<Integer, MicroCluster> closestMicroClusterByLabel = new HashMap<>();
        microClustersByLabel.forEach((key, value) -> {
            final MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(target, value);
            closestMicroClusterByLabel.put(key, closestMicroCluster);
        });

        final double n = 1.0 / closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> microCluster.calculateCentroid().distance(target))
                .min(Double::compare)
                .orElse(0.0);

        final double d = closestMicroClusterByLabel
                .values()
                .stream()
                .map(microCluster -> 1.0 / (microCluster.calculateCentroid().distance(target)))
                .reduce(0.0, Double::sum);

        return 1 - (n/d);

    }

}
