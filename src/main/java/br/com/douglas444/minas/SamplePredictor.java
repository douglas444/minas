package br.com.douglas444.minas;


import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;

@FunctionalInterface
public interface SamplePredictor {

    Prediction predict(final Sample sample, final List<MicroCluster> microClusters);

}
