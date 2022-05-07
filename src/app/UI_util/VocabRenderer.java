package app.UI_util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class VocabRenderer extends JPanel implements ListCellRenderer<String>{
    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        this.removeAll();
        this.setLayout(null);

        JLabel name = new JLabel(value);
        name.setBounds(30, 0, 270, 50);
        this.add(name);

        Border blackLine = BorderFactory.createLineBorder(Color.black);
        this.setBorder(blackLine);

        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(296, 50);
    }
}
