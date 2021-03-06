package com.kasry.main;

import com.kasry.core.XmlChecker;
import com.kasry.dataStructures.Tree;

import java.io.File;

/**
 * This class is the one that contains the main class
 */
public class XmlCheckerMain {

    public static void main(String[] args) {
        Tree xmlTree;
        //First I'll create a method that reads the file, and checks if the file is well-formed
        String path = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\example.xml";
        String dpath = "D:\\Documents\\Data & Knowledge\\Period 1\\Java projects\\Xml_Checker\\src\\main\\resources\\example.dtd";
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

        System.out.println("================================================================");

        String folderPath = "D:/Documents/Data & Knowledge/Period 1/Java projects/Xml_Generator/massyTest";
        String dpath1 = "D:/Documents/Data & Knowledge/Period 1/Java projects/Xml_Generator/massyTest/exam.dtd";
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        System.out.println(files.length);
        for (final File fileEntry : files) {
            System.out.println(fileEntry.getName());
//            System.out.print(fileEntry.getName().split("_")[0] + ",");
            if (fileEntry.getName().contains(".xml")) {
                xmlChecker = new XmlChecker(fileEntry.getPath(), dpath1);
                xmlChecker.checkFile();
                System.out.println();
            }
        }
//        xmlChecker.checkFile();
//        System.out.println("The validity of the xml file given as an input: " + xmlChecker.isWellFormed());
    }
}
