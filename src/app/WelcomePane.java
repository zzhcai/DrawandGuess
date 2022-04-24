package app;

import javax.swing.*;

public class WelcomePane extends JPanel {
    private JTextField userName;
    private JButton submitButton;

    public WelcomePane() {
        super();
        this.setLayout(null);

        userName = new JTextField();
        userName.setBounds(400, 600, 300, 50);


        submitButton = new JButton("Submit");
        submitButton.setBounds(720, 600, 60, 50);
        submitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "Your name: " + userName.getText(), "Title", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (result == 0) {
                System.out.println("Name " + userName.getText());
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
