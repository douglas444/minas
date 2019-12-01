package br.com.douglas444.minas.internal.config;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.Sample;

import java.util.List;

public interface ClusteringAlgorithm {

    List<Cluster> execute(List<Sample> samples);

}
