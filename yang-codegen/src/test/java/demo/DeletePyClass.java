package demo;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by airlenet on 17/8/29.
 */
public class DeletePyClass {
    public static void main(String args[]){
delete(new File("/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/jython2.7.1/Lib"));
    }
    public static void delete(File f){
        File[] listFiles = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname.isDirectory()){
                    delete(pathname);
                }
                return pathname.getAbsolutePath().endsWith("$py.class");
            }
        });

        for(File file:listFiles){
            file.delete();
        }
    }
}
