package net.digimonworld.dw1.fonttool.font;

import java.io.File;
import java.io.IOException;

import net.digimonworld.decodetools.core.Access;
import net.digimonworld.decodetools.core.FileAccess;
import net.digimonworld.dw1.fonttool.Utils;

public class DW1FontUS extends DW1Font<DW1GlyphUS> {
    public DW1FontUS(Access access) {
        super(new DW1GlyphUS());
        
        for (long i = 0; i < 78; i++) {
            short charCode = access.readShort(i * 2);
            DW1GlyphUS data = new DW1GlyphUS();
            data.setGlyph(charCode);
            data.setPixelData(access.readShortArray(11, 0xA0 + i * 0x18));
            data.setWidth(access.readByte(0xA0 + i * 0x18 + 22));
            data.setEmpty(access.readByte(0xA0 + i * 0x18 + 23));
            
            map.put(Short.toUnsignedInt(charCode), data);
        }
        
        getFallback().setPixelData(access.readShortArray(11, 0xA0 + 78L * 0x18));
        getFallback().setWidth(access.readByte(0xA0 + 78L * 0x18 + 22));
        getFallback().setEmpty(access.readByte(0xA0 + 78L * 0x18 + 23));
    }
    
    @Override
    public void export(File file) {
        try (FileAccess access = new FileAccess(file)) {
            
            long i = 0;
            for (DW1GlyphUS g : map.values()) {
                access.writeShort(g.getGlyph(), i * 2);
                access.writeShortArray(g.getPixelData(), 0xA0 + i * 0x18);
                access.writeByte(g.getWidth(), 0xA0 + i * 0x18 + 22);
                access.writeByte(g.getEmpty(), 0xA0 + i * 0x18 + 23);
                i++;
            }
            
            access.writeShort(getFallback().getGlyph(), 78L * 2);
            access.writeShortArray(getFallback().getPixelData(), 0xA0 + 78L * 0x18);
            access.writeByte(getFallback().getWidth(), 0xA0 + 78L * 0x18 + 22);
            access.writeByte(getFallback().getEmpty(), 0xA0 + 78L * 0x18 + 23);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public int convertChar(char letter) {
        return Utils.convertCharToShiftJIS(letter);
    }
}
