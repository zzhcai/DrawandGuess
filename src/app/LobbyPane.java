package app;

import app.UI_util.MyMouseAdapter;
import app.UI_util.RoomRenderer;
import app.socket_threads.lobby_group.InLobbyReceiveThread;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LobbyPane extends JPanel {
    private final DefaultListModel<Room> dlm = new DefaultListModel<>();
    private final ConcurrentMap<Room, Instant> roomsLastUpdated = new ConcurrentHashMap<>();
    private final JList<Room> roomList = new JList<>(dlm);
    private final JTextField searchBar;
    private final JButton searchButton;
    private final InLobbyReceiveThread thread;
    private final JButton refreshButton;

    public LobbyPane() {
        super();
        this.setLayout(null);

        thread = new InLobbyReceiveThread(roomsLastUpdated);
        thread.start();
        roomList.setCellRenderer(new RoomRenderer());

        JScrollPane sp = new JScrollPane(roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //scroll speed
        sp.getVerticalScrollBar().setUnitIncrement(10);

        sp.setBounds(175, 90, 410, 300);

        roomList.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));

        JButton createRoom = new JButton("Create Room");
        createRoom.setBounds(200, 420, 150, 30);
        createRoom.addActionListener(e -> createRoom());

        createRoom.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));

        // Joining a selected room. Do not worry if the room has become empty after the last refresh,
        // joining an empty room results in you become the new host automatically, no worries.
        JButton joinRoomButton = new JButton("Join Room");
        joinRoomButton.addActionListener(e -> {
            Room room = roomList.getSelectedValue();
            joinRoom(room);
        });
        joinRoomButton.setBounds(400, 420, 150, 30);
        joinRoomButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));

        searchBar = new JTextField();
        searchBar.setBounds(200, 40, 200, 40);
        searchBar.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        searchButton = new JButton("Search");
        searchButton.setBounds(400, 45, 80, 30);
        searchButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> {
            searchButton.setEnabled(false);
            refresh();
            dlm.removeAllElements();
            synchronized (roomsLastUpdated) {
                for (Room room: roomsLastUpdated.keySet()) {
                    if (room.roomName.contains(searchBar.getText())) {
                        dlm.addElement(room);
                    }
                }
            }
            searchButton.setEnabled(true);
        });

        // Refresh works by sharing a concurrent map with the socket thread,
        // only when the refresh button is pressed, does the UI retrieve contents from the map.
        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(480, 45, 80, 30);
        refreshButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            refreshButton.setEnabled(false);
            refresh();
            refreshButton.setEnabled(true);
        });

        this.add(sp);
        this.add(createRoom);
        this.add(joinRoomButton);
        this.add(searchBar);
        this.add(searchButton);
        this.add(refreshButton);


    }

    /**
     * Use JOptionPane dialogs to get necessary user input and update the final currentRoom object.
     * Then redirect to the waiting room panel.
     */
    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(this,
                    "Please enter the room name",
                    "Create room",
                    JOptionPane.QUESTION_MESSAGE);
        if (roomName == null) return;

        Integer numRounds = (Integer) JOptionPane.showInputDialog(this,
                    "Select number of rounds",
                    "Create room",
                    JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{1, 2, 3, 4 ,5}, 1);
        if (numRounds == null) return;

        DrawandGuess.self.isHost = true;
        DrawandGuess.self.ready = true;
        thread.interrupted = true;
        synchronized (DrawandGuess.currentRoom) {
            DrawandGuess.currentRoom.host = DrawandGuess.self;
            DrawandGuess.currentRoom.roomName = roomName;
            DrawandGuess.currentRoom.numRounds = numRounds;
        }

        WhiteBoardGUI.redirectTo(this, WhiteBoardGUI.waitingRoom);
        WhiteBoardGUI.frame.setTitle("Waiting Room");
        WhiteBoardGUI.waitingRoom.startRoom();
    }

    /**
     * Join an existing not-null room.
     * @param room the room to join
     */
    private void joinRoom(Room room) {
        if (room != null) {
            synchronized (DrawandGuess.currentRoom) {
                DrawandGuess.currentRoom.host = room.host;
                DrawandGuess.currentRoom.roomName = room.roomName;
                DrawandGuess.currentRoom.numRounds = room.numRounds;
                DrawandGuess.currentRoom.IP = room.IP;
                DrawandGuess.currentRoom.port = room.port;
            }
            DrawandGuess.self.isHost = false;
            thread.interrupted = true;
            WhiteBoardGUI.redirectTo(this, WhiteBoardGUI.waitingRoom);
            WhiteBoardGUI.frame.setTitle("Waiting Room");
            WhiteBoardGUI.waitingRoom.startRoom();
        }
    }

    /**
     * Refresh the current room list to eliminate any inactive rooms
     */
    public void refresh() {
        Instant now = Instant.now();
        dlm.removeAllElements();
        Set<Map.Entry<Room, Instant>> entrySet = roomsLastUpdated.entrySet();
        for (Map.Entry<Room, Instant> entry: entrySet) {
            if (Duration.between(entry.getValue(), now).toMillis() < DrawandGuess.ROOM_TIMEOUT) {
                dlm.addElement(entry.getKey());
            } else roomsLastUpdated.remove(entry.getKey());
        }
    }
}
