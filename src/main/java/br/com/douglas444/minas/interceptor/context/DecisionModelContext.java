package br.com.douglas444.minas.interceptor.context;

import br.com.douglas444.dsframework.interceptor.Context;
import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;

public class DecisionModelContext implements Context {

    private List<MicroCluster> decisionModel;
    private Sample sampleTarget;
    private MicroCluster microClusterTarget;

    public List<MicroCluster> getDecisionModel() {
        return decisionModel;
    }

    public void setDecisionModel(List<MicroCluster> decisionModel) {
        this.decisionModel = decisionModel;
    }

    public Sample getSampleTarget() {
        return sampleTarget;
    }

    public void setSampleTarget(Sample sampleTarget) {
        this.sampleTarget = sampleTarget;
    }

    public MicroCluster getMicroClusterTarget() {
        return microClusterTarget;
    }

    public void setMicroClusterTarget(MicroCluster microClusterTarget) {
        this.microClusterTarget = microClusterTarget;
    }
}
