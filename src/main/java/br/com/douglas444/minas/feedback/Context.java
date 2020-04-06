package br.com.douglas444.minas.feedback;

import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import br.com.douglas444.mltk.Sample;

import java.util.List;

public class Context {

    private Prediction prediction;
    private MicroCluster concept;
    private List<Sample> samples;
    private List<MicroCluster> knownConcepts;

    public Context(Prediction prediction, MicroCluster concept, List<Sample> samples, List<MicroCluster> knownConcepts) {
        this.prediction = prediction;
        this.concept = concept;
        this.samples = samples;
        this.knownConcepts = knownConcepts;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public MicroCluster getConcept() {
        return concept;
    }

    public void setConcept(MicroCluster concept) {
        this.concept = concept;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public List<MicroCluster> getKnownConcepts() {
        return knownConcepts;
    }

    public void setKnownConcepts(List<MicroCluster> knownConcepts) {
        this.knownConcepts = knownConcepts;
    }
}
