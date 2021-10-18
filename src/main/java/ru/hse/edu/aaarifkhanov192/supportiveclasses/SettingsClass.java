package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import org.jetbrains.skija.*;

import java.util.HashMap;
import java.util.Map;

public class SettingsClass {
    public final Font mainFont = new Font(Typeface.makeFromName("Consolas", FontStyle.NORMAL));;
    public final Paint mainColor = new Paint().setColor(0x000000);

    public final Map<String,Integer> colorMap = new HashMap<>();

    public final Integer undoRedoSize = 50;

    public SettingsClass(){
        // TODO Сюда добавить обработку разных подсветок
        colorMap.put(null,null);
    }
}
