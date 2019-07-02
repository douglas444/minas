package br.com.douglas444.minas;

import br.com.douglas444.dsframework.DSClassifierController;
import br.com.douglas444.minas.internal.MINAS;
import br.com.douglas444.minas.internal.MicroCluster;
import br.com.douglas444.mltk.Point;
import java.util.List;
import java.util.Optional;

public class MINASController implements DSClassifierController {

    private MINAS minas;

    public MINASController(List<Point> trainSet) {
        this.minas = new MINAS(trainSet);
    }

    @Override
    public Optional<Double> predictAndUpdate(Point point) {

        Optional<MicroCluster> microCluster = minas.predictAndUpdate(point);
        return microCluster.map(MicroCluster::getLabel);

    }

    @Override
    public double getAccuracy() {
        return minas.calculatesAccuracy();
    }

}
