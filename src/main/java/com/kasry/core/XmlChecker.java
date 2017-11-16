package com.kasry.core;

import com.kasry.dataModels.Automaton;
import com.kasry.dataModels.Node;
import com.kasry.dataModels.State;
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
        //Get all the regular expressions, and for each construct the NFA
        Map<String, String> regExs = parseDtd();
        Map<String, Automaton> regExNFA = new HashMap<>(regExs.size());
//        constructSuccTable();
        for (Map.Entry<String, String> entry : regExs.entrySet()) {
            //Construct the NFA of all the regular expressions of the elements
            System.out.println("The automaton of " + entry.getKey() +", "+entry.getValue());
            Automaton thompsonAutomaton = thompsonConstruct(RegExConverter.infixToPostfix(entry.getValue()));
            System.out.println(thompsonAutomaton);
            Map<String, Set<State>> closure = thompsonAutomaton.getEpsilonClosure(thompsonAutomaton.getStart(), new HashSet<String>());
            for (Map.Entry<String, Set<State>> tmp : closure.entrySet()) {
                System.out.print(tmp.getKey() + ": ");
                for (State st :
                        tmp.getValue()) {
                    System.out.print(st.getName());
                }
                System.out.println();
            }
//            System.out.println();
            regExNFA.put(entry.getKey(), thompsonAutomaton);
        }
    }

    public Automaton thompsonConstruct(String regEx) {
        Stack<Automaton> stack = new Stack<>();
        Automaton automaton = null;
        for (int i = 0; i < regEx.length(); i++) {
            switch (regEx.charAt(i)) {
                case '_':
                    State startFinish = new State();
                    stack.push(new Automaton(startFinish, startFinish));
                    break;
                case '.':
                    Automaton automatonF = stack.pop();
                    Automaton automatonI = stack.pop();
                    //To link the two automatons
                    automatonI.getFinish().setEpsilon(automatonF.getStart().getEpsilon());
                    automatonI.getFinish().setTransitions(automatonF.getStart().getTransitions());
                    //Create the updated automaton
                    Automaton concatenated = new Automaton(automatonI.getStart(), automatonF.getFinish());
                    stack.push(concatenated);
                    break;
                case '*':
                    automaton = stack.pop();
                    automaton.addEpsilonFinishStart();
                    State nStart = new State();
                    State nFinish = new State();
                    automaton.getFinish().addEpsilon(nFinish);
                    nStart.addEpsilon(automaton.getStart());
                    nStart.addEpsilon(nFinish);
                    Automaton kleeneStarAut = new Automaton(nStart, nFinish);
                    stack.push(kleeneStarAut);
                    break;
                case '+':
                    automaton = stack.pop();
                    automaton.addEpsilonFinishStart();
                    State pStart = new State();
                    State pFinish = new State();
                    automaton.getFinish().addEpsilon(pFinish);
                    pStart.addEpsilon(automaton.getStart());
                    Automaton plusAut = new Automaton(pStart, pFinish);
                    stack.push(plusAut);
                    break;
                case '?':
                    automaton = stack.pop();
                    State qStart = new State();
                    State qFinish = new State();
                    automaton.getFinish().addEpsilon(qFinish);
                    qStart.addEpsilon(automaton.getStart());
                    qStart.addEpsilon(qFinish);
                    Automaton questionAut = new Automaton(qStart, qFinish);
                    stack.push(questionAut);
                    break;
                default:
                    //Create transitions
                    State finish = new State();
                    //Transition added during construction
                    State start = new State(regEx.charAt(i), finish);
                    automaton = new Automaton(start, finish);
//                    start.addTransition(regEx.charAt(i), finish);
                    stack.push(automaton);
                    break;
            }
        }
        return stack.pop();
    }

    /**
     * In this method we do construct the successors' table
     * But first we do convert the RegEx to the postfix form
     */
    @Deprecated
    private void glushkovConstruct() {
        Map<String, String> regExs = parseDtd();
        Map<String, List<String>> succesorMatrix = new HashMap<>();
        for (Map.Entry<String, String> entry : regExs.entrySet()) {
            String postFixRegEx = null;
            Stack<String> stack = new Stack<>();
            System.out.println(entry.getKey() + "/" + entry.getValue());
            //Updating all regular expressions to postfix notation
            postFixRegEx = RegExConverter.infixToPostfix(entry.getValue());
            entry.setValue(postFixRegEx);
//            regExToPostFix(entry);
            //We'll iterate through all the element of the regEx t construct the successor's table
            //While iterating we'll need the current & the next element, and a stack
            for (int i = 0; i < entry.getValue().length(); i++) {
                char current = entry.getValue().charAt(i);
                char next = '0';
                if (i < entry.getValue().length()){
                    next = entry.getValue().charAt(i+1);
                }
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
                    //Base case is this current is element and next is also element
                    if (current != '.' & current != '?' & current != '*' & current != '+') {
                        //Updating the stack
                        stack.push(Character.toString(current));
                        //Updating the successors matrix
                        List<String> eltSucc = new ArrayList<String>();
                        eltSucc.add(Character.toString(current));
                        succesorMatrix.put(Character.toString(current), eltSucc);
                        lastEltAdded = current;
                        if (next == '+' | next =='*') {
                            eltSucc.add(Character.toString(current));
                            succesorMatrix.put(Character.toString(current), eltSucc);
                        }
                    } else if(current == '+') {
                        //Update the stack
                        String tmp1 = stack.pop();
                        stack.push(tmp1+Character.toString(current));
                        //Updating the successors matrix
                        List<String> eltSucc = succesorMatrix.get(Character.toString(lastEltAdded));
                        eltSucc.add(Character.toString(next));
                        succesorMatrix.put(Character.toString(current), eltSucc);
                    }
                    //The only case where we do something according to current, is for '?' or '*'
                    else if (current == '?' | current == '*') {
                        //Update the stack
                        String tmp1 = stack.pop();
                        stack.push(tmp1+Character.toString(current));
                        //We have to add the next char to the "LAST element added to the stack"
                        if (next != '.' & next != '*' & next != '?' & next != '+') {
                            //Update the successors matrix
                            List<String> eltSucc = succesorMatrix.get(Character.toString(lastEltAdded));
                            eltSucc.add(Character.toString(next));
                            succesorMatrix.put(Character.toString(current), eltSucc);
                        }
                    } else if(current == '.') {
                        //Update the stack
                        String tmp1 = stack.pop();
                        String tmp2 = stack.pop();
                        stack.push(tmp1+Character.toString(current)+tmp2);
                        if (next == '*' | next == '+') {
                            //Update the successors matrix
                            List<String> eltSucc = succesorMatrix.get(Character.toString(lastEltAdded));
                            eltSucc.add(Character.toString(stack.peek().charAt(0)));
                            succesorMatrix.put(Character.toString(current), eltSucc);
                        }else if(next != '.' & next != '*' & next != '?' & next != '+'){

                        }
                    } else {
                        //Add next to
                    }
                }
//                System.out.println(c);
            }
        }
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
