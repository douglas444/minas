package br.com.douglas444.minas.feedback;

import br.com.douglas444.minas.core.Category;
import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.mltk.Sample;

import java.util.*;
import java.util.stream.Collectors;

public class Feedback {

    public static boolean validateConceptDrift(MicroCluster closestConcept, MicroCluster concept,
                                               List<Sample> samples, List<MicroCluster> decisionModelConcepts) {

        if (closestConcept.getCategory() == Category.NOVELTY) {
            return true;
        }

        Set<MicroCluster> concepts = new HashSet<>(decisionModelConcepts);
        concepts.add(closestConcept);

        if (estimateBayesError(concept.calculateCenter(), concepts) > 0.5) {
            final Sample sample = getMostInformativeSample(samples, concepts);
            final Integer label = Oracle.label(sample);
            return label == closestConcept.getLabel();
        }

        return true;
    }

    public static boolean validateConceptEvolution(MicroCluster closestConcept, MicroCluster concept,
                                                   List<Sample> samples, List<MicroCluster> decisionModelConcepts) {

        if (closestConcept.getCategory() == Category.NOVELTY) {
            return true;
        }

        Set<MicroCluster> concepts = new HashSet<>(decisionModelConcepts);
        concepts.add(closestConcept);

        if (estimateBayesError(concept.calculateCenter(), concepts) < 0.8) {
            final Sample sample = getLessInformativeSample(samples, concepts);
            final Integer label = Oracle.label(sample);
            return label != closestConcept.getLabel();
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

    private static Sample getLessInformativeSample(List<Sample> samples, Set<MicroCluster> microClusters) {

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
