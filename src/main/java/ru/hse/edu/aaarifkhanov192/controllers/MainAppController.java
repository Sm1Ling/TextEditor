package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.controllers.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.controllers.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.lexer.Java9Lexer;
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


    private Rope text = Rope.BUILDER.build("private void render(Scene scene) {\n" +
            "        var canvas = (javafx.scene.canvas.Canvas) scene.getRoot().lookup(\"#myCanvas\");\n" +
            "        var gc = canvas.getGraphicsContext2D();\n" +
            "        gc.clearRect(0, 0, 320, 240);\n" +
            "        var data = makeImageWithSkija().encodeToData().getBytes();\n" +
            "        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));\n" +
            "        gc.drawImage(img, 0, 0);\n" +
            "    }");

    private List<? extends Token> tokens;

    private SettingsClass settingsClass = new SettingsClass();

    int maxLineLength;
    int linesCount;

    //Координаты центра пространства отображаемого в canvas
    float centerX;
    float centerY;

    float letterWidth;
    float lineHeight;



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

        lineHeight = -settingsClass.mainFont.getMetrics().getAscent()
                + settingsClass.mainFont.getMetrics().getDescent()
                + settingsClass.mainFont.getMetrics().getLeading();
        //TODO Сделать норм ширину буквы (8 = 7ширина буквы + 1 как пропуск)
        letterWidth = 8;

        maxLineLength = Math.round(((float)myCanvas.getWidth() - settingsClass.startXPosition)/letterWidth);
        linesCount = Math.round((float)myCanvas.getHeight()/lineHeight);
        System.out.println(maxLineLength);
        System.out.println(linesCount);


        runLexer();
        render();

        vScroll.setMin(0);
        vScroll.setVisibleAmount(myCanvas.getHeight());

        hScroll.setMin(0);
        hScroll.setVisibleAmount(myCanvas.getWidth());

        myCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> myCanvas.requestFocus());

        centerX = 0;
        centerY = 0;



        vScroll.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                centerY = (float)(new_val.doubleValue()
                        *(vScroll.getMax() - myCanvas.getHeight())/vScroll.getMax());
                render();
            }
        });

        hScroll.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                centerX = (float)(new_val.doubleValue()
                        * (hScroll.getMax() - myCanvas.getWidth())/hScroll.getMax());
                render();
            }
        });
    }

    @FXML
    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            text = text.append("\n");
        }
        else if (keyEvent.getCode() == KeyCode.BACK_SPACE){
            text = text.delete(text.length()-1,text.length());
        }
        else if(keyEvent.getCode() == KeyCode.V && keyEvent.isShortcutDown()){
            System.out.println("Here should be Ctrl V Handler");
        }
        else {
            var c = keyEvent.getText();
            if(keyEvent.isShiftDown()){
                c = c.toUpperCase(Locale.ROOT);
            }
            text = text.append(c);
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
        var gc = myCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        var data = makeImageWithSkija().encodeToData().getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        gc.drawImage(img, 0, 0);


    }

    //TODO Переделать с учетом конкретной области
    private Image makeImageWithSkija() {
        Surface surface = Surface.makeRasterN32Premul((int)myCanvas.getWidth(), (int)myCanvas.getHeight());
        Canvas canvas = surface.getCanvas();

        Paint paint = settingsClass.mainColor;

        double x = settingsClass.startXPosition - centerX;
        double y = settingsClass.startYPosition - centerY;

        int charIter = 0;
        int lineNum = 1;

        int mchar = 0;

        var font = settingsClass.mainFont;
        for (int i = 0; i < text.length(); i++) {
            //Token currToken = null;
            //for (var token : tokens) {
            //    if (token.getStartIndex() <= i && token.getStopIndex() >= i) {
            //        currToken = token;
            //        break;
            //    }
            //}
//
            //if (currToken != null) {
            //    switch (currToken.getType()) {
            //        case 1 -> paint.setColor(0xAABB0000);
            //        case 2 -> paint.setColor(0xBBBBCC00);
            //        case 3 -> paint.setColor(0x55551100);
            //        case 4 -> paint.setColor(0x88683400);
            //    }
            //}

            char c = text.charAt(i);
            if (c != '\n') {
                var textLine = TextLine.make(String.valueOf(c), font);
                canvas.drawTextLine(textLine, (float)x, (float)y, paint);
                x += textLine.getWidth() + 1;
                charIter += 1;
                mchar = Math.max(mchar,charIter);
            }
            else {
                y += -font.getMetrics().getAscent() + font.getMetrics().getDescent() + font.getMetrics().getLeading();
                x = settingsClass.startXPosition  - centerX;
                charIter = 0;
                lineNum += 1;
            }

            vScroll.setMax(Math.max(linesCount,lineNum)*lineHeight);
            hScroll.setMax(letterWidth*Math.max(maxLineLength,mchar)+settingsClass.startXPosition);

        }

        paint = new Paint().setColor(0xFF00FF00);
        canvas.drawLine(0,0,(float)myCanvas.getWidth(),0, paint);
        canvas.drawLine(0,0,0,(float)myCanvas.getHeight(), paint);
        canvas.drawLine((float)myCanvas.getWidth(),0,(float)myCanvas.getWidth(),(float)myCanvas.getHeight(), paint);
        canvas.drawLine(0,(float)myCanvas.getHeight(),(float)myCanvas.getWidth(),(float)myCanvas.getHeight(), paint);

        return surface.makeImageSnapshot();
    }
}