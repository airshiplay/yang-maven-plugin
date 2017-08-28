package com.airlenet.yang.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by lig on 17/8/28.
 */
public class ProcessUtil {
   static Logger logger = LoggerFactory.getLogger(ProcessUtil.class);

    /**
     *
     * @param command
     * @return 执行错误返回false
     * @throws IOException
     */
    public static boolean process(String... command) throws IOException {
        for(String cmd:command)
         System.out.print(cmd+" ");
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command(command);

        Process process = processBuilder.start();
       String error= input2str(process.getErrorStream());
       String print= input2str(process.getInputStream());
       logger.info(print);
       if(error.equals("")){
           return false;
       }else{
           logger.error(error);
           return true;
       }
    }

    public static String input2str(InputStream inputStream) throws UnsupportedEncodingException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outSteam.toString("utf-8");

    }

}
