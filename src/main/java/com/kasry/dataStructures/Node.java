package com.kasry.dataStructures;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String data;
    private Node parent;
    private List<Node> children;

    Node() {
        parent = new Node("none");
        children = new ArrayList<Node>();
        data = null;
    }

    public Node(String data) {
        this.data = data;
//        parent = new Node("none");
        children = new ArrayList<Node>();
    }

    public String childrendAsString() {
        String childrenString = "";
        for (Node child :
                children) {
            childrenString = childrenString.concat(child.getData());
        }
        return childrenString;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    void setChildren(List<Node> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        String string = null;
        String childs = new String();
        if (children != null) {
            for (Node tmp :
                    children) {
                childs = childs + ("\t" + tmp.toString());
            }
        }
        string = "Node{" +
                "data='" + data + '\'' +
                ", parent=" + parent.getData() +
                ", children=" + childs +
                '}';
        return string;
    }
}
