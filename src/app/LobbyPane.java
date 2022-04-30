package app;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.NumberFormat;

public class LobbyPane extends JPanel {
    private JScrollPane sp;
    private final DefaultListModel<Room> dlm = new DefaultListModel<>();
    private JList<Room> roomList = new JList<>(dlm);
    private JButton createRoom;
    private JButton joinRoom;
    private JTextField searchBar;
    private JButton searchButton;
    private LobbyReceiveThread thread;

    public LobbyPane(Room[] rooms) {
        super();
        this.setLayout(null);

        try {
            thread = new LobbyReceiveThread();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        thread.start();

        for (Room room: rooms) {
            dlm.addElement(room);
        }
        roomList.setCellRenderer(new RoomRender());

        sp = new JScrollPane(roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //scroll speed
        sp.getVerticalScrollBar().setUnitIncrement(10);

        sp.setBounds(300, 100, 600, 600);

        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                JList<Room> source = (JList<Room>) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                JList<Room> source = (JList<Room>) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        createRoom = new JButton("Create Room");
        createRoom.setBounds(350, 720, 150, 30);
        createRoom.addActionListener(e -> createRoom());

        createRoom.addMouseListener(new MouseAdapter() {
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


        joinRoom = new JButton("Join Room");
        joinRoom.addActionListener(e -> {
            System.out.println(roomList.getSelectedIndex());
            System.out.println(roomList.getSelectedValue());
        });
        joinRoom.setBounds(700, 720, 150, 30);
        joinRoom.addMouseListener(new MouseAdapter() {
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

        searchBar = new JTextField();
        searchBar.setBounds(350, 50, 300, 40);
        searchBar.addMouseListener(new MouseAdapter() {
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

        searchButton = new JButton("Search");
        searchButton.setBounds(655, 55, 100, 30);
        searchButton.addMouseListener(new MouseAdapter() {
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

        this.add(sp);
        this.add(createRoom);
        this.add(joinRoom);
        this.add(searchBar);
        this.add(searchButton);

//        createRoom();
    }

    private void createRoom() {
        sp.setVisible(false);
        createRoom.setVisible(false);
        joinRoom.setVisible(false);
        searchBar.setVisible(false);
        searchButton.setVisible(false);

        JPanel createRoomPane = new JPanel();
        createRoomPane.setBounds(300, 100, 600, 600);
        createRoomPane.setLayout(null);

        JLabel nameLabel = new JLabel("Room name: ");
        nameLabel.setBounds(100, 50, 150, 30);

        JTextField nameField = new JTextField();
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

        // Restricts max player number to a number from 0 to 10
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(10);
//        formatter.setAllowsInvalid(false);
        JTextField numField = new JFormattedTextField(formatter);
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

        JButton createButton = new JButton("Create");
        createButton.setBounds(300, 150, 100, 30);
        createButton.addMouseListener(new MouseAdapter() {
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

        createButton.addActionListener(e -> {
            try {
                DrawandGuess.self.setRoom(new Room(nameField.getText(), Integer.parseInt(numField.getText())));
                WhiteBoardGUI.redirectTo(this, new WaitingRoomPane(DrawandGuess.self.room));
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                        "Please enter number 1 to 10 for max player",
                        "Wrong max player number",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } );


        this.add(createRoomPane);
        createRoomPane.setBackground(Color.PINK);
        createRoomPane.add(nameLabel);
        createRoomPane.add(nameField);
        createRoomPane.add(numLabel);
        createRoomPane.add(numField);
        createRoomPane.add(createButton);
    }
}
