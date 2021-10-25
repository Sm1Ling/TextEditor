package ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;

import org.antlr.v4.runtime.Token;

public class MyNode {
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
