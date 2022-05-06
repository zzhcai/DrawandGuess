package app;

import app.UI_util.MyMouseAdapter;

import javax.swing.*;
import java.awt.*;

public class EndGamePane extends JPanel {

    public EndGamePane() {
        this.setLayout(null);
        JLabel endLabel = new JLabel("End of Game");
        endLabel.setFont(new Font(endLabel.getFont().getName(), Font.PLAIN, 50));
        endLabel.setBounds(450, 250, 400, 200);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(500, 550, 200, 60);
        exitButton.addMouseListener(new MyMouseAdapter(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> {System.exit(0);});

        this.add(endLabel);
        this.add(exitButton);
    }
}
