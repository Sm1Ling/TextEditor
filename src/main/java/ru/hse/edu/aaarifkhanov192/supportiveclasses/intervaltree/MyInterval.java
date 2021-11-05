package ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;

public record MyInterval(int start, int stop) implements Comparable<MyInterval> {
    public boolean isOverlaps(MyInterval interval) {
        return (this.start <= interval.stop && interval.start <= this.stop);
    }

    public boolean isOverlaps(int start, int stop){
        return (this.start <= stop && start <= this.stop);
    }

    public boolean isOverlaps(int point){
        return (this.start <= point && point <= this.stop);
    }

    @Override
    public String toString() {
        return "(" + start + ", " + stop + ')';
    }


    @Override
    public int compareTo(MyInterval o) {
        if (start < o.start) {
            return -1;
        } else if (start > o.start) {
            return 1;
        } else if (stop < o.stop) {
            return 1;
        } else if (stop > o.stop) {
            return -1;
        }
        return 0;
    }
}
