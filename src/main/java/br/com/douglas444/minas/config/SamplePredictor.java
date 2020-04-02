package br.com.douglas444.minas.config;

import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import br.com.douglas444.mltk.Sample;

import java.util.List;

@FunctionalInterface
public interface SamplePredictor {

    Prediction predict(Sample sample, List<MicroCluster> temporaryMemory);

}
