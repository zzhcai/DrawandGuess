package app;

import app.UI_util.ColorLine;
import app.UI_util.WaitingPane;

import javax.swing.*;
import java.util.ArrayList;

public class WhiteBoardGUI {

    public static JFrame frame;
    public static LobbyPane lobby = new LobbyPane();
    public static WaitingRoomPane waitingRoom = new WaitingRoomPane();
    public static DrawPane drawPane = new DrawPane();
    public static GuessPane guessPane = new GuessPane();
    public static WaitingPane wait = new WaitingPane();
    public static ShowPane showPane = new ShowPane();

    public static void setUp() {
        frame = new JFrame("Draw and Guess");
        frame.setSize(1200, 800);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new WelcomePane());
//        frame.add(new ShowPane());
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

    public static void setPrevDraw(ArrayList<ColorLine> drawing) {
        guessPane.setPrevDrawing(drawing);
    }
}
