package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.CharStreams;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.lexer.PascalLexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.*;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.dirchanges.WatchFileChanges;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.RuleBasedCollator;
import java.util.*;

public class MainAppController {

    @FXML
    private TreeView<String> treeView;
    @FXML
    private javafx.scene.canvas.Canvas tfCanvas;
    @FXML
    private javafx.scene.canvas.Canvas indexCanvas;
    @FXML
    private ScrollBar hScroll;
    @FXML
    private ScrollBar vScroll;
    @FXML
    private HBox tabbox;
    @FXML
    public AnchorPane background;


    // region Общие поля
    public final SettingsClass settingsClass = new SettingsClass();
    private DirectoryTree dt;

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
    private float indexScreenWidth;
    //номер строки и символа, на который указывает курсор
    //информация о максимальном размере строки и количестве строк в canvas
    private float maxLineLength;
    private float linesCount;
    //маркер запущенного приложения
    private boolean appstarted;
    //окно активного текст
    private TextCanvas activeCanvas;
    //список окон с текстами

    //For copy and paste indexes of old and new positions.
    private int lettersToCursorOld = 0;
    private int lettersToCursorNew = 0;

    private WatchFileChanges wfc;   //WatchService

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final FileManager fm = new FileManager();
    // endregion


    private final Debouncer<Integer> wordPrint = new Debouncer<>(this::relex, 500);
    private final Debouncer<Integer> fileSaver = new Debouncer<>(this::saveFile, 30000);


    /**
     * Заполнение <code>TreeView</code> и инициализация {@link DirectoryTree}.
     *
     * @param dir Выбранная директория.
     */
    private void fillTreeView(File dir) {
        dt = new DirectoryTree(dir);
        DirectoryResult r = dt.fillRoot();
        treeView.setRoot(r.rootTreeNode);
        treeView.setOnMouseClicked(mouseEvent -> {
            fm.setFilePath(dt.getPathToTappedFile(mouseEvent, treeView));
            if (fm.getFilePath() != null) {
                //Убираю лишнюю "\" в конце.
                fm.setFilePath(fm.getFilePath().substring(0, fm.getFilePath().length() - 1));
                initFileWatcher();

                createPannel(fm.readText(), fm.getFilePath());
            }
        });
    }


    /**
     * Инициализирует <code>{@link WatchFileChanges}</code> для наблюдения за файлом.
     */
    private void initFileWatcher() {
        if (wfc != null && !wfc.isInterrupted()) {
            wfc.stopObserve();
        }
        wfc = new WatchFileChanges(dt.getPath());
        wfc.setObservedFileName(fm.getFilePath());
        wfc.addListener(event -> {
            //Значит еще не произошло debounce ранее, но был изменен файл
            if (fm.getSaveHash() == null) {
                //Если ничего не изменилось
                try {
                    if (!fm.getHash(activeCanvas.text.toString())
                            .equals(fm.getHash(fm.readText()))) {
                        System.out.println("FILE CHANGED, WOULD YOU LIKE DO LOAD IT?");
                        createModulePane();
                    } else {
                        System.out.println("FILE DID NOT CHANGED");
                    }
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                }
            } else {
                //Значит сохранилась неизмененная версия
                fm.setSaveHash(null);
            }
        });
        wfc.startObserve();
    }


    //TODO Возможно присобачить выделение
    //TODO Однажды тут будут тесты
    @FXML
    private void initialize() {

        Screen screen = Screen.getPrimary();
        screenScaleX = (float) screen.getOutputScaleX();
        screenScaleY = (float) screen.getOutputScaleY();

        screenHeight = (float) tfCanvas.getHeight() * screenScaleY;
        screenWidth = (float) (tfCanvas.getWidth()) * screenScaleX;
        indexScreenWidth = (float) (indexCanvas.getWidth()) * screenScaleX;

        settingsClass.mainFont.setSize(settingsClass.mainFont.getSize() * screenScaleY);

        lineHeight = (-settingsClass.mainFont.getMetrics().getTop()
                + settingsClass.mainFont.getMetrics().getBottom());
        letterWidth = TextLine.make("x", settingsClass.mainFont).getWidth();

        settingsClass.startYPosition *= screenScaleY;
        settingsClass.startXPosition *= screenScaleX;

        maxLineLength = (screenWidth - settingsClass.startXPosition) / letterWidth;
        linesCount = screenHeight / lineHeight;

        setActiveElements(new Node[]{tfCanvas, hScroll, vScroll, indexCanvas}, false);
    }


