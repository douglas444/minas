package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierController;
import br.com.douglas444.minas.core.MINAS;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.Optional;

public class MINASController implements DSClassifierController {

    private final MINAS minas;

    public MINASController(MINAS minas) {

        this.minas = minas;

    }

    @Override
    public Optional<Integer> process(Sample sample) {
        final ClassificationResult classificationResult = minas.process(sample);
        return classificationResult.getLabel();
    }

    @Override
    public String getLog() {

        if (this.minas.isWarmed()) {

            return "Timestamp = " + minas.getTimestamp() +
                    "\nCER = " + minas.calculateCER() +
                    "\nUnkR = " + minas.calculateUnkR() +
                    "\n\n"+
                    minas.getConfusionMatrix().toString() +
                    "\n-----------------------------------";

        } else {

            return "Timestamp = " + minas.getTimestamp() +
                    "\nCER = xx" +
                    "\nUnkR = xx" +
                    "\n\n       Warming...\n"+
                    "\n-----------------------------------";

        }

    }


}
