package br.com.douglas444.minas.core;

import java.util.Optional;

public class Prediction {

    private MicroCluster closestMicroCluster;
    private boolean explained;

    public Prediction(MicroCluster closestMicroCluster, boolean explained) {
        this.closestMicroCluster = closestMicroCluster;
        this.explained = explained;
    }

    public boolean isExplained() {
        return explained;
    }

    public Optional<Integer> getLabel() {
        if (this.closestMicroCluster == null) {
            return Optional.empty();
        } else {
            return Optional.of(this.closestMicroCluster.getLabel());
        }
    }

    public Optional<MicroCluster> getClosestMicroCluster() {
        if (this.closestMicroCluster != null) {
            return Optional.of(closestMicroCluster);
        } else {
            return Optional.empty();
        }
    }


}
