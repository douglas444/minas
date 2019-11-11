package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierController;
import br.com.douglas444.minas.internal.MINAS;
import br.com.douglas444.minas.internal.MicroCluster;
import br.com.douglas444.mltk.Sample;
import java.util.List;
import java.util.Optional;

public class MINASController implements DSClassifierController {

    private MINAS minas;

    public MINASController(List<Sample> trainSet) {
        this.minas = new MINAS(trainSet);
    }

    @Override
    public Optional<Integer> predictAndUpdate(Sample sample) {

        Optional<MicroCluster> microCluster = minas.predictAndUpdate(sample);
        return microCluster.map(MicroCluster::getLabel);

    }

    @Override
    public String getLog() {
        return " Timestamp: " + minas.getTimestamp() +
                " Unexplained Samples (%): " + (double) minas.getUnexplainedSamplesCount() / minas.getTimestamp() +
                "\n\n" +
                minas.getConfusionMatrix().toString();
    }


}
