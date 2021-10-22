package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class Interval implements Comparable<Interval> {

    private long start;
    private long end;
    private Token token;

    public Interval(long start, long end, Token token) {
        this.start = start;
        this.end = end;
        this.token = token;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean contains(long time) {
        return time < end && time > start;
    }

    public boolean intersects(Interval other) {
        return other.getEnd() > start && other.getStart() < end;
    }

    public int compareTo(Interval other) {
        if (start < other.getStart())
            return -1;
        else if (start > other.getStart())
            return 1;
        else if (end < other.getEnd())
            return -1;
        else if (end > other.getEnd())
            return 1;
        else
            return 0;
    }

}

class Node {

    private final SortedMap<Interval, List<Interval>> intervals;
    private long center;
    private Node leftNode;
    private Node rightNode;

    public Node() {
        intervals = new TreeMap<>();
        center = 0;
        leftNode = null;
        rightNode = null;
    }



    public Node(List<Interval> intervalList) {
        intervals = new TreeMap<>();

        SortedSet<Long> endpoints = new TreeSet<>();
        for (Interval interval : intervalList) {
            endpoints.add(interval.getStart());
            endpoints.add(interval.getEnd());
        }


        long median = getMedian(endpoints);
        center = median;

        List<Interval> left = new ArrayList<>();
        List<Interval> right = new ArrayList<>();

        for (Interval interval : intervalList) {
            if (interval.getEnd() < median)
                left.add(interval);
            else if (interval.getStart() > median)
                right.add(interval);
            else {
                List<Interval> posting = intervals.get(interval);
                if (posting == null) {
                    posting = new ArrayList<>();
                    intervals.put(interval, posting);
                }
                posting.add(interval);
            }
        }

        if (left.size() > 0)
            leftNode = new Node(left);
        if (right.size() > 0)
            rightNode = new Node(right);
    }

    public List<Interval> stab(long time) {
        List<Interval> result = new ArrayList<>();

        for (Entry<Interval, List<Interval>> entry : intervals
                .entrySet()) {
            if (entry.getKey().contains(time))
                result.addAll(entry.getValue());
            else if (entry.getKey().getStart() > time)
                break;
        }

        if (time < center && leftNode != null)
            result.addAll(leftNode.stab(time));
        else if (time > center && rightNode != null)
            result.addAll(rightNode.stab(time));
        return result;
    }

    public List<Interval> query(Interval target) {
        List<Interval> result = new ArrayList<>();

        for (Entry<Interval, List<Interval>> entry : intervals
                .entrySet()) {
            if (entry.getKey().intersects(target))
                result.addAll(entry.getValue());
            else if (entry.getKey().getStart() > target.getEnd())
                break;
        }

        if (target.getStart() < center && leftNode != null)
            result.addAll(leftNode.query(target));
        if (target.getEnd() > center && rightNode != null)
            result.addAll(rightNode.query(target));
        return result;
    }

    public long getCenter() {
        return center;
    }

    public void setCenter(long center) {
        this.center = center;
    }

    public Node getLeft() {
        return leftNode;
    }

    public void setLeft(Node left) {
        this.leftNode = left;
    }

    public Node getRight() {
        return rightNode;
    }

    public void setRight(Node right) {
        this.rightNode = right;
    }

    private Long getMedian(SortedSet<Long> set) {
        int i = 0;
        int middle = set.size() / 2;
        for (Long point : set) {
            if (i == middle)
                return point;
            i++;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(center + ": ");
        for (Entry<Interval, List<Interval>> entry : intervals
                .entrySet())
        {
            sb.append("[" + entry.getKey().getStart() + ","
                    + entry.getKey().getEnd() + "]:{");
            for (Interval interval : entry.getValue())
            {
                sb.append("(" + interval.getStart() + "," + interval.getEnd()
                        + "," + interval.getToken() + ")");
            }
            sb.append("} ");
        }
        return sb.toString();
    }

}

class IntervalTree {

    private Node head;
    private final List<Interval> intervalList;
    private boolean inSync;
    private int size;

    public IntervalTree() {
        this.head = new Node();
        this.intervalList = new ArrayList<>();
        this.inSync = true;
        this.size = 0;
    }

    public IntervalTree(List<Interval> intervalList) {
        this.head = new Node(intervalList);
        this.intervalList = new ArrayList<>();
        this.intervalList.addAll(intervalList);
        this.inSync = true;
        this.size = intervalList.size();
    }

    public Token get(long time) {//List<Long>
        for (var i:
                intervalList) {
            if(i.getStart() <= time && i.getEnd() >= time)
                return i.getToken();
        }
        return null;
//        List<Interval> intervals = getIntervals(time);
//        List<Long> result = new ArrayList<>();
//        for (Interval interval : intervals)
//            result.add(interval.getToken());
//        return result;
    }

    public List<Interval> getIntervals(long time) {
        build();
        return head.stab(time);
    }

    public List<Token> get(long start, long end) {
        List<Interval> intervals = getIntervals(start, end);
        List<Token> result = new ArrayList<>();
        for (Interval interval : intervals)
            result.add(interval.getToken());
        return result;
    }

    public List<Interval> getIntervals(long start, long end) {
        build();
        return head.query(new Interval(start, end, null));
    }

    public void addInterval(Interval interval) {
        intervalList.add(interval);
        inSync = false;
    }

    //Когда идут [0, 1], [0, 2], [0, 3] -> [0, 3], следует ли заменить координату
    private boolean getWholeNode(List<Interval> lI, Long end) {
        return lI.get(lI.size() - 1).getEnd() < end;
    }

    public boolean addInterval(long begin, long end, Token token) {
        boolean tokenChanged = false;
        //Группируем по стартам и сортируем, чтобы были [0, 1], [0, 2], [0, 3]
        Map<Long, List<Interval>> mapGroupedByStarts =  intervalList.stream()
                .collect(Collectors.groupingBy(Interval::getStart));

        var mpL = mapGroupedByStarts.get(begin);
        if(mpL != null && getWholeNode(mpL, end)){
            var ind = intervalList.indexOf(mpL.get(mpL.size() - 1));
            var t = intervalList.get(ind);
            t.setEnd(end);
            tokenChanged = token.getType() != t.getToken().getType();
            t.setToken(token);
            intervalList.set(ind, t);
        }
        else{
            intervalList.add(new Interval(begin, end, token));
        }

//        intervalList.add(new Interval(begin, end, token));
        inSync = false;

        return tokenChanged;
    }

    public boolean inSync() {
        return inSync;
    }

    public void build() {
        if (!inSync) {
            head = new Node(intervalList);
            inSync = true;
            size = intervalList.size();
        }
    }

    public int currentSize() {
        return size;
    }

    public int listSize() {
        return intervalList.size();
    }

    @Override
    public String toString() {
        return nodeString(head, 0);
    }

    private String nodeString(Node node, int level)
    {
        if (node == null)
            return "";

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < level; i++)
            sb.append("\t");
        sb.append(node + "\n");
        sb.append(nodeString(node.getLeft(), level + 1));
        sb.append(nodeString(node.getRight(), level + 1));
        return sb.toString();
    }
}

