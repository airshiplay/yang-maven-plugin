package com.airlenet.yang.codegen;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by airlenet on 17/8/30.
 */
public class PyangInstall {


    public static void copy(File targetDirectory) throws IOException {
        String fullClassName = PyangInstall.class.getName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        URL url = PyangInstall.class.getResource(className + ".class");
        File pyangSourceFile;
        String plus = "\\+";
        String escapedPlus = "__ppluss__";
        String rawUrl = url.toString();
        rawUrl = rawUrl.replaceAll("\\+", "__ppluss__");
        String urlString = URLDecoder.decode(rawUrl, "UTF-8");
        urlString = urlString.replaceAll("__ppluss__", "\\+");
        int jarSeparatorIndex = urlString.lastIndexOf("!");
        if(urlString.startsWith("jar:file:") && jarSeparatorIndex > 0) {
            String jarFileName = urlString.substring("jar:file:".length(), jarSeparatorIndex);
            pyangSourceFile = new File(jarFileName);
        }else{
            pyangSourceFile =  new File(url.getFile());
        }

        ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(pyangSourceFile), 1024));


        ZipEntry zipEntry = zipInput.getNextEntry();
        while (zipEntry != null) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.startsWith("pyang")) {
                createDirectories(targetDirectory, zipEntryName);
                if (!zipEntry.isDirectory()) {
                    File file = createFile(targetDirectory, zipEntryName);
                    FileOutputStream output = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInput.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                    output.close();
                    file.setLastModified(zipEntry.getTime());
                }
            }
            zipInput.closeEntry();
            zipEntry = zipInput.getNextEntry();
        }
    }

    private static void createDirectories(File targetDirectory, String zipEntryName) {
        int lastSepIndex = zipEntryName.lastIndexOf("/");
        if (lastSepIndex > 0) {
            File directory = new File(targetDirectory, zipEntryName.substring(0, lastSepIndex));
            if (((!directory.exists()) || (!directory.isDirectory())) &&
                    (!directory.mkdirs())) {
                throw new RuntimeException();
            }
        }
    }

    private static File createFile(File targetDirectory, String zipEntryName)
            throws IOException {
        File file = new File(targetDirectory, zipEntryName);
        if (((!file.exists()) || (!file.isFile())) &&
                (!file.createNewFile())) {
            throw new RuntimeException();
        }
        return file;
    }
}
