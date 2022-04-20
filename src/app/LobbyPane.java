package app;

import javax.swing.*;

public class LobbyPane extends JPanel {
    private JList<Room> roomList;
    private JButton createRoom;
    private JButton joinRoom;
    private JTextField searchBar;
    private JButton searchButton;

    public LobbyPane(Room[] rooms) {
        super();
        this.setLayout(null);
        roomList = new JList<Room>(rooms);
        roomList.setCellRenderer(new RoomRender());

        JScrollPane sp = new JScrollPane(roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //scroll speed
        sp.getVerticalScrollBar().setUnitIncrement(10);

        sp.setBounds(300, 100, 600, 600);


        createRoom = new JButton("Create Room");
        createRoom.setBounds(350, 720, 150, 30);


        joinRoom = new JButton("Join Room");
        joinRoom.addActionListener(e -> {
            System.out.println(roomList.getSelectedIndex());
            System.out.println(roomList.getSelectedValue());
        });
        joinRoom.setBounds(700, 720, 150, 30);

        searchBar = new JTextField();
        searchBar.setBounds(350, 50, 300, 40);

        searchButton = new JButton("Search");
        searchButton.setBounds(655, 55, 100, 30);

        this.add(sp);
        this.add(createRoom);
        this.add(joinRoom);
        this.add(searchBar);
        this.add(searchButton);
    }
}
