package app;

import app.UI_util.ColorLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GuessPane extends JPanel {
    private JTextField guessWord;
    private JButton submitButton;
    private ArrayList<ColorLine> prevDrawing = new ArrayList<>();

    public GuessPane() {
        super();
        this.setLayout(null);

        guessWord = new JTextField();
        guessWord.setBounds(400, 600, 300, 50);
        guessWord.addMouseListener(new MouseAdapter() {
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


        submitButton = new JButton("Submit");
        submitButton.setBounds(720, 600, 60, 50);
        submitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "You guessed: " + guessWord.getText(), "Title", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (result == 0) {
                System.out.println("Guessed " + guessWord.getText());
                DrawandGuess.self.guessedList.add(guessWord.getText());
                WhiteBoardGUI.moveToWait(this);
            } else {
                System.out.println("Not submit");
            }
        });

        this.add(guessWord);
        this.add(submitButton);
    }

    public void paint(Graphics g) {

        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;
        synchronized (DrawandGuess.currentRoom) {
            int index = DrawandGuess.currentRoom.playerList.indexOf(DrawandGuess.self);
            int prevPlayer = DrawandGuess.currentRoom.playerList.size()-1;
            if (index != 0) {
                prevPlayer = index - 1;
            }

            if (prevPlayer >= 0) {
                if (DrawandGuess.currentRoom.playerList.get(prevPlayer).drawingList.size() > 0) {
                    for (ColorLine line : DrawandGuess.currentRoom.playerList.get(prevPlayer)
                            .drawingList.get((DrawandGuess.turn - 1) / 2)) {
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
//        super.paint(g);
    }

    public void setPrevDrawing(ArrayList<ColorLine> drawing) {
        this.prevDrawing = drawing;
        repaint();
    }
}
