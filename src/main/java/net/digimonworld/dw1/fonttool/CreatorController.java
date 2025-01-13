package net.digimonworld.dw1.fonttool;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.digimonworld.decodetools.core.FileAccess;

public class CreatorController {

  class CustomGlyph implements Comparable<CustomGlyph> {
    private final short codepoint;
    private short rows[];
    private int width;

    public CustomGlyph(short codepoint, int width) {
      this.codepoint = codepoint;
      this.width = width;
      this.rows = new short[32];
    }

    public CustomGlyph(FileAccess access) {
      this.codepoint = access.readShort();
      this.width = access.readShort();
      this.rows = access.readShortArray(32);
    }

    public void setWidth(int width) {
      this.width = width;
    }

    public int getWidth() {
      return width;
    }

    public short getCodepoint() {
      return codepoint;
    }

    public void togglePixel(int x, int y) {
      if (y >= rows.length)
        return;

      rows[y] ^= (1 << (15 - x));
    }

    public boolean getPixel(int x, int y) {
      if (y >= rows.length)
        return true;

      return (rows[y] & (1 << (15 - x))) != 0;
    }

    public short[] getRows(int height) {
      short copy[] = rows.clone();
      var mask = ((1 << width) - 1) << (Short.BYTES * 8 - width);

      for (int i = 0; i < copy.length; i++) {
        copy[i] = (short) ((copy[i] & mask) >> 8);
      }
      return Arrays.copyOf(copy, height);
    }

    @Override
    public int compareTo(CustomGlyph o) {
      return Comparator.comparing((CustomGlyph a) -> Short.toUnsignedInt(a.getCodepoint())).compare(this, o);
    }

    public void export(FileAccess access) {
      access.writeShort(codepoint);
      access.writeShort((short) width);
      access.writeShortArray(rows);
    }
  }

  class CustomFont {
    boolean isWide = false;
    int height;
    Map<Short, CustomGlyph> glyphs = new HashMap<>();
    Map<Short, Short> mappings = new HashMap<>();
    CustomGlyph fallback;

    public CustomFont(int height) {
      this.height = height;
      this.fallback = new CustomGlyph((short) 0xFFFF, height);
    }

    public void setHeight(int height) {
      this.height = height;
    }

    public int getHeight() {
      return height;
    }

    public List<CustomGlyph> getGlyphs() {
      List<CustomGlyph> list = new ArrayList<>();
      list.addAll(glyphs.values());
      list.add(fallback);
      return list;
    }

    public Map<Short, Short> getMappings() {
      return mappings;
    }

    public CustomGlyph getFallback() {
      return fallback;
    }

    public boolean addGlyph(CustomGlyph glyph) {
      if (glyph.getCodepoint() == fallback.getCodepoint())
        return false;
      if (glyphs.containsKey(glyph.getCodepoint()))
        return false;
      mappings.remove(glyph.getCodepoint()); // removes mapping if it exists, otherwise no-op
      glyphs.put(glyph.getCodepoint(), glyph);
      return true;
    }

    public void addMapping(short source, short target) {
      // fallback glyph, don't use
      if (source == fallback.getCodepoint() || target == fallback.getCodepoint())
        return;
      // can't map codepoint if it already is a glyph
      if (glyphs.containsKey(source))
        return;

      // instead of mapping to a mapping, map to it's glyph
      if (mappings.containsKey(target))
        target = mappings.get(target);

      // can't map codepoint if the target is not a glyph
      if (!glyphs.containsKey(target))
        return;

      mappings.put(source, target);
    }

    public void removeMapping(short codepoint) {
      mappings.remove(codepoint);
    }

    public void removeGlyph(CustomGlyph glyph) {
      glyphs.remove(glyph.getCodepoint());
      mappings.entrySet().removeIf((a) -> a.getValue() == glyph.getCodepoint());
    }

    public void export(FileAccess access) {
      access.writeByte(isWide ? (byte) 0 : (byte) 1);
      access.writeByte((byte) height);
      access.writeShort((short) (glyphs.size() + 1));

      for (var g : getGlyphs()) {
        g.export(access);
      }

      access.writeShort((short) getMappings().size());
      for( var m : getMappings().entrySet()) {
        access.writeShort(m.getKey());
        access.writeShort(m.getValue());
      }
    }

    public CustomFont(FileAccess access) {
      this.isWide = access.readByte() == 1;
      this.height = access.readByte();
      var glyphCount = access.readShort();

      for (int i = 0; i < glyphCount - 1; i++) {
        var glyph = new CustomGlyph(access);
        glyphs.put(glyph.codepoint, glyph);
      }

      this.fallback = new CustomGlyph(access);

      var mappingCount = access.readShort();
      for (int i = 0; i < mappingCount; i++) {
        mappings.put(access.readShort(), access.readShort());
      }
    }

