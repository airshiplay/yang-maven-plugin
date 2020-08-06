/* 
 * @(#)TfcgPrefix.java        1.0
 *
 * This file has been auto-generated by JNC, the
 * Java output format plug-in of pyang.
 * Origin: module "tailf-common-monitoring", revision: "2013-06-14".
 */

package com.airlenet.yang.model.tailfCommonMonitoring;

import com.tailf.jnc.JNCException;
import com.tailf.jnc.SchemaNode;
import com.tailf.jnc.SchemaParser;
import com.tailf.jnc.SchemaTree;
import com.tailf.jnc.Tagpath;
import com.tailf.jnc.YangElement;

import java.util.HashMap;

/**
 * The root class for namespace http://tail-f.com/yang/common-monitoring (accessible from 
 * TailfCommonMonitoring.NAMESPACE) with prefix "tfcgPrefix" (TailfCommonMonitoring.PREFIX).
 *
 * @version 1.0
 * @author Auto Generated
 */
public class TfcgPrefix {

    public static final String NAMESPACE = "http://tail-f.com/yang/common-monitoring";

    public static final String PREFIX = "tfcg";

    /**
     * Enable the elements in this namespace to be aware
     * of the data model and use the generated classes.
     */
    public static void enable() throws JNCException {
        YangElement.setPackage(NAMESPACE, "com.airlenet.yang.model.tailfCommonMonitoring");
        TfcgPrefix.registerSchema();
    }

    /**
     * Register the schema for this namespace in the global
     * schema table (CsTree) making it possible to lookup
     * CsNode entries for all tagpaths
     */
    public static void registerSchema() throws JNCException {
        SchemaParser parser = new SchemaParser();
        HashMap<Tagpath, SchemaNode> h = SchemaTree.create(NAMESPACE);
        parser.findAndReadFile("TfcgPrefix.schema", h, TfcgPrefix.class);
    }

}