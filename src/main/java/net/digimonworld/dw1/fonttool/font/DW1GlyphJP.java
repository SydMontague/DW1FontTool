package net.digimonworld.dw1.fonttool.font;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DW1GlyphJP extends DW1Glyph {
    @Override
    public char getConvertedGlyph() {
        ByteBuffer buff = ByteBuffer.allocate(2);
        buff.putShort(getGlyph());
        buff.flip();
        
        return Charset.forName("Shift-JIS").decode(buff).get();
    }
    
    @Override
    public byte getWidth() {
        return 12;
    }
    
    @Override
    public void setWidth(byte value) {
        // no op
    }
}
