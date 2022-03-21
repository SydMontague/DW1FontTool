package net.digimonworld.dw1.fonttool.font;

import java.awt.Color;
import java.awt.image.BufferedImage;

public abstract class DW1Glyph {
    private short glyph;
    private short[] pixelData = new short[11];
    
    public void setPixelData(short[] pixelData) {
        if (pixelData.length != 11)
            throw new IllegalArgumentException("Wrong array size.");
        
        this.pixelData = pixelData;
    }
    
    public short[] getPixelData() {
        return pixelData;
    }
    
    public void togglePixel(int x, int y) {
        pixelData[y] ^= (1 << (15 - x));
    }
    
    public boolean getPixel(int x, int y) {
        return (pixelData[y] & (1 << (15 - x))) != 0;
    }
    
    public short getGlyph() {
        return glyph;
    }
    
    public void setGlyph(short glyph) {
        this.glyph = glyph;
    }
    
    public BufferedImage toImage(Color color) {
        BufferedImage image = new BufferedImage(getWidth(), 11, BufferedImage.TYPE_INT_RGB);
        
        for (int i = 0; i < 11; i++)
            for (int j = 0; j < getWidth(); j++) {
                
                boolean isSet = (pixelData[i] & (1 << (15 - j))) == 0;
                
                if (isSet)
                    image.setRGB(j, i, color.getRGB());
            }
        
        return image;
    }
    
    public abstract char getConvertedGlyph();
    
    public abstract byte getWidth();
    
    public abstract void setWidth(byte value);
}
