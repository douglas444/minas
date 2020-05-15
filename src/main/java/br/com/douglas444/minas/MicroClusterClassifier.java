package br.com.douglas444.minas;

import java.util.List;

@FunctionalInterface
public interface MicroClusterClassifier {

    ClassificationResult classify(final MicroCluster microCluster, final List<MicroCluster> microClusters);

}
