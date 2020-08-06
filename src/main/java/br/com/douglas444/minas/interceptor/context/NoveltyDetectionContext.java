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

    public MINAS getMinas() {
        return minas;
    }

    public void setMinas(MINAS minas) {
        this.minas = minas;
    }

    public MicroCluster getTargetMicroCluster() {
        return targetMicroCluster;
    }

    public void setTargetMicroCluster(MicroCluster targetMicroCluster) {
        this.targetMicroCluster = targetMicroCluster;
    }

    public List<Sample> getTargetSamples() {
        return targetSamples;
    }

    public void setTargetSamples(List<Sample> targetSamples) {
        this.targetSamples = targetSamples;
    }

    public MicroCluster getClosestMicroCluster() {
        return closestMicroCluster;
    }

    public void setClosestMicroCluster(MicroCluster closestMicroCluster) {
        this.closestMicroCluster = closestMicroCluster;
    }
}
