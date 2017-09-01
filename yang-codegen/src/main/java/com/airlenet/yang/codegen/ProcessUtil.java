package com.airlenet.yang.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created by airlenet on 17/8/28.
 */
public class ProcessUtil {
    static Logger logger = LoggerFactory.getLogger(ProcessUtil.class);

    /**
     * @param command
     * @return 执行错误返回false
     * @throws IOException
     */
    public static void process(String... command) throws Exception {
        process(null, command);
    }

    public static void process(boolean showWaring, String... command) throws Exception {
        process(showWaring, null, command);
    }
    public static void process(boolean showWaring, List<String> commandList) throws Exception {
        process(showWaring, null, commandList.toArray(new String[0]));
    }
    public static void process(File base, String... command) throws Exception {
        process(true, base, command);
    }

    public static void process(boolean showWaring, File base, String... command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (base != null)
            processBuilder.directory(base);
        processBuilder.command(command);

        Process process = processBuilder.start();
        String print = input2str(process.getInputStream());
        String error = input2str(process.getErrorStream());
        if (!"".equals(print))
            logger.info(print);
        if (error.equals("")) {

        } else {
            if (error.contains("Error") || error.contains("error") || error.contains("ERROR")) {
                throw new Exception(error);
            }
            if (error.contains("warning") || error.contains("WARNING")) {
                if (showWaring)
                    logger.warn(error);
            } else {
                logger.info(error);
            }
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
