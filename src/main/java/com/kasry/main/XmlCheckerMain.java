package com.kasry.main;

import com.kasry.core.XmlChecker;
import com.kasry.dataModels.Tree;

import java.util.Map;

/**
 * This class is the one that contains the main class
 */
public class XmlCheckerMain {

    public static void main(String[] args) {
        Tree xmlTree;
        //First I'll create a method that reads the file, and checks if the file is well-formed
        String path = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\example.xml";
        String xmlPath = path.replace("\\", "/");
        String dpath = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\example.dtd";
        String dtdPath = dpath.replace("\\", "/");;
        XmlChecker xmlChecker = new XmlChecker(xmlPath, dtdPath);
        xmlChecker.parseTree(path);
        System.out.println(xmlChecker.getXmlTree()+"\n");
        xmlChecker.checkValidity();
//        xmlChecker.checkFile();
//        System.out.println("The validity of the xml file given as an input: " + xmlChecker.isWellFormed());
    }
}
