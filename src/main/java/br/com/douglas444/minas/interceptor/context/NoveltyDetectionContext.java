package br.com.douglas444.minas.interceptor.context;

import br.com.douglas444.dsframework.interceptor.Context;
import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.minas.MINAS;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;

public class NoveltyDetectionContext implements Context {

    private MINAS minas;
    private MicroCluster targetMicroCluster;
    private List<Sample> targetSamples;
    private MicroCluster closestMicroCluster;

    public NoveltyDetectionContext minas(MINAS minas) {
        this.minas = minas;
        return this;
    }

    public NoveltyDetectionContext targetMicroCluster(MicroCluster targetMicroCluster) {
        this.targetMicroCluster = targetMicroCluster;
        return this;
    }

    public NoveltyDetectionContext targetSamples(List<Sample> targetSamples) {
        this.targetSamples = targetSamples;
        return this;
    }

    public NoveltyDetectionContext closestMicroCluster(MicroCluster closestMicroCluster) {
        this.closestMicroCluster = closestMicroCluster;
        return this;
    }

    public MINAS getMinas() {
        return minas;
    }

    public MicroCluster getTargetMicroCluster() {
        return targetMicroCluster;
    }

    public List<Sample> getTargetSamples() {
        return targetSamples;
    }

    public MicroCluster getClosestMicroCluster() {
        return closestMicroCluster;
    }
}
