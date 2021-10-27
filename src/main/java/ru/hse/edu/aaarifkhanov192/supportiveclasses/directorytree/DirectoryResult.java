package ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree;

import javafx.scene.control.TreeItem;

public class DirectoryResult {
    public final TreeItem<String> rootTreeNode;
    public final String dirPath;

    public DirectoryResult(TreeItem<String> rootTreeNode, String dirPath) {
        this.rootTreeNode = rootTreeNode;
        this.dirPath = dirPath;
    }
}
