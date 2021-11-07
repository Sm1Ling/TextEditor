package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import java.util.List;

public class CursorSupporter {
    public int cursorX = 0;
    public int cursorY = 0;

    public int cursorOldX = -1;
    public int cursorOldY = -1;

    public int lettersToCursorOld = -1;
    public int lettersToCursorNew = -1;


    public void resetCursor(List<Integer> linesLengths) {
        cursorY = Math.min(cursorY, linesLengths.size() - 1);
        cursorY = Math.max(cursorY, 0);

        cursorX = Math.min(linesLengths.get(cursorY) - 1, cursorX);
        cursorX = Math.max(cursorX, 0);
    }

    public int[] lineCharToCursor(int letterNum, List<Integer> linesLengths){
        int lineNum = 0;
        int charNum = 0;
        for (int i = 0; i < linesLengths.size(); i++) {
            if(linesLengths.get(i) < letterNum){
                letterNum -= linesLengths.get(i);
                lineNum += 1;
            }
            else {
                charNum += letterNum;
                break;
            }
        }
        return new int[]{lineNum,charNum};
    }

    public int lettersToCursor(List<Integer> linesLength) {
        int letters = 0;
        for (int i = 0; i < linesLength.size(); i++) {  //activeCanvas.linesLengths.size()
            if (i != cursorY) {
                letters += linesLength.get(i);//activeCanvas.linesLengths.get(i);
            } else {
                letters += cursorX;
                return letters;
            }
        }
        return letters;
    }

    public void clipboardTextAnalyzer(String text) {
        String[] splitAr = text.split("\n");
        if (splitAr.length > 1) {
            cursorX += splitAr.length;
            cursorY = splitAr[splitAr.length - 1].length() - 1;
        } else {
            cursorX += splitAr[0].length();
        }
    }

    public void resetValues() {
        cursorOldX = -1;
        cursorOldY = -1;
        lettersToCursorOld = -1;
        lettersToCursorNew = -1;
    }
}