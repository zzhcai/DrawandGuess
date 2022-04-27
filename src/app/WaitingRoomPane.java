package app;

import javax.swing.*;

public class WaitingRoomPane extends JPanel {
    private JScrollPane sp;
    private final DefaultListModel<Player> dlm = new DefaultListModel<>();
    private JList<Player> playerList = new JList<>(dlm);
}
