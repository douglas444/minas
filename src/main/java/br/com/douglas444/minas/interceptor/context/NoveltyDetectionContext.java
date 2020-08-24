package br.com.douglas444.minas.interceptor.context;

import br.com.douglas444.dsframework.interceptor.Context;
import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NoveltyDetectionContext implements Context {

    private MicroCluster targetMicroCluster;
    private List<Sample> targetSamples;
    private MicroCluster closestMicroCluster;
    private List<MicroCluster> decisionModelMicroClusters;

    private BiConsumer<MicroCluster, MicroCluster> addExtension;
    private Consumer<MicroCluster> addNovelty;
    private Consumer<MicroCluster> awake;
    private Runnable defaultAction;

    public NoveltyDetectionContext NoveltyDetectionContext() {
        return this;
    }

    public MicroCluster getTargetMicroCluster() {
        return targetMicroCluster;
    }

    public NoveltyDetectionContext setTargetMicroCluster(MicroCluster targetMicroCluster) {
        this.targetMicroCluster = targetMicroCluster;
        return this;
    }

    public List<Sample> getTargetSamples() {
        return targetSamples;
    }

    public NoveltyDetectionContext setTargetSamples(List<Sample> targetSamples) {
        this.targetSamples = targetSamples;
        return this;
    }

    public MicroCluster getClosestMicroCluster() {
        return closestMicroCluster;
    }

    public NoveltyDetectionContext setClosestMicroCluster(MicroCluster closestMicroCluster) {
        this.closestMicroCluster = closestMicroCluster;
        return this;
    }

    public List<MicroCluster> getDecisionModelMicroClusters() {
        return decisionModelMicroClusters;
    }

    public NoveltyDetectionContext setDecisionModelMicroClusters(List<MicroCluster> decisionModelMicroClusters) {
        this.decisionModelMicroClusters = decisionModelMicroClusters;
        return this;
    }

    public BiConsumer<MicroCluster, MicroCluster> getAddExtension() {
        return addExtension;
    }

    public NoveltyDetectionContext setAddExtension(BiConsumer<MicroCluster, MicroCluster> addExtension) {
        this.addExtension = addExtension;
        return this;
    }

    public Consumer<MicroCluster> getAddNovelty() {
        return addNovelty;
    }

    public NoveltyDetectionContext setAddNovelty(Consumer<MicroCluster> addNovelty) {
        this.addNovelty = addNovelty;
        return this;
    }

    public Consumer<MicroCluster> getAwake() {
        return awake;
    }

    public NoveltyDetectionContext setAwake(Consumer<MicroCluster> awake) {
        this.awake = awake;
        return this;
    }

    public Runnable getDefaultAction() {
        return defaultAction;
    }

    public NoveltyDetectionContext setDefaultAction(Runnable defaultAction) {
        this.defaultAction = defaultAction;
        return this;
    }
}
