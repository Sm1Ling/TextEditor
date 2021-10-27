package ru.hse.edu.aaarifkhanov192.supportiveclasses;

public class TextBuffer {
    private StringBuffer text;
    private int lineNum;
    private int charNum;

    public TextBuffer(){
        setText(new StringBuffer());
    }

    public TextBuffer(int bufferSize){
        setText(new StringBuffer(bufferSize));
    }

    public StringBuffer getText() {
        return text;
    }

    public void setText(StringBuffer text) {
        this.text = text;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getCharNum() {
        return charNum;
    }

    public void setCharNum(int charNum) {
        this.charNum = charNum;
    }

    public void clear(){
        text.delete(0,text.length());
        lineNum = -1;
        charNum = -1;
    }

    public boolean isFilled(){
        return lineNum >= 1 && charNum >= 0;
    }
}
