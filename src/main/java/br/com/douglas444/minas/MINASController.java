package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierController;
import br.com.douglas444.minas.config.Configuration;
import br.com.douglas444.mltk.Sample;
import java.util.List;
import java.util.Optional;

public class MINASController implements DSClassifierController {

    private MINAS minas;

    public MINASController(List<Sample> trainSet, Configuration configuration) {

        this.minas = new MINAS(trainSet, configuration);

    }

    @Override
    public Optional<Integer> predictAndUpdate(Sample sample) {

        Prediction prediction = minas.predictAndUpdate(sample);
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
