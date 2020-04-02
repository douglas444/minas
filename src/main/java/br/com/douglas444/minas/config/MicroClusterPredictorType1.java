package br.com.douglas444.minas.config;

import br.com.douglas444.minas.core.MicroCluster;
import br.com.douglas444.minas.core.Prediction;
import br.com.douglas444.mltk.Sample;

import java.util.List;
import java.util.Optional;

public class MicroClusterPredictorType1 implements MicroClusterPredictor {

    private double thresholdMultiplier;

    public MicroClusterPredictorType1(double thresholdMultiplier) {
        this.thresholdMultiplier = thresholdMultiplier;
    }

    @Override
    public Prediction predict(MicroCluster microCluster, List<MicroCluster> microClusters) {

        Sample center = microCluster.calculateCenter();
        Optional<MicroCluster> closestMicroCluster = MicroCluster.calculateClosestMicroCluster(center, microClusters);

        if (closestMicroCluster.isPresent()) {

            double distance = closestMicroCluster.get().calculateCenter().distance(center);
            double microClusterStandardDeviation = closestMicroCluster.get().calculateStandardDeviation();

            if (distance <= microClusterStandardDeviation * this.thresholdMultiplier) {
                return new Prediction(closestMicroCluster.get(), true);
            }

        }

        return new Prediction(closestMicroCluster.orElse(null), false);

    }

}
