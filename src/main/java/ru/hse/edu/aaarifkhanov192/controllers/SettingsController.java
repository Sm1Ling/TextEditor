package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.jetbrains.skija.Paint;


public class SettingsController {

    public MainAppController mcontroller;

    @FXML
    ColorPicker mainFont;
    @FXML
    ColorPicker carriageColor;
    @FXML
    ColorPicker backgroundColor;
    @FXML
    ColorPicker lineColor;
    @FXML
    ColorPicker tokensColor;
    @FXML
    ChoiceBox<String> choiceBox;

    @FXML
    private void initialize(){
    }

    public void start(){
        mainFont.setValue(makeColorFromInt(mcontroller.settingsClass.mainColor));
        carriageColor.setValue(makeColorFromInt(mcontroller.settingsClass.coursorColor));
        backgroundColor.setValue(makeColorFromInt(new Paint().setColor(
                Integer.valueOf(mcontroller.background.getStyle().split("#")[1].substring(0,6),16))));
        lineColor.setValue(makeColorFromInt(mcontroller.settingsClass.lineColor));

        choiceBox.setItems(FXCollections.observableArrayList(mcontroller.settingsClass.colorMap.keySet()));
        choiceBox.setValue("Choose token");

        choiceBox.setOnAction((e) ->{
            tokensColor.setDisable(!choiceBox.getItems().contains(choiceBox.getValue()));
        });

        tokensColor.setDisable(true);
        tokensColor.setOnAction((e)->{
            mcontroller.settingsClass.colorMap.put(choiceBox.getValue(),
                    makePaintFromColor(tokensColor.getValue()));
            mcontroller.render();
        });

        mainFont.setOnAction((e)->{
            mcontroller.settingsClass.mainColor = makePaintFromColor(mainFont.getValue());
            mcontroller.render();
        });

        carriageColor.setOnAction((e)->{
            mcontroller.settingsClass.coursorColor = makePaintFromColor(carriageColor.getValue());
            mcontroller.render();
        });

        backgroundColor.setOnAction((e)->{
            mcontroller.background.setStyle("-fx-background-color: #" +
                    Integer.toHexString((int)(backgroundColor.getValue().getRed()*255))+
                    Integer.toHexString((int)(backgroundColor.getValue().getGreen()*255))+
                    Integer.toHexString((int)(backgroundColor.getValue().getBlue()*255))+";");
        });

        mainFont.setOnAction((e)->{
            mcontroller.settingsClass.mainColor = makePaintFromColor(mainFont.getValue());
            mcontroller.render();
        });

        lineColor.setOnAction((e)->{
            mcontroller.settingsClass.lineColor = makePaintFromColor(lineColor.getValue());
            mcontroller.render();
        });
    }

    private javafx.scene.paint.Color makeColorFromInt(Paint paint){
        return new Color((double)org.jetbrains.skija.Color.getR(paint.getColor())/255,
                (double)org.jetbrains.skija.Color.getG(paint.getColor())/255,
                (double)org.jetbrains.skija.Color.getB(paint.getColor())/255,
                1); //rgbo
    }

    private Paint makePaintFromColor(Color color){
        return new Paint().setColor(org.jetbrains.skija.Color.makeRGB(
                (int)(color.getRed()*255),
                (int)(color.getGreen()*255),
                (int)(color.getBlue()*255)));
    }

}
