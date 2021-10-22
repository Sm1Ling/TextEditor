package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.controllers.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.controllers.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.lexer.Java9Lexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.Debouncer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.SettingsClass;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;


public class MainAppController {

    @FXML
    private TreeView<String> treeView;
    @FXML
    private javafx.scene.canvas.Canvas myCanvas;
    @FXML
    private ScrollBar hScroll;
    @FXML
    private ScrollBar vScroll;


    private Rope text;

    private StringBuffer textBlock = new StringBuffer(32);
    Debouncer<Integer> wordPrint = new Debouncer<Integer>(this::shortTextPrint,500);

    private List<? extends Token> tokens;

    private SettingsClass settingsClass = new SettingsClass();

    float maxLineLength;
    float linesCount;

    //Координаты центра пространства отображаемого в canvas
    float centerX;
    float centerY;

    float letterWidth;
    float lineHeight;



    double screenScaleX;
    double screenScaleY;

    double screenHeight;
    double screenWidth;

    //TODO сделать отдельный класс с графическими настройками (или добавить их в класс с настройками)

    @FXML
    private void initialize() {
        DirectoryTree dt = new DirectoryTree("D:\\Projects\\AndroidStudioProjects\\advanced-2021-architecture-1\\apikey.properties\\");
        DirectoryResult r = dt.fillRoot();
        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                dt.getPathToTappedFile(mouseEvent, treeView);
            }
        });
        treeView.setRoot(r.rootTreeNode);

        Screen screen = Screen.getPrimary();
        screenScaleX = screen.getOutputScaleX();
        screenScaleY = screen.getOutputScaleY();

        screenHeight = myCanvas.getHeight()*screenScaleY;
        screenWidth = myCanvas.getWidth()*screenScaleX;

        settingsClass.mainFont.setSize((float)(settingsClass.mainFont.getSize()*screenScaleY));

        lineHeight = (-settingsClass.mainFont.getMetrics().getAscent()
                + settingsClass.mainFont.getMetrics().getDescent()
                + settingsClass.mainFont.getMetrics().getLeading());
        letterWidth = TextLine.make("x", settingsClass.mainFont).getWidth() + 1;

        settingsClass.startYPosition = settingsClass.startYPosition*(float)screenScaleY;
        settingsClass.startXPosition = settingsClass.startXPosition*(float)screenScaleX;

        maxLineLength = ((float)screenWidth - settingsClass.startXPosition)/letterWidth;
        linesCount = (float)screenHeight/lineHeight;


        displayText("private void render() {\n" +
                "        myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());\n" +
                "        var data = Objects.requireNonNull(makeImageWithSkija().encodeToData()).getBytes();\n" +
                "        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));\n" +
                "        myCanvas.getGraphicsContext2D().drawImage(img, 0, 0, myCanvas.getWidth(), myCanvas.getHeight());\n" +
                "    }");


        vScroll.valueProperty().addListener((ov, oldVal, newVal) -> {
            centerY = (float) (newVal.doubleValue() * (vScroll.getMax() - screenHeight) / vScroll.getMax());
            render();
        });

        hScroll.valueProperty().addListener((ov, oldVal, newVal) -> {
            centerX = (float)(newVal.doubleValue() * (hScroll.getMax() - screenWidth)/hScroll.getMax());
            render();
        });
    }

    private void displayText(String txt){
        text = Rope.BUILDER.build(txt);

        centerX = 0;
        centerY = 0;

        vScroll.setMin(0);
        vScroll.setVisibleAmount(screenHeight);

        hScroll.setMin(0);
        hScroll.setVisibleAmount(screenWidth);

        runLexer();
        render();

        myCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> myCanvas.requestFocus());


    }


    private void shortTextPrint(Integer x){
        fillTextFromBuffer();
        runLexer();
        render();
    }

    private void fillTextFromBuffer(){
        if(!textBlock.isEmpty()) {
            text = text.append(textBlock.toString());
            textBlock.delete(0,textBlock.length());
        }
    }

    @FXML
    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            fillTextFromBuffer();
            text = text.append("\n");
        }
        else if (keyEvent.getCode() == KeyCode.BACK_SPACE){
            fillTextFromBuffer();
            text = text.delete(text.length()-1,text.length());
        }
        else if(keyEvent.getCode() == KeyCode.V && keyEvent.isShortcutDown()){
            System.out.println("Here should be Ctrl V Handler");
        }
        else if(keyEvent.getCode() == KeyCode.SPACE){
            fillTextFromBuffer();
            text = text.append(" ");
        }
        else {
            var c = keyEvent.getText();
            if(keyEvent.isShiftDown()){
                c = c.toUpperCase(Locale.ROOT);
            }
            textBlock.append(c);
            wordPrint.call(1);
            //TODO приделать отрисовку букв в конце текста
            System.out.println(textBlock);
            return;
        }

        runLexer();
        render();
    }

    private void runLexer() {
        Java9Lexer lexer = new Java9Lexer(new ANTLRInputStream(text.toString()));
        tokens = lexer.getAllTokens().stream().toList();
    }

    // TODO Переделать
    private void render() {
        myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        var data = Objects.requireNonNull(makeImageWithSkija().encodeToData()).getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        myCanvas.getGraphicsContext2D().drawImage(img, 0, 0, myCanvas.getWidth(), myCanvas.getHeight());
    }

    //TODO Переделать с учетом конкретной области
    private Image makeImageWithSkija() {
        Surface surface = Surface.makeRasterN32Premul((int)(screenWidth),(int)(screenHeight));

        Canvas canvas = surface.getCanvas();

        Paint paint = settingsClass.mainColor;

        double x = settingsClass.startXPosition - centerX;
        double y = settingsClass.startYPosition - centerY;

        int charIter = 0;
        int lineNum = 1;

        int mchar = 0;

        var font = settingsClass.mainFont;
        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);
            if (c != '\n') {
                var textLine = TextLine.make(String.valueOf(c), font);
                canvas.drawTextLine(textLine, (float)x, (float)y, paint);
                x += textLine.getWidth()+1;
                charIter += 1;
                mchar = Math.max(mchar,charIter);
            }
            else {
                y += lineHeight;
                x = settingsClass.startXPosition  - centerX;
                charIter = 0;
                lineNum += 1;
            }
        }

        vScroll.setMax(Math.max(linesCount,lineNum)*lineHeight);
        hScroll.setMax(Math.max(maxLineLength,mchar)*letterWidth + settingsClass.startXPosition);

        paint = new Paint().setColor(0xFF00FF00);
        canvas.drawLine(0,0,(float)screenWidth,0, paint);
        canvas.drawLine(0,0,0,(float)screenHeight, paint);
        canvas.drawLine((float)screenWidth-1,0,(float)screenWidth-1,(float)screenHeight, paint);
        canvas.drawLine(0,(float)screenHeight-1,(float)screenWidth,(float)screenHeight-1, paint);

        return surface.makeImageSnapshot();
    }
}