package app;

import app.UI_util.MyMouseAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class WelcomePane extends JPanel {
    private JTextField userName;
    private JButton submitButton;

    public WelcomePane() {
        super();
        this.setLayout(null);

        userName = new JTextField();
        userName.setBounds(400, 600, 300, 50);
        userName.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        submitButton = new JButton("Submit");
        submitButton.setBounds(720, 600, 60, 50);
        submitButton.addActionListener(e -> {
            String name = userName.getText() + "#" + new Random().nextInt(10000);
            int result = JOptionPane.showConfirmDialog(null, "Your name: " + name, "Title", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (result == 0) {
                System.out.println("Name " + userName.getText());
                DrawandGuess.self.setName(name);
                WhiteBoardGUI.redirectTo(this, new LobbyPane(new Room[0]));
            } else {
                System.out.println("Not submit");
            }
        });

        this.add(userName);
        this.add(submitButton);

        JLabel instruction = new JLabel("Enter your name");
        instruction.setBounds(500, 550, 300, 50);
        this.add(instruction);
    }
}
