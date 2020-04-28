package br.com.douglas444.minas.type;

import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;

@FunctionalInterface
public interface SamplePredictor {

    Prediction predict(final Sample sample, final List<MicroCluster> temporaryMemory);

}
