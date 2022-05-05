package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GuessPane extends JPanel {
    private JTextField guessWord;
    private JButton submitButton;
    private ArrayList<ArrayList<ColorPoint>> prevDrawing;

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
        if (prevDrawing != null) {
            Graphics2D g2 = (Graphics2D) g;
            for (ArrayList<ColorPoint> line: prevDrawing) {
                if (line.size() > 0) {
                    g.setColor(line.get(0).getColor());
                    g2.setColor(line.get(0).getColor());
                    g2.setStroke(new BasicStroke((float) (line.get(0).size*0.85)));
                    for (int i = 0; i < line.size(); i++) {
                        if (i != line.size()-1) {
                            g2.drawLine(line.get(i).x + line.get(i).size/2, line.get(i).y+ line.get(i).size/2,
                                    line.get(i+1).x+ line.get(i).size/2, line.get(i+1).y+ line.get(i).size/2);
                        }
                        g.fillOval(line.get(i).x, line.get(i).y, line.get(i).size, line.get(i).size);
                    }
                }
            }
        }
    }

    public void setPrevDrawing(ArrayList<ArrayList<ColorPoint>> drawing) {
        this.prevDrawing = drawing;
        repaint();
    }
}
