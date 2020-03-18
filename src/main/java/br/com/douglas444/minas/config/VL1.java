package br.com.douglas444.minas.config;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.minas.Prediction;
import br.com.douglas444.mltk.Sample;

import java.util.List;
import java.util.Optional;

public class VL1 implements VL {

    private double thresholdMultiplier;

    public VL1(double thresholdMultiplier) {
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
                return new Prediction(closestMicroCluster.get());
            }

        }

        return new Prediction(null);

    }

}
