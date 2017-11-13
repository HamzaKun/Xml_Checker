package com.kasry.dataModels;

import java.util.ArrayList;

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
}
