package br.com.douglas444.minas.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSRunnable;
import br.com.douglas444.minas.MINASController;
import br.com.douglas444.mltk.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MovingSquares {
    public static void main(String[] args) {

        FileReader dataFile;
        FileReader labelFile;

        try {
            dataFile = new FileReader(new File("./datasets/movingSquares.data"));
            labelFile = new FileReader(new File("./datasets/movingSquares.labels"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        DSFileReader dsFileReader = new DSFileReader(" ", dataFile, labelFile);
        List<Point> trainSet = new ArrayList<>();
        Set<Integer> knownLabels = new HashSet<>();
        for (int i = 0; i < 5000; ++i) {
            try {
                Point point = dsFileReader.next();
                knownLabels.add((int) point.getY());
                trainSet.add(point);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            DSRunnable.run(new MINASController(trainSet, new ArrayList<>(knownLabels)), dsFileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}