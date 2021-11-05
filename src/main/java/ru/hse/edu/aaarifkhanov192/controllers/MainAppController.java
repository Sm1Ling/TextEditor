package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.*;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.lexer.PascalLexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.*;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private float indexScreenWidth;
    //номер строки и символа, на который указывает курсор
    //информация о максимальном размере строки и количестве строк в canvas
    private float maxLineLength;
    private float linesCount;
    //маркер запущенного приложения
    private boolean appstarted;
    //окно активного текст
    private TextCanvas activeCanvas;
    // endregion


    private final Debouncer<Integer> wordPrint = new Debouncer<>(this::relex, 500);
    private final Debouncer<String> fileSaver = new Debouncer<>(this::saveFile, 30000);

    //TODO Разобраться с добавлением таб окон
    //TODO Возможно присобачить выделение
    //TODO Однажды тут будут тесты
    @FXML
    private void initialize() {

        Screen screen = Screen.getPrimary();
        screenScaleX = (float) screen.getOutputScaleX();
        screenScaleY = (float) screen.getOutputScaleY();

        screenHeight = (float) tfCanvas.getHeight() * screenScaleY;
        screenWidth = (float) (tfCanvas.getWidth())*screenScaleX;
        indexScreenWidth = (float) (indexCanvas.getWidth())*screenScaleX;

        settingsClass.mainFont.setSize(settingsClass.mainFont.getSize() * screenScaleY);

        lineHeight = (-settingsClass.mainFont.getMetrics().getTop()
                + settingsClass.mainFont.getMetrics().getBottom());
        letterWidth = TextLine.make("x", settingsClass.mainFont).getWidth();

        settingsClass.startYPosition *=  screenScaleY;
        settingsClass.startXPosition *=  screenScaleX;

        maxLineLength = (screenWidth - settingsClass.startXPosition) / letterWidth;
        linesCount = screenHeight / lineHeight;

        setActiveElements(new Node[]{tfCanvas,hScroll,vScroll,indexCanvas}, false);

        DirectoryResult r = dt.fillRoot();
        treeView.setOnMouseClicked(mouseEvent -> {
            String path = dt.getPathToTappedFile(mouseEvent, treeView);
            if(path != null){
                if(activeCanvas == null || !activeCanvas.filePath.equals(path)){
                    closeCanvas(activeCanvas);
                    createPannel(dt.readText(path), path);
                }
            }

        });

        treeView.setRoot(r.rootTreeNode);
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
            tfCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, canvasMouseClicked);
            vScroll.valueProperty().addListener(vScrollListener);
            hScroll.valueProperty().addListener(hScrollListener);
            appstarted = true;
        }

        setActiveElements(new Node[]{tfCanvas,hScroll,vScroll,indexCanvas}, true);

        activeCanvas = new TextCanvas();

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

    private void saveFile(String x) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(x), StandardCharsets.UTF_8))) {
            writer.write(activeCanvas.text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        saveFile(canvas.filePath);
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

    private void renderIndexes(){
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

        int temp = Math.min(activeCanvas.startEditChars, activeCanvas.startEditChars + activeCanvas.editChars);

        for (Character character : activeCanvas.text) {
            if (character != '\n') {
                var textLine = TextLine.make(String.valueOf(character), settingsClass.mainFont);
                //region неэффективный способ
                 if(temp == -1 || i < temp) {
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
                    fileSaver.call(activeCanvas.filePath);
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
        fileSaver.call(activeCanvas.filePath);
        textAnalyzer();
        resetCursor();
        render();
    }

    private final EventHandler<MouseEvent> canvasMouseClicked = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            tfCanvas.requestFocus();

            activeCanvas.coursorX = (int) Math.round((mouseEvent.getX() * screenScaleX
                    + centerX - settingsClass.startXPosition)
                    / letterWidth);

            activeCanvas.coursorY = (int) Math.round((mouseEvent.getY() * screenScaleY
                    + 1 + lineHeight / 2 + centerY - settingsClass.startYPosition) / lineHeight); //номер строки

            if(activeCanvas.startEditChars != -1){
                runLexer();
            }
            resetCursor();
            render();
        }
    };

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
    //endregion
}