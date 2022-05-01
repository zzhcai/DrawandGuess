package app.UI_util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A self-defined mouse adapter that changes cursor style when enters an area.
 */
public class MyMouseAdapter extends MouseAdapter {

    int inAreaCursor;

    /**
     * Construct and specify the cursor style when mouse enters the special area.
     * @param inAreaCursor Pre-defined cursor style in Cursor class.
     */
    public MyMouseAdapter(int inAreaCursor) {
        this.inAreaCursor = inAreaCursor;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        Component source = (Component) e.getSource();
        source.setCursor(Cursor.getPredefinedCursor(inAreaCursor));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        Component source = (Component) e.getSource();
        source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
