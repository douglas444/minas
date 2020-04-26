package br.com.douglas444.minas.core;

import br.com.douglas444.minas.type.MicroCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;

interface Heater {

    void process(Sample sample);
    List<MicroCluster> close();

}
