package net.digimonworld.dw1.fonttool.font;

import net.digimonworld.dw1.fonttool.Utils;

public class DW1GlyphUS extends DW1Glyph {
    private byte width;
    private byte empty = 0;
    
    @Override
    public byte getWidth() {
        return width;
    }
    
    @Override
    public void setWidth(byte value) {
        this.width = value;
    }
    
    @Override
    public char getConvertedGlyph() {
        return Utils.convertShiftJISToChar(getGlyph());
    }
    
    public void setEmpty(byte empty) {
        this.empty = empty;
    }
    
    public byte getEmpty() {
        return empty;
    }
}
