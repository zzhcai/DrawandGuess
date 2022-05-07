package app;

import app.UI_util.MyMouseAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomePane extends JPanel {
    private JTextField userName;
    private JButton submitButton;

    public WelcomePane() {
        super();
        this.setLayout(null);

        userName = new JTextField();
        userName.setBounds(200, 380, 250, 50);
        userName.addMouseListener(new MyMouseAdapter(Cursor.TEXT_CURSOR));

        submitButton = new JButton("Submit");
        submitButton.setBounds(470, 380, 100, 50);
        submitButton.addActionListener(e -> {
            if (userName.getText().length() > 0) {
                // Blizzard style naming
                String name = userName.getText() + "#" + new Random().nextInt(1000, 10000);
                int result = JOptionPane.showConfirmDialog(null, "Your name: " + name, "Title", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (result == 0) {
                    DrawandGuess.self.name = name;
                    WhiteBoardGUI.redirectTo(this, WhiteBoardGUI.lobby);
                    WhiteBoardGUI.frame.setTitle("Lobby");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            WhiteBoardGUI.lobby.refresh();
                        }
                    }, DrawandGuess.ROOM_TIMEOUT);
                }
            }
        });

        this.add(userName);
        this.add(submitButton);

        JLabel instruction = new JLabel("Enter your name:", SwingConstants.CENTER);
        instruction.setBounds(200, 330, 250, 50);
        this.add(instruction);

        JLabel title = new JLabel("Draw and Guess", SwingConstants.CENTER);
        title.setBounds(0, 140, 750, 100);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 36));
        this.add(title);
    }
}
