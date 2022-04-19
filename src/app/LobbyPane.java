package app;

import javax.swing.*;

public class LobbyPane extends JPanel {
    JList<Room> roomList;

    public LobbyPane(Room[] rooms) {
        super();
        this.setLayout(null);
        roomList = new JList<Room>(rooms);
        roomList.setCellRenderer(new RoomRender());

        JScrollPane sp = new JScrollPane(roomList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //scroll speed
        sp.getVerticalScrollBar().setUnitIncrement(10);

        sp.setBounds(300, 200, 600, 400);
        this.add(sp);

    }
}
