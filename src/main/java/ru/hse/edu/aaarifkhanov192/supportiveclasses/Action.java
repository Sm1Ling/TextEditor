package ru.hse.edu.aaarifkhanov192.supportiveclasses;



public class Action {
    private final int start;
    private final int end;
    private final String text;
    private final boolean isDelete;

    Action(int start, int end, String text, boolean isDelete) {
        this.start = start;
        this.end = end;
        this.text = text;
        this.isDelete = isDelete;
    }

    public static Action getReversedAction(Action act){
        return new Action(act.start, act.end, act.text, !act.isDelete);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public final String getText() {
        return text;
    }

    public final boolean isDelete() {
        return isDelete;
    }
}
