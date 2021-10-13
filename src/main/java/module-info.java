module net.digimonworld.dw1.fonttool {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.swing;
    requires transitive net.digimonworld.decode.decode_tools;
    requires java.desktop;

    opens net.digimonworld.dw1.fonttool to javafx.fxml;
    exports net.digimonworld.dw1.fonttool;
}