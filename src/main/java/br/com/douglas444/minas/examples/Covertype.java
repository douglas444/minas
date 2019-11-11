package br.com.douglas444.minas.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSRunnable;
import br.com.douglas444.minas.MINASController;
import br.com.douglas444.mltk.Sample;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Covertype {

    public static void main(String[] args) throws IOException {

        FileReader fileReader = new FileReader(new File("./datasets/covtype.data"));
        DSFileReader dsFileReader = new DSFileReader(",", fileReader);

        List<Sample> trainSet = dsFileReader.next(58100);
        MINASController minasController = new MINASController(trainSet);

        DSRunnable.run(minasController, dsFileReader);

    }

}