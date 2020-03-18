package br.com.douglas444.minas.config;

import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import java.util.List;

public interface VL {

    Prediction predict(MicroCluster microCluster, List<MicroCluster> temporaryMemory);

}
