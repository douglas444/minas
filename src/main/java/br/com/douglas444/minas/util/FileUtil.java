package br.com.douglas444.minas.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileUtil {

    public static FileReader getFileReader(final String fileName) {

        File file = new File(fileName);
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileReader;
    }

}