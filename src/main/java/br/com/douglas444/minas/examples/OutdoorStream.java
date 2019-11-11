package br.com.douglas444.minas.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSRunnable;
import br.com.douglas444.minas.MINASController;
import br.com.douglas444.mltk.Sample;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class OutdoorStream {
    public static void main(String[] args) throws IOException {


        FileReader dataFileReader = new FileReader(new File("./datasets/outdoorStream.data"));
        FileReader labelFileReader = new FileReader(new File("./datasets/outdoorStream.labels"));

        DSFileReader dsFileReader = new DSFileReader(" ", dataFileReader, labelFileReader);
        List<Sample> trainSet = dsFileReader.next(400);

        DSRunnable.run(new MINASController(trainSet), dsFileReader);
    }

}