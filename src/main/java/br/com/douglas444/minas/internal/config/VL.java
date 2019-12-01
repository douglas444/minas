package br.com.douglas444.minas.internal.config;

import br.com.douglas444.minas.internal.MicroCluster;

import java.util.List;
import java.util.Optional;

public interface VL {

    Optional<MicroCluster> predict(MicroCluster microCluster, List<MicroCluster>temporaryMemory);

}
