package br.com.douglas444.minas;

import java.util.Optional;
import java.util.function.Consumer;

public class Prediction {

    private MicroCluster explainedBy;

    public Prediction(MicroCluster explainedBy) {
        this.explainedBy = explainedBy;
    }

    public boolean isExplained() {
        return explainedBy != null;
    }

    public void ifExplained(Consumer<MicroCluster> action) {
        if (explainedBy != null) {
            action.accept(explainedBy);
        }
    }

    public Optional<Integer> getLabel() {
        if (this.explainedBy == null) {
            return Optional.empty();
        } else {
            return Optional.of(this.explainedBy.getLabel());
        }
    }

    public MicroCluster getExplainedBy() {
        return explainedBy;
    }
}
