package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GuessPane extends JPanel {
    private JTextField guessWord;
    private JButton submitButton;

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
            } else {
                System.out.println("Not submit");
            }
        });

        this.add(guessWord);
        this.add(submitButton);

    }
}
