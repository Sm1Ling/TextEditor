package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.Stack;


public class UndoRedoController {

    //Fist=Front=Head это начало очереди - [0].  Last=Rear=Tail это конец [-1] (вечно путаю)
    private final Deque<Action> undo;
    private final Deque<Action> redo;

    private final int queueSize;

    UndoRedoController(int queueSize){
        if (queueSize < 1){
            throw new IllegalArgumentException("Queue size must be no less than 1");
        }
        this.queueSize = queueSize;
        undo = new ArrayDeque<>(queueSize);
        redo = new ArrayDeque<>(queueSize);
    }

    public void addAction(Action act){
        if (undo.size() == queueSize){
            undo.removeFirst();
        }
        if (!redo.isEmpty()){
            redo.clear();
        }
        undo.addLast(Action.getReversedAction(act));
    }

    public boolean isCanUndo(){
        return !undo.isEmpty();
    }

    public boolean isCanRedo(){
        return !redo.isEmpty();
    }

    public Action undoAction(){
        if(isCanUndo()){
            redo.addLast(Action.getReversedAction(undo.getLast()));
            return undo.pollLast();
        }
        else {
            return null;
        }
    }

    public Action redoAction(){
        if(isCanRedo()){
            undo.addLast(Action.getReversedAction(redo.getLast()));
            return redo.pollLast();
        }
        else {
            return null;
        }
    }


}