    private void createPannel(String text, String path) {
        if(!appstarted){
            tfCanvas.setOnKeyTyped(onKeyTyped);
            tfCanvas.setOnKeyPressed(onKeyPressed);

            tfCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                    ctrlZPressed);
            tfCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                    ctrlYPressed);
            tfCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
                    ctrlCPressed
            );
            tfCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN),
                    ctrlVPressed
            );
            tfCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, canvasMouseClicked);
            tfCanvas.addEventFilter(MouseEvent.MOUSE_PRESSED, canvasMousePressed);
            tfCanvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, canvasMouseDragged);
            vScroll.valueProperty().addListener(vScrollListener);
            hScroll.valueProperty().addListener(hScrollListener);
            appstarted = true;
        }

        setActiveElements(new Node[]{tfCanvas,hScroll,vScroll,indexCanvas}, true);

        activeCanvas = new TextCanvas();
        fm.setActiveCanvas(activeCanvas);

        activeCanvas.text = Rope.BUILDER.build(text);
        activeCanvas.filePath = path;

        centerX = 0;
        centerY = 0;

        vScroll.setMin(0);
        vScroll.setVisibleAmount(screenHeight);

        hScroll.setMin(0);
        hScroll.setVisibleAmount(screenWidth);

        textAnalyzer();
        runLexer();
        render();

    }

    private void relex(Integer x) {
        runLexer();
        render();
    }

    private void runLexer() {

        PascalLexer lexer = new PascalLexer(CharStreams.fromString(activeCanvas.text.toString()));
        activeCanvas.colorTree = new MyIntervalTree<>();
        activeCanvas.highlight = new MyIntervalTree<>();
        Thread tokenThread = new Thread(() -> TokenFiller.refillColorTree(lexer.getAllTokens(),activeCanvas.colorTree));

        try {
            tokenThread.start();
            /* TODO
            не сказал бы что есть нужда в бОльших стуктурах многопоточности.
            МБ стоит перекроить метод чтоб не было завала
            lexer.reset();
            var parser = new PascalParser(new CommonTokenStream(lexer));
            parser.addParseListener(new PascalBaseListener(activeCanvas.highlight));
            parser.program();*/
            tokenThread.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        finally{
            activeCanvas.editChars = 0;
            activeCanvas.startEditChars = -1;
        }
    }

    private void closeCanvas(TextCanvas canvas){
        if(canvas == null){
            return;
        }
        saveFile(1);
        wordPrint.terminate();
        fileSaver.terminate();

        setActiveElements(new Node[]{tfCanvas,hScroll,vScroll,indexCanvas}, false);

        activeCanvas = null;
    }


    //region Additional Methods
    private void textAnalyzer() {
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
        vScroll.setValue(Math.min(vScroll.getValue(),vScroll.getMax()));
        centerY = (float) (vScroll.getValue() * (vScroll.getMax() - screenHeight) / vScroll.getMax());

        hScroll.setMax(Math.max(maxLineLength, Collections.max(activeCanvas.linesLengths))
                * letterWidth + settingsClass.startXPosition);
        hScroll.setValue(Math.min(hScroll.getValue(),hScroll.getMax()));
        centerX = (float) (hScroll.getValue() * (hScroll.getMax() - screenWidth) / hScroll.getMax());
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

    private int[] lineCharToCursor(int letterNum){
        int lineNum = 0;
        int charNum = 0;
        for (int i = 0; i < activeCanvas.linesLengths.size(); i++) {
            if(activeCanvas.linesLengths.get(i) < letterNum){
                letterNum -= activeCanvas.linesLengths.get(i);
                lineNum += 1;
            }
            else {
                charNum += letterNum;
                break;
            }
        }
        return new int[]{lineNum,charNum};
    }

    private void resetCursor() {
        activeCanvas.coursorY = Math.min(activeCanvas.coursorY, activeCanvas.linesLengths.size() - 1);
        activeCanvas.coursorY = Math.max(activeCanvas.coursorY, 0);

        activeCanvas.coursorX = Math.min(
                activeCanvas.linesLengths.get(activeCanvas.coursorY) - 1, activeCanvas.coursorX);
        activeCanvas.coursorX = Math.max(activeCanvas.coursorX, 0);
    }

    private void setActiveElements(Node[] nodes, boolean activeness){
        for(var e: nodes){
            e.setDisable(!activeness);
            e.setVisible(activeness);
        }
    }

    //endregion

    //region Render Block

    public void render() {

        if(activeCanvas == null){
            return;
        }

        Thread renderIdx = new Thread(this::renderIndexes);
        try {
            renderIdx.start();
            var data = Objects.requireNonNull(makeImage().encodeToData()).getBytes();
            javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
            renderIdx.join();

            tfCanvas.getGraphicsContext2D().clearRect(0, 0, tfCanvas.getWidth(), tfCanvas.getHeight());

            tfCanvas.getGraphicsContext2D().drawImage(img, 0, 0,
                    tfCanvas.getWidth(), tfCanvas.getHeight());
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void renderIndexes() {
        var data = Objects.requireNonNull(makeIndexes().encodeToData()).getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));

        indexCanvas.getGraphicsContext2D().clearRect(0, 0, indexCanvas.getWidth(), indexCanvas.getHeight());
        indexCanvas.getGraphicsContext2D().drawImage(img, 0, 0,
                indexCanvas.getWidth(), indexCanvas.getHeight());
    }

    private Image makeImage() {
        Surface surface = Surface.makeRasterN32Premul((int) (screenWidth), (int) (screenHeight));
        Canvas canvas = surface.getCanvas();


        float x = settingsClass.startXPosition - centerX;
        float y = settingsClass.startYPosition - centerY;

        int i = 0;

        int lettersToCur = lettersToCursor();

        var chosenInterval = activeCanvas.colorTree.getIntervals(lettersToCur);
        if(!chosenInterval.isEmpty() && activeCanvas.startEditChars != -1){
            activeCanvas.colorTree.delete(chosenInterval.get(0).getInterval()); // избавляюсь от цвета посреди слова
        }

        int shift = Math.min(activeCanvas.startEditChars, activeCanvas.startEditChars + activeCanvas.editChars);

        int leftSelectionBorder = Math.min(lettersToCursorOld,lettersToCursorNew);
        int rightSelectionBorder = Math.max(lettersToCursorOld,lettersToCursorNew);

        //highlightOnDrag(canvas, settingsClass.mainFont.getMetrics());
        for (Character character : activeCanvas.text) {
            if (character != '\n') {
                var textLine = TextLine.make(String.valueOf(character), settingsClass.mainFont);
                //region неэффективный способ
                if( i>= leftSelectionBorder && i<rightSelectionBorder){
                    canvas.drawRect(new Rect(x,y + settingsClass.mainFont.getMetrics().getTop(),
                            x+textLine.getWidth(), y + settingsClass.mainFont.getMetrics().getBottom()),
                            settingsClass.selectionColor);
                }

                if(shift == -1 || i < shift) {
                    drawText(i,x,y,canvas,textLine);
                }
                else if(i >= activeCanvas.startEditChars &&
                        i <= activeCanvas.startEditChars + activeCanvas.editChars){
                    canvas.drawTextLine(textLine,x,y,settingsClass.mainColor);
                }
                else {
                    drawText(i - activeCanvas.editChars,x,y,canvas,textLine);
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

        canvas.drawLine(x, y + settingsClass.mainFont.getMetrics().getDescent(),
                x, y + settingsClass.mainFont.getMetrics().getAscent(), settingsClass.coursorColor);

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

    private Image makeIndexes(){
        Surface surface = Surface.makeRasterN32Premul((int)indexScreenWidth, (int) (screenHeight));
        Canvas canvas = surface.getCanvas();

        float x = indexScreenWidth/8;
        float y = lineHeight - centerY;

        for (int i = 0; i < activeCanvas.linesLengths.size(); i++){
            canvas.drawTextLine(TextLine.make(String.valueOf(i + 1), settingsClass.mainFont),x,y,
                    settingsClass.lineColor);
            y += lineHeight;
        }

        canvas.drawLine(indexScreenWidth-1,0,indexScreenWidth-1,screenHeight,
                settingsClass.lineColor);

        return surface.makeImageSnapshot();
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
                    textAnalyzer();
                    resetCursor();
                    render();
                }
            }

    };

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

    private final EventHandler<KeyEvent> onKeyPressed = new EventHandler<>() {
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
                ctrlMethod(act);
            }
        }
    };

    private final Runnable ctrlCPressed = new Runnable() {
        @Override
        public void run() {
            Clipboard clipboard = Clipboard.getSystemClipboard();

            ClipboardContent content = new ClipboardContent();
            content.putString(activeCanvas.text.subSequence(
                    Math.min(lettersToCursorOld,lettersToCursorNew),
                    Math.max(lettersToCursorOld,lettersToCursorNew)).toString());
            clipboard.setContent(content);
            lettersToCursorOld = 0;
            lettersToCursorNew = 0;
        }
    };

    private final Runnable  ctrlVPressed = new Runnable() {

        @Override
        public void run() {
            int letterCursor = lettersToCursor();
            String clipBoardText = Clipboard.getSystemClipboard().getString();
            activeCanvas.text = activeCanvas.text.insert(letterCursor, clipBoardText);

            activeCanvas.undoRedo.addAction(
                    new Action(
                            letterCursor,
                            letterCursor + clipBoardText.length(),
                            clipBoardText,
                            false));
            runLexer();
            fileSaver.call(1);
            clipboardTextAnalyzer(clipBoardText);
            textAnalyzer();
            resetCursor();
            render();

        }
    };

    private void clipboardTextAnalyzer(String text) {
        String[] splitAr = text.split("\n");
        if (splitAr.length > 1) {
            activeCanvas.coursorY += splitAr.length;
            activeCanvas.coursorX = splitAr[splitAr.length - 1].length() - 1;
        } else {
            activeCanvas.coursorX += splitAr[0].length();
        }
    }

    private final Runnable ctrlZPressed = new Runnable() {
        @Override
        public void run() {
            if (activeCanvas.undoRedo.isCanUndo()) {
                Action act = activeCanvas.undoRedo.undoAction();
                ctrlMethod(act);
            }
        }
    };

    private void ctrlMethod(Action act) {
        if(activeCanvas.startEditChars == -1){
            activeCanvas.startEditChars = act.getStart();
        }

        if (act.isDelete()) {
            activeCanvas.text = activeCanvas.text.delete(act.getStart(), act.getEnd());
            activeCanvas.coursorX = lineCharToCursor(act.getStart())[1];
            activeCanvas.coursorY = lineCharToCursor(act.getStart())[0];
        } else {
            activeCanvas.text = activeCanvas.text.insert(act.getStart(), act.getText());
            activeCanvas.coursorX = lineCharToCursor(act.getEnd())[1];
            activeCanvas.coursorY = lineCharToCursor(act.getEnd())[0];
        }
        runLexer();
        fileSaver.call(1);
        textAnalyzer();
        resetCursor();
        render();
    }

    //For copy(then paste)
    private final EventHandler<MouseEvent> canvasMousePressed = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            tfCanvas.requestFocus();
            countCursor(mouseEvent);
            resetCursor();
            render();

            lettersToCursorOld = lettersToCursor();
        }
    };

    //For paste
    private final EventHandler<MouseEvent> canvasMouseDragged = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            tfCanvas.requestFocus();
            countCursor(mouseEvent);
            resetCursor();
            lettersToCursorNew = lettersToCursor();

            render();
        }

    };


    private final EventHandler<MouseEvent> canvasMouseClicked = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            tfCanvas.requestFocus();

            countCursor(mouseEvent);

            if(activeCanvas.startEditChars != -1){
                runLexer();
            }

            resetCursor();
            render();

            //for paste
            lettersToCursorNew = lettersToCursor();
        }
    };

    private void countCursor(MouseEvent mouseEvent) {
        activeCanvas.coursorX = (int) Math.round((mouseEvent.getX() * screenScaleX
                + centerX - settingsClass.startXPosition)
                / letterWidth);

        activeCanvas.coursorY = (int) Math.round((mouseEvent.getY() * screenScaleY
                + 1 + lineHeight / 2 + centerY - settingsClass.startYPosition) / lineHeight); //номер строки
    }

    private final ChangeListener<Number> hScrollListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
            centerX = (float) (newVal.doubleValue() * (hScroll.getMax() - screenWidth)
                    / hScroll.getMax());
            activeCanvas.scrollPositionX = newVal.floatValue();
            render();
        }
    };

    private final ChangeListener<Number> vScrollListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
            centerY = (float) (newVal.doubleValue() * (vScroll.getMax() - screenHeight) / vScroll.getMax());
            activeCanvas.scrollPositionY = newVal.floatValue();
            render();
        }
    };

    /**
     * Срабатывает при нажатии "Menu > Open".
     */
    @FXML private void openMenuClicked() {
        directoryChooser.setInitialDirectory(new File("."));
        File selectedDir = directoryChooser.showDialog(treeView.getScene().getWindow());
        if (selectedDir != null) {
            fillTreeView(selectedDir);
        }
    }

    //endregion

    //region Other Controls
    @FXML
    private void openSettingsMenu(){
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SettingsController.class.getResource("/ru.hse.edu.aaarifkhanov192/settings-view.fxml"));
        try {
            Pane root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setMinHeight(root.getMinHeight()); //устанавливаю минимальные значения для окна добавления
            stage.setMinWidth(root.getMinWidth());
            stage.setTitle("Settings Window");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tfCanvas.getScene().getWindow());
            stage.setScene(new Scene(root));
            //передаю в класс-контроллер ссылку на этот контроллер
            SettingsController controller = fxmlLoader.getController();
            controller.mcontroller = this;
            controller.start();
            stage.showAndWait();

        } catch (IOException e) { //если возникает ошибка - показываю диалоговое окно с этой ошибкой
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Что-то не так");
            alert.setHeaderText("Возникла IOException");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void closeFile(){
        if(activeCanvas != null){
            closeCanvas(activeCanvas);
        }
    }

    /**
     * Метод для Debounce.
     *
     * @param x Ключ.
     */
    private void saveFile(Integer x) {
        fm.saveFile(x);
    }

    /**
     * Метод для создания и отображения модульного окна в случаях, когда изменен файл извне.
     */
    private void createModulePane() {
        Alert alertConf = new Alert(Alert.AlertType.CONFIRMATION);
        alertConf.setTitle("Внимание!");
        alertConf.setHeaderText("Дефолтовая база данных будет дополнена импортом");
        alertConf.setContentText("Все-равно продолжить импорт данных?");
        Optional<ButtonType> res = alertConf.showAndWait();
        if (res.isPresent() && res.get() != ButtonType.OK) {
            return;
        }

        createPannel(fm.readText(), fm.getFilePath());
    }



    //endregion
}