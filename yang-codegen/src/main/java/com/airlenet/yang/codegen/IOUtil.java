package com.airlenet.yang.codegen;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lig on 17/8/28.
 */
public class IOUtil {
    public static void cp(InputStream inputStream,String file){
        byte[] buffer = new byte[1024];

        int len = -1;
        try {
            FileOutputStream fileWriter = new FileOutputStream(file);
            while ((len = inputStream.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, len);
            }
            fileWriter.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
