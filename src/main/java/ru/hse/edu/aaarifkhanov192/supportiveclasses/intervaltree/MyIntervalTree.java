package ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;

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

    public MyIntervalTree(int start, int stop, Token token) {
        this(new MyInterval(start, stop), token);
    }

    public void insert(MyInterval interval, Token token) {
        root = insert(root, interval, token);
    }


    public List<MyNode> getOverlaps(int start, int stop) {
        return getOverlaps(new MyInterval(start, stop));
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

        getOverlaps(node.getLeft(), interval);
        getOverlaps(node.getRight(), interval);
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

