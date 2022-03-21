package net.digimonworld.dw1.fonttool.font;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import net.digimonworld.decodetools.core.Access;
import net.digimonworld.decodetools.core.FileAccess;

public class DW1FontJP extends DW1Font<DW1GlyphJP> {
    static final long GLYPH_COUNT = 991L;
    
    public DW1FontJP(Access access) {
        super(new DW1GlyphJP());
        
        for (long i = 0; i < GLYPH_COUNT; i++) {
            short charCode = access.readShort(i * 2);
            DW1GlyphJP data = new DW1GlyphJP();
            data.setGlyph(charCode);
            data.setPixelData(access.readShortArray(11, 2 * (GLYPH_COUNT + 1) + i * 0x16));
            
            map.put(Short.toUnsignedInt(charCode), data);
        }
        
        getFallback().setPixelData(access.readShortArray(11, 2 * (GLYPH_COUNT + 1) + GLYPH_COUNT * 0x16));
    }
    
    @Override
    public void export(File file) {
        try (FileAccess access = new FileAccess(file)) {
            
            long i = 0;
            for (DW1GlyphJP g : map.values()) {
                access.writeShort(g.getGlyph(), i * 2);
                access.writeShortArray(g.getPixelData(), 2 * (GLYPH_COUNT + 1) + i * 0x16);
                i++;
            }
            
            access.writeShort(getFallback().getGlyph(), (GLYPH_COUNT + 1) * 2);
            access.writeShortArray(getFallback().getPixelData(), 2 * (GLYPH_COUNT + 1) + GLYPH_COUNT * 0x16);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public int convertChar(char letter) {
        var converted = Charset.forName("Shift-JIS").encode(String.valueOf(letter));
        return converted.remaining() == 2 ? converted.getChar() : getFallback().getConvertedGlyph();
    }
}
