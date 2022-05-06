package app;

import javax.swing.*;
import java.awt.*;

public class WaitingPane extends JPanel {

    public WaitingPane() {
        this.setLayout(null);
        JLabel waitingLabel = new JLabel("Waiting for other players to submit");
        waitingLabel.setFont(new Font(waitingLabel.getFont().getName(), Font.PLAIN, 20));
        waitingLabel.setBounds(450, 250, 400, 200);
        this.add(waitingLabel);
    }
}
