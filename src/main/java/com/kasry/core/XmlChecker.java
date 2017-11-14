package com.kasry.core;

import com.kasry.dataModels.Node;
import com.kasry.dataModels.Tree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class XmlChecker {
    private String xmlPath;
    private String dtdPath;
    private boolean wellFormed = false;
    private boolean valid = false;
    private Tree xmlTree;


    public void checkFile() {
        checkWellFormedness();
        checkValidity();
    }

    private void checkValid() {
        System.out.println("bcd".matches("(bc)+d"));
        //Maybe I should to have a map of string and list of strings
        Map<String, String> elmtsRegEX = parseDtd();
        try{
            BufferedReader br = new BufferedReader(new FileReader(xmlPath));
            Stack<String> openElmts = new Stack<>();

            String line = br.readLine();
            while (line != null) {
                StringBuilder childs = new StringBuilder();
                System.out.println(line);
                //The first element is the "0|1" and the second is the element name
                String[] parts = line.split(" ");
                if (Integer.parseInt(parts[0]) == 0 && openElmts.empty()){
                    childs = new StringBuilder();
                    childs.append(parts[1]);
                    openElmts.push(parts[1]);
                    //If the child has other children
                } else if (Integer.parseInt(parts[0]) == 0 /*&& !openElmts.peek().equals(parts[1])*/) {
                    childs.append(parts[1]);
                    elmtsRegEX.put(openElmts.peek(), childs.toString());
                    openElmts.push(parts[1]);
                } else if (Integer.parseInt(parts[0]) == 1 && openElmts.peek().equals(parts[1])) {
                    // If an element has been closed, we need to remove it from the stack
                    elmtsRegEX.put(openElmts.peek(), childs.toString());
                    childs = new StringBuilder();
                    openElmts.pop();
                }
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkValidity() {
        constructSuccTable();
    }

    /**
     * In this method we do construct the successors' table
     * But first we do convert the RegEx to the postfix form
     */
    private void constructSuccTable() {
        Map<String, String> regExs = parseDtd();
        Map<String, List<String>> succesorMatrix = new HashMap<>();
        for (Map.Entry<String, String> entry : regExs.entrySet()) {
            String postFixRegEx = null;
            Stack<String> stack = new Stack<>();
            System.out.println(entry.getKey() + "/" + entry.getValue());
            //Updating all regular expressions to postfix notation
            regExToPostFix(entry);
            //We'll iterate through all the element of the regEx t construct the successor's table
            //While iterating we'll need the current & the next element, and a stack
            for (int i = 0; i < entry.getValue().length()-1; i++) {
                char current = entry.getValue().charAt(i);
                char next = entry.getValue().charAt(i+1);
                char lastEltAdded = '0';
                // We verify the stack for the first element
                if (stack.isEmpty()) {
                    List<String> iniSucc = new ArrayList<String>();
                    iniSucc.add(Character.toString(current));
                    succesorMatrix.put("init", iniSucc);
                    stack.push(Character.toString(current));
                    lastEltAdded = current;
                }
                else {
                    //Base case is this one
                    if (current != '.' & current != '?' & current != '*' & current != '+') {
                        List<String> eltSucc = new ArrayList<String>();
                        eltSucc.add(Character.toString(current));
                        succesorMatrix.put(Character.toString(current), eltSucc);
                        stack.push(Character.toString(current));
                        lastEltAdded = current;
                    }
                    //The only case where we do something according to current, is for '?' or '*'
                    if (current == '?' | current == '*') {
                        //We have to add the next char to the "LAST element added to the stack"

                    }
                    else {
                        //Add next to
                    }
                }
//                System.out.println(c);
            }
        }
    }

    private void regExToPostFix(Map.Entry<String, String> entry) {
        String postFixRegEx;
        if (entry.getValue().equals("_")) {
            postFixRegEx = entry.getValue();
            entry.setValue(postFixRegEx);
        } else {
            postFixRegEx = RegExConverter.infixToPostfix(entry.getValue());
            entry.setValue(postFixRegEx);
        }
        System.out.println(postFixRegEx);
    }

    /**
     * Done
     * @return for each key, its corresponding regEx
     */
    public Map<String, String> parseDtd() {
        BufferedReader br = null;
        Map<String, String> elmtsRegEX = new HashMap<>();
        try {
            br = new BufferedReader(new FileReader(dtdPath));
            //A HashMap of elements and their regular expressions
            String line = br.readLine();
            while (line != null) {
//                System.out.println(line);
                //The first string is the element and the second is the regEx
                String[] parts = line.split(" ");
                elmtsRegEX.put(parts[0], parts[1]);
                line = br.readLine();
            }
            return elmtsRegEX;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Done
     */
    private void checkWellFormedness() {
        try{
            BufferedReader br = new BufferedReader(new FileReader(xmlPath));
            String line = br.readLine();
            Stack<String> elements = new Stack<>();
            while (line != null) {
                System.out.println(line);
                //The first element is the "0|1" and the second is the element name
                String[] parts = line.split(" ");
                if (Integer.parseInt(parts[0]) == 0){
                    elements.push(parts[1]);
                } else if (Integer.parseInt(parts[0]) == 1 && elements.peek().equals(parts[1])) {
                    elements.pop();
                }
                line = br.readLine();
            }
            if( elements.empty()) {
                wellFormed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Done
     * @param xmlPath
     */
    public void parseTree(String xmlPath) {
        xmlTree = new Tree();
        try{
            BufferedReader br = new BufferedReader(new FileReader(xmlPath));
            String line = br.readLine();
            //Stack to be filled with elements
            Stack<String> elements = new Stack<>();
            Node actualNode = xmlTree.getRoot();
            // For all the nodes of the tree
            while (line != null) {
                System.out.println(line);
                //The first element is the "0|1" and the second is the element name
                String[] parts = line.split(" ");
                // First tag's element
                if (Integer.parseInt(parts[0]) == 0 & xmlTree.getRoot().getData() == null) {
                    actualNode.setData(parts[1]);
                    elements.push(parts[1]);
                    // An new element's openning tag
                } else if (Integer.parseInt(parts[0]) == 0){
                    Node child = new Node(parts[1]);
                    child.setParent(actualNode);
                    actualNode.addChild(child);
                    elements.push(parts[1]);
                    actualNode = child;
                    // An element's closing tag
                } else if (Integer.parseInt(parts[0]) == 1 && elements.peek().equals(parts[1])) {
                    elements.pop();
                    actualNode = actualNode.getParent();
                }
                line = br.readLine();
            }
            if( elements.empty()) {
                wellFormed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public XmlChecker() {
        xmlTree = new Tree();
    }

    public XmlChecker(String xmlPath, String dtdPath) {
        this.xmlPath = xmlPath;
        this.dtdPath = dtdPath;
    }

    public boolean isWellFormed() {
        return wellFormed;
    }

    public void setWellFormed(boolean wellFormed) {
        this.wellFormed = wellFormed;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getDtdPath() {
        return dtdPath;
    }

    public void setDtdPath(String dtdPath) {
        this.dtdPath = dtdPath;
    }

    public Tree getXmlTree() {
        return xmlTree;
    }

    public void setXmlTree(Tree xmlTree) {
        this.xmlTree = xmlTree;
    }

    @Override
    public String toString() {
        return "XmlChecker{" +
                "xmlTree=" + xmlTree +
                '}';
    }
}
