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
import javafx.stage.Screen;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.controllers.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.controllers.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.lexer.Java9Lexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.Debouncer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.SettingsClass;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.TextCanvas;

import java.io.ByteArrayInputStream;
import java.util.*;


public class MainAppController {

    @FXML
    private TreeView<String> treeView;
    @FXML
    private javafx.scene.canvas.Canvas myCanvas;
    @FXML
    private ScrollBar hScroll;
    @FXML
    private ScrollBar vScroll;


    // region Общие поля
    private SettingsClass settingsClass = new SettingsClass();
    private DirectoryTree dt = new DirectoryTree(".");

    //Координаты центра пространства отображаемого в canvas
    float centerX;
    float centerY;

    //ширина и высота ячейки буквы
    float letterWidth;
    float lineHeight;
    //коэффициенты увеличения разрешения изображения
    float screenScaleX;
    float screenScaleY;
    //размеры рендеримой картинки
    float screenHeight;
    float screenWidth;
    //номер строки и символа, на который указывает курсор
    int coursorX;
    int coursorY;
    //информация о максимальном размере строки и количестве строк в canvas
    public float maxLineLength;
    public float linesCount;
    //окно активного текст
    TextCanvas activeCanvas;
    //список окон с текстами
    List<TextCanvas> canWindows = new ArrayList<>();
    // endregion



    Debouncer<Integer> wordPrint = new Debouncer<Integer>(this::shortTextPrint,500);

    //TODO сделать отдельный класс с графическими настройками (или добавить их в класс с настройками)
    //TODO сделать класс-содержатель информации для отдельных подокон с текстами

    @FXML
    private void initialize() {

        Screen screen = Screen.getPrimary();
        screenScaleX = (float)screen.getOutputScaleX();
        screenScaleY = (float)screen.getOutputScaleY();

        screenHeight = (float)myCanvas.getHeight()*screenScaleY;
        screenWidth = (float)myCanvas.getWidth()*screenScaleX;

        settingsClass.mainFont.setSize(settingsClass.mainFont.getSize()*screenScaleY);

        lineHeight = (-settingsClass.mainFont.getMetrics().getTop()
                + settingsClass.mainFont.getMetrics().getBottom());
        letterWidth = TextLine.make("x", settingsClass.mainFont).getWidth();
        System.out.println(letterWidth);

        settingsClass.startYPosition = settingsClass.startYPosition*(float)screenScaleY;
        settingsClass.startXPosition = settingsClass.startXPosition*(float)screenScaleX;

        maxLineLength = ((float)screenWidth - settingsClass.startXPosition)/letterWidth;
        linesCount = (float)screenHeight/lineHeight;

        hScroll.setVisible(false);
        vScroll.setVisible(false);
        myCanvas.setVisible(false);

        DirectoryResult r = dt.fillRoot();
        treeView.setOnMouseClicked(mouseEvent -> {
            String path = dt.getPathToTappedFile(mouseEvent, treeView);
            if(path != null) {
                createPannel(dt.readText(path), path);
            }
        });
        treeView.setRoot(r.rootTreeNode);
    }

    private void createPannel(String text, String path){
        activeCanvas = new TextCanvas();
        canWindows.add(activeCanvas);

        activeCanvas.text = Rope.BUILDER.build(text);
        activeCanvas.filePath = path;

        if(canWindows.size() == 1){
            myCanvas.setVisible(true);
            hScroll.setVisible(true);
            vScroll.setVisible(true);

            //TODO Рассмотреть переопределение всех событий на поля активного окна
            myCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, canvasMouseClicked);
            vScroll.valueProperty().addListener(vScrollListener);
            hScroll.valueProperty().addListener(hScrollListener);
        }

        centerX = 0;
        centerY = 0;

        vScroll.setMin(0);
        vScroll.setVisibleAmount(screenHeight);

        hScroll.setMin(0);
        hScroll.setVisibleAmount(screenWidth);

