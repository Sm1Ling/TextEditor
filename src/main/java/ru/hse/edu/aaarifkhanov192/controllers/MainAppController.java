package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.lexer.PascalBaseListener;
import ru.hse.edu.aaarifkhanov192.lexer.PascalLexer;
import ru.hse.edu.aaarifkhanov192.lexer.PascalParser;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.*;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyInterval;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    @FXML
    private HBox tabbox;


    // region Общие поля
    private final SettingsClass settingsClass = new SettingsClass();
    private final DirectoryTree dt = new DirectoryTree(".");

    //Координаты центра пространства отображаемого в canvas
    private float centerX;
    private float centerY;

    //ширина и высота ячейки буквы
    private float letterWidth;
    private float lineHeight;
    //коэффициенты увеличения разрешения изображения
    private float screenScaleX;
    private float screenScaleY;
    //размеры рендеримой картинки
    private float screenHeight;
    private float screenWidth;
    //номер строки и символа, на который указывает курсор
    //информация о максимальном размере строки и количестве строк в canvas
    private float maxLineLength;
    private float linesCount;
    //окно активного текст
    private TextCanvas activeCanvas;
    //список окон с текстами
    private final List<TextCanvas> canWindows = new ArrayList<>();
    // endregion


    private final Debouncer<Integer> wordPrint = new Debouncer<Integer>(this::relex, 500);
    private final Debouncer<Integer> fileSaver = new Debouncer<>(this::saveFile, 30000);

    //TODO Разобраться с добавлением таб окон
    //TODO Возможно присобачить выделение
    //TODO Однажды тут будут тесты
    @FXML
    private void initialize() {

        Screen screen = Screen.getPrimary();
        screenScaleX = (float) screen.getOutputScaleX();
        screenScaleY = (float) screen.getOutputScaleY();

        screenHeight = (float) myCanvas.getHeight() * screenScaleY;
        screenWidth = (float) myCanvas.getWidth() * screenScaleX;

        settingsClass.mainFont.setSize(settingsClass.mainFont.getSize() * screenScaleY);

        lineHeight = (-settingsClass.mainFont.getMetrics().getTop()
                + settingsClass.mainFont.getMetrics().getBottom());
        letterWidth = TextLine.make("x", settingsClass.mainFont).getWidth();

        settingsClass.startYPosition = settingsClass.startYPosition * (float) screenScaleY;
        settingsClass.startXPosition = settingsClass.startXPosition * (float) screenScaleX;

        maxLineLength = (screenWidth - settingsClass.startXPosition) / letterWidth;
        linesCount = screenHeight / lineHeight;

        hScroll.setVisible(false);
        vScroll.setVisible(false);
        myCanvas.setVisible(false);

        DirectoryResult r = dt.fillRoot();
        treeView.setOnMouseClicked(mouseEvent -> {
            String path = dt.getPathToTappedFile(mouseEvent, treeView);
            if (path != null &&
                    canWindows.stream().noneMatch((o) -> o.filePath.equals(path))) {
                createPannel(dt.readText(path), path);
            }
        });

        treeView.setRoot(r.rootTreeNode);
    }

    private void createPannel(String text, String path) {
        activeCanvas = new TextCanvas();
        canWindows.add(activeCanvas);

        activeCanvas.text = Rope.BUILDER.build(text);
        activeCanvas.filePath = path;

        centerX = 0;
        centerY = 0;

        vScroll.setMin(0);
        vScroll.setVisibleAmount(screenHeight);

        hScroll.setMin(0);
        hScroll.setVisibleAmount(screenWidth);

        textAnalizer();
        runLexer();
        render();

        if (canWindows.size() == 1) {
            myCanvas.setVisible(true);
            hScroll.setVisible(true);
            vScroll.setVisible(true);

            myCanvas.setOnKeyTyped(onKeyTyped);
            myCanvas.setOnKeyPressed(onKeyPressed);
            myCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                    ctrlZPressed);
            myCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                    ctrlYPressed);
            myCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, canvasMouseClicked);
            vScroll.valueProperty().addListener(vScrollListener);
            hScroll.valueProperty().addListener(hScrollListener);
        }
    }

    private void switchPannel(int i) {
        //TODO не забыть поменять позицию относительно скролла
    }

    private void relex(Integer x) {
        runLexer();
        render();
    }

    private void saveFile(Integer x) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(activeCanvas.filePath), StandardCharsets.UTF_8))) {
            writer.write(activeCanvas.text.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runLexer() {

        PascalLexer lexer = new PascalLexer(new ANTLRInputStream(activeCanvas.text.toString()));
        activeCanvas.colorTree = new MyIntervalTree<>();
        activeCanvas.highlight = new MyIntervalTree<>();
        Thread tokenThread = new Thread(() -> TokenFiller.refillColorTree(lexer.getAllTokens(),activeCanvas.colorTree));

        try {
            tokenThread.start();
            //не сказал бы что есть нужда в больших стуктурах многопоточности. МБ стоит перекроить метод чтоб не было завала
            //lexer.reset();
            //var parser = new PascalParser(new CommonTokenStream(lexer));
            //parser.addParseListener(new PascalBaseListener(activeCanvas.highlight));
            //parser.program();

            tokenThread.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        finally{
            var tkn = activeCanvas.colorTree.getTree();
            activeCanvas.editChars = 0;
            activeCanvas.startEditChars = -1;
        }
    }



    private void textAnalizer() {
        int charIter = 0;

        activeCanvas.linesLengths = new ArrayList<>(Math.max(activeCanvas.linesLengths.size(), 64));

        for (Character character : activeCanvas.text) {
            if (character != '\n') {
                charIter += 1;
            }
            else {
                activeCanvas.linesLengths.add(charIter + 1);
                charIter = 0;
            }
        }
        activeCanvas.linesLengths.add(charIter);

        vScroll.setMax(Math.max(linesCount, activeCanvas.linesLengths.size()) * lineHeight);
        hScroll.setMax(Math.max(maxLineLength, Collections.max(activeCanvas.linesLengths))
                * letterWidth + settingsClass.startXPosition);
    }

    private int lettersToCursor() {
        int letters = 0;
        for (int i = 0; i < activeCanvas.linesLengths.size(); i++) {
            if (i != activeCanvas.coursorY) {
                letters += activeCanvas.linesLengths.get(i);
            } else {
                letters += activeCanvas.coursorX;
                return letters;
            }
        }
        return letters;
    }

    private void resetCursor() {
        activeCanvas.coursorY = Math.min(activeCanvas.coursorY, activeCanvas.linesLengths.size() - 1);
        activeCanvas.coursorY = Math.max(activeCanvas.coursorY, 0);

        activeCanvas.coursorX = Math.min(
                activeCanvas.linesLengths.get(activeCanvas.coursorY) - 1, activeCanvas.coursorX);
        activeCanvas.coursorX = Math.max(activeCanvas.coursorX, 0);
    }

    private boolean checkChar(char ch, int letNum, boolean isShiftDown){
        switch (ch) {
            case '\r' -> {
                activeCanvas.text = activeCanvas.text.insert(letNum, "\n");
                activeCanvas.coursorX = 0;
                activeCanvas.coursorY += 1;
                activeCanvas.undoRedo.addAction(new Action(letNum, letNum + 1, "\n", false));
                activeCanvas.editChars += 1;
            }
            case '\b' -> {
                if (activeCanvas.coursorY == 0 && activeCanvas.coursorX == 0) {
                    return false;
                }
                if (activeCanvas.coursorX != 0) {
                    activeCanvas.coursorX -= 1;
                } else {
                    activeCanvas.coursorX = activeCanvas.linesLengths.get(activeCanvas.coursorY - 1) - 1;
                    activeCanvas.coursorY -= 1;
                }
                activeCanvas.undoRedo.addAction(new Action(letNum - 1, letNum,
                        String.valueOf(activeCanvas.text.charAt(letNum - 1)), true));
                activeCanvas.text = activeCanvas.text.delete(
                        letNum - 1, letNum);
                activeCanvas.editChars -= 1;
            }
            case (char)127-> {
                if(letNum == activeCanvas.text.length()-1){
                    return false;
                }
                activeCanvas.undoRedo.addAction(new Action(letNum, letNum+1,
                        String.valueOf(activeCanvas.text.charAt(letNum)), true));
                activeCanvas.text = activeCanvas.text.delete(
                        letNum, letNum+1);
                activeCanvas.editChars -= 1;
            }
            case '\t' -> {
                activeCanvas.undoRedo.addAction(new Action(letNum, letNum + 4, "    ", false));
                activeCanvas.text = activeCanvas.text.insert(letNum, "    ");
                activeCanvas.coursorX += 4;
                activeCanvas.editChars += 4;
            }
            default -> {
                String c = String.valueOf(ch);
                if (isShiftDown) {
                    c = c.toUpperCase(Locale.ROOT);
                }
                activeCanvas.undoRedo.addAction(new Action(letNum, letNum + 1, c, false));
                activeCanvas.text = activeCanvas.text.insert(letNum, c);
                activeCanvas.coursorX += 1;
                activeCanvas.editChars += 1;
            }
        }
        return true;
    }

    //region Render Block

    private void render() {
        var data = Objects.requireNonNull(makeImage().encodeToData()).getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        myCanvas.getGraphicsContext2D().drawImage(img, 0, 0, myCanvas.getWidth(), myCanvas.getHeight());
    }

    private Image makeImage() {
        Surface surface = Surface.makeRasterN32Premul((int) (screenWidth), (int) (screenHeight));
        Canvas canvas = surface.getCanvas();

        float x = settingsClass.startXPosition - centerX;
        float y = settingsClass.startYPosition - centerY;

        var font = settingsClass.mainFont;
        int i = 0;

        int lettersToCur = lettersToCursor()-activeCanvas.editChars;

        var chosenInterval = activeCanvas.colorTree.getIntervals(lettersToCur);
        if(!chosenInterval.isEmpty() && activeCanvas.startEditChars != -1){
            activeCanvas.colorTree.delete(chosenInterval.get(0).getInterval()); // избавляюсь от цвета посреди слова
        }

        //TODO Доделать  корректный сдвиг цветов
        int temp = Math.min(activeCanvas.startEditChars, activeCanvas.startEditChars + activeCanvas.editChars);
        int holder = Math.max(0,activeCanvas.editChars);

        //var tree = activeCanvas.colorTree.getTree();
        //int treeI = 0;
        for (Character character : activeCanvas.text) {
            if (character != '\n') {
                var textLine = TextLine.make(String.valueOf(character), font);
                //region неэффективный способ
                 if(temp == -1 || i <= temp) {
                     drawText(i,x,y,canvas,textLine);
                 }
                 else {
                     drawText(i - activeCanvas.editChars + holder,x,y,canvas,textLine);
                     holder--;
                     holder = Math.max(holder, 0);
                 }
                //endregion
                x += textLine.getWidth();
            } else {
                y += lineHeight;
                x = settingsClass.startXPosition - centerX;
            }
            i++;
        }

        x = settingsClass.startXPosition - centerX + activeCanvas.coursorX * letterWidth;
        y = settingsClass.startYPosition - centerY + activeCanvas.coursorY * lineHeight;

        canvas.drawLine(x, y + font.getMetrics().getDescent(),
                x, y + font.getMetrics().getAscent(), settingsClass.coursorColor);

        return surface.makeImageSnapshot();
    }

    private void drawText(int i, float x, float y, Canvas canvas, TextLine textLine){
        var intervals =  activeCanvas.colorTree
                .getIntervals(i);

        if(intervals.isEmpty()){
            canvas.drawTextLine(textLine, x, y,
                    settingsClass.mainColor);
        }
        else {
            canvas.drawTextLine(textLine, x, y,
                    settingsClass.colorMap.get(intervals.get(0).getToken()));
        }
    }

    //endregion Render Block

    //region handlers Block

    private final EventHandler<KeyEvent> onKeyTyped = new javafx.event.EventHandler<>() {
        @Override
        public void handle(KeyEvent keyEvent) {
            int letNum = lettersToCursor();
                if (!keyEvent.isShortcutDown()) {

                    if (activeCanvas.startEditChars == -1) {
                        activeCanvas.startEditChars = letNum;
                    }

                    if(keyEvent.getCharacter().isEmpty()){
                        return;
                    }

                    String line = keyEvent.getCharacter();
                    for (var w: line.toCharArray()) {
                        if(!checkChar(w,letNum, keyEvent.isShiftDown())){
                            return;
                        }
                    }

                    wordPrint.call(1);
                    fileSaver.call(1);
                    textAnalizer();
                    resetCursor();
                    render();
                }
            }

    };

    private final EventHandler<KeyEvent> onKeyPressed = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent keyEvent) {
            if(keyEvent.getCode().isArrowKey()){
                switch(keyEvent.getCode()){
                    case LEFT -> {
                        if(activeCanvas.coursorX == 0 && activeCanvas.coursorY == 0){
                            return;
                        }
                        if (activeCanvas.coursorX != 0) {
                            activeCanvas.coursorX -= 1;
                        } else {
                            activeCanvas.coursorX = activeCanvas.linesLengths.get(activeCanvas.coursorY - 1) - 1;
                            activeCanvas.coursorY -= 1;
                        }
                    }
                    case RIGHT -> {
                        if (activeCanvas.coursorX == activeCanvas.linesLengths.get(activeCanvas.coursorY)-1) {
                            if(activeCanvas.coursorY == activeCanvas.linesLengths.size()-1){
                                return;
                            }
                            else{
                                activeCanvas.coursorY += 1;
                                activeCanvas.coursorX = 0;
                            }
                        }
                        else {
                            activeCanvas.coursorX += 1;
                        }
                    }
                    case DOWN -> {
                        if(activeCanvas.coursorY == 0){
                            return;
                        }else {
                            activeCanvas.coursorY += 1;
                        }
                    }
                    case UP -> {
                        if(activeCanvas.coursorY == activeCanvas.linesLengths.size()-1){
                            return;
                        }else {
                            activeCanvas.coursorY -= 1;
                        }
                    }
                    default -> {return;}
                }
                resetCursor();
                render();
            }
        }
    };

    private final Runnable ctrlYPressed = new Runnable() {
        @Override
        public void run() {
            if (activeCanvas.undoRedo.isCanRedo()) {
                Action act = activeCanvas.undoRedo.redoAction();

                if(activeCanvas.startEditChars == -1){
                    activeCanvas.startEditChars = act.getStart();
                }

                if (act.isDelete()) {
                    activeCanvas.text = activeCanvas.text.delete(act.getStart(), act.getEnd());
                    activeCanvas.editChars -= 1;
                } else {
                    activeCanvas.text = activeCanvas.text.insert(act.getStart(), act.getText());
                    activeCanvas.editChars += 1;
                }
                wordPrint.call(1);
                fileSaver.call(1);
                textAnalizer();
                resetCursor();
                render();
            }

        }
    };

    private final Runnable ctrlZPressed = new Runnable() {
        @Override
        public void run() {
            if (activeCanvas.undoRedo.isCanUndo()) {
                Action act = activeCanvas.undoRedo.undoAction();

                if(activeCanvas.startEditChars == -1){
                    activeCanvas.startEditChars = act.getStart();
                }

                if (act.isDelete()) {
                    activeCanvas.text = activeCanvas.text.delete(act.getStart(), act.getEnd());
                    activeCanvas.editChars -= 1;
                } else {
                    activeCanvas.text = activeCanvas.text.insert(act.getStart(), act.getText());
                    activeCanvas.editChars += 1;
                }
                wordPrint.call(1);
                fileSaver.call(1);
                textAnalizer();
                resetCursor();
                render();
            }

        }
    };

    private final EventHandler<MouseEvent> canvasMouseClicked = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            myCanvas.requestFocus();

            activeCanvas.coursorX = (int) Math.round((mouseEvent.getX() * screenScaleX
                    + centerX - settingsClass.startXPosition) / letterWidth); //номер буквы в строке
            activeCanvas.coursorY = (int) Math.round((mouseEvent.getY() * screenScaleY
                    + 1 + lineHeight / 2 + centerY - settingsClass.startYPosition) / lineHeight); //номер строки
            if(activeCanvas.startEditChars != -1){
                runLexer();
            }
            resetCursor();
            render();
        }
    };

    private final ChangeListener<Number> hScrollListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
            centerX = (float) (newVal.doubleValue() * (hScroll.getMax() - screenWidth) / hScroll.getMax());
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