package br.com.douglas444.minas.core;

import java.util.Optional;
import java.util.function.Consumer;

public class Prediction {

    private MicroCluster closestMicroCluster;
    private boolean explained;

    public Prediction(MicroCluster closestMicroCluster, boolean explained) {

        assert !explained || closestMicroCluster != null;

        this.closestMicroCluster = closestMicroCluster;
        this.explained = explained;
    }

    public boolean isExplained() {
        return explained;
    }

    public void ifExplained(Consumer<MicroCluster> action) {

        if (explained) {
            action.accept(this.closestMicroCluster);
        }

    }

    public void ifExplainedOrElse(Consumer<MicroCluster> consumer, Runnable runnable) {

        if (explained) {
            consumer.accept(this.closestMicroCluster);
        } else {
            runnable.run();
        }

    }

    public void ifExplainedOrElse(Consumer<MicroCluster> consumer1, Consumer<Optional<MicroCluster>> consumer2) {

        if (explained) {
            consumer1.accept(this.closestMicroCluster);
        } else {

            Optional<MicroCluster> argument;

            if (this.closestMicroCluster == null) {
                argument = Optional.empty();
            } else {
                argument = Optional.of(this.closestMicroCluster);
            }

            consumer2.accept(argument);
        }

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
