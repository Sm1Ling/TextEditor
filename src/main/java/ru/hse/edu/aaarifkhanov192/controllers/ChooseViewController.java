package ru.hse.edu.aaarifkhanov192.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.viewclistener.ViewInfo;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.viewclistener.ViewListener;

public class ChooseViewController {

    private static ViewListener viewListener;

    public static void getListener(ViewListener listener) {
        viewListener = listener;
    }

    @FXML
    private CheckBox dontAsk;

    @FXML
    public void initialize() {
    }

    @FXML
    public void yes() {
        viewListener.onChoose(new ViewInfo(dontAsk.isSelected()));
        this.close();
    }

    @FXML
    public void close() {
        ((javafx.stage.Stage) dontAsk.getScene().getWindow()).close();
    }
}

