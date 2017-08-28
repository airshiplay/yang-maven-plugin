package demo;

import com.airlenet.yang.codegen.Codegen;

import java.io.File;
import java.io.IOException;

/**
 * Created by lig on 17/8/28.
 */
public class CodegenTest {

    public static void main(String args[]) throws IOException {
        File yangRootFile = new File("/Users/lig/Documents/workspace/play-yang/yang-codegen/src/test/resources/yang");
        File outDirFile = new File("/Users/lig/Documents/workspace/play-yang/yang-codegen/target/generated-sources/java/src");
        Codegen codegen = new Codegen(yangRootFile, outDirFile, "com.airlenet.yang.model");
        codegen.generatorCode();
    }
}