        runLexer();
        render();
    }

    private void switchPannel(int i){
        //TODO не забыть поменять позицию относительно скролла
    }

    private void shortTextPrint(Integer x){
        fillTextFromBuffer();
        render();
    }

    private boolean fillTextFromBuffer(){
        if(!activeCanvas.textBlock.getText().isEmpty()) {
            activeCanvas.text = activeCanvas.text.append(activeCanvas.textBlock.getText().toString());
            activeCanvas.textBlock.clear();
            return true;
        }
        return false;
    }

    @FXML
    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            fillTextFromBuffer();
            activeCanvas.text = activeCanvas.text.append("\n");
        }
        else if (keyEvent.getCode() == KeyCode.BACK_SPACE){
            fillTextFromBuffer();

            activeCanvas.text = activeCanvas.text.delete(
                    activeCanvas.text.length()-1,activeCanvas.text.length());
        }
        else if(keyEvent.getCode() == KeyCode.V && keyEvent.isShortcutDown()){
            System.out.println("Here should be Ctrl V Handler");
        }
        else if(keyEvent.getCode() == KeyCode.SPACE){
            fillTextFromBuffer();
            activeCanvas.text = activeCanvas.text.append(" ");
        }
        else {
            var c = keyEvent.getText();
            if(keyEvent.isShiftDown()){
                c = c.toUpperCase(Locale.ROOT);
            }
            if(!activeCanvas.textBlock.isFilled()){ // запоминаю место с которого начинает писаться минибуфер
                activeCanvas.textBlock.setCharNum(activeCanvas.coursorX);
                activeCanvas.textBlock.setLineNum(activeCanvas.coursorY+1);
            }
            activeCanvas.textBlock.getText().append(c);
            wordPrint.call(1);
            //TODO отрисовка rope+textBlock  (с учетом местоположения textBlock)
            render();
            return;
        }

        runLexer();
        render();
    }

    //region Render Block

    private void runLexer() {
        Java9Lexer lexer = new Java9Lexer(new ANTLRInputStream(activeCanvas.text.toString()));
        activeCanvas.tokens = lexer.getAllTokens().stream().toList();
    }

    // TODO Переделать
    private void render() {
        var data = Objects.requireNonNull(makeImage().encodeToData()).getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        myCanvas.getGraphicsContext2D().drawImage(img, 0, 0, myCanvas.getWidth(), myCanvas.getHeight());
    }

    /** Сомневаюсь в потребности этого метода
    private void renderLine(int lineBegin, int lineEnd) {
        var data = Objects.requireNonNull(makeLineImage(lineBegin,lineEnd).encodeToData()).getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        float ybegin = (lineBegin*lineHeight + settingsClass.mainFont.getMetrics().getTop()
                + settingsClass.startYPosition-centerY) / screenScaleY;
        float yend = (lineEnd*lineHeight + settingsClass.mainFont.getMetrics().getBottom()
                + settingsClass.startYPosition-centerY) / screenScaleY;
        System.out.println(ybegin + " " + yend);
        myCanvas.getGraphicsContext2D().clearRect(0, ybegin, myCanvas.getWidth(),yend);
        myCanvas.getGraphicsContext2D().drawImage(img, 0, ybegin, myCanvas.getWidth(),yend);
    }*/

    /**
    private Image makeLineImage(int lineBegin, int lineEnd) {
        Surface surface = Surface.makeRasterN32Premul((int)(screenWidth),(int)(lineHeight*(lineEnd-lineBegin+1)));
        Canvas canvas = surface.getCanvas();
        Paint paint = settingsClass.mainColor;

        float x = settingsClass.startXPosition - centerX;
        float y = settingsClass.startYPosition;

        int charIter = 0;
        int mchar = 0;
        int lineNumber = 1;

        var font = settingsClass.mainFont;
        for (int i = 0; i < activeCanvas.text.length(); i++) {

            char c = activeCanvas.text.charAt(i);
            if (c == '\n') {
                if(lineNumber == lineEnd){
                    break;
                }
                if(lineNumber >= lineBegin){
                    y += lineHeight;
                    x = settingsClass.startXPosition  - centerX;
                    charIter = 0;
                }
                lineNumber += 1;
            }
            else if(lineNumber > lineBegin) {
                if (lineNumber == activeCanvas.coursorY){
                    if(activeCanvas.coursorX == charIter){
                        var textLine = TextLine.make(activeCanvas.textBlock.getText().toString(), font);
                        canvas.drawTextLine(textLine, x, y, paint);
                        System.out.println(textLine.getWidth());
                        x += textLine.getWidth() + 1;
                        charIter += activeCanvas.textBlock.getText().length();
                    }
                }
                var textLine = TextLine.make(String.valueOf(c), font);
                canvas.drawTextLine(textLine, x, y, paint);
                x += textLine.getWidth() + 1;
                charIter += 1;
                mchar = Math.max(mchar, charIter);
            }
        }

        x = settingsClass.startXPosition - centerX + activeCanvas.coursorX*letterWidth;
        y = settingsClass.startYPosition - centerY + (activeCanvas.coursorY-lineBegin)*lineHeight;


        canvas.drawLine(x,y+font.getMetrics().getDescent(),
                x,y+font.getMetrics().getAscent(),settingsClass.coursorColor);

        hScroll.setMax(Math.max(maxLineLength,mchar)*letterWidth + settingsClass.startXPosition);

        return surface.makeImageSnapshot();
    }*/


    //TODO Переделать с учетом конкретной области
    private Image makeImage() {
        Surface surface = Surface.makeRasterN32Premul((int)(screenWidth),(int)(screenHeight));

        Canvas canvas = surface.getCanvas();

        Paint paint = settingsClass.mainColor;

        float x = settingsClass.startXPosition - centerX;
        float y = settingsClass.startYPosition - centerY;

        int charIter = 0;
        int lineNum = 1;

        int mchar = 0;
        boolean isTextBlockWritten = false;

        var font = settingsClass.mainFont;


        for (int i = 0; i < activeCanvas.text.length(); i++) {

            char c = activeCanvas.text.charAt(i);
            if (c != '\n') {
                if(lineNum == activeCanvas.textBlock.getLineNum() &&
                        charIter == activeCanvas.textBlock.getCharNum()){
                    var textLine = TextLine.make(activeCanvas.textBlock.getText().toString(),font);
                    canvas.drawTextLine(textLine,x,y,paint);
                    x += textLine.getWidth();
                    charIter += activeCanvas.textBlock.getText().length();
                    activeCanvas.coursorX = charIter;
                    isTextBlockWritten = true;
                }
                var textLine = TextLine.make(String.valueOf(c), font);
                canvas.drawTextLine(textLine, x, y, paint);
                x += textLine.getWidth();
                charIter += 1;
                mchar = Math.max(mchar,charIter);
            }
            else {
                if(!isTextBlockWritten && lineNum == activeCanvas.textBlock.getLineNum()){
                    var textLine = TextLine.make(activeCanvas.textBlock.getText().toString(),font);
                    canvas.drawTextLine(textLine,x,y,paint);
                    charIter += activeCanvas.textBlock.getText().length();
                    activeCanvas.coursorX = charIter;
                }
                y += lineHeight;
                x = settingsClass.startXPosition  - centerX;
                charIter = 0;
                lineNum += 1;
            }
        }

        x = settingsClass.startXPosition - centerX + activeCanvas.coursorX*letterWidth;
        y = settingsClass.startYPosition - centerY + activeCanvas.coursorY*lineHeight;

        canvas.drawLine(x,y+font.getMetrics().getDescent(),
                x,y+font.getMetrics().getAscent(),settingsClass.coursorColor);

        vScroll.setMax(Math.max(linesCount,lineNum)*lineHeight);
        hScroll.setMax(Math.max(maxLineLength,mchar)*letterWidth + settingsClass.startXPosition);

        return surface.makeImageSnapshot();
    }

    //endregion Render Block

    //region listeners Block

    private final EventHandler<MouseEvent> canvasMouseClicked = new javafx.event.EventHandler<>(){
        @Override
        public void handle(MouseEvent mouseEvent) {
            myCanvas.requestFocus();

            if(fillTextFromBuffer()){
                runLexer();
            }

            activeCanvas.coursorX = (int) Math.round((mouseEvent.getX() * screenScaleX
                    + centerX - settingsClass.startXPosition)/letterWidth); //номер буквы в строке
            activeCanvas.coursorY = (int) Math.round((mouseEvent.getY() * screenScaleY
                    + 1 + lineHeight/2 + centerY - settingsClass.startYPosition)/lineHeight); //номер строки

            render();
        }
    };

    private final ChangeListener<Number> hScrollListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
            centerX = (float)(newVal.doubleValue() * (hScroll.getMax() - screenWidth)/hScroll.getMax());
            activeCanvas.scrollPositionX = newVal.floatValue();
            render();
        }
    };

    private final ChangeListener<Number> vScrollListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
            centerY = (float) (newVal.doubleValue() * (vScroll.getMax() - screenHeight) / vScroll.getMax());
            activeCanvas.scrollPositionY = newVal.floatValue();
            render();
        }
    };


    //endregion
}