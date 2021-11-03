package ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval tree implementation. https://github.com/vvondra/Interval-Tree and https://www.geeksforgeeks.org/interval-tree/.
 */
public class MyIntervalTree<T> {
    private MyNode<T> root;
    private List<MyNode<T>> overlappedIntervals;
    private List<MyNode<T>> tree;

    public MyNode<T> getRoot() {
        return root;
    }

    public MyIntervalTree() {
    }

    public MyIntervalTree(MyInterval interval, T token) {
        root = new MyNode<>(interval, token);
    }

    public MyIntervalTree(int start, int stop, T token) {
        this(new MyInterval(start, stop), token);
    }

    public void insert(int start, int stop, T token) {
        insert(new MyInterval(start, stop), token);
    }
//    public void insert(MyInterval interval, T token) {
//        root = insert(root, interval, token);
//    }

    public void insert(MyInterval interval, T token) {
        MyNode<T> node = new MyNode<>(interval, token);
        if (root == null) {
            node.setColor(NodeColor.BLACK);
            root = node;
        } else {
            insert(root, interval, token);
        }
    }


    public List<MyNode<T>> getOverlaps(int start, int stop) {
        return getOverlaps(new MyInterval(start, stop));
    }

    public List<MyNode<T>> getOverlaps(MyInterval interval) {
        overlappedIntervals = new ArrayList<>();
        getOverlaps(root, interval);
        return overlappedIntervals;
    }

    public List<MyNode<T>> getIntervals(int digit) {
        overlappedIntervals = new ArrayList<>();
        getOverlaps(root, new MyInterval(digit, digit));
        return overlappedIntervals;
    }

    public List<MyNode<T>> getTree() {
        tree = new ArrayList<>();
        getTreeList(root);
        return tree;
    }

    public void delete(MyInterval interval) {
        deleteInterval(findInterval(root, interval));
    }

    public void shiftIntervals(int biggerStart, int shift) {
        shift(root, biggerStart, shift);
    }

    private void shift(MyNode<T> node, int biggerStart, int shift) {
        if(node == null){
            return;
        }

        if(node.getInterval().start() > biggerStart){
            node.setInterval(new MyInterval(node.getInterval().start() + shift, node.getInterval().stop() + shift));
        }

//        if(node.getLeft().getInterval().start() > biggerStart)
        shift(node.getLeft(), biggerStart, shift);
        shift(node.getRight(), biggerStart, shift);
    }

    public MyNode<T> findInterval(MyNode<T> node, MyInterval interval) {
        while (node != null) {
            if (node.getInterval().compareTo(interval) > 0) {
                node = node.getLeft();
                continue;
            }

            if (node.getInterval().compareTo(interval) < 0) {
                node = node.getRight();
                continue;
            }

            if (node.getInterval().compareTo(interval) == 0) {
                return node;
            }
        }
        return null;
    }

    private MyNode<T> minLowInterval(MyNode<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

//    private MyNode<T> deleteInterval(MyNode<T> node, MyInterval interval) {
//        if (node == null) {
//            return null;
//        }
//        MyNode<T> tmp;
//        if (interval.start() < node.getInterval().start()) {
//            node.setLeft(deleteInterval(node.getLeft(), interval));
//        } else if (interval.start() > node.getInterval().start()) {
//            node.setRight(deleteInterval(node.getRight(), interval));
//        } else if (interval.start() == node.getInterval().start()) {
//            if (interval.stop() == node.getInterval().stop()) {
//                if (node.getLeft() == null) {
//                    return node.getRight();
//                } else if (node.getRight() == null) {
//                    return node.getLeft();
//                }
//                tmp = minLowInterval(node.getRight());
//                node.setInterval(tmp.getInterval());
//                node.setRight(deleteInterval(node.getRight(), tmp.getInterval()));
//            } else {
//                node.setRight(deleteInterval(node.getRight(), interval));
//            }
//        }
//
//        return node;
//    }

    private void deleteInterval(MyNode<T> node) {
        if (node == null) {
            return;
        }

        MyNode<T> tmp = node;
        if (node.getRight() != null && node.getLeft() != null) {
            tmp = node.getSuccessor();
            node.setInterval(tmp.getInterval());

            while (node.getParent() != null) {
                node = node.getParent();
            }
        }

        node = tmp;
        tmp = node.getLeft() != null ? node.getLeft() : node.getRight();
        if (tmp != null) {
            tmp.setParent(node.getParent());
        }

        if (node.isRoot()) {
            root = tmp;
        } else {
            if (node.parentDirection() == NodeDirection.RIGHT) {
                node.getParent().setLeft(tmp);
            } else {
                node.getParent().setRight(tmp);
            }

            MyNode<T> maxAux = node.getParent();
            while (maxAux.getParent() != null) {
                maxAux = maxAux.getParent();
            }
        }

        if (node.getColor() != NodeColor.BLACK) {
            rebalanceDeletion(tmp);
        }
    }

    private void getTreeList(MyNode<T> root) {
        if (root == null) {
            return;
        }
        getTreeList(root.getLeft());
        tree.add(root);
        getTreeList(root.getRight());
    }

    private void getOverlaps(MyNode<T> node, MyInterval interval) {
        if (node == null) {
            return;
        }
        if (node.getInterval().isOverlaps(interval)) {
            overlappedIntervals.add(node);
        }

        getOverlaps(node.getLeft(), interval);
        getOverlaps(node.getRight(), interval);
    }

//    private MyNode<T> insert(MyNode<T> node, MyInterval interval, T token) {
//        if (node == null) {
//            return new MyNode<>(interval, token);
//        }
//        MyNode<T> added = new MyNode<>(interval, token);
//        added.setColor(NodeColor.RED);
//
//        int leftStart = node.getInterval().start();
//        if (interval.start() < leftStart) {
//            node.setLeft(insert(node.getLeft(), interval, token));
//        } else {
//            node.setRight(insert(node.getRight(), interval, token));
//        }
//
//        rebalanceInsertion(added);
//        return node;
//    }

    private void insert(MyNode<T> node, MyInterval interval, T token) {
        MyNode<T> added = null;
        if (interval.compareTo(node.getInterval()) < 0) {
            if (node.getLeft() == null) {
                added = new MyNode<>(interval, token);
                added.setColor(NodeColor.RED);
                node.setLeft(added);
                added.setParent(node);
            } else {
                insert(node.getLeft(), interval, token);
                return;
            }
        } else if (interval.compareTo(node.getInterval()) > 0) {
            if (node.getRight() == null) {
                added = new MyNode<>(interval, token);
                added.setColor(NodeColor.RED);
                node.setRight(added);
                added.setParent(node);
            } else {
                insert(node.getRight(), interval, token);
                return;
            }
        } else {
            return;
        }

        rebalanceInsertion(added);
        root.setColor(NodeColor.BLACK);
    }

    private void rebalanceInsertion(MyNode<T> node) {
        if (node.getParent() == null) {
            return;
        }
        if (node.getParent().getColor() == NodeColor.BLACK) {
            return;
        }

        MyNode<T> uncle = node.uncle();
        if (uncle != null && uncle.getColor() == NodeColor.RED) {
            node.getParent().setColor(NodeColor.BLACK);
            uncle.setColor(NodeColor.BLACK);

            MyNode<T> gpr = node.grandParent();
            if (gpr != null && !gpr.isRoot()) {
                gpr.setColor(NodeColor.RED);
                rebalanceInsertion(gpr);
            }
        } else {
            if (node.parentDirection() == NodeDirection.LEFT && node.getParent().parentDirection() == NodeDirection.RIGHT) {
                rotateLeft(node.getParent());
                node = node.getLeft();
            } else if (node.parentDirection() == NodeDirection.RIGHT && node.getParent().parentDirection() == NodeDirection.LEFT) {
                rotateRight(node.getParent());
                node = node.getRight();
            }

            node.getParent().setColor(NodeColor.BLACK);
            if (node.grandParent() == null) {
                return;
            }

            node.grandParent().setColor(NodeColor.RED);
            if (node.parentDirection() == NodeDirection.RIGHT) {
                rotateRight(node.grandParent());
            } else {
                rotateLeft(node.grandParent());
            }
        }
    }

    private void rebalanceDeletion(MyNode<T> node) {
        while (node != root && node != null && node.getColor() == NodeColor.BLACK) {
            if (node.parentDirection() == NodeDirection.RIGHT) {
                MyNode<T> aux = node.getParent().getRight();
                if (aux != null && aux.getColor() == NodeColor.RED) {
                    aux.setColor(NodeColor.BLACK);
                    node.getParent().setColor(NodeColor.RED);
                    rotateLeft(node.getParent());
                    aux = node.getParent().getRight();
                }

                if (aux != null && aux.getLeft() != null && aux.getLeft().getColor() == NodeColor.BLACK && aux.getRight().getColor() == NodeColor.BLACK) {
                    aux.setColor(NodeColor.RED);
                    node = node.getParent();
                } else {
                    if (aux != null && aux.getRight() != null && aux.getRight().getColor() == NodeColor.BLACK) {
                        aux.getLeft().setColor(NodeColor.BLACK);
                        aux.setColor(NodeColor.RED);
                        rotateRight(aux);
                        aux = node.getParent().getRight();
                    }

                    if (aux != null) {
                        aux.setColor(node.getParent().getColor());
                    }
                    node.getParent().setColor(NodeColor.BLACK);
                    if (aux != null && aux.getRight() != null) {
                        aux.getRight().setColor(NodeColor.BLACK);
                    }
                    rotateLeft(node.getParent());
                    node = root;
                }
            } else {
                MyNode<T> aux = node.getParent().getLeft();
                if (aux.getColor() == NodeColor.RED) {
                    aux.setColor(NodeColor.BLACK);
                    node.getParent().setColor(NodeColor.RED);
                    rotateRight(node.getParent());
                    aux = node.getParent().getLeft();
                }

                if (aux != null && aux.getLeft() != null && aux.getLeft().getColor() == NodeColor.BLACK && aux.getRight() != null && aux.getRight().getColor() == NodeColor.BLACK) {
                    aux.setColor(NodeColor.RED);
                    node = node.getParent();
                } else {
                    if (aux != null && aux.getLeft() != null && aux.getLeft().getColor() == NodeColor.BLACK) {
                        if (aux.getRight() != null) {
                            aux.getRight().setColor(NodeColor.BLACK);
                        }
                        aux.setColor(NodeColor.RED);
                        rotateRight(aux);
                        aux = node.getParent().getLeft();
                    }

                    if (aux != null) {
                        aux.setColor(node.getParent().getColor());
                    }
                    node.getParent().setColor(NodeColor.BLACK);
                    if (aux != null && aux.getLeft() != null) {
                        aux.getLeft().setColor(NodeColor.BLACK);
                    }
                    rotateRight(node.getParent());
                    node = root;
                }
            }
        }
        if (node != null) {//
            node.setColor(NodeColor.BLACK);
        }
    }

    private void rotateLeft(MyNode<T> node) {
        MyNode<T> pivot = node.getRight();
        if (pivot == null) {
            return;
        }
        NodeDirection dir = node.parentDirection();
        MyNode<T> prnt = node.getParent();
        MyNode<T> tmp = pivot.getLeft();
        pivot.setLeft(node);
        node.setParent(pivot);
        node.setRight(tmp);

        if (tmp != null) {
            tmp.setParent(node);
        }

        if (dir == NodeDirection.LEFT) {
            prnt.setRight(pivot);
        } else if (dir == NodeDirection.RIGHT) {
            prnt.setLeft(pivot);
        } else {
            root = pivot;
        }

        pivot.setParent(prnt);
    }

    private void rotateRight(MyNode<T> node) {
        MyNode<T> pivot = node.getLeft();
        if(pivot == null) {
            return;
        }
        NodeDirection dir = node.parentDirection();
        MyNode<T> prnt = node.getParent();
        MyNode<T> tmp = pivot.getRight();
        pivot.setRight(node);
        node.setParent(pivot);
        node.setLeft(tmp);

        if (tmp != null) {
            tmp.setParent(node);
        }

        if (dir == NodeDirection.LEFT) {
            prnt.setRight(pivot);
        } else if (dir == NodeDirection.RIGHT) {
            prnt.setLeft(pivot);
        } else {
            root = pivot;
        }

        pivot.setParent(prnt);
    }
}

