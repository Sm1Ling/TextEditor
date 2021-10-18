package ru.hse.edu.aaarifkhanov192;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;
import org.jetbrains.skija.*;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.ahmadsoft.ropes.*;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Font;
import org.jetbrains.skija.Image;
import org.jetbrains.skija.Paint;
import ru.hse.edu.aaarifkhanov192.lexer.Java9Lexer;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.UndoRedoController;

import java.awt.datatransfer.*;

public class TextEditor extends Application {
    private Rope text = Rope.BUILDER.build("private void render(Scene scene) {\n" +
            "        var canvas = (javafx.scene.canvas.Canvas) scene.getRoot().lookup(\"#myCanvas\");\n" +
            "        var gc = canvas.getGraphicsContext2D();\n" +
            "        gc.clearRect(0, 0, 320, 240);\n" +
            "        var data = makeImageWithSkija().encodeToData().getBytes();\n" +
            "        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));\n" +
            "        gc.drawImage(img, 0, 0);\n" +
            "    }");
    private List<? extends Token> tokens;

    private javafx.scene.canvas.Canvas canvas;
    private Clipboard clipboard;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextEditor.class.getResource("/ru.hse.edu.aaarifkhanov192/editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        canvas = (javafx.scene.canvas.Canvas) scene.getRoot().lookup("#myCanvas");
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        runLexer();
        render(scene);

        //TODO Добить обработку сочетаний ctrl и прочего
        scene.setOnKeyTyped(new EventHandler<>() {
            @Override
            public void handle(KeyEvent keyEvent){

                var c = keyEvent.getCharacter();
                if (c.equals("\r")) {
                    System.out.println("Enter");
                    text = text.append("\n");
                }
                else if (c.equals("\b")){
                    System.out.println("Backspace");
                    text = text.delete(text.length()-1,text.length());
                }
                else {
                    System.out.println(c);
                    text = text.append(c);
                }

                runLexer();
                render(scene);
            }
        });

        stage.setTitle("TextEditor");
        stage.setScene(scene);

        makeImageWithSkija();


        stage.show();
    }

    private String clipboardInsertHandler(){
        try {
            if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)){
                return clipboard.getData(DataFlavor.stringFlavor).toString();
            }
        } catch(IOException | UnsupportedFlavorException e){
            e.printStackTrace();
        }
        return "";
    }

    private void runLexer() {
        Java9Lexer lexer = new Java9Lexer(new ANTLRInputStream(text.toString()));
        tokens = lexer.getAllTokens().stream().toList();
    }

    // TODO Переделать
    private void render(Scene scene) {

        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        var data = makeImageWithSkija().encodeToData().getBytes();
        javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(data));
        gc.drawImage(img, 0, 0);
    }

    //TODO Переделать с учетом конкретной области
    private Image makeImageWithSkija() {
        Surface surface = Surface.makeRasterN32Premul((int)canvas.getWidth()-1, (int)canvas.getHeight()-1);
        Canvas canvas = surface.getCanvas();

        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);

        int x = 15;
        int y = 50;

        var font = new Font(Typeface.makeFromName("Consolas", FontStyle.NORMAL));
        for (int i = 0; i < text.length(); i++) {
            Token currToken = null;
            for (var token : tokens) {
                if (token.getStartIndex() <= i && token.getStopIndex() >= i) {
                    currToken = token;
                    break;
                }
            }

            if (currToken != null) {
                switch (currToken.getType()) {
                    case 1 -> paint.setColor(0xAABB0000);
                    case 2 -> paint.setColor(0xBBBBCC00);
                    case 3 -> paint.setColor(0x55551100);
                    case 4 -> paint.setColor(0x88683400);
                }
            }

            char c = text.charAt(i);
            if (c != '\n') {
                var textLine = TextLine.make(String.valueOf(c), font);
                canvas.drawTextLine(textLine, x, y, paint);
                x += textLine.getWidth() + 1;
            }
            else {
                y += -font.getMetrics().getAscent() + font.getMetrics().getDescent() + font.getMetrics().getLeading();
                x = 15;
            }
        }


        return surface.makeImageSnapshot();
    }

    public static void main(String[] args) {
        launch();
    }
}