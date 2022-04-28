package app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class WaitingRoomPane extends JPanel {
    private final DefaultListModel<Player> dlm = new DefaultListModel<>();
    private final JList<Player> playerList = new JList<>(dlm);
    private JScrollPane sp = new JScrollPane(playerList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private static final Room room = new Room();
    private static final DefaultListModel<String> dlmS = new DefaultListModel<>();
    private final JList<String> vocabList = new JList<>(dlmS);
    private JScrollPane sp2 = new JScrollPane(vocabList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    public WaitingRoomPane() {
        super();
        this.setLayout(null);

        dlm.addElement(new Player("A"));
        dlm.addElement(new Player("B"));
        dlm.addElement(new Player("C"));
        dlm.addElement(new Player("D"));
        dlm.addElement(new Player("E"));

        playerList.setCellRenderer(new PlayerRender());
        sp.getVerticalScrollBar().setUnitIncrement(10);
        sp.setBounds(700, 100, 400, 600);

        this.add(sp);

        vocabList.setCellRenderer(new VocabRender());
        sp2.getVerticalScrollBar().setUnitIncrement(10);
        sp2.setBounds(100, 300, 400, 400);
        this.add(sp2);

        JLabel nameLabel = new JLabel("Room name: ");
        nameLabel.setBounds(100, 50, 150, 30);

        JTextField nameField = new JTextField();
        nameField.setText(room.roomName);
        nameField.setBounds(250, 50, 200, 30);
        nameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                JTextField source = (JTextField) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                JTextField source = (JTextField) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        JLabel numLabel = new JLabel("Max player num: ");
        numLabel.setBounds(100, 110, 150, 30);

        JTextField numField = new JTextField();
        numField.setText(String.valueOf(room.maxPlayer));
        numField.setBounds(250, 110, 200, 30);
        numField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                JTextField source = (JTextField) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                JTextField source = (JTextField) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        this.add(nameLabel);
        this.add(nameField);
        this.add(numLabel);
        this.add(numField);

        JButton fileButton = new JButton("Choose vocabulary file");
        fileButton.setBounds(150, 180, 250, 30);
        fileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                JButton source = (JButton) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                JButton source = (JButton) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

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

    public static void addDictionary(File file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempString;
            room.dictionary.clear();
            dlmS.clear();
            while ((tempString = reader.readLine()) != null) {
                room.dictionary.add(tempString);
                dlmS.addElement(tempString);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
