package br.com.douglas444.minas;

import java.util.Optional;
import java.util.function.Consumer;

public class Classification {

    private final MicroCluster closestMicroCluster;
    private final boolean explained;

    public Classification(MicroCluster closestMicroCluster, boolean explained) {

        if (explained && closestMicroCluster == null) {
            throw new IllegalArgumentException();
        }

        this.closestMicroCluster = closestMicroCluster;
        this.explained = explained;
    }

    public void ifExplained(final Consumer<MicroCluster> action) {

        if (this.explained) {
            action.accept(this.closestMicroCluster);
        }

    }

    public void ifExplainedOrElse(final Consumer<MicroCluster> consumer, final Runnable runnable) {

        if (this.explained) {
            consumer.accept(this.closestMicroCluster);
        } else {
            runnable.run();
        }

    }

    public void ifExplainedOrElse(final Consumer<MicroCluster> consumer1,
                                  final Consumer<Optional<MicroCluster>> consumer2) {

        if (this.explained) {
            consumer1.accept(this.closestMicroCluster);
        } else {

            final Optional<MicroCluster> argument;

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

}