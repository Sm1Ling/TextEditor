package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.util.Pair;
import org.ahmadsoft.ropes.Rope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;
import org.jetbrains.skija.*;
import ru.hse.edu.aaarifkhanov192.LineLight;
import ru.hse.edu.aaarifkhanov192.controllers.dirchanges.FileEvent;
import ru.hse.edu.aaarifkhanov192.controllers.dirchanges.IFileChangeListener;
import ru.hse.edu.aaarifkhanov192.controllers.dirchanges.WatchFileChanges;
import ru.hse.edu.aaarifkhanov192.lexer.Java9Lexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.Action;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.Debouncer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.SettingsClass;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.TextCanvas;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyInterval;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyNode;

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

    private Clipboard clipboard;

    private Rope text = Rope.BUILDER.build("private void render(Scene scene)/*comment {\n" +
            "        var canvas = (javafx.scene.canvas.Canvas) scene.getRoot().lookup(\"#myCanvas\");\n" +
            "        var gc = canvas.getGraphicsContext2D();\n" +
            "        gc.clearRect(0, 0, 320, 240);\n" +
            "        var data = makeImageWithSkija().encodeToData().getBytes();\n" +
            "        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));\n" +
            "        gc.drawImage(img, 0, 0);\n" +
            "    }");


    /* private void render(Scene scene)/*comment {\n" +
        "        var canvas = (javafx.scene.canvas.Canvas) scene.getRoot().lookup(\"#myCanvas\");\n" +
                "        var gc = canvas.getGraphicsContext2D();\n" +
                "        gc.clearRect(0, 0, 320, 240);\n" +
                "        var data = makeImageWithSkija().encodeToData().getBytes();\n" +
                "        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));\n" +
                "        gc.drawImage(img, 0, 0);\n" +
                "    } */

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

    //For copy and paste indexes of old and new positions.
    private int lettersToCursorOld = 0;
    private int lettersToCursorNew = 0;

    //Для правильной работы выделения текста.
    private Pair<Integer, Integer> cursorOld;   //координаты, где произошел onPressed
    private Pair<Integer, Integer> cursorNew;   //Координаты, где происходит drag
    private final LineLight lineLight = new LineLight();    //Класс для выделенных линий.
    // endregion


    private void onCtrlCRelease() {
        clipboard = Clipboard.getSystemClipboard();

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
        textAnalizer();
        resetCursor();
        render();
    }

    private MyIntervalTree<Token> mit;

    //    private final Debouncer<Integer> wordPrint = new Debouncer<Integer>(this::relex(),500);
    private final Debouncer<Integer> fileSaver = new Debouncer<>(this::saveFile, 30000);
//    private final Debouncer<Integer> fileSaver = new Debouncer<>(this::saveFile,30000);

    //TODO Разобраться с CtrlZ CtrlV
    //TODO Ало, когда буквы красить будем (лексер, аст, токены, интервалТри с цветами)
    //TODO Разобраться с добавлением таб окон
    //TODO Возможно присобачить выделение
    //TODO Однажды тут будут тесты
    @FXML
    private void initialize() {

        WatchFileChanges wfc = new WatchFileChanges("D:\\Projects\\IDE\\TextEditor\\");
        wfc.addListener(new IFileChangeListener() {
            @Override
            public void fileEdited(FileEvent event) {
                System.out.println(event.file() + " changed");
            }
        }).startObserve();

        /*wfc.stopObserve();*/

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

        DirectoryResult r = dt.fillRoot();
        treeView.setOnMouseClicked(mouseEvent -> {
            String path = dt.getPathToTappedFile(mouseEvent, treeView);
            if (path != null &&
                    canWindows.stream().noneMatch((o) -> o.filePath.equals(path))) {
                mit = new MyIntervalTree<>();
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

    private void runLexer(int start, int stop, int mbShift) {
        Java9Lexer lexer = new Java9Lexer(new ANTLRInputStream(activeCanvas.text.subSequence(start, stop).toString()));
        activeCanvas.tokens = lexer.getAllTokens().stream().toList();
        for (Token t :
                activeCanvas.tokens) {
            mit.insert(t.getStartIndex() + mbShift, t.getStopIndex() + mbShift, t);
            System.out.println(t.getStartIndex() + mbShift + " " + t.getStopIndex() + mbShift + t.getText() + " " + t.getType());
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

    // TODO Переделать

    boolean go = false;

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

        if (lettersToCursorOld - lettersToCursorNew != 0) {
            int ctr = 0;
            var highlightColor = new Paint().setColor(Color.makeRGB(33,66,131));
            for (Integer k : lineLight.getLines()) {
                //попали на изначальную строку
                if (Objects.equals(k, cursorOld.getValue())) {
//                    x = settingsClass.startXPosition - centerX + activeCanvas.coursorX * letterWidth;
//                    y = settingsClass.startYPosition - centerY + activeCanvas.coursorY * lineHeight;
//                    canvas.drawLine(x, y + font.getMetrics().getDescent(),
//                            x, y + font.getMetrics().getAscent(), settingsClass.coursorColor);
                    //И это линия, на которой курсор
                    if(ctr == lineLight.size() - 1){
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition + cursorOld.getKey() * letterWidth,
                                        (cursorOld.getValue()) * lineHeight,
                                        settingsClass.startXPosition + (cursorNew.getKey()) * letterWidth,
                                        (cursorNew.getValue()) * (lineHeight) + settingsClass.startYPosition + 8
                                ),
                                highlightColor//settingsClass.coursorColor
                        );

                        ctr++;
                        continue;
                    }

                    //Если курсор на другой линии
                    if(!lineLight.isReversed()){
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition + cursorOld.getKey() * letterWidth,
                                        cursorOld.getValue() * lineHeight,
                                        settingsClass.startXPosition + (activeCanvas.linesLengths.get(cursorOld.getValue()) - 1) * letterWidth,
                                        cursorOld.getValue() * (lineHeight) + settingsClass.startYPosition + 8
                                ),
                                highlightColor//settingsClass.coursorColor
                        );
                    }
                    else {
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition - centerX,
                                        cursorOld.getValue() * (lineHeight),
                                        settingsClass.startXPosition + (cursorOld.getKey()) * letterWidth,
                                        cursorOld.getValue() * lineHeight + settingsClass.startYPosition + 8
                                ),
                                highlightColor// settingsClass.coursorColor
                        );
                    }

                }

                //Если это не текущая строка
                if(ctr != lineLight.size() - 1 && !Objects.equals(k, cursorOld.getValue())){
                    canvas.drawRect(new Rect(
                                    settingsClass.startXPosition - centerX,
                                    k * lineHeight,
                                    settingsClass.startXPosition +(activeCanvas.linesLengths.get(k) - 1) * letterWidth,
                                    k * (lineHeight) + settingsClass.startYPosition + 8
                            ),
                            highlightColor//settingsClass.cursorColor
                    );
                }

                if(ctr == lineLight.size() - 1 && !Objects.equals(k, cursorOld.getValue())){
//                    //Если же мы на друой линии, но нашли текущую
                    if(!lineLight.isReversed()){
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition - centerX,
                                        k * lineHeight,
                                        settingsClass.startXPosition+ (cursorNew.getKey()) * letterWidth,
                                        cursorNew.getValue() * (lineHeight) + settingsClass.startYPosition + 8
                                ),
                                highlightColor//settingsClass.coursorColor
                        );
                    }
                    else{
                        canvas.drawRect(new Rect(
                                        settingsClass.startXPosition+(cursorNew.getKey()) * letterWidth,
                                        cursorNew.getValue() * (lineHeight),
                                        settingsClass.startXPosition+(activeCanvas.linesLengths.get(k) - 1)* letterWidth,
                                        k * lineHeight  + settingsClass.startYPosition + 8
                                ),
                                highlightColor//settingsClass.coursorColor
                        );
                    }
                }
                ctr++;
            }
        }
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
//                        rerenderLineInterval();
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
            myCanvas.requestFocus();
            countCursor(mouseEvent);
            resetCursor();
//            render();
            lettersToCursorNew = lettersToCursor();
            cursorNew = new Pair<>(activeCanvas.coursorX, activeCanvas.coursorY);

            lineLight.process(cursorNew.getValue());
            render();
        }
    };


    private final EventHandler<MouseEvent> canvasMouseClicked = new javafx.event.EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            myCanvas.requestFocus();
            countCursor(mouseEvent);
            resetCursor();

            //
            if(activeCanvas.coursorX != cursorNew.getKey() || activeCanvas.coursorY != cursorNew.getValue()){
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
