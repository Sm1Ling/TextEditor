package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class MyIntervalTree {
    private MyNode root;
    private List<MyNode> overlappedIntervals;
    private List<MyNode> tree;

    public MyIntervalTree() {
    }

    public MyIntervalTree(MyInterval interval, Token token) {
        root = new MyNode(interval, token);
    }

    public void insert(MyInterval interval, Token token) {
        root = insert(root, interval, token);
    }


    public List<MyNode> getOverlaps(MyInterval interval) {
        overlappedIntervals = new ArrayList<>();
        getOverlaps(root, interval);
        return overlappedIntervals;
    }

    public List<MyNode> getIntervals(int digit) {
        overlappedIntervals = new ArrayList<>();
        getOverlaps(root, new MyInterval(digit, digit));
        return overlappedIntervals;
    }

    public List<MyNode> getTree() {
        tree = new ArrayList<>();
        getTreeList(root);
        return tree;
    }

    private void getTreeList(MyNode root) {
        if (root == null) {
            return;
        }
        getTreeList(root.getLeft());
        tree.add(root);
        getTreeList(root.getRight());
    }


    private void getOverlaps(MyNode node, MyInterval interval) {
        if (node == null) {
            return;
        }
        if (node.getInterval().isOverlaps(interval)) {
            overlappedIntervals.add(node);
        }

        if (node.getLeft() != null && node.getLeft().getInterval().start() > interval.start()) {
            getOverlaps(node.getLeft(), interval);
        } else {
            getOverlaps(node.getRight(), interval);
        }
    }

    private MyNode insert(MyNode node, MyInterval interval, Token token) {
        if (node == null) {
            return new MyNode(interval, token);
        }

        int leftStart = node.getInterval().start();
        if (interval.start() < leftStart) {
            node.setLeft(insert(node.getLeft(), interval, token));
        } else {
            node.setRight(insert(node.getRight(), interval, token));
        }

        return node;
    }
}

record MyInterval(int start, int stop) {
    public boolean isOverlaps(MyInterval interval) {
        return (this.start <= interval.stop && interval.start <= this.stop);
    }

    @Override
    public String toString() {
        return "(" + start + ", " + stop + ')';
    }
}

class MyNode {
    private MyInterval interval;
    private Token token;
    private MyNode left;
    private MyNode right;

    public MyNode(MyInterval interval, Token token) {
        this.setInterval(interval);
        this.setToken(token);
    }

    @Override
    public String toString() {
        return "{" + getInterval() +
                ", v: " + getToken() +
                ", [left:" + getLeft() +
                ", right:" + getRight() +
                "]}";
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public MyInterval getInterval() {
        return interval;
    }

    public void setInterval(MyInterval interval) {
        this.interval = interval;
    }

    public MyNode getLeft() {
        return left;
    }

    public void setLeft(MyNode left) {
        this.left = left;
    }

    public MyNode getRight() {
        return right;
    }

    public void setRight(MyNode right) {
        this.right = right;
    }
}

