package app;

import javax.swing.*;

public class GuessPane extends JPanel {
    private JTextField guessWord;
    private JButton submitButton;

    public GuessPane() {
        super();
        this.setLayout(null);

        guessWord = new JTextField();
        guessWord.setBounds(400, 600, 300, 50);


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
