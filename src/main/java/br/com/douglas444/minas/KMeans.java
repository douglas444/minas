package br.com.douglas444.minas;

import br.com.douglas444.streams.datastructures.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public final class KMeans {

    public static List<Cluster> execute(List<Sample> samples, final int k, final Random random) {

        samples = new ArrayList<>(samples);
        final List<Sample> centroids = chooseCentroids(samples, k, random);
        return execute(samples, centroids);

    }

    public static List<Cluster> execute(List<Sample> samples, List<Sample> centroids) {

        samples = new ArrayList<>(samples);
        centroids = new ArrayList<>(centroids);

        List<Cluster> clusters;
        List<Sample> oldCentroids;

        do {

            clusters = groupByClosestCentroid(samples, centroids);
            oldCentroids = new ArrayList<>(centroids);
            centroids.clear();

            clusters.stream()
                    .filter(cluster -> !cluster.isEmpty())
                    .map(Cluster::calculateCentroid)
                    .forEach(centroids::add);

        } while(!oldCentroids.containsAll(centroids));

        return clusters;
    }

    static List<Sample> chooseCentroids(final List<Sample> samples, final int k, final Random random) {

        final List<Sample> centroids = new ArrayList<>();
        final List<Sample> candidates = new ArrayList<>(samples);

        int n = k;

        while (n > 0 && !candidates.isEmpty()) {
            final int randomIndex = random.nextInt(candidates.size());
            final Sample centroid = candidates.get(randomIndex);
            candidates.remove(centroid);
            centroids.add(centroid);
            --n;
        }

        return centroids;

    }

    static List<Cluster> groupByClosestCentroid(final List<Sample> samples, final List<Sample> centroids) {

        final HashMap<Sample, List<Sample>> samplesByCentroid = new HashMap<>();

        centroids.forEach(centroid -> samplesByCentroid.put(centroid, new ArrayList<>()));

        samples.forEach(sample -> {
            final Sample closestCentroid = sample.calculateClosestSample(centroids);
            samplesByCentroid.get(closestCentroid).add(sample);
        });

        final List<Cluster> clusters = new ArrayList<>();

        samplesByCentroid.forEach((key, value) -> {
            if (!value.isEmpty()) {
                clusters.add(new Cluster(value));
            }
        });

        return clusters;

    }

    static double distanceToTheClosestCentroid(final Sample sample, List<Sample> centroids) {
        if (centroids.isEmpty()) {
            return 0.0;
        }
        final Sample closestCentroid = sample.calculateClosestSample(centroids);
        return sample.distance(closestCentroid);
    }
}
