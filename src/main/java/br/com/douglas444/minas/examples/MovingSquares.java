package br.com.douglas444.minas.examples;

import br.com.douglas444.common.Point;
import br.com.douglas444.datastream.DSFileReader;
import br.com.douglas444.datastream.DSRunnable;
import br.com.douglas444.minas.MINASController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        for (int i = 0; i < 5000; ++i) {
            try {
                trainSet.add(dsFileReader.next());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            DSRunnable.run(new MINASController(trainSet), dsFileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}