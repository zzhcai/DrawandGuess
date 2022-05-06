package app.UI_util;

import java.awt.*;
import java.util.ArrayList;

public class ColorLine {
    public int size;
    public int rgb;
    public ArrayList<Integer> x = new ArrayList<>();
    public ArrayList<Integer> y = new ArrayList<>();

    public ColorLine(int size, int rgb) {
        this.size = size;
        this.rgb = rgb;
    }

    public Color getColor() {
        return new Color(rgb);
    }
}