    public String exportCPP() {
      var glyphStructString = """
          struct MyFont {
            uint8_t height;
            bool isWide;
            uint16_t mappingCount;
          };

          struct GlyphMapping {
            uint16_t codepoint;
            uint16_t index;
          };

          struct MyGlyph {
            uint8_t width;
            uint8_t rows[%d]; // height
          };
          """;

      var struct = String.format(glyphStructString, height);
      var font = String.format("MyFont myFont = { .height = %d, .isWide = %b, .mappingCount = %d,};\n", height, isWide,
          glyphs.size() + mappings.size() + 1);
      var mapping = """
          GlyphMapping myMapping[%1$d] = { %3$s };

          MyGlyph myGlyphs[%2$d] = { %4$s };
          """;
      StringBuilder mappingEntries = new StringBuilder();
      StringBuilder glyphEntries = new StringBuilder();
      int index = 0;
      for (var i : getGlyphs()) {
        mappingEntries.append(String.format(" { .codepoint = 0x%x, .index = %d },", i.codepoint, index));

        StringBuilder row = new StringBuilder();
        for (var a : i.getRows(height)) {
          row.append(String.format("0x%02x", ~a & 0xFF));
          row.append(",");
        }
        row.deleteCharAt(row.length() - 1);

        glyphEntries.append(String.format("{ .width = %d, .rows = { %s }},", i.width, row.toString()));
        index++;
      }

      for(var i :  getMappings().entrySet()) {
        mappingEntries.append(String.format(" { .codepoint = 0x%x, .index = %d },", i.getKey(), getGlyphs().stream().map(a -> a.getCodepoint()).toList().indexOf(i.getValue())));

      }

      String s = String.format(mapping, getGlyphs().size() + getMappings().size(), getGlyphs().size(), mappingEntries.toString(), glyphEntries.toString());

      return struct + font + s;
    }
  }

  @FXML
  private Scene root;
  @FXML
  private Spinner<Integer> widthSpinner;
  @FXML
  private Spinner<Integer> heightSpinner;
  @FXML
  private ImageView glyphPixel;
  @FXML
  private ListView<CustomGlyph> glyphList;
  @FXML
  private Button addButton;
  @FXML
  private TextField addInput;
  @FXML
  private TextField addMappingInput;
  @FXML
  private ListView<Map.Entry<Short, Short>> mappingsList;

  private CustomFont font;
  private BufferedImage glyphImage;
  private CustomGlyph activeGlyph;

  private char decode(short codepoint) {
    if ((codepoint & 0x8000) == 0)
      codepoint = (short) (codepoint << 8);
    ByteBuffer buff = ByteBuffer.allocate(2);
    buff.putShort(codepoint);
    buff.flip();

    return Charset.forName("Shift-JIS").decode(buff).get();
  }

  private void updateList() {
    glyphList.getItems().setAll(font.getGlyphs());
    glyphList.getItems().sort((a, b) -> a.compareTo(b));
    glyphList.getSelectionModel().select(activeGlyph);

    mappingsList.getItems().setAll(font.getMappings().entrySet());
    mappingsList.getItems().sort((a, b) -> a.getKey().compareTo(b.getKey()));
  }

  public void actionExportAsCPP() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Where to save CPP file?");
    chooser.setInitialDirectory(new File("."));
    chooser.getExtensionFilters().add(new ExtensionFilter("C++", "*.cpp"));
    File selected = chooser.showSaveDialog(root.getWindow());

    if (selected == null)
      return;

