package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import org.jetbrains.skija.*;

import java.util.HashMap;
import java.util.Map;

public class SettingsClass {
    //TODO Класс создан для того, чтобы можно было менять настройки
    public final Font mainFont = new Font(Typeface.makeFromName("Consolas", FontStyle.NORMAL));;

    public Paint mainColor = new Paint().setColor(0xffA9B7C6); //opacity-red-green-blue
    public Paint lineColor = new Paint().setColor(0xff5e5e5e);
    public Paint coursorColor = new Paint().setColor(0xffffffff);
    public Paint selectionColor = new Paint().setColor(Color.makeRGB(33, 66, 131));

    public final Map<String,Paint> colorMap = new HashMap<String,Paint>(16);

    public final Integer undoRedoSize = 50;

    public float startXPosition = 2;
    public float startYPosition = mainFont.getMetrics().getHeight();

    public SettingsClass(){
        colorMap.put("program",new Paint().setColor(0xff7d6691));
        colorMap.put("var",new Paint().setColor(0xff7d6691));
        colorMap.put("begin",new Paint().setColor(0xff7d6691));
        colorMap.put("ident",new Paint().setColor(0xffA9B7C6));
        colorMap.put("keyword",new Paint().setColor(0xffc67530));
        colorMap.put("operation",new Paint().setColor(0xff82452b));
        colorMap.put("number",new Paint().setColor(0xff5e90b1));
        colorMap.put("comment",new Paint().setColor(0xff777c7c));
        colorMap.put("comma",new Paint().setColor(0xff82452b));
        colorMap.put("string",new Paint().setColor(0xff5b6c44));
        colorMap.put("parentheses",new Paint().setColor(0xffA9B7C6));
    }
}
