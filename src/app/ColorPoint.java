package app;

import java.awt.*;

public class ColorPoint {
    public int x;
    public int y;
    public int size;
    public int rgb;

    public ColorPoint(int x, int y, int size, int rgb) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.rgb = rgb;
    }

    public Color getColor() {
        return new Color(rgb);
    }
}
