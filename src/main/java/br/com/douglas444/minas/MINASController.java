package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierController;
import br.com.douglas444.minas.type.Category;
import br.com.douglas444.minas.core.MINAS;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.Optional;

public class MINASController implements DSClassifierController {

    private MINAS minas;

    public MINASController(MINAS minas) {

        this.minas = minas;

    }

    @Override
    public Optional<Integer> predictAndUpdate(Sample sample) {

        final Category.Prediction prediction = minas.process(sample);
        return prediction.getLabel();
    }

    @Override
    public String getLog() {
        return "Timestamp: " + minas.getTimestamp() +
                "\nCER = " + minas.cer() +
                "\nUnkR = " + minas.unkR() +
                "\n\n"+
                minas.getConfusionMatrix().toString() +
                "\n-----------------------------------";
    }


}
