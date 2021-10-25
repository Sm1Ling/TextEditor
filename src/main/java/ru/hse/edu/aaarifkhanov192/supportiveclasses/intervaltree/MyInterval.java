package ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;

public record MyInterval(int start, int stop) {
    public boolean isOverlaps(MyInterval interval) {
        return (this.start <= interval.stop && interval.start <= this.stop);
    }

    @Override
    public String toString() {
        return "(" + start + ", " + stop + ')';
    }
}
