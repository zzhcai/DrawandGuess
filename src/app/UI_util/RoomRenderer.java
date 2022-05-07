package app.UI_util;

import app.Room;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class RoomRenderer extends JPanel implements ListCellRenderer<Room> {
    private final Color DANLANZI = new Color(167, 168, 189);

    @Override
    public Component getListCellRendererComponent(JList<? extends Room> list, Room value, int index, boolean isSelected, boolean cellHasFocus) {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(Color.gray);

        JLabel name = new JLabel(value.roomName);
        name.setBounds(30, 0, 200, 80);
        this.add(name);

        JLabel num = new JLabel("Players: " + value.playerList.size(), SwingConstants.RIGHT);
        num.setBounds(280, 0, 80, 80);
        this.add(num);
        Border blackLine = BorderFactory.createLineBorder(Color.black);
        this.setBorder(blackLine);
        if (isSelected) {
            this.setBackground(DANLANZI);
        }


        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(396, 80);
    }
}
