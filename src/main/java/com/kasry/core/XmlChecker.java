package com.kasry.core;

import com.kasry.dataStructures.Automaton;
import com.kasry.dataStructures.Node;
import com.kasry.dataStructures.State;
import com.kasry.dataStructures.Tree;

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
        Runtime rt = Runtime.getRuntime();
        long prevTotal = rt.totalMemory();
        long prevFree = rt.freeMemory();

        long start = System.nanoTime();
        this.parseTree(xmlPath);

        double parseInNa = (System.nanoTime() - start);
//        System.out.println("parsing ms: " + parseInNa* 1.0e-6);
        long startWell = System.nanoTime();
        checkWellFormedness();
        double wellInNa = (System.nanoTime() - startWell);
//        System.out.println("wellForm ms: " + (wellInNa* 1.0e-6));
        long startVal = System.nanoTime();
        checkValidity();
        double valInNa = (System.nanoTime() - startVal);
//        System.out.println(parseInNa* 1.0e-6 + "," + wellInNa* 1.0e-6 + "," + valInNa* 1.0e-6);
        long total = rt.totalMemory();
        long free = rt.freeMemory();
//        if (total != prevTotal || free != prevFree) {
//            System.out.println(
//                    String.format("%s,%s,%s",
//                            total ,
//                            free ,
//                            total - free));
////        }
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
        Map<String, Automaton> regExDFA = thompsonNfaToDfa(regExs);
        //Verifying the validity of the xml file according to the dtd
        boolean valid = xmlTree.checkValidity(regExDFA);
        System.out.println((valid?"Valid":"Not Valid"));
    }

    private Map<String, Automaton> thompsonNfaToDfa(Map<String, String> regExs) {
        Map<String, Automaton> regExNFA = new HashMap<>(regExs.size());
        Map<String, Automaton> regExDFA = new HashMap<>(regExs.size());
//        constructSuccTable();
        for (Map.Entry<String, String> entry : regExs.entrySet()) {

            //Construct the NFA of all the regular expressions of the elements
//            System.out.println("The automaton of " + entry.getKey() +", "+entry.getValue());
            Automaton thompsonAutomaton = thompsonConstruct(RegExConverter.infixToPostfix(entry.getValue()));
//            System.out.println(thompsonAutomaton);
            Map<String, Set<State>> closure = thompsonAutomaton.getEpsilonClosure(thompsonAutomaton.getStart(), new HashSet<String>());
           /* for (Map.Entry<String, Set<State>> tmp : closure.entrySet()) {
                System.out.print(tmp.getKey() + ": ");
                for (State st :
                        tmp.getValue()) {
                    System.out.print(st.getName());
                }
                System.out.println();
            }*/
            //Creating the DFA
            Automaton dfaAutomaton = constructDfa(closure, thompsonAutomaton);
//            System.out.println(dfaAutomaton);
//            System.out.println();
            regExNFA.put(entry.getKey(), thompsonAutomaton);
            regExDFA.put(entry.getKey(), dfaAutomaton);
        }
        return regExDFA;
    }

    private Automaton constructDfa(Map<String, Set<State>> closure, Automaton nfa) {
        Automaton dfaAutomaton = new Automaton();
        Set<Character> transchars = nfa.getAutomateTransitions(nfa.getStart(), new HashSet<String>());
        Set<State> goalStates = closure.get(nfa.getStart().getName());
        State newStart = createState(goalStates, transchars, closure, new HashMap<String, State>());
        dfaAutomaton.setStart(newStart);
        Set<State> finalStates = getFinalStates(newStart, nfa.getFinish(), new HashSet<String>());
        dfaAutomaton.setFinalStates(finalStates);
        return  dfaAutomaton;
    }

    private State createState(Set<State> goalStates, Set<Character> transchars, Map<String, Set<State>> closure, HashMap<String, State> done) {
        if(goalStates == null || goalStates.size() <1)
            return null;
        String stateName = "";
        for (State st : goalStates) {
            stateName = stateName.concat(st.getName());
        }
        if (done.get(stateName) != null)
            return done.get(stateName);
        else {
            State state = new State();
            state.setName(stateName);
            done.put(stateName, state);
            for (Character transition : transchars) {
                Set<State> newGoals = new HashSet<>();
                for (State tmpState : goalStates) {
                    if(tmpState.getTransitions() != null){
                        State fState = tmpState.getTransitions().get(transition);
                        if (fState != null) {
                            newGoals.addAll(closure.get(fState.getName()));
                        }
                    }
                }
                if (newGoals.size() < 1) {
                    state.addTransition(transition, null);
                } else {
                    State endState = createState(newGoals, transchars, closure, done);
                    state.addTransition(transition, endState);
                }
            }
            return state;
        }
    }

    private Set<State> getFinalStates(State newStart, State finish, Set<String> visited) {
        if (visited.contains(newStart.getName())) {
            return null;
        }else {
            Set<State> finalStates = new HashSet<>();
            visited.add(newStart.getName());
            if(newStart.getName().contains(finish.getName())) {
                finalStates.add(newStart);
            }
            for (Map.Entry<Character, State> entry : newStart.getTransitions().entrySet()) {
                if(entry.getValue() != null && entry.getValue().getName().contains(finish.getName())) {
                    finalStates.add(entry.getValue());
                }
                Set<State> states = null;
                if (entry.getValue() != null)
                    states = getFinalStates(entry.getValue(), finish, visited);
                if (states != null)
                    finalStates.addAll(states);
            }
            for (State state : newStart.getEpsilon()) {
                String recString = state.toString(visited);
                if(state.getName().contains(finish.getName())) {
                    finalStates.add(state);
                }
                Set<State> states = getFinalStates(state, finish, visited);
                if (states != null)
                    finalStates.addAll(states);
            }
            return finalStates;
        }
    }

    @Deprecated
    private Automaton constructDfaFromClosure(Map<String, Set<State>> closure, Automaton nfa) {
        //Determine all transitions in the automaton
        Set<Character> transchars = nfa.getAutomateTransitions(nfa.getStart(), new HashSet<String>());
        System.out.print("Transitions: " + transchars);
        //Determine initial & final states
        State state = new State();
        Set<State> closureStates = closure.get(nfa.getStart().getName());
        //Setting the name
        String startName = "";
        for (State st : closureStates) {
            startName = startName.concat(st.getName());
        }
        state.setName(startName);
        //This set contains the names of the states we need to build
        List<String> toProcess = new ArrayList<>();
        toProcess.add(startName);
        ListIterator<String> iterator = toProcess.listIterator();

        //In this loop, we'll have to build a state, for each transition symbol, the state will point to an epsilon-closure
        while (iterator.hasNext()) {
            String newStateName = iterator.next();
            Set<State> eltsSet = closure.get(newStateName);
            for (Character transition : transchars) {
                State endState = new State();
                Set<State> endStates = new HashSet<>();
                for (State stateInClosure : closureStates) {
                    if(stateInClosure.getTransitions().get(transition) != null) {
                        eltsSet.addAll(closure.get(stateInClosure.getTransitions().get(transition)));
                        String tmpName = "";
                        //creating the name
                        for (State st : closure.get(stateInClosure.getTransitions().get(transition))) {
                            tmpName = tmpName.concat(st.getName());
                        }
                        iterator.add(tmpName);
                    }
                    endState = stateInClosure.getTransitions().get(transition);
//                start.addTransition(transition, );
                }

            }
        }
        for (String stateName : toProcess) {
            State endState = new State();

        }

        return null;
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
//                System.out.println(line);
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
            System.out.println((wellFormed?"Well-formed":"not Well-formed"));
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
//                System.out.println(line);
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
