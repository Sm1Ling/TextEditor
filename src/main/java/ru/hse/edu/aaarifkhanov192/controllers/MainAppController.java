package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.LineLight;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.dirchanges.WatchFileChanges;
import ru.hse.edu.aaarifkhanov192.lexer.Java9Lexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.*;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyInterval;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyNode;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.viewclistener.ViewListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
    //номер строки и символа, на который указывает курсор
    //информация о максимальном размере строки и количестве строк в canvas
    private float maxLineLength;
    private float linesCount;
    //окно активного текст
    private TextCanvas activeCanvas;
    //список окон с текстами
    private final List<TextCanvas> canWindows = new ArrayList<>();

    //For copy and paste indexes of old and new positions.
    private int lettersToCursorOld = 0;
    private int lettersToCursorNew = 0;

    //Для правильной работы выделения текста.
    private Pair<Integer, Integer> cursorOld;   //координаты, где произошел onPressed
    private Pair<Integer, Integer> cursorNew;   //Координаты, где происходит drag
    private final LineLight lineLight = new LineLight();    //Класс для выделенных линий.

    private WatchFileChanges wfc;   //WatchService
    private boolean dontAsk = false;    //Нужно отображать модульное окно или нет повторно.

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final FileManager fm = new FileManager();
    // endregion


    private MyIntervalTree<Token> mit;

    //    private final Debouncer<Integer> wordPrint = new Debouncer<Integer>(this::relex(),500);
    private final Debouncer<Integer> fileSaver = new Debouncer<>(this::saveFile, 3000);
