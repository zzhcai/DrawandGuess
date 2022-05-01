package app;

import app.UI_util.MyMouseAdapter;
import app.UI_util.PlayerRenderer;
import app.UI_util.VocabRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.NumberFormat;

public class WaitingRoomPane extends JPanel {
    private final DefaultListModel<Player> dlmPlayers = new DefaultListModel<>();
    private final JList<Player> playerList = new JList<>(dlmPlayers);
    private JScrollPane spPlayers = new JScrollPane(playerList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private static final DefaultListModel<String> dlmWords = new DefaultListModel<>();
    private final JList<String> wordList = new JList<>(dlmWords);
    private JScrollPane spWords = new JScrollPane(wordList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    public RoomAdvertiseThread thread;

    public WaitingRoomPane(Room r) {
        super();
        this.setLayout(null);
        DrawandGuess.currentRoom = r;

        this.thread = new RoomAdvertiseThread(r);
        thread.start();

        for (Player player: r.playerList) dlmPlayers.addElement(player);

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

        JTextField nameField = new JTextField();
        nameField.setText(r.roomName);
        nameField.setBounds(250, 50, 200, 30);
        nameField.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        JLabel numLabel = new JLabel("Max player num: ");
        numLabel.setBounds(100, 110, 150, 30);

        // Format numField so that only 1 to 10 players are allowed.
//        NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
//        formatter.setValueClass(Integer.class);
//        formatter.setMinimum(3);
//        formatter.setMaximum(10);
//        formatter.setAllowsInvalid(false);
//        JFormattedTextField numField = new JFormattedTextField(formatter);
        JTextField numField = new JTextField(r.maxPlayer);
        numField.setEditable(false);
        numField.setBounds(250, 110, 200, 30);
        numField.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        numField.setText(String.valueOf(r.maxPlayer));
        numField.setBounds(250, 110, 200, 30);
        numField.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        this.add(nameLabel);
        this.add(nameField);
        this.add(numLabel);
        this.add(numField);

        JButton fileButton = new JButton("Choose vocabulary file");
        fileButton.setBounds(150, 180, 250, 30);
        fileButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));

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

    }
//TODO change dictionary should be multicasted to all players within the room
    public static void addDictionary(File file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempString;
            DrawandGuess.currentRoom.dictionary.clear();
            dlmWords.clear();
            while ((tempString = reader.readLine()) != null) {
                DrawandGuess.currentRoom.dictionary.add(tempString);
                dlmWords.addElement(tempString);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
