package net.digimonworld.dw1.fonttool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

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
import net.digimonworld.decodetools.core.Access;
import net.digimonworld.decodetools.core.FileAccess;

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
    
    private DW1Font font;
    private BufferedImage glyphImage = new BufferedImage(12 * 32, 11 * 32, BufferedImage.TYPE_INT_RGB);
    private DW1Glyph activeGlyph;
    
    @FXML
    public void initialize() throws IOException {
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Please select font to modify?");
        chooser.setInitialDirectory(new File("."));
        chooser.getExtensionFilters().add(new ExtensionFilter("DW1Font", "*.dw1f"));
        File selected = chooser.showOpenDialog(root.getWindow());
        
        try (FileAccess access = new FileAccess(selected)) {
            font = new DW1Font(access);
        }
        
        this.activeGlyph = font.fallback;
        
        widthSpinner.valueProperty().addListener((a, b, c) -> {
            activeGlyph.width = c.byteValue();
            updateTextbox();
        });
        
        list.getItems().addAll(font.map.values());
        list.getItems().add(font.fallback);
        
        list.setCellFactory(a -> new ListCell<>() {
            @Override
            protected void updateItem(DW1Glyph item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null)
                    setText(String.format("0x%4X - %s", item.glyph, convertShiftJISToChar(item.glyph)));
            }
        });
        
        list.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
            activeGlyph = c;
            widthSpinner.getValueFactory().setValue((int) c.width);
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
        chooser.getExtensionFilters().add(new ExtensionFilter("DW1Font", "*.dw1f"));
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
            var c = font.map.getOrDefault(convertCharToShiftJIS(letter), font.fallback);
            
            if (xOffset + c.width >= imageWidth / 3 || letter == '\n') {
                xOffset = 1;
                yPos += 14;
            }
            
            if (letter != '\n') {
                graphics.drawImage(c.toImage(Color.WHITE), 4 * ((xOffset + 3) / 4), yPos, null);
                xOffset += c.width;
            }
        }
        
        image.setImage(SwingFXUtils.toFXImage(a, null));
    }
    
    private char convertShiftJISToChar(short glyph) {
        switch (Short.toUnsignedInt(glyph)) {
            case 0x824F:
                return '0';
            case 0x8250:
                return '1';
            case 0x8251:
                return '2';
            case 0x8252:
                return '3';
            case 0x8253:
                return '4';
            case 0x8254:
                return '5';
            case 0x8255:
                return '6';
            case 0x8256:
                return '7';
            case 0x8257:
                return '8';
            case 0x8258:
                return '9';
            
            case 0x8260:
                return 'A';
            case 0x8261:
                return 'B';
            case 0x8262:
                return 'C';
            case 0x8263:
                return 'D';
            case 0x8264:
                return 'E';
            case 0x8265:
                return 'F';
            case 0x8266:
                return 'G';
            case 0x8267:
                return 'H';
            case 0x8268:
                return 'I';
            case 0x8269:
                return 'J';
            case 0x826A:
                return 'K';
            case 0x826B:
                return 'L';
            case 0x826C:
                return 'M';
            case 0x826D:
                return 'N';
            case 0x826E:
                return 'O';
            case 0x826F:
                return 'P';
            case 0x8270:
                return 'Q';
            case 0x8271:
                return 'R';
            case 0x8272:
                return 'S';
            case 0x8273:
                return 'T';
            case 0x8274:
                return 'U';
            case 0x8275:
                return 'V';
            case 0x8276:
                return 'W';
            case 0x8277:
                return 'X';
            case 0x8278:
                return 'Y';
            case 0x8279:
                return 'Z';
            
            case 0x8281:
                return 'a';
            case 0x8282:
                return 'b';
            case 0x8283:
                return 'c';
            case 0x8284:
                return 'd';
            case 0x8285:
                return 'e';
            case 0x8286:
                return 'f';
            case 0x8287:
                return 'g';
            case 0x8288:
                return 'h';
            case 0x8289:
                return 'i';
            case 0x828A:
                return 'j';
            case 0x828B:
                return 'k';
            case 0x828C:
                return 'l';
            case 0x828D:
                return 'm';
            case 0x828E:
                return 'n';
            case 0x828F:
                return 'o';
            case 0x8290:
                return 'p';
            case 0x8291:
                return 'q';
            case 0x8292:
                return 'r';
            case 0x8293:
                return 's';
            case 0x8294:
                return 't';
            case 0x8295:
                return 'u';
            case 0x8296:
                return 'v';
            case 0x8297:
                return 'w';
            case 0x8298:
                return 'x';
            case 0x8299:
                return 'y';
            case 0x829A:
                return 'z';
            
            case 0x8140:
                return ' ';
            case 0x8142:
                return '.';
            case 0x8143:
                return ',';
            case 0x8146:
                return ':';
            case 0x8147:
                return ';';
            case 0x8148:
                return '?';
            case 0x8149:
                return '!';
            case 0x815F:
                return '\\';
            case 0x8175:
                return '\'';
            case 0x8176:
                return '"';
            case 0x817B:
                return '+';
            case 0x817C:
                return '-';
            case 0x8181:
                return '=';
            
            case 0x819B:
                return '◯';
            case 0x81A0:
                return '□';
            case 0x81A2:
                return '╳';
            default:
                return '€';
        }
    }
    
    public int convertCharToShiftJIS(char val) {
        switch (val) {
            default:
                return val;
            case '\n':
                return 0x0D00;
            
            case '0':
                return 0x824F;
            case '1':
                return 0x8250;
            case '2':
                return 0x8251;
            case '3':
                return 0x8252;
            case '4':
                return 0x8253;
            case '5':
                return 0x8254;
            case '6':
                return 0x8255;
            case '7':
                return 0x8256;
            case '8':
                return 0x8257;
            case '9':
                return 0x8258;
            
            case 'A':
                return 0x8260;
            case 'B':
                return 0x8261;
            case 'C':
                return 0x8262;
            case 'D':
                return 0x8263;
            case 'E':
                return 0x8264;
            case 'F':
                return 0x8265;
            case 'G':
                return 0x8266;
            case 'H':
                return 0x8267;
            case 'I':
                return 0x8268;
            case 'J':
                return 0x8269;
            case 'K':
                return 0x826A;
            case 'L':
                return 0x826B;
            case 'M':
                return 0x826C;
            case 'N':
                return 0x826D;
            case 'O':
                return 0x826E;
            case 'P':
                return 0x826F;
            case 'Q':
                return 0x8270;
            case 'R':
                return 0x8271;
            case 'S':
                return 0x8272;
            case 'T':
                return 0x8273;
            case 'U':
                return 0x8274;
            case 'V':
                return 0x8275;
            case 'W':
                return 0x8276;
            case 'X':
                return 0x8277;
            case 'Y':
                return 0x8278;
            case 'Z':
                return 0x8279;
            
            case 'a':
                return 0x8281;
            case 'b':
                return 0x8282;
            case 'c':
                return 0x8283;
            case 'd':
                return 0x8284;
            case 'e':
                return 0x8285;
            case 'f':
                return 0x8286;
            case 'g':
                return 0x8287;
            case 'h':
                return 0x8288;
            case 'i':
                return 0x8289;
            case 'j':
                return 0x828A;
            case 'k':
                return 0x828B;
            case 'l':
                return 0x828C;
            case 'm':
                return 0x828D;
            case 'n':
                return 0x828E;
            case 'o':
                return 0x828F;
            case 'p':
                return 0x8290;
            case 'q':
                return 0x8291;
            case 'r':
                return 0x8292;
            case 's':
                return 0x8293;
            case 't':
                return 0x8294;
            case 'u':
                return 0x8295;
            case 'v':
                return 0x8296;
            case 'w':
                return 0x8297;
            case 'x':
                return 0x8298;
            case 'y':
                return 0x8299;
            case 'z':
                return 0x829A;
            
            case ' ':
                return 0x8140;
            case '.':
                return 0x8142;
            case ',':
                return 0x8143;
            case ':':
                return 0x8146;
            case ';':
                return 0x8147;
            case '?':
                return 0x8148;
            case '!':
                return 0x8149;
            case '\\':
                return 0x815F;
            case '\'':
                return 0x8175;
            case '"':
                return 0x8176;
            case '+':
                return 0x817B;
            case '-':
                return 0x817C;
            case '=':
                return 0x8181;
            case '◯':
                return 0x819B;
            case '□':
                return 0x81A0;
            case '╳':
                return 0x81A2;
        }
    }
    
    static class DW1Font {
        Map<Integer, DW1Glyph> map = new TreeMap<>();
        DW1Glyph fallback = new DW1Glyph();
        
        public DW1Font(Access access) {
            for (long i = 0; i < 78; i++) {
                short charCode = access.readShort(i * 2);
                DW1Glyph data = new DW1Glyph();
                data.glyph = charCode;
                data.pixelData = access.readShortArray(11, 0xA0 + i * 0x18);
                data.width = access.readByte(0xA0 + i * 0x18 + 22);
                data.empty = access.readByte(0xA0 + i * 0x18 + 23);
                
                map.put(Short.toUnsignedInt(charCode), data);
            }
            
            fallback.pixelData = access.readShortArray(11, 0xA0 + 78L * 0x18);
            fallback.width = access.readByte(0xA0 + 78L * 0x18 + 22);
            fallback.empty = access.readByte(0xA0 + 78L * 0x18 + 23);
        }
        
        public void export(File file) {
            try (FileAccess access = new FileAccess(file)) {
                
                long i = 0;
                for (DW1Glyph g : map.values()) {
                    access.writeShort(g.glyph, i * 2);
                    access.writeShortArray(g.pixelData, 0xA0 + i * 0x18);
                    access.writeByte(g.width, 0xA0 + i * 0x18 + 22);
                    access.writeByte(g.empty, 0xA0 + i * 0x18 + 23);
                    i++;
                }
                
                access.writeShort(fallback.glyph, 78L * 2);
                access.writeShortArray(fallback.pixelData, 0xA0 + 78L * 0x18);
                access.writeByte(fallback.width, 0xA0 + 78L * 0x18 + 22);
                access.writeByte(fallback.empty, 0xA0 + 78L * 0x18 + 23);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static class DW1Glyph {
        public short glyph;
        public short[] pixelData = new short[11];
        public byte width;
        public byte empty = 0;
        
        void togglePixel(int x, int y) {
            pixelData[y] ^= (1 << (15 - x));
        }
        
        boolean getPixel(int x, int y) {
            return (pixelData[y] & (1 << (15 - x))) != 0;
        }
        
        BufferedImage toImage(Color color) {
            BufferedImage image = new BufferedImage(width, 11, BufferedImage.TYPE_INT_RGB);
            
            for (int i = 0; i < 11; i++)
                for (int j = 0; j < width; j++) {
                    
                    boolean isSet = (pixelData[i] & (1 << (15 - j))) == 0;
                    
                    if (isSet)
                        image.setRGB(j, i, color.getRGB());
                    
                }
            
            return image;
        }
    }
}
