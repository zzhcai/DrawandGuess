package app;

import javax.swing.*;

public class WhiteBoardGUI {

    public static JFrame frame;
    public static LobbyPane lobby;

    public static void setUp() {
        frame = new JFrame("Draw and Guess");
        frame.setSize(1200, 800);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new WelcomePane());
        frame.setVisible(true);
    }

    public static void redirectTo(JPanel oldPane, JPanel nextPane) {
        frame.remove(oldPane);
        frame.add(nextPane);
        frame.validate();
    }
}
