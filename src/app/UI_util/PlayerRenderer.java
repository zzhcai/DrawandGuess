package app.UI_util;

import app.Player;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PlayerRenderer extends JPanel implements ListCellRenderer<Player> {

    private final Color TIANYUANLV = new Color(104, 184, 142);
    private final Color JIANGHUANG = new Color(226, 192, 39);

    @Override
    public Component getListCellRendererComponent(JList<? extends Player> list, Player value, int index, boolean isSelected, boolean cellHasFocus) {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(Color.gray);

        JLabel name = new JLabel(value.name);
        name.setBounds(30, 0, 200, 80);
        this.add(name);

        JLabel ready = new JLabel();
        ready.setBounds(220, 0, 80, 80);
        if (value.ready) {
            ready.setText("ready");
            setBackground(TIANYUANLV);
        } else {
            ready.setText("not ready");
        }

        if (value.isHost) setBackground(JIANGHUANG);

        this.add(ready);

        Border blackLine = BorderFactory.createLineBorder(Color.black);
        this.setBorder(blackLine);

        return this;
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(296, 80);
    }

}
