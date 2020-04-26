package br.com.douglas444.minas.type;

import java.util.List;

@FunctionalInterface
public interface MicroClusterPredictor {

    Category.Prediction predict(MicroCluster microCluster, List<MicroCluster> temporaryMemory);

}
