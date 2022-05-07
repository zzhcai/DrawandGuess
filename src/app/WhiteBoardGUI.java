package app;

import javax.swing.*;

public class WhiteBoardGUI {

    public static JFrame frame;
    public static LobbyPane lobby = new LobbyPane();
    public static WaitingRoomPane waitingRoom = new WaitingRoomPane();
    public static DrawPane drawPane = new DrawPane();
    public static GuessPane guessPane = new GuessPane();
    public static WaitingPane wait = new WaitingPane();
    public static ShowPane showPane = new ShowPane();
    public static EndGamePane end = new EndGamePane();

    public static void setUp() {
        frame = new JFrame("Draw and Guess");
        frame.setSize(750, 500);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new WelcomePane());
        WhiteBoardGUI.frame.setTitle("Welcome");
        frame.setVisible(true);
    }

    public static void redirectTo(JPanel oldPane, JPanel nextPane) {
        frame.remove(oldPane);
        frame.add(nextPane);
        frame.validate();
    }

    public static void moveToWait(JPanel oldPane) {
        frame.remove(oldPane);
        wait = new WaitingPane();
        frame.add(wait);
        frame.validate();
    }

    public static void setPrevWord(String word) {
        drawPane.setWord(word);
    }

}
