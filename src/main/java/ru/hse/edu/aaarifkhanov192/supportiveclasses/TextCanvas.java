package ru.hse.edu.aaarifkhanov192.supportiveclasses;


import javafx.util.Pair;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.Token;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;

import java.util.ArrayList;
import java.util.List;

public class TextCanvas {

    public int pannelNum;
    public Rope text;

    public MyIntervalTree<String> colorTree;
    public MyIntervalTree<String> highlight;

    //пары для подсчета символов в строке и итогового количества символов
    public List<Integer> linesLengths = new ArrayList<>();

    public float scrollPositionY;
    public float scrollPositionX;

    public final UndoRedoController  undoRedo = new UndoRedoController(20);

    public final CursorSupporter cursor = new CursorSupporter();

    public int editChars = 0;
    public volatile int startEditChars = -1;

}
