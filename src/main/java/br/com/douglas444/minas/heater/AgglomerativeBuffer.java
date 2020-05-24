package br.com.douglas444.minas.heater;

import br.com.douglas444.minas.MicroCluster;
import br.com.douglas444.minas.MicroClusterCategory;
import br.com.douglas444.mltk.datastructure.Sample;

import java.util.*;

public class AgglomerativeBuffer {

    private int timestamp;
    private final List<MicroCluster> buffer;
    private final int threshold;

    public AgglomerativeBuffer(List<MicroCluster> buffer, int threshold) {

        if (buffer.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.timestamp = 0;
        this.buffer = new ArrayList<>(buffer);
        this.buffer.forEach(microCluster -> microCluster.setTimestamp(0));
        this.threshold = threshold;
    }

    public void add(final Sample sample) {

        sample.setT(this.timestamp++);

        MicroCluster closestMicroCluster = MicroCluster.calculateClosestMicroCluster(sample, this.buffer);
        double distance = sample.distance(closestMicroCluster.calculateCentroid());

        if (distance < closestMicroCluster.calculateStandardDeviation() * 2) {
            closestMicroCluster.update(sample);
            closestMicroCluster.setTimestamp(sample.getT());
        } else {
            MicroCluster microCluster = new MicroCluster(sample);
            microCluster.setTimestamp(this.timestamp);
            microCluster.setLabel(sample.getY());
            microCluster.setMicroClusterCategory(MicroClusterCategory.KNOWN);
            this.add(microCluster);
        }

    }

    private void add(final MicroCluster microCluster) {

        if (this.timestamp - this.buffer.get(0).getTimestamp() > this.threshold) {
            this.buffer.remove(0);
        } else {

            MicroCluster m1 = null;
            MicroCluster m2 = null;
            double minDistance = Double.MAX_VALUE;

            for (MicroCluster a : this.buffer) {

                for (MicroCluster b : this.buffer) {

                    double distance = a.distance(b);
                    if (distance > 0 && distance < minDistance) {
                        minDistance = distance;
                        m1 = a;
                        m2 = b;
                    }
                }
            }

            if (m1 != null) {
                this.buffer.remove(m1);
                this.buffer.remove(m2);
                this.buffer.add(MicroCluster.merge(m1, m2));
                this.buffer.sort(Comparator.comparingInt(MicroCluster::getTimestamp).reversed());
            } else {
                throw new IllegalStateException("could not decrease agglomerative buffer size");
            }

        }

        this.buffer.add(microCluster);

    }

    public List<MicroCluster> getBuffer() {
        return this.buffer;
    }

}
