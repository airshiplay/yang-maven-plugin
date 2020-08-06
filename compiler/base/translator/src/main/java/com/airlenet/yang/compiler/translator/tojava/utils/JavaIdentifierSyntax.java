/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airlenet.yang.compiler.translator.tojava.utils;

import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangRevision;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoContainer;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.airlenet.yang.compiler.utils.io.YangToJavaNamingConflictUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.airlenet.yang.compiler.datamodel.utils.DataModelUtils.getParentNodeInGenCode;
import static com.airlenet.yang.compiler.utils.UtilConstants.ASTERISK;
import static com.airlenet.yang.compiler.utils.UtilConstants.COLON;
import static com.airlenet.yang.compiler.utils.UtilConstants.DEFAULT_BASE_PKG;
import static com.airlenet.yang.compiler.utils.UtilConstants.HYPHEN;
import static com.airlenet.yang.compiler.utils.UtilConstants.ONE_DOT_ONE;
import static com.airlenet.yang.compiler.utils.UtilConstants.PERIOD;
import static com.airlenet.yang.compiler.utils.UtilConstants.REGEX_WITH_ALL_SPECIAL_CHAR;
import static com.airlenet.yang.compiler.utils.UtilConstants.REVISION_PREFIX;
import static com.airlenet.yang.compiler.utils.UtilConstants.SLASH;
import static com.airlenet.yang.compiler.utils.UtilConstants.UNDER_SCORE;
import static com.airlenet.yang.compiler.utils.UtilConstants.VERSION_PREFIX;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.addPackageInfo;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.createDirectories;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getAbsolutePackagePath;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getCamelCase;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getJavaPackageFromPackagePath;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getPackageDirPathFromJavaJPackage;

/**
 * Represents an utility Class for translating the name from YANG to java convention.
 */
public final class JavaIdentifierSyntax {

    private static final int INDEX_ZERO = 0;
    private static final int INDEX_ONE = 1;
    private static final int VALUE_CHECK = 10;
    private static final String ZERO = "0";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Create instance of java identifier syntax.
     */
    private JavaIdentifierSyntax() {
    }

    public static String getRootPackage(YangPluginConfig yangPlugin,String version, String name,
                                        YangRevision revision,
                                        YangToJavaNamingConflictUtil resolver) {
        if("jnc".equals(yangPlugin.getFormat())){
            StringBuilder pkg = new StringBuilder(yangPlugin.getCodeGenPackage())
                    .append(PERIOD)
//                    .append(getYangVersion(version))
//                    .append(PERIOD)
                    .append(getCamelCase(name, resolver));
            if (revision != null) {
                pkg.append(PERIOD)
                        .append(getYangRevisionStr(revision.getRevDate()));
            }
            return pkg.toString().toLowerCase();
        }else{
            return  getRootPackage(version,name,revision,resolver);
        }
    }
    /**
     * Returns the root package string.
     *
     * @param version  YANG version
     * @param name     name of the module
     * @param revision revision of the module defined
     * @param resolver object of YANG to java naming conflict util
     * @return the root package string
     */
    public static String getRootPackage(String version, String name,
                                        YangRevision revision,
                                        YangToJavaNamingConflictUtil resolver) {

        StringBuilder pkg = new StringBuilder(DEFAULT_BASE_PKG)
                .append(PERIOD)
                .append(getYangVersion(version))
                .append(PERIOD)
                .append(getCamelCase(name, resolver));
        if (revision != null) {
            pkg.append(PERIOD)
                    .append(getYangRevisionStr(revision.getRevDate()));
        }
        return pkg.toString().toLowerCase();
    }

    /**
     * Returns version.
     *
     * @param ver YANG version
     * @return version
     */
    private static String getYangVersion(String ver) {
        if (ver.equals(ONE_DOT_ONE)) {
            ver = "11";
        }
        return VERSION_PREFIX + ver;
    }

    /**
     * Returns revision string array.
     *
     * @param date YANG module revision
     * @return revision string
     */
    private static String getYangRevisionStr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String dateInString = sdf.format(date);
        String[] revisionArr = dateInString.split(HYPHEN);

        StringBuilder rev = new StringBuilder(REVISION_PREFIX)
                .append(revisionArr[INDEX_ZERO]);

        for (int i = INDEX_ONE; i < revisionArr.length; i++) {
            Integer val = Integer.parseInt(revisionArr[i]);
            if (val < VALUE_CHECK) {
                rev.append(ZERO);
            }
            rev.append(val);
        }
        return rev.toString();
    }

    /**
     * Returns enum's java name.
     *
     * @param name enum's name
     * @return enum's java name
     */
    public static String getEnumJavaAttribute(String name) {

        if (name.equals("*")) {
            name = ASTERISK;
        } else {
            name = name.replaceAll(REGEX_WITH_ALL_SPECIAL_CHAR, COLON);
        }

        String[] strArray = name.split(COLON);
        StringBuilder output = new StringBuilder();
        if (strArray[0].isEmpty()) {
            List<String> stringArrangement = new ArrayList<>();
            stringArrangement.addAll(Arrays.asList(strArray).subList(1, strArray.length));
            strArray = stringArrangement.toArray(new String[stringArrangement.size()]);
        }
        for (int i = 0; i < strArray.length; i++) {
            if (i > 0 && i < strArray.length) {
                output.append(UNDER_SCORE);
            }
            output.append(strArray[i]);
        }
        return output.toString();
    }

    /**
     * Creates a package structure with package info java file if not present.
     *
     * @param yangNode YANG node for which code is being generated
     * @throws IOException any IO exception
     */
    public static void createPackage(YangNode yangNode)
            throws IOException {
        if (!(yangNode instanceof JavaFileInfoContainer)) {
            throw new TranslatorException("current node must have java file info " +
                                                  yangNode.getName() + " in " +
                                                  yangNode.getLineNumber() + " at " +
                                                  yangNode.getCharPosition() +
                                                  " in " + yangNode.getFileName());
        }
        String pkgInfo;
        JavaFileInfoTranslator javaFileInfo = ((JavaFileInfoContainer) yangNode)
                .getJavaFileInfo();
        String pkg = getAbsolutePackagePath(javaFileInfo.getBaseCodeGenPath(),
                                            javaFileInfo.getPackageFilePath());
        JavaFileInfoTranslator parentInfo;
        if (!doesPackageExist(pkg)) {
            try {
                File pack = createDirectories(pkg);
                YangNode parent = getParentNodeInGenCode(yangNode);
                if (parent != null) {
                    parentInfo = ((JavaFileInfoContainer) parent).getJavaFileInfo();
                    pkgInfo = parentInfo.getJavaName();
                    addPackageInfo(pack, pkgInfo, getJavaPackageFromPackagePath(pkg),
                                   true,parentInfo.getYangFileName(),parentInfo.getLineNumber());
                } else {
                    pkgInfo = javaFileInfo.getJavaName();
                    addPackageInfo(pack, pkgInfo, getJavaPackageFromPackagePath(pkg),
                                   false,javaFileInfo.getYangFileName(),javaFileInfo.getLineNumber());
                }
            } catch (IOException e) {
                throw new IOException("failed to create package-info file for " + pkg, e);
            }
        }
    }

    /**
     * Checks if the package directory structure created.
     *
     * @param pkg Package to check if it is created
     * @return existence status of package
     */
    static boolean doesPackageExist(String pkg) {
        File pkgDir = new File(getPackageDirPathFromJavaJPackage(pkg));
        File pkgWithFile = new File(pkgDir + SLASH + "package-info.java");
        return pkgDir.exists() && pkgWithFile.isFile();
    }
}