//    private final Debouncer<Integer> fileSaver = new Debouncer<>(this::saveFile,30000);

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
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChooseViewController.class
                .getResource("/ru.hse.edu.aaarifkhanov192/choose-view.fxml"));
        Parent pane = null;
        try {
            pane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(Objects.requireNonNull(pane)));

        stage.showAndWait();
    }

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
     * Инициализирует слушатель нажатия кнопки "yes" в choose-view.fxml.
     */
    private void initViewListener() {
        ViewListener viewListener = data -> {
            dontAsk = data.dontAsk();
            createPannel(fm.readText(), fm.getFilePath());
        };

        ChooseViewController.setListener(viewListener);
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
                        if (!dontAsk) {
                            Thread panelThread = new Thread(() ->
                                    Platform.runLater(this::createModulePane));
                            panelThread.start();
                            panelThread.interrupt();
                        } else {
                            createPannel(fm.readText(), fm.getFilePath());
                        }
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


    //TODO Разобраться с CtrlZ CtrlV
    //TODO Ало, когда буквы красить будем (лексер, аст, токены, интервалТри с цветами)
    //TODO Разобраться с добавлением таб окон
    //TODO Возможно присобачить выделение
    //TODO Однажды тут будут тесты
    @FXML
    private void initialize() {
        mit = new MyIntervalTree<>();
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
    }

    /**
     * Срабатывает при нажатии "Menu > Open".
     */
    @FXML
    private void openMenuClicked() {
        File selectedDir = directoryChooser.showDialog(treeView.getScene().getWindow());
        directoryChooser.setInitialDirectory(selectedDir);
        if (selectedDir != null) {
            fillTreeView(selectedDir);
            initViewListener();
        }
    }

    private void createPannel(String text, String path) {
        activeCanvas = new TextCanvas();
        fm.setActiveCanvas(activeCanvas);
        canWindows.add(activeCanvas);

        activeCanvas.text = Rope.BUILDER.build(text);
        activeCanvas.filePath = path;
        System.out.println("Create " + path);

        centerX = 0;
        centerY = 0;

        vScroll.setMin(0);
        vScroll.setVisibleAmount(screenHeight);

        hScroll.setMin(0);
        hScroll.setVisibleAmount(screenWidth);

        textAnalizer();
        runLexer(0, activeCanvas.text.length(), 0);
        render();

        if (canWindows.size() == 1) {
            myCanvas.setVisible(true);
            hScroll.setVisible(true);
            vScroll.setVisible(true);

            //TODO Рассмотреть переопределение всех событий на поля активного окна
            myCanvas.setOnKeyTyped(onKeyTyped);

            myCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
                    ctrlCPressed
            );

            myCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN),
                    ctrlVPressed
            );

            myCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                    ctrlZPressed);
            myCanvas.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                    ctrlYPressed);

            myCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, canvasMouseClicked);

            myCanvas.addEventFilter(MouseEvent.MOUSE_PRESSED, canvasMousePressed);
            myCanvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, canvasMouseDragged);
            vScroll.valueProperty().addListener(vScrollListener);
            hScroll.valueProperty().addListener(hScrollListener);
        }
    }

    private void switchPannel(int i) {
        //TODO не забыть поменять позицию относительно скролла
    }

    private void relex(Integer x) {
        runLexer(0, activeCanvas.text.length(), 0);
        render();
    }

    private void runLexer(int start, int stop, int mbShift) {
        Java9Lexer lexer = new Java9Lexer(
                new ANTLRInputStream(activeCanvas.text.subSequence(start, stop).toString())
        );
        activeCanvas.tokens = lexer.getAllTokens().stream().toList();
        for (Token t :
                activeCanvas.tokens) {
            mit.insert(t.getStartIndex() + mbShift, t.getStopIndex() + mbShift, t);
        }
    }

    private void textAnalizer() {
        int charIter = 0;

        activeCanvas.linesLengths = new ArrayList<>(Math.max(activeCanvas.linesLengths.size(), 64));

        for (Character character : activeCanvas.text) {
            if (character != '\n') {
                charIter += 1;
            } else {
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

    //region Render Block


    /**
     * Метод для покраски выделенных символов.
     *
     * @param canvas  Полотно отрисовки.
     * @param metrics Метрики шрифта.
     */
    private void highlightOnDrag(Canvas canvas, FontMetrics metrics) {
        if (lettersToCursorOld - lettersToCursorNew != 0) {
            int ctr = 0;
            var highlightColor = new Paint().setColor(Color.makeRGB(33, 66, 131));
            for (Integer k : lineLight.getLines()) {
                //попали на изначальную строку
                if (Objects.equals(k, cursorOld.getValue())) {
                    //И это линия, на которой курсор
                    if (ctr == lineLight.size() - 1) {
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition + cursorOld.getKey() * letterWidth,
                                        (cursorOld.getValue()) * lineHeight,
                                        settingsClass.startXPosition + (cursorNew.getKey()) * letterWidth,
                                        (cursorNew.getValue()) * (lineHeight) + settingsClass.startYPosition
                                                + metrics.getDescent()
                                ),
                                highlightColor
                        );

                        ctr++;
                        continue;
                    }

                    //Если курсор на другой линии
                    if (!lineLight.isReversed()) {
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition + cursorOld.getKey() * letterWidth,
                                        cursorOld.getValue() * lineHeight,
                                        settingsClass.startXPosition + (activeCanvas.linesLengths.get(cursorOld.getValue()) - 1) * letterWidth,
                                        cursorOld.getValue() * (lineHeight) + settingsClass.startYPosition
                                                + metrics.getDescent()
                                ),
                                highlightColor
                        );
                    } else {
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition - centerX,
                                        cursorOld.getValue() * (lineHeight),
                                        settingsClass.startXPosition + (cursorOld.getKey()) * letterWidth,
                                        cursorOld.getValue() * lineHeight + settingsClass.startYPosition
                                                + metrics.getDescent()
                                ),
                                highlightColor
                        );
                    }

                }

                //Если это не текущая строка
                if (ctr != lineLight.size() - 1 && !Objects.equals(k, cursorOld.getValue())) {
                    canvas.drawRect(new Rect(
                                    settingsClass.startXPosition - centerX,
                                    k * lineHeight,
                                    settingsClass.startXPosition
                                            + (activeCanvas.linesLengths.get(k) - 1) * letterWidth,
                                    k * (lineHeight) + settingsClass.startYPosition
                                            + metrics.getDescent()
                            ),
                            highlightColor
                    );
                }

                if (ctr == lineLight.size() - 1 && !Objects.equals(k, cursorOld.getValue())) {
                    //Если же мы на друой линии, но нашли текущую
                    if (!lineLight.isReversed()) {
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition - centerX,
                                        k * lineHeight,
                                        settingsClass.startXPosition + (cursorNew.getKey()) * letterWidth,
                                        cursorNew.getValue() * (lineHeight) + settingsClass.startYPosition
                                                + metrics.getDescent()
                                ),
                                highlightColor
                        );
                    } else {
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition + (cursorNew.getKey()) * letterWidth,
                                        cursorNew.getValue() * (lineHeight),
                                        settingsClass.startXPosition +
                                                (activeCanvas.linesLengths.get(k) - 1) * letterWidth,
                                        k * lineHeight + settingsClass.startYPosition + metrics.getDescent()
                                ),
                                highlightColor
                        );
                    }
                }
                ctr++;
            }
        }
    }

    private void render() {
        var data = Objects.requireNonNull(makeImage().encodeToData()).getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        myCanvas.getGraphicsContext2D().drawImage(img, 0, 0, myCanvas.getWidth(), myCanvas.getHeight());
    }

    private Image makeImage() {
        Surface surface = Surface.makeRasterN32Premul((int) (screenWidth), (int) (screenHeight));
        Canvas canvas = surface.getCanvas();

        Paint paint = settingsClass.mainColor;

        float x = settingsClass.startXPosition - centerX;
        float y = settingsClass.startYPosition - centerY;

        var font = settingsClass.mainFont;
        int i = 0;

        highlightOnDrag(canvas, font.getMetrics());
        for (Character character : activeCanvas.text) {
            List<MyNode<Token>> tk = mit.getIntervals(i);
            if (tk != null && !tk.isEmpty()) {
//                System.out.println(tk.get(0).getInterval());
                int tType = tk.get(0).getToken().getType();
                if (tType >= 1 && tType <= 61 || tType == 64 || tType == 67 || tType == 74 || tType == 75) {
                    paint.setColor(Color.makeRGB(204, 120, 50));
                } else if (tType == 117 || tType == 118) {
                    paint.setColor(Color.makeRGB(128, 128, 128));
                } else if (tType == 62) {
                    paint.setColor(Color.makeRGB(104, 151, 187));
                } else if (tType == 66) {
                    paint.setColor(Color.makeRGB(106, 135, 89));
                } else {
                    paint.setColor(Color.makeRGB(169, 183, 198));
                }
            }

            if (character != '\n') {
                var textLine = TextLine.make(String.valueOf(character), font);
                canvas.drawTextLine(textLine, x, y, paint);
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

    //endregion Render Block

    //region handlers Block

    private void rerenderLineInterval() {
        int sym = 0;    //Длина строк от нулевой до текущей. -2 делай
        int lastlen = 0;    //Длина последней строки., -1 делай
        int firstLen = activeCanvas.text.toString().trim().lastIndexOf(" ");
        System.out.println(activeCanvas.linesLengths.get(activeCanvas.coursorY) - 1);
        for (int i = 0; i <= activeCanvas.coursorY; i++) {
            sym += (activeCanvas.linesLengths.get(i));
            if (i == activeCanvas.coursorY)
                lastlen = activeCanvas.linesLengths.get(i);

        }

        int indexInTree = sym - (lastlen - activeCanvas.coursorX) - 1;
        int startOfLine = sym - lastlen;    //индекс первого символа в нужной строке относительно ROpe

        System.out.println(typedKeys + " " + startOfLine + " " + lastlen + " " + sym);

        for (var t : mit.getTree()) {
            if (t.getInterval().isOverlaps(new MyInterval(startOfLine, sym - 2 - typedKeys))) {//startOfLine, sym - 2
                mit.delete(t.getInterval());
            }
        }
        System.out.println("INT TREE S " + mit.getTree().size());
        mit.shiftIntervals(sym - 1 - typedKeys, typedKeys);   //shift = newLen - oldLen; newLen = activeCanvas.linesLengths.get(activeCanvas.coursorY) - 1
        runLexer(startOfLine, sym - 1, sym - lastlen);  //startOfLine, sym - 2

        System.out.println("NEW INT" + (sym - lastlen));
        for (var t :
                mit.getTree()) {
            System.out.println(t.getInterval());
        }
        typedKeys = 0;
        render();
    }

    private void onCtrlCRelease() {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        //swap
        if (lettersToCursorNew < lettersToCursorOld) {
            Pair<Integer, Integer> tmp = new Pair<>(cursorOld.getKey(), cursorOld.getValue());
            cursorOld = new Pair<>(cursorNew.getKey(), cursorNew.getValue());
            cursorNew = new Pair<>(tmp.getKey(), tmp.getValue());

            lettersToCursorNew += lettersToCursorOld;
            lettersToCursorOld = lettersToCursorNew - lettersToCursorOld;
            lettersToCursorNew -= lettersToCursorOld;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(activeCanvas.text.subSequence(lettersToCursorOld, lettersToCursorNew).toString());
        clipboard.setContent(content);
        lettersToCursorOld = 0;
        lettersToCursorNew = 0;
    }

    private void onCtrlVRelease() {
        int letterCursor = lettersToCursor();
        String clipBoardText = Clipboard.getSystemClipboard().getString();
        activeCanvas.text = activeCanvas.text.insert(letterCursor, clipBoardText);

        activeCanvas.undoRedo.addAction(
                new Action(
                        letterCursor,
                        letterCursor + clipBoardText.length(),
                        clipBoardText,
                        false));
        fileSaver.call(1);
        clipboardTextAnalyzer(clipBoardText);
        textAnalizer();
        resetCursor();
        render();
    }

    private void clipboardTextAnalyzer(String text) {
        String[] splitAr = text.split("\n");
        System.out.println(splitAr.length);
        if (splitAr.length > 1) {
            activeCanvas.coursorY += splitAr.length;
            activeCanvas.coursorX = splitAr[splitAr.length - 1].length() - 1;
        } else {
            activeCanvas.coursorX += splitAr[0].length();
        }
    }

    private Integer typedKeys = 0;
    private final EventHandler<KeyEvent> onKeyTyped = new javafx.event.EventHandler<>() {
        @Override
        public void handle(KeyEvent keyEvent) {
            int letNum = lettersToCursor();

            if (keyEvent.isShiftDown()) {
                System.out.println("SHORT CUT");
            }
            if (!keyEvent.isShortcutDown()) {
                switch (keyEvent.getCharacter()) {
                    case "\\" -> {

                        return;
                    }
                    case "\r" -> {
                        activeCanvas.text = activeCanvas.text.insert(letNum, "\n");
                        activeCanvas.coursorX = 0;
                        activeCanvas.coursorY += 1;
                        activeCanvas.undoRedo.addAction(new Action(letNum, letNum + 1, "\n", false));
                    }
                    case "\b" -> {
                        typedKeys--;
                        System.out.println("TY" + typedKeys);
                        if (activeCanvas.coursorY == 0 && activeCanvas.coursorX == 0) {
                            return;
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

                    }
                    case "\t" -> {
                        activeCanvas.undoRedo.addAction(new Action(letNum, letNum + 4, "    ", false));
                        activeCanvas.text = activeCanvas.text.insert(letNum, "    ");
                        activeCanvas.coursorX += 4;
                    }
                    case "" -> {
                        return;
                    }
                    default -> {
                        var c = keyEvent.getCharacter();
                        if (keyEvent.isShiftDown()) {
                            c = c.toUpperCase(Locale.ROOT);
                        }
                        activeCanvas.undoRedo.addAction(new Action(letNum, letNum + 1, c, false));
                        activeCanvas.text = activeCanvas.text.insert(letNum, c);
                        activeCanvas.coursorX += 1;
                        typedKeys++;
                    }
                }

//                wordPrint.call(1);
                fileSaver.call(1);
                textAnalizer();
                resetCursor();
                render();
            }
        }
    };

    private final Runnable ctrlCPressed = this::onCtrlCRelease;

    private final Runnable ctrlVPressed = this::onCtrlVRelease;

    private final Runnable ctrlYPressed = new Runnable() {
        @Override
        public void run() {
            if (activeCanvas.undoRedo.isCanRedo()) {
                Action act = activeCanvas.undoRedo.redoAction();
                if (act.isDelete()) {
                    activeCanvas.text = activeCanvas.text.delete(act.getStart(), act.getEnd());
                } else {
                    activeCanvas.text = activeCanvas.text.insert(act.getStart(), act.getText());
                }
//                wordPrint.call(1);
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
                if (act.isDelete()) {
                    activeCanvas.text = activeCanvas.text.delete(act.getStart(), act.getEnd());
                } else {
                    activeCanvas.text = activeCanvas.text.insert(act.getStart(), act.getText());
                }
//                wordPrint.call(1);
                fileSaver.call(1);
                textAnalizer();
                resetCursor();
                render();
            }

        }
    };

    //For copy(then paste)
    private final EventHandler<MouseEvent> canvasMousePressed = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            lineLight.reload();
            myCanvas.requestFocus();
            countCursor(mouseEvent);
            resetCursor();
            render();

            lettersToCursorOld = lettersToCursor();
            cursorOld = new Pair<>(activeCanvas.coursorX, activeCanvas.coursorY);
        }
    };

    //For paste
    private final EventHandler<MouseEvent> canvasMouseDragged = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {

            Thread t = new Thread(() -> Platform.runLater(() -> {
                myCanvas.requestFocus();
                countCursor(mouseEvent);
                resetCursor();
//            render();
                lettersToCursorNew = lettersToCursor();
                cursorNew = new Pair<>(activeCanvas.coursorX, activeCanvas.coursorY);

                lineLight.process(cursorNew.getValue());
                render();
            }));
            t.start();
        }
    };


    private final EventHandler<MouseEvent> canvasMouseClicked = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            myCanvas.requestFocus();
            countCursor(mouseEvent);
            resetCursor();

            if ((cursorNew != null && activeCanvas.coursorX != cursorNew.getKey()) ||
                    (cursorNew != null && activeCanvas.coursorY != cursorNew.getValue())) {
                lineLight.reload();
            }
            render();

            //for paste
            lettersToCursorNew = lettersToCursor();

        }
    };

    private void countCursor(MouseEvent mouseEvent) {
        activeCanvas.coursorX = (int) Math.round((mouseEvent.getX() * screenScaleX
                + centerX - settingsClass.startXPosition) / letterWidth); //номер буквы в строке
        activeCanvas.coursorY = (int) Math.round((mouseEvent.getY() * screenScaleY
                + 1 + lineHeight / 2 + centerY - settingsClass.startYPosition) / lineHeight); //номер строки
    }

    private final ChangeListener<Number> hScrollListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
            centerX = (float) (newVal.doubleValue() * (hScroll.getMax() - screenWidth) / hScroll.getMax());
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
}
