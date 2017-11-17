package com.kasry.main;

import com.kasry.core.XmlChecker;
import com.kasry.dataModels.Tree;

import java.io.File;
import java.util.Map;

/**
 * This class is the one that contains the main class
 */
public class XmlCheckerMain {

    public static void main(String[] args) {
        Tree xmlTree;
        //First I'll create a method that reads the file, and checks if the file is well-formed
        String path = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\bug.xml";
        String dpath = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\bug.dtd";
        String dpath1 = "D:/Documents/Data & Knowledge/Period 1/Java projects/Xml_Checker/src/main/resources/exampleBig.dtd";
        String xmlPath = path.replace("\\", "/");
        String dtdPath = dpath.replace("\\", "/");;
        XmlChecker xmlChecker = new XmlChecker(xmlPath, dtdPath);
//        xmlChecker.parseTree(path);
//        System.out.println(xmlChecker.getXmlTree()+"\n");
        xmlChecker.checkFile();
        path = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\25001_nodes.xml";
        dpath = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\exam.dtd";
        xmlChecker = new XmlChecker(path, dpath);
        xmlChecker.checkFile();


        String folderPath = "D:/Documents/Data & Knowledge/Period 1/Java projects/Xml_Checker/src/main/resources/generated files";
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        for (final File fileEntry : files) {
            if (fileEntry.getName().equals("generatedFile427.xml")) {
                xmlChecker = new XmlChecker(fileEntry.getPath(), dpath1);
                xmlChecker.checkFile();
                System.out.println();
            }
//            System.out.println(fileEntry.getPath());
        }
//        xmlChecker.checkFile();
//        System.out.println("The validity of the xml file given as an input: " + xmlChecker.isWellFormed());
    }
}
