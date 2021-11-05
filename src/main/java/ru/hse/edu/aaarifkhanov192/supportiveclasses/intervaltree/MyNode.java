package ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;

public class MyNode<T> {
    private MyInterval interval;
    private T token;
    private MyNode<T> left;
    private MyNode<T> right;
    private MyNode<T> parent;
    private NodeColor color;

    public MyNode(){}


    public MyNode(MyInterval interval, T token) {
        this.setInterval(interval);
        this.setToken(token);
        this.setColor(NodeColor.BLACK);
        this.setParent(null);
    }

    /**
     * Направление родителя для дальнейшего поворота.
     */
    public NodeDirection parentDirection(){
        if(parent == null){
            return NodeDirection.NONE;
        }

        return parent.left == this ? NodeDirection.RIGHT : NodeDirection.LEFT;
    }

    /**
     * Возвращает прародителя <code>{@link MyNode}</code>.
     * @return Возвращает прародителя <code>{@link MyNode}</code>.
     */
    public MyNode<T> grandParent(){
        if(parent != null){
            return parent.parent;
        }
        return null;
    }

    /**
     * Возвращает брата родителя.
     * @return Возвращает брата родителя.
     */
    public MyNode<T> uncle(){
        MyNode<T> gpr = grandParent();
        if(gpr == null){
            return null;
        }

        if(parent == gpr.left){
            return gpr.right;
        }

        return gpr.left;
    }

    public MyNode<T> getSuccessor(){
        if(right == null){
            return null;
        }

        MyNode<T> node = right;
        while (node.left != null){
            node = node.left;
        }

        return node;
    }

    public boolean isRoot(){
        return parent == null;
    }

    public T getToken() {
        return token;
    }

    public void setToken(T token) {
        this.token = token;
    }

    public MyInterval getInterval() {
        return interval;
    }

    public void setInterval(MyInterval interval) {
        this.interval = interval;
    }

    public MyNode<T> getLeft() {
        return left;
    }

    public void setLeft(MyNode<T> left) {
        this.left = left;
//        this.left.parent = this;
    }

    public MyNode<T> getRight() {
        return right;
    }

    public void setRight(MyNode<T> right) {
        this.right = right;
//        this.right.parent = this;
    }

    public NodeColor getColor() {
        return color;
    }

    public void setColor(NodeColor color) {
        this.color = color;
    }

    public MyNode<T> getParent() {
        return parent;
    }

    public void setParent(MyNode<T> parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "{" + getInterval() +
                ", v: " + getToken()  +
                ", [left:" + getLeft() +
                ", right:" + getRight() +
                "]}" ;
    }
}
