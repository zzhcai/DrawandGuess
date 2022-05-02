package app.UI_util;

import app.Player;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PlayerRenderer extends JPanel implements ListCellRenderer<Player> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Player> list, Player value, int index, boolean isSelected, boolean cellHasFocus) {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(Color.gray);

        JLabel name = new JLabel(value.name);
        name.setBounds(30, 0, 200, 100);
        this.add(name);

        JLabel ready = new JLabel();
        ready.setBounds(300, 0, 100, 100);
        if (value.ready) {
            ready.setText("ready");
        } else {
            ready.setText("not ready");
        }

        if (value.isHost) setBackground(Color.yellow);

        this.add(ready);

        Border blackLine = BorderFactory.createLineBorder(Color.black);
        this.setBorder(blackLine);

        return this;
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(396, 100);
    }

}
