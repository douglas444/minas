package br.com.douglas444.minas.config;

import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import br.com.douglas444.mltk.Sample;

import java.util.List;
import java.util.Optional;

public class SamplePredictorType1 implements SamplePredictor {

    private double thresholdMultiplier;

    public SamplePredictorType1(double thresholdMultiplier) {
        this.thresholdMultiplier = thresholdMultiplier;
    }

    @Override
    public Prediction predict(Sample sample, List<MicroCluster> temporaryMemory) {

        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample,
                temporaryMemory);

        if (closestMicroCluster.isPresent()) {

            Sample center = closestMicroCluster.get().calculateCenter();
            double distance = center.distance(sample);
            double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

            if (distance <= thresholdMultiplier * microClusterStandardDeviation) {
                return new Prediction(closestMicroCluster.get(), true);
            }
        }

        return new Prediction(closestMicroCluster.orElse(null), false);
    }
}
