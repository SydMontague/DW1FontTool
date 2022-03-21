package net.digimonworld.dw1.fonttool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.digimonworld.decodetools.core.FileAccess;
import net.digimonworld.dw1.fonttool.font.DW1Font;
import net.digimonworld.dw1.fonttool.font.DW1FontJP;
import net.digimonworld.dw1.fonttool.font.DW1FontUS;
import net.digimonworld.dw1.fonttool.font.DW1Glyph;

public class Controller {
    @FXML
    private Scene root;
    @FXML
    private ImageView image;
    @FXML
    private TextArea inputBox;
    @FXML
    private ImageView glyphImageView;
    @FXML
    private ListView<DW1Glyph> list;
    @FXML
    private Spinner<Integer> widthSpinner;
    
    @SuppressWarnings("rawtypes")
    private DW1Font font;
    private BufferedImage glyphImage = new BufferedImage(12 * 32, 11 * 32, BufferedImage.TYPE_INT_RGB);
    private DW1Glyph activeGlyph;
    
    @FXML
    public void initialize() throws IOException {
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Please select font to modify?");
        chooser.setInitialDirectory(new File("."));
        chooser.getExtensionFilters().add(new ExtensionFilter("DW1FontUS", "*.dw1f"));
        chooser.getExtensionFilters().add(new ExtensionFilter("DW1FontJP", "*.dw1fj"));
        File selected = chooser.showOpenDialog(root.getWindow());
        
        try (FileAccess access = new FileAccess(selected)) {
            if (selected.getName().endsWith(".dw1f"))
                font = new DW1FontUS(access);
            else if (selected.getName().endsWith(".dw1fj"))
                font = new DW1FontJP(access);
        }
        
        this.activeGlyph = font.getFallback();
        
        widthSpinner.valueProperty().addListener((a, b, c) -> {
            activeGlyph.setWidth(c.byteValue());
            updateTextbox();
        });
        
        list.getItems().addAll(font.getMap().values());
        list.getItems().add(font.getFallback());
        
        list.setCellFactory(a -> new ListCell<>() {
            @Override
            protected void updateItem(DW1Glyph item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null)
                    setText(String.format("0x%4X - %s", item.getGlyph(), item.getConvertedGlyph()));
            }
        });
        
        list.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
            activeGlyph = c;
            widthSpinner.getValueFactory().setValue((int) c.getWidth());
            updateImage();
        });
        
        inputBox.textProperty().addListener((a, b, c) -> updateTextbox());
        
        updateImage();
        updateTextbox();
    }
    
    private void updateImage() {
        var g = glyphImage.createGraphics();
        g.clearRect(0, 0, 12 * 32, 11 * 32);
        
        for (int i = 0; i < 11; i++)
            for (int j = 0; j < 12; j++) {
                if (activeGlyph.getPixel(j, i))
                    g.setColor(Color.BLACK);
                else
                    g.setColor(Color.WHITE);
                
                g.fillRect(j * 32, i * 32, 32, 32);
            }
        
        g.setColor(Color.GRAY);
        for (int i = 0; i < 10; i++)
            g.drawLine(0, (i + 1) * 32, 384, (i + 1) * 32);
        for (int i = 0; i < 11; i++)
            g.drawLine((i + 1) * 32, 0, (i + 1) * 32, 352);
        
        glyphImageView.setImage(SwingFXUtils.toFXImage(glyphImage, null));
    }
    
    public void gridClicked(MouseEvent event) {
        int x = (int) (event.getX() / 32);
        int y = (int) (event.getY() / 32);
        
        activeGlyph.togglePixel(x, y);
        updateImage();
        updateTextbox();
    }
    
    public void export() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Where to save font file?");
        chooser.setInitialDirectory(new File("."));
        chooser.getExtensionFilters().add(new ExtensionFilter("DW1FontUS", "*.dw1f"));
        chooser.getExtensionFilters().add(new ExtensionFilter("DW1FontJP", "*.dw1fj"));
        File selected = chooser.showSaveDialog(root.getWindow());
        
        if (selected == null)
            return;
        
        font.export(selected);
    }
    
    public void updateTextbox() {
        String toRender = inputBox.getText();
        
        int imageWidth = (int) image.getFitWidth();
        int imageHeight = (int) image.getFitHeight();
        
        BufferedImage a = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = a.createGraphics();
        graphics.scale(3, 3);
        int xOffset = 1;
        int yPos = 1;
        
        for (var letter : toRender.toCharArray()) {
            DW1Glyph c = (DW1Glyph) font.getMap().getOrDefault(font.convertChar(letter), font.getFallback());
            
            if (xOffset + c.getWidth() >= imageWidth / 3 || letter == '\n') {
                xOffset = 1;
                yPos += 14;
            }
            
            if (letter != '\n') {
                graphics.drawImage(c.toImage(Color.WHITE), 4 * ((xOffset + 3) / 4), yPos, null);
                xOffset += c.getWidth();
            }
        }
        
        image.setImage(SwingFXUtils.toFXImage(a, null));
    }
    
}
