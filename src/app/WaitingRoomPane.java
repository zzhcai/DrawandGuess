package app;

import app.UI_util.MyMouseAdapter;
import app.UI_util.PlayerRenderer;
import app.UI_util.VocabRenderer;
import app.socket_threads.lobby_group.InLobbyAdvertiseThread;
import app.socket_threads.room_group.InRoomAdvertiseThread;
import app.socket_threads.room_group.InRoomReceiveThread;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public class WaitingRoomPane extends JPanel {
    private final DefaultListModel<Player> dlmPlayers = new DefaultListModel<>();
    private final JList<Player> playerList = new JList<>(dlmPlayers);
    private JScrollPane spPlayers = new JScrollPane(playerList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final DefaultListModel<String> dlmWords = new DefaultListModel<>();
    private final JList<String> wordList = new JList<>(dlmWords);
    private JScrollPane spWords = new JScrollPane(wordList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final JTextField nameField;
    private final JTextField numField;
    private final JButton fileButton;
    private final JButton prepareStartButton;

    public WaitingRoomPane() {
        super();
        this.setLayout(null);

        WaitingRoomMonitorThread monitorThread = new WaitingRoomMonitorThread();
        InLobbyAdvertiseThread inLobbyAdvertiseThread = new InLobbyAdvertiseThread();
        inLobbyAdvertiseThread.start();
        new InRoomAdvertiseThread().start();
        new InRoomReceiveThread().start();

        for (Player player: DrawandGuess.currentRoom.playerList) dlmPlayers.addElement(player);

        playerList.setCellRenderer(new PlayerRenderer());
        spPlayers.getVerticalScrollBar().setUnitIncrement(10);
        spPlayers.setBounds(700, 100, 400, 600);

        this.add(spPlayers);

        wordList.setCellRenderer(new VocabRenderer());
        spWords.getVerticalScrollBar().setUnitIncrement(10);
        spWords.setBounds(100, 300, 400, 400);
        this.add(spWords);

        JLabel nameLabel = new JLabel("Room name: ");
        nameLabel.setBounds(100, 50, 150, 30);

        nameField = new JTextField();
        nameField.setText(DrawandGuess.currentRoom.roomName);
        nameField.setBounds(250, 50, 200, 30);
        nameField.setEditable(false);
        nameField.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));
        nameField.setToolTipText("Press enter to confirm name change");
        nameField.addActionListener(e -> DrawandGuess.currentRoom.roomName = nameField.getText());

        JLabel numLabel = new JLabel("Max draw time: ");
        numLabel.setBounds(100, 110, 150, 30);

        // Format numField so that only 1 to 10 players are allowed.
//        NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
//        formatter.setValueClass(Integer.class);
//        formatter.setMinimum(3);
//        formatter.setMaximum(10);
//        formatter.setAllowsInvalid(false);
//        JFormattedTextField numField = new JFormattedTextField(formatter);
        numField = new JTextField(Integer.toString(DrawandGuess.currentRoom.timeLimit));
        numField.setEditable(false);
        numField.setBounds(250, 110, 200, 30);
        numField.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        numField.setBounds(250, 110, 200, 30);
        numField.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        this.add(nameLabel);
        this.add(nameField);
        this.add(numLabel);
        this.add(numField);

        fileButton = new JButton("Choose vocabulary file");
        fileButton.setBounds(150, 180, 250, 30);
        fileButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        // Only available for the host
        fileButton.setEnabled(false);
        fileButton.addActionListener(e -> {
            JFileChooser addChooser = new JFileChooser(".");
            addChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            addChooser.setMultiSelectionEnabled(false);

            // Only allow txt file
            addChooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));

            int returnVal = addChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                addDictionary(addChooser.getSelectedFile());
            }

        });
        this.add(fileButton);

        prepareStartButton = new JButton("Prepare");
        prepareStartButton.setBounds(700, 50, 100, 30);
        prepareStartButton.setEnabled(false);
        prepareStartButton.setToolTipText("Need at least 3 players and words to start game.");
        prepareStartButton.addActionListener(e -> {
            prepareStartButton.setEnabled(false);
            if (prepareStartButton.getText().equals("Start")) {
                WhiteBoardGUI.redirectTo(this, new DrawPane());
                inLobbyAdvertiseThread.isInterrupted = true;
                monitorThread.isInterrupted = true;
            }else
                DrawandGuess.self.ready = !DrawandGuess.self.ready;
            prepareStartButton.setEnabled(true);
        });
        this.add(prepareStartButton);

        monitorThread.start();
    }

    private void addDictionary(File file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempString;
            synchronized (DrawandGuess.currentRoom) {
                DrawandGuess.currentRoom.dictionary.clear();
                while ((tempString = reader.readLine()) != null) {
                    DrawandGuess.currentRoom.dictionary.add(tempString);
                }
                DrawandGuess.currentRoom.notifyAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This class starts and ends with the WaitingROomPane. It waits on the currentRoom object to be changed,
     * and updates the UI of the waiting room accordingly.
     */
    private class WaitingRoomMonitorThread extends Thread {
        // TODO interrupt this thread when leaving this page
        private volatile boolean isInterrupted = false;
        @Override
        public void run() {
            while (!isInterrupted) {
                ArrayList<Player> players;
                ArrayList<String> words;
                boolean canStart = true;
                synchronized (DrawandGuess.currentRoom) {
                    // Wait until getting notified current room's changed
                    try {
                        DrawandGuess.currentRoom.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long now = Instant.now().toEpochMilli();
                    for (Player player: DrawandGuess.currentRoom.playerList) {
                        if (now - player.lastActive > DrawandGuess.PLAYER_TIMEOUT) DrawandGuess.currentRoom.playerList.remove(player);
                        if (!player.ready) canStart = false;
                    }
                    players = DrawandGuess.currentRoom.playerList;
                    words = DrawandGuess.currentRoom.dictionary;
                }
                Collections.sort(players);
                dlmPlayers.removeAllElements();
                dlmPlayers.addAll(players);
                dlmWords.removeAllElements();
                dlmWords.addAll(words);
                synchronized (DrawandGuess.self) {
                    if (DrawandGuess.self.isHost) {
                        nameField.setEditable(true);
                        fileButton.setEnabled(true);
                        prepareStartButton.setText("Start");
                        prepareStartButton.setEnabled(canStart && players.size() >= 3 && words.size() >= 3);
                    } else {
                        nameField.setText(DrawandGuess.currentRoom.roomName);
                        prepareStartButton.setEnabled(true);
                    }
                }
            }
        }
    }
}
