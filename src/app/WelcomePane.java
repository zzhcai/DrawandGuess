package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class WelcomePane extends JPanel {
    private JTextField userName;
    private JButton submitButton;

    public WelcomePane() {
        super();
        this.setLayout(null);

        userName = new JTextField();
        userName.setBounds(400, 600, 300, 50);
        userName.addMouseListener(new MouseAdapter() {
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
            int result = JOptionPane.showConfirmDialog(null, "Your name: " + userName.getText(), "Title", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (result == 0) {
                System.out.println("Name " + userName.getText());
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
