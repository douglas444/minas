package br.com.douglas444.minas;

import java.util.List;

@FunctionalInterface
public interface MicroClusterPredictor {

    Prediction predict(final MicroCluster microCluster, final List<MicroCluster> microClusters);

}
