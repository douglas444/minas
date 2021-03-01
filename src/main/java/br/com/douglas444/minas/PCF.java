package br.com.douglas444.minas;

import br.com.douglas444.ndc.datastructures.Sample;
import br.ufu.facom.pcf.core.Category;
import br.ufu.facom.pcf.core.ClusterSummary;
import br.ufu.facom.pcf.core.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PCF {

    public static Context buildContext(final MicroCluster pattern,
                                       final List<Sample> samples,
                                       final MicroClusterCategory category,
                                       final List<MicroCluster> decisionModelMicroClusters,
                                       final List<MicroCluster> sleepMemoryMicroClusters) {

        final Set<Integer> knownLabels = new HashSet<>();

        knownLabels.addAll(decisionModelMicroClusters.stream()
                .filter(microCluster -> microCluster.getMicroClusterCategory() != MicroClusterCategory.NOVELTY)
                .map(MicroCluster::getLabel)
                .collect(Collectors.toSet()));

        knownLabels.addAll(sleepMemoryMicroClusters.stream()
                .filter(microCluster -> microCluster.getMicroClusterCategory() != MicroClusterCategory.NOVELTY)
                .map(MicroCluster::getLabel)
                .collect(Collectors.toSet()));

        final int sentence = samples.stream()
                .map(sample -> !knownLabels.contains(sample.getY()))
                .map(isNovel -> isNovel ? 1 : -1)
                .reduce(0, Integer::sum);

        final Context context = new Context();
        context.setPatternClusterSummary(microClusterToClusterSummary(pattern));
        context.setKnownLabels(knownLabels);

        if (sentence / (double) samples.size() >= 0) {
            context.setRealCategory(Category.NOVELTY);
        } else {
            context.setRealCategory(Category.KNOWN);
        }

        final List<ClusterSummary> knownClusterSummaries = decisionModelMicroClusters
                .stream()
                .filter(microCluster -> microCluster.getMicroClusterCategory() != MicroClusterCategory.NOVELTY)
                .map(PCF::microClusterToClusterSummary)
                .collect(Collectors.toList());

        knownClusterSummaries.addAll(sleepMemoryMicroClusters
                .stream()
                .filter(microCluster -> microCluster.getMicroClusterCategory() != MicroClusterCategory.NOVELTY)
                .map(PCF::microClusterToClusterSummary)
                .collect(Collectors.toList()));

        context.setClusterSummaries(knownClusterSummaries);

        context.setSamplesAttributes(samples
                .stream()
                .map(Sample::getX)
                .map(double[]::clone)
                .collect(Collectors.toList()));

        context.setSamplesLabels(samples
                .stream()
                .map(Sample::getY)
                .collect(Collectors.toList()));

        if (category == MicroClusterCategory.KNOWN) {
            context.setPredictedCategory(Category.KNOWN);
        } else {
            context.setPredictedCategory(Category.NOVELTY);
        }

        return context;
    }

    static ClusterSummary microClusterToClusterSummary(final MicroCluster microCluster) {
        return new ClusterSummary(){

            final double[] centroidAttributes = microCluster.calculateCentroid().getX().clone();
            final double standardDeviation = microCluster.calculateStandardDeviation();
            final Integer label = microCluster.getLabel();

            @Override
            public double[] getCentroidAttributes() {
                return centroidAttributes;
            }

            @Override
            public double getStandardDeviation() {
                return standardDeviation;
            }

            @Override
            public Integer getLabel() {
                return label;
            }
        };
    }

}
