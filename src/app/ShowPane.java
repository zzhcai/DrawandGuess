package app;

import app.UI_util.ColorLine;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ShowPane extends JPanel {
    public JLabel wordLabel;
    public JLabel guessLabel;
    public ArrayList<ColorLine> drawing = new ArrayList<>();

    public ShowPane() {
        super();
        this.setLayout(null);
        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setFont(new Font(wordLabel.getFont().getName(), Font.PLAIN, 16));
        wordLabel.setBounds(0, 20, 750, 50);

        guessLabel = new JLabel("", SwingConstants.CENTER);
        guessLabel.setFont(new Font(guessLabel.getFont().getName(), Font.PLAIN, 16));
        guessLabel.setBounds(0, 400, 750, 50);

        this.add(wordLabel);
        this.add(guessLabel);
    }

    public void showing() throws InterruptedException {
        for (int word = 0; word < DrawandGuess.currentRoom.numPlayers; word++) {
            for (int turn = 0; turn < DrawandGuess.currentRoom.numTurn/2; turn++) {
                drawing.clear();
                guessLabel.setText("");
                wordLabel.setText("");
                repaint();
                Thread.sleep(1000);

                guessLabel.setText("");
                int index;
                if (turn == 0) {
                    wordLabel.setText("Init word: " + DrawandGuess.currentRoom.playerList
                            .get((word+turn)%DrawandGuess.currentRoom.numPlayers).guessedList.get(turn));
                    index = word+turn;
                } else {
                    wordLabel.setText(DrawandGuess.currentRoom.playerList
                            .get((word+turn)%DrawandGuess.currentRoom.numPlayers).name +
                            " guessed: " + DrawandGuess.currentRoom.playerList
                            .get((word+turn)%DrawandGuess.currentRoom.numPlayers).guessedList.get(turn));
                    index = word+turn+1;
                }
                Thread.sleep(500);

                for (ColorLine line: DrawandGuess.currentRoom.playerList
                        .get((index)%DrawandGuess.currentRoom.numPlayers).drawingList.get(turn)) {
                    drawing.add(new ColorLine(line.size, line.rgb));
                    long sleepTime = 400/line.x.size();
                    for (int i = 0; i < line.x.size(); i++) {
                        drawing.get(drawing.size()-1).x.add(line.x.get(i));
                        drawing.get(drawing.size()-1).y.add(line.y.get(i));
                        Thread.sleep(sleepTime);
                        repaint();
                    }
                    Thread.sleep(200);
                }

                Thread.sleep(500);
                guessLabel.setText(DrawandGuess.currentRoom.playerList
                        .get((index+1)%DrawandGuess.currentRoom.numPlayers).name +
                        " guessed: " + DrawandGuess.currentRoom.playerList
                        .get((index+1)%DrawandGuess.currentRoom.numPlayers).guessedList.get(turn + 1));
                Thread.sleep(2000);
            }
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (drawing.size() > 0) {
            Graphics2D g2 = (Graphics2D) g;
            for (ColorLine line : drawing) {
                g.setColor(line.getColor());
                g2.setColor(line.getColor());
                g2.setStroke(new BasicStroke((float) (line.size * 0.85)));
                for (int i = 0; i < line.x.size(); i++) {
                    if (i != line.x.size() - 1) {
                        g2.drawLine(line.x.get(i) + line.size / 2, line.y.get(i) + line.size / 2,
                                line.x.get(i + 1) + line.size / 2, line.y.get(i + 1) + line.size / 2);
                    }
                    g.fillOval(line.x.get(i), line.y.get(i), line.size, line.size);
                }
            }
        }



    }
}
