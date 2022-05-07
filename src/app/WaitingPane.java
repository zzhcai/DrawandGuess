package app;

import javax.swing.*;
import java.awt.*;

public class WaitingPane extends JPanel {

    public WaitingPane() {
        this.setLayout(null);
        JLabel waitingLabel = new JLabel("Waiting for other players to submit");
        waitingLabel.setFont(new Font(waitingLabel.getFont().getName(), Font.PLAIN, 25));
        waitingLabel.setBounds(160, 120, 500, 200);
        this.add(waitingLabel);
    }
}
