package ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import ru.hse.edu.aaarifkhanov192.controllers.dirchanges.WatchFileChanges;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.DirectoryResult;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree.OnMouseClick;

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
    public DirectoryTree(String path){
        setPath(path);
    }

    private String activeFileText = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Рекурсивный проход по дереву файлов <code>file</code> и заполнение <code>{@link TreeItem}</code> <code>root</code>.
     * @param file Текущий файл.
     * @param root Элемент для заполнения.
     */
    private void recursiveCreate(File file, TreeItem<String> root) {
        if (file.isDirectory()) {
            TreeItem<String> ti = new TreeItem<>(file.getName());
            root.getChildren().add(ti);
            for (File f : file.listFiles()) {
                recursiveCreate(f, ti);
            }
        } else if (file.isFile()) {
            TreeItem<String> ti = new TreeItem<>(file.getName());

            root.getChildren().add(ti);
        }
    }

    /**
     * Создает и заполняет корневой <code>{@link TreeItem}</code> и возращает путь к верхней папке и корень.
     * @return Возвращает {@link DirectoryResult}.
     */
    public DirectoryResult fillRoot(){
        Path currRelPath = Path.of(getPath());
        Path absPath = currRelPath.toAbsolutePath();
        //Наш отец -- папка, в которой лежит файл.
        String dir = absPath.getParent().toString();
        File[] filesInFldr = new File(dir).listFiles();
        TreeItem<String> rootTreeNode = new TreeItem<>(absPath.getName(absPath.getNameCount() - 2).toString());

        if (filesInFldr != null) {
            for (var f : filesInFldr) {
                recursiveCreate(f, rootTreeNode);
            }
        }

        myRes = new DirectoryResult(rootTreeNode, dir);
        return myRes;
    }

    //TODO ?Паттерн стратегия для разного заполнения текста -- роуп, обычный стринг

    /**
     * Считывает текст по заданному пути файла <code>path</code>.
     * @param path Путь до файла.
     */
    public String readText(String path) {
        StringBuilder txt = new StringBuilder();
        try {
            File file = new File(path);
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) {
                String data = scan.nextLine();
                txt.append(data).append("\n");
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return txt.toString();
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
                        if (pp.getParent() == null)
                            break;
                    }
                    return myRes.dirPath + File.separator + res;
                }
            }
        }
        return null;
    }
}

