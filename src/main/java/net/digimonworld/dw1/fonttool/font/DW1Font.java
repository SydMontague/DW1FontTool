package net.digimonworld.dw1.fonttool.font;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public abstract class DW1Font<T extends DW1Glyph> {
    Map<Integer, T> map = new TreeMap<>();
    private T fallback;
    
    protected DW1Font(T fallback) {
        this.fallback = fallback;
    }
    
    public T getFallback() {
        return fallback;
    }
    
    public Map<Integer, T> getMap() {
        return map;
    }
    
    public abstract int convertChar(char letter);
    
    public abstract void export(File file);
}
