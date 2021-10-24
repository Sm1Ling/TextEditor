package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class TextCanvas {

    public int pannelNum;
    public Rope text;

    public List<? extends Token> tokens;

    public List<Integer> linesLengths = new ArrayList<>();

    public TextBuffer textBlock = new TextBuffer(32);

    public int coursorX;
    public int coursorY;

    public float scrollPositionY;
    public float scrollPositionX;

    public String filePath;




}
