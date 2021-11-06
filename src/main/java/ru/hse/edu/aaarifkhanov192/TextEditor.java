package ru.hse.edu.aaarifkhanov192;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyInterval;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;

import java.io.IOException;

public class TextEditor extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextEditor.class.getResource("/ru.hse.edu.aaarifkhanov192/editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TextEditor");
        stage.setScene(scene);

        stage.show();
    }

    /*
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
    */




    public static void main(String[] args) {
        launch();
//        MyIntervalTree<Integer> mit = new MyIntervalTree<>();
////        mit.insert(4, 4, 5);
////        mit.insert(3, 6, 5);
////        mit.insert(3, 6, 5);
////        mit.insert(3, 6, 5);
////        mit.insert(3, 6, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(4, 8, 5);
////        mit.insert(0, 6, 5);
////        mit.insert(0, 6, 5);
////        mit.insert(0, 0, 5);
////        mit.insert(0, 0, 5);
////        mit.insert(0, 32, 5);
////        mit.insert(0, 32, 5);
////        mit.delete(new MyInterval(4, 4));
//        mit.insert(15, 15, 3);
//        mit.insert(18, 20, 3);
//        mit.insert(25, 25, 3);
//        mit.insert(28, 28, 3);
//        mit.insert(31, 31, 3);
//        mit.insert(34, 34, 3);
//        mit.insert(40, 40, 3);
//        mit.insert(42, 48, 3);
//        mit.insert(49, 49, 3);
//        mit.insert(58, 58, 3);
//        mit.insert(60, 64, 3);
//        mit.insert(66, 66, 3);
//        mit.insert(67, 67, 3);
//        mit.insert(68, 69, 3);
//        mit.insert(70, 71, 3);
//        mit.insert(72, 72, 3);
//        mit.insert(74, 75, 3);
//        mit.insert(77, 83, 3);
//        mit.insert(84, 84, 3);
//        mit.insert(87, 91, 3);
//        mit.insert(95, 97, 3);
//        mit.insert(100, 101, 3);
//        mit.insert(102, 102, 3);
//        mit.insert(104, 105, 3);
//        mit.insert(107, 108, 3);
//        mit.insert(110, 111, 3);
//        mit.insert(118, 118, 3);
//        mit.insert(120, 120, 3);
//        mit.insert(122, 122, 3);
//        mit.insert(123, 123, 3);
//        mit.insert(124, 124, 3);
//        mit.insert(126, 141, 3);
//        mit.insert(145, 147, 3);
//        mit.insert(150, 151, 3);
//        mit.insert(152, 152, 3);
//        mit.insert(154, 155, 3);
//        mit.insert(157, 158, 3);
//        mit.insert(160, 161, 3);
//        mit.insert(169, 169, 3);
//        mit.insert(171, 171, 3);
//        mit.insert(173, 173, 3);
//        mit.insert(174, 174, 3);
//        mit.insert(176, 178, 3);
//        mit.insert(179, 179, 3);
//        mit.insert(180, 180, 3);
//        mit.insert(182, 198, 3);
//        mit.insert(203, 205, 3);
//        mit.insert(208, 209, 3);
//        mit.insert(210, 210, 3);
//        mit.insert(212, 213, 3);
//        mit.insert(215, 215, 3);
//        mit.insert(217, 218, 3);
//        mit.insert(222, 226, 3);
//        mit.insert(236, 237, 3);
//        mit.insert(239, 239, 3);
//        mit.insert(245, 247, 3);
//        mit.insert(250, 251, 3);
//        mit.insert(253, 253, 3);
//        mit.insert(254, 254, 3);
//        mit.insert(256, 257, 3);
//        mit.insert(259, 260, 3);
//        mit.insert(262, 263, 3);
//        mit.insert(271, 272, 3);
//        mit.insert(275, 275, 3);
//        mit.insert(277, 277, 3);
//        mit.insert(278, 278, 3);
//        mit.insert(280, 280, 3);
//        mit.insert(285, 285, 3);
//        mit.insert(287, 290, 3);
//        mit.insert(304, 305, 3);
//        mit.insert(307, 307, 3);
//        mit.insert(315, 316, 3);
//        mit.insert(318, 318, 3);
//        mit.insert(320, 320, 3);
//        mit.insert(321, 321, 3);
//        mit.insert(328, 328, 3);
//        mit.insert(330, 330, 3);
//        mit.insert(331, 332, 3);
//        mit.insert(334, 334, 3);
//        mit.insert(339, 339, 3);
//        mit.insert(340, 340, 3);
//        mit.insert(347, 347, 3);
//        mit.insert(352, 352, 3);
//        mit.insert(353, 354, 3);
//        mit.insert(359, 361, 3);
//        mit.insert(362, 362, 3);
//        mit.insert(367, 369, 3);
//        mit.insert(372, 373, 3);
//        mit.insert(374, 374, 3);
//        mit.insert(376, 377, 3);
//        mit.insert(379, 380, 3);
//        mit.insert(382, 383, 3);
//        mit.insert(394, 394, 3);
//        mit.insert(396, 396, 3);
//        mit.insert(398, 398, 3);
//        mit.insert(399, 399, 3);
//        mit.insert(401, 403, 3);
//        mit.insert(404, 404, 3);
//        mit.insert(406, 408, 3);
//        mit.insert(409, 409, 3);
//
//        mit.delete(new MyInterval(87, 91));
//
//        for (var t:
//             mit.) {
//
//        }


//        mit.shiftIntervals(1, 2);
//        for (var t:
//             mit.getTree()) {
//            System.out.println(t.getInterval());
//        }
//        System.out.println(mit.getTree());
    }
}
