package net.digimonworld.dw1.fonttool;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
        FXMLLoader loader = new FXMLLoader(DW1FontTool.class.getResource("MainWindow.fxml"));
        stage.setScene(loader.load());
        stage.setTitle("Digimon World 1 Font Tool");
        stage.show();
    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
