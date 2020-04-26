package br.com.douglas444.minas.type;

import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;

@FunctionalInterface
public interface SamplePredictor {

    Category.Prediction predict(Sample sample, List<MicroCluster> temporaryMemory);

}
