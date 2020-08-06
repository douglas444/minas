package br.com.douglas444.minas.interceptor;

import br.com.douglas444.dsframework.interceptor.ConsumerOrRunnableInterceptor;
import br.com.douglas444.dsframework.interceptor.FunctionOrSupplierInterceptor;
import br.com.douglas444.minas.Classification;
import br.com.douglas444.minas.interceptor.context.DecisionModelContext;
import br.com.douglas444.minas.interceptor.context.NoveltyDetectionContext;

public class MINASInterceptor {

    public final ConsumerOrRunnableInterceptor<NoveltyDetectionContext> MICRO_CLUSTER_EXPLAINED;
    public final ConsumerOrRunnableInterceptor<NoveltyDetectionContext> MICRO_CLUSTER_EXPLAINED_BY_ASLEEP;
    public final ConsumerOrRunnableInterceptor<NoveltyDetectionContext> MICRO_CLUSTER_UNEXPLAINED;
    public final FunctionOrSupplierInterceptor<DecisionModelContext, Classification> SAMPLE_CLASSIFIER;
    public final FunctionOrSupplierInterceptor<DecisionModelContext, Classification> MICRO_CLUSTER_CLASSIFIER;

    public MINASInterceptor() {

        this.MICRO_CLUSTER_EXPLAINED = new ConsumerOrRunnableInterceptor<>();
        this.MICRO_CLUSTER_EXPLAINED_BY_ASLEEP = new ConsumerOrRunnableInterceptor<>();
        this.MICRO_CLUSTER_UNEXPLAINED = new ConsumerOrRunnableInterceptor<>();
        this.SAMPLE_CLASSIFIER = new FunctionOrSupplierInterceptor<>();
        this.MICRO_CLUSTER_CLASSIFIER = new FunctionOrSupplierInterceptor<>();

    }

}
