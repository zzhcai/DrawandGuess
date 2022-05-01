package app;

import app.UI_util.MyMouseAdapter;
import app.UI_util.RoomRenderer;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LobbyPane extends JPanel {
    private JScrollPane sp;
    private final DefaultListModel<Room> dlm = new DefaultListModel<>();
    private final ConcurrentMap<Room, Instant> roomsLastUpdated = new ConcurrentHashMap<>();
    private JList<Room> roomList = new JList<>(dlm);
    private JButton createRoom;
    private JButton joinRoom;
    private JTextField searchBar;
    private JButton searchButton;
    private LobbyReceiveThread thread;
    private JButton refreshButton;

    public LobbyPane() {
        super();
        this.setLayout(null);

        thread = new LobbyReceiveThread(roomsLastUpdated);
        thread.start();
        roomList.setCellRenderer(new RoomRenderer());

        sp = new JScrollPane(roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //scroll speed
        sp.getVerticalScrollBar().setUnitIncrement(10);

        sp.setBounds(300, 100, 600, 600);

        roomList.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));

        createRoom = new JButton("Create Room");
        createRoom.setBounds(350, 720, 150, 30);
        createRoom.addActionListener(e -> createRoom());

        createRoom.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));


        joinRoom = new JButton("Join Room");
        joinRoom.addActionListener(e -> {
            //TODO
            System.out.println(roomList.getSelectedIndex());
            System.out.println(roomList.getSelectedValue());
        });
        joinRoom.setBounds(700, 720, 150, 30);
        joinRoom.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));

        searchBar = new JTextField();
        searchBar.setBounds(350, 50, 300, 40);
        searchBar.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        searchButton = new JButton("Search");
        searchButton.setBounds(655, 55, 100, 30);
        searchButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        //TODO search hostID and join

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(780, 55, 100, 30);
        refreshButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            refreshButton.setEnabled(false);
            Instant now = Instant.now();
            dlm.removeAllElements();
            Set<Map.Entry<Room, Instant>> entrySet = roomsLastUpdated.entrySet();
            for (Map.Entry<Room, Instant> entry: entrySet) {
                if (Duration.between(entry.getValue(), now).toMillis() < 1500) {
                    dlm.addElement(entry.getKey());
                } else roomsLastUpdated.remove(entry.getKey());
            }
            refreshButton.setEnabled(true);
        });

        this.add(sp);
        this.add(createRoom);
        this.add(joinRoom);
        this.add(searchBar);
        this.add(searchButton);
        this.add(refreshButton);
    }

    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(this,
                    "Please enter the room name",
                    "Create room",
                    JOptionPane.QUESTION_MESSAGE);
        if (roomName == null) return;

        Integer maxPlayerNum = (Integer) JOptionPane.showInputDialog(this,
                    "Select max player number",
                    "Create room",
                    JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1);
        if (maxPlayerNum == null) return;

        DrawandGuess.currentRoom = new Room(DrawandGuess.self, roomName, maxPlayerNum);
        DrawandGuess.self.isHost = true;
        thread.interrupted = true;
        WhiteBoardGUI.redirectTo(this, new WaitingRoomPane(DrawandGuess.currentRoom));
    }
}
