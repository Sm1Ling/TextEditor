module ru.hse.edu.aaarifkhanov192.TextEditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.skija.windows;
    requires org.jetbrains.skija.shared;
    requires org.antlr.antlr4.runtime;
    //if meaven doesn't recognize -- use Project Structure - Modules - Add Jar, select it from "lib" folder
    requires ropes; //library taken from http://www.ahmadsoft.org/source/xref/ropes-1.2.5/
    requires java.desktop;
    requires java.datatransfer;

    exports ru.hse.edu.aaarifkhanov192;
    exports ru.hse.edu.aaarifkhanov192.controllers;
    exports ru.hse.edu.aaarifkhanov192.supportiveclasses;
    exports ru.hse.edu.aaarifkhanov192.supportiveclasses.directorytree;
    exports ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree;
    exports ru.hse.edu.aaarifkhanov192.supportiveclasses.viewclistener;

    opens ru.hse.edu.aaarifkhanov192.supportiveclasses to javafx.fxml;
    opens ru.hse.edu.aaarifkhanov192 to javafx.fxml;
    opens ru.hse.edu.aaarifkhanov192.controllers to javafx.fxml;
    opens ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree to javafx.fxml;
    opens ru.hse.edu.aaarifkhanov192.supportiveclasses.viewclistener to javafx.fxml;
}