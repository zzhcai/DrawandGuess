package app;

import javax.swing.*;
import java.awt.*;

public class RoomRender extends JPanel implements ListCellRenderer<Room> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Room> list, Room value, int index, boolean isSelected, boolean cellHasFocus) {
        this.removeAll();
        this.setBackground(Color.gray);

        JLabel name = new JLabel(value.roomName);
        name.setBounds(50, 50, 200, 100);
        this.add(name);

        JLabel num = new JLabel(value.numPlayer + "/" + value.maxPlayer);

        if (isSelected) {
            this.setBackground(Color.blue);
        }


        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 200);
    }
}
