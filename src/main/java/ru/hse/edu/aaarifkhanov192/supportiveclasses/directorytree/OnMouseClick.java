package ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree;

import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

public interface OnMouseClick {
    String getPathToTappedFile(MouseEvent mouseEvent, TreeView<String> treeView);
}
