package app;

import app.UI_util.MyMouseAdapter;

import javax.swing.*;
import java.awt.*;

public class EndGamePane extends JPanel {

    public EndGamePane() {
        this.setLayout(null);
        JLabel endLabel = new JLabel("End of Game");
        endLabel.setFont(new Font(endLabel.getFont().getName(), Font.PLAIN, 50));
        endLabel.setBounds(230, 150, 400, 100);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(280, 320, 200, 60);
        exitButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> {System.exit(0);});

        this.add(endLabel);
        this.add(exitButton);
    }
}
