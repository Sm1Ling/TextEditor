package ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import ru.hse.edu.aaarifkhanov192.controllers.dirchanges.WatchFileChanges;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Класс представляет работу с файлами и верхней директорией, также заполняет {@link TreeItem}
 * для установки его в {@link ru.hse.edu.aaarifkhanov192.controllers.MainAppController}.
 */
public class DirectoryTree implements OnMouseClick {

    private String path;
    private DirectoryResult myRes;

    public DirectoryTree(String path) {
        this.path = path;
    }

    public DirectoryTree(File directory){
        this.path = directory.getAbsolutePath();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Рекурсивный проход по дереву файлов <code>file</code> и заполнение <code>{@link TreeItem}</code>
     * <code>root</code>.
     *
     * @param file Текущий файл.
     * @param root Элемент для заполнения.
     */
    private void recursiveCreate(File file, TreeItem<String> root) {
        try {
            if (file.isDirectory()) {
                TreeItem<String> ti = new TreeItem<>(file.getName());
                root.getChildren().add(ti);
                var listF = file.listFiles();
                if (listF != null) {
                    for (File f : listF) {
                        recursiveCreate(f, ti);
                    }
                }
            } else if (file.isFile()) {
                TreeItem<String> ti = new TreeItem<>(file.getName());

                root.getChildren().add(ti);
            }
        } catch (Throwable e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    /**
     * Создает и заполняет корневой <code>{@link TreeItem}</code> и возращает путь к верхней папке и корень.
     *
     * @return Возвращает {@link DirectoryResult}.
     */
    public DirectoryResult fillRoot() {
        Path currRelPath = Path.of(getPath());
        Path absPath = currRelPath.toAbsolutePath();

        File[] filesInFldr = new File(path).listFiles();
        TreeItem<String> rootTreeNode = new TreeItem<>(absPath.getName(absPath.getNameCount() - 1).toString());

        if (filesInFldr != null) {
            for (var f : filesInFldr) {
                recursiveCreate(f, rootTreeNode);
            }
        }

        myRes = new DirectoryResult(rootTreeNode, path);
        return myRes;
    }


    @Override
    public String getPathToTappedFile(MouseEvent mouseEvent, TreeView<String> treeView) {
        if (mouseEvent.getClickCount() == 2) {
            //TODO Обработка дабл клика
            MultipleSelectionModel<TreeItem<String>> sm = treeView.getSelectionModel();
            TreeItem<String> pp = sm.getSelectedItem();
            if (pp != null) {
                boolean isFile = pp.getValue().contains(".") && (pp.getChildren() == null
                        || pp.getChildren().isEmpty());

                if (isFile) {
                    StringBuilder res = new StringBuilder();
                    while (pp != null) {
                        res.insert(0, pp.getValue() + File.separator);
                        pp = pp.getParent();
                        //Чтобы не дошел до корня, чтобы не было пути <root>/<root>/file.txt/
                        if (pp.getParent() == null) {
                            break;
                        }
                    }
                    return myRes.dirPath + File.separator + res;
                }
            }
        }
        return null;
    }
}

