package br.com.douglas444.minas.interceptor;

import br.com.douglas444.dsframework.interceptor.ConsumerOrRunnableInterceptor;
import br.com.douglas444.dsframework.interceptor.FunctionOrSupplierInterceptor;
import br.com.douglas444.minas.ClassificationResult;

public class MINASInterceptor {

    public final ConsumerOrRunnableInterceptor<NoveltyDetectionContext> MICRO_CLUSTER_EXPLAINED_INTERCEPTOR;
    public final ConsumerOrRunnableInterceptor<NoveltyDetectionContext> MICRO_CLUSTER_EXPLAINED_BY_ASLEEP_INTERCEPTOR;
    public final ConsumerOrRunnableInterceptor<NoveltyDetectionContext> MICRO_CLUSTER_UNEXPLAINED_INTERCEPTOR;
    public final FunctionOrSupplierInterceptor<DecisionModelContext, ClassificationResult> SAMPLE_CLASSIFIER_INTERCEPTOR;
    public final FunctionOrSupplierInterceptor<DecisionModelContext, ClassificationResult> MICRO_CLUSTER_CLASSIFIER_INTERCEPTOR;

    public MINASInterceptor() {

        this.MICRO_CLUSTER_EXPLAINED_INTERCEPTOR = new ConsumerOrRunnableInterceptor<>();
        this.MICRO_CLUSTER_EXPLAINED_BY_ASLEEP_INTERCEPTOR = new ConsumerOrRunnableInterceptor<>();
        this.MICRO_CLUSTER_UNEXPLAINED_INTERCEPTOR = new ConsumerOrRunnableInterceptor<>();
        this.SAMPLE_CLASSIFIER_INTERCEPTOR = new FunctionOrSupplierInterceptor<>();
        this.MICRO_CLUSTER_CLASSIFIER_INTERCEPTOR = new FunctionOrSupplierInterceptor<>();

    }

}