    try {
      Files.writeString(selected.toPath(), font.exportCPP());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void actionSave() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Where to save the font file?");
    chooser.setInitialDirectory(new File("."));
    chooser.getExtensionFilters().add(new ExtensionFilter("DW1 MyFont", "*.myfont"));
    File selected = chooser.showSaveDialog(root.getWindow());

    if (selected == null)
      return;

    try (FileAccess access = new FileAccess(selected)) {
      font.export(access);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void actionLoad() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("What font to load?");
    chooser.setInitialDirectory(new File("."));
    chooser.getExtensionFilters().add(new ExtensionFilter("DW1 MyFont", "*.myfont"));
    File selected = chooser.showOpenDialog(root.getWindow());

    if (selected == null)
      return;

    try (FileAccess access = new FileAccess(selected)) {
      font = new CustomFont(access);
      activeGlyph = font.getFallback();
      heightSpinner.getValueFactory().setValue(font.height);
      updateImage();
      updateList();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void actionNew() {
    font = new CustomFont(8);
    activeGlyph = font.getFallback();
    heightSpinner.getValueFactory().setValue(8);

    updateImage();
    updateList();
  }

  public void onAddMapping() {
    if (addMappingInput.getLength() == 0)
      return;
    var input = addMappingInput.getText(0, 1);
    var buf = Charset.forName("Shift-JIS").encode(input);
    if (buf.remaining() == 0)
      return;

    short codepoint = (short) 0xFFFF;
    if (buf.remaining() == 1)
      codepoint = buf.get();
    else if (buf.remaining() == 2)
      codepoint = buf.getShort();

    font.addMapping(codepoint, activeGlyph.getCodepoint());
    addMappingInput.clear();

    updateList();
  }

  @FXML
  public void onAdd() {
    if (addInput.getLength() == 0)
      return;
    var input = addInput.getText(0, 1);
    var buf = Charset.forName("Shift-JIS").encode(input);
    if (buf.remaining() == 0)
      return;

    short codepoint = (short) 0xFFFF;
    if (buf.remaining() == 1)
      codepoint = buf.get();
    else if (buf.remaining() == 2)
      codepoint = buf.getShort();

    CustomGlyph g = new CustomGlyph((short) codepoint, 8);
    font.addGlyph(g);
    addInput.clear();

    updateList();
    glyphList.getSelectionModel().select(g);
  }

  @FXML
  public void initialize() throws IOException {
    font = new CustomFont(heightSpinner.getValue().byteValue());
    activeGlyph = font.getFallback();

    widthSpinner.valueProperty().addListener((a, b, c) -> {
      activeGlyph.setWidth(c.byteValue());
      updateImage();
    });
    heightSpinner.valueProperty().addListener((a, b, c) -> {
      font.setHeight(c.byteValue());
      updateImage();
    });

    glyphList.setCellFactory(a -> new ListCell<>() {
      @Override
      protected void updateItem(CustomGlyph item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty)
          setText("");
        else
          setText(String.format("0x%04X - %s", item.getCodepoint(), decode(item.getCodepoint())));
      }
    });

    mappingsList.setCellFactory(a -> new ListCell<>() {
      @Override
      protected void updateItem(Map.Entry<Short, Short> item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty)
          setText("");
        else
          setText(String.format("0x%04x %s -> 0x%04x %s", item.getKey(), decode(item.getKey()), item.getValue(), decode(item.getValue())));
      }
    });

    glyphList.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
      if (c == null)
        return;
      activeGlyph = c;
      widthSpinner.getValueFactory().setValue((int) c.getWidth());
      updateImage();
    });
    glyphList.setOnKeyPressed(e -> {
      if (e.getCode() != KeyCode.DELETE)
        return;
      if (activeGlyph == font.getFallback())
        return;

      font.removeGlyph(activeGlyph);
      activeGlyph = font.getFallback();
      updateList();
    });

    mappingsList.setOnKeyPressed(e -> {
      if (e.getCode() != KeyCode.DELETE)
        return;

      font.removeMapping(mappingsList.getSelectionModel().getSelectedItem().getKey());

      updateList();
    });

    updateList();
    updateImage();
  }

  public void gridClicked(MouseEvent event) {
    int x = (int) (event.getX() / 32);
    int y = (int) (event.getY() / 32);

    activeGlyph.togglePixel(x, y);
    updateImage();
  }

  private void updateImage() {
    var width = activeGlyph.width;
    var height = font.height;

    glyphImage = new BufferedImage(width * 32, height * 32, BufferedImage.TYPE_INT_RGB);
    var g = glyphImage.createGraphics();
    g.clearRect(0, 0, width * 32, height * 32);

    for (int i = 0; i < height; i++)
      for (int j = 0; j < width; j++) {
        if (activeGlyph.getPixel(j, i))
          g.setColor(Color.BLACK);
        else
          g.setColor(Color.WHITE);

        g.fillRect(j * 32, i * 32, 32, 32);
      }

    g.setColor(Color.GRAY);
    for (int i = 0; i < height - 1; i++)
      g.drawLine(0, (i + 1) * 32, width * 32, (i + 1) * 32);
    for (int i = 0; i < width - 1; i++)
      g.drawLine((i + 1) * 32, 0, (i + 1) * 32, height * 32);

    glyphPixel.setImage(SwingFXUtils.toFXImage(glyphImage, null));
  }

}
