package br.com.douglas444.minas.examples;

import br.com.douglas444.dsframework.DSFileReader;
import br.com.douglas444.dsframework.DSRunnable;
import br.com.douglas444.minas.MINASController;
import br.com.douglas444.minas.internal.config.Configuration;
import br.com.douglas444.minas.internal.config.KMeans;
import br.com.douglas444.minas.internal.config.VL1;
import br.com.douglas444.mltk.Sample;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MoaFold1On {

    public static void main(String[] args) throws IOException {

        Configuration configuration = new Configuration(
                2000,
                200,
                4000,
                4000,
                4000,
                new KMeans(10),
                new VL1(1.1));

        FileReader fileReader = new FileReader(new File("./datasets/moa_fold1_on.data"));
        DSFileReader dsFileReader = new DSFileReader(",", fileReader);
        List<Sample> trainSet = dsFileReader.next(1000);

        MINASController minasController = new MINASController(trainSet, configuration);

        DSRunnable.run(minasController, dsFileReader);

    }

}