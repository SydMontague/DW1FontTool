package net.digimonworld.dw1.fonttool;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class DW1FontTool extends Application {
    private static DW1FontTool instance;

    public DW1FontTool() {
        synchronized (DW1FontTool.class) {
            if (instance == null)
                instance = this;
            else
                throw new UnsupportedOperationException("Tried to instantiate the App's main class more than once.");
        }
    }

    public static DW1FontTool getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {
        ButtonType vanilla = new ButtonType("Vanilla", ButtonBar.ButtonData.NO);
        ButtonType custom = new ButtonType("Custom", ButtonBar.ButtonData.YES);
        ButtonType abort = new ButtonType("Abort", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(AlertType.NONE, "Which font type to modify?", vanilla, custom, abort);

        alert.setTitle("Select tool type");
        Optional<ButtonType> result = alert.showAndWait();
        FXMLLoader loader;

        if (!result.isPresent())
            return;
        if (result.get() == abort)
            return;
        if (result.get() == vanilla)
            loader = new FXMLLoader(DW1FontTool.class.getResource("MainWindow.fxml"));
        else
            loader = new FXMLLoader(DW1FontTool.class.getResource("FontCreator.fxml"));

        stage.setScene(loader.load());
        stage.setTitle("Digimon World 1 Font Tool");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
