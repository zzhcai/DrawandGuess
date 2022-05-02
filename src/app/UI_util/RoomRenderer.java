package app.UI_util;

import app.Room;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class RoomRenderer extends JPanel implements ListCellRenderer<Room> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Room> list, Room value, int index, boolean isSelected, boolean cellHasFocus) {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(Color.gray);

        JLabel name = new JLabel(value.roomName);
        name.setBounds(30, 0, 200, 100);
        this.add(name);

        JLabel num = new JLabel(value.playerList.size() + "/" + value.maxPlayer, SwingConstants.RIGHT);
        num.setBounds(400, 0, 100, 100);
        this.add(num);
        Border blackLine = BorderFactory.createLineBorder(Color.black);
        this.setBorder(blackLine);
        if (isSelected) {
            this.setBackground(Color.blue);
        }


        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(596, 100);
    }
}
