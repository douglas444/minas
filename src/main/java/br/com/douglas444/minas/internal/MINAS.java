package br.com.douglas444.minas.internal;

import br.com.douglas444.mltk.Cluster;
import br.com.douglas444.mltk.DynamicConfusionMatrix;
import br.com.douglas444.mltk.Point;
import br.com.douglas444.mltk.kmeans.KMeansPlusPlus;

import java.util.*;
import java.util.stream.Collectors;

public class MINAS {

    private DecisionModel decisionModel;
    private DecisionModel sleepMemory;
    private List<Point> temporaryMemory;
    private int nextNoveltyLabel;
    private int timestamp;

    public MINAS(List<Point> trainSet) {

        this.timestamp = 0;
        this.decisionModel = buildDecisionModel(trainSet);
        this.temporaryMemory = new ArrayList<>();
        this.sleepMemory = new DecisionModel();
        this.nextNoveltyLabel = 0;

    }

    //Offline Phase
    private static DecisionModel buildDecisionModel(List<Point> trainSet) {

        List<MicroCluster> microClusters = new ArrayList<>();
        HashMap<Integer, List<Point>> pointsByLabel = new HashMap<>();

        trainSet.forEach(point -> {
            pointsByLabel.putIfAbsent(point.getY(), new ArrayList<>());
            pointsByLabel.get(point.getY()).add(point);
        });

        pointsByLabel.forEach((key, value) -> {
            KMeansPlusPlus kMeansPlusPlus = new KMeansPlusPlus(value, Hyperparameter.K);
            List<Cluster> clusters = kMeansPlusPlus.fit();
            microClusters.addAll(clusters.stream().map(MicroCluster::new).collect(Collectors.toList()));
            microClusters.forEach(microCluster -> microCluster.setLabel(key));
        });

        microClusters.forEach(microCluster -> microCluster.setCategory(Category.KNOWN));

        return new DecisionModel(microClusters);
    }

    private List<MicroCluster> noveltyDetection() {

        List<MicroCluster> microClusters = new ArrayList<>();

        //Generates clusters
        KMeansPlusPlus kMeansPlusPlus = new KMeansPlusPlus(temporaryMemory, Hyperparameter.K);
        List<Cluster> clusters = kMeansPlusPlus.fit();

        //Selects valid clusters and remove their points from the temporary memory
        for (Cluster cluster : clusters) {

            if (this.decisionModel.calculateSilhouette(cluster) > 0 &&
                    cluster.getPoints().size() > Hyperparameter.MICRO_CLUSTER_MIN_SIZE) {

                temporaryMemory.removeAll(cluster.getPoints());
                MicroCluster microCluster = new MicroCluster(cluster);
                microClusters.add(microCluster);
            }

        }

        List<MicroCluster> awakenedMicroClusters = new ArrayList<>();

        //For each micro cluster, checks if it is a extension or a novelty
        microClusters.forEach(microCluster -> {

            Optional<MicroCluster> extended = this.decisionModel.predict(microCluster);
            if (extended.isPresent()) {

                //Extension in decision model
                if (extended.get().getCategory() == Category.NOVELTY ||
                        extended.get().getCategory() == Category.NOVELTY_EXTENSION) {
                    microCluster.setCategory(Category.NOVELTY_EXTENSION);
                } else {
                    microCluster.setCategory(Category.KNOWN_EXTENSION);
                }
                microCluster.setLabel(extended.get().getLabel());


            } else {

                extended = this.sleepMemory.predict(microCluster);

                if (extended.isPresent()) {

                    //Extension in sleep memory
                    if (extended.get().getCategory() == Category.NOVELTY ||
                            extended.get().getCategory() == Category.NOVELTY_EXTENSION) {
                        microCluster.setCategory(Category.NOVELTY_EXTENSION);
                    } else {
                        microCluster.setCategory(Category.KNOWN_EXTENSION);
                    }
                    microCluster.setLabel(extended.get().getLabel());
                    awakenedMicroClusters.add(extended.get());


                } else {

                    //Novelty
                    microCluster.setCategory(Category.NOVELTY);
                    microCluster.setLabel(this.nextNoveltyLabel);
                    ++this.nextNoveltyLabel;

                }
            }

        });

        microClusters.addAll(awakenedMicroClusters);
        return microClusters;
    }

    //Online Phase
    public Optional<MicroCluster> predictAndUpdate(Point point) {

        Optional<MicroCluster> microCluster = decisionModel.predictAndUpdate(point);

        if (!microCluster.isPresent()) {

            temporaryMemory.add(point);

            if (temporaryMemory.size() >= Hyperparameter.TEMPORARY_MEMORY_MIN_SIZE) {
                List<MicroCluster> microClusters = noveltyDetection();
                decisionModel.merge(microClusters);
            }

        }

        ++timestamp;
        if (timestamp % Hyperparameter.WINDOW_MAX_SIZE == 0) {
            sleepMemory.merge(decisionModel.extractInactiveMicroClusters(timestamp));
            temporaryMemory.removeIf(p -> (timestamp - p.getT()) > Hyperparameter.TS);
        }

        return microCluster;

    }

}
