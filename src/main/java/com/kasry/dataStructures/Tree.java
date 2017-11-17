package com.kasry.dataStructures;

import java.util.*;

public class Tree {
    private Node root;

    public Tree() {
        root = new Node();
    }

    public Tree(Node rootData) {
        root.setChildren(new ArrayList<Node>());
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return "Tree{" +
                "root=" + root +
                '}';
    }

    /**
     * Checking the validity of the document using the regEXDfa
     * @param regExDFA returns for each element it's DFA automaton
     * @return if the documents is valid or not
     */
    public boolean checkValidity(Map<String, Automaton> regExDFA) {
        boolean isValid = true;
        Stack<Node> stack = new Stack<>();
        Set<Node> visitedNodes = new HashSet<>();
        stack.push(root);
        while (!stack.isEmpty() && isValid) {
            Node current = stack.pop();
            if(!visitedNodes.contains(current)) {
                visitedNodes.add(current);
                Automaton currentAutomato = regExDFA.get(current.getData());
                String childrenString = current.childrendAsString();
                isValid = currentAutomato.match(childrenString);
                for (Node child :
                        current.getChildren()) {
                    stack.push(child);
                }

            }
        }

        return isValid;
    }
}
