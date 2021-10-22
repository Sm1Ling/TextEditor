package ru.hse.edu.aaarifkhanov192.controllers.directorytree;

import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

interface OnMouseClick {
    void getPathToTappedFile(MouseEvent mouseEvent, TreeView<String> treeView);
}
