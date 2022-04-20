package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class GamePane extends JPanel {
    private ArrayList<ColorPoint> points;
    private int size = 20;
    private Color color = Color.blue;
    private boolean rubber = false;

    public GamePane() {
        super();
        this.setLayout(null);
        points = new ArrayList<ColorPoint>();

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (rubber) {
                    removeFromList(e.getX(), e.getY());
                } else {
                    points.add(new ColorPoint(e.getX(), e.getY(), size, color));
                }
                repaint();
            }
        });

        JButton switchButton = new JButton("switch");
        switchButton.setBounds(1000, 100, 100, 30);
        switchButton.addActionListener(e -> {
            rubber = !rubber;
        });

        this.add(switchButton);
    }

    public void paint(Graphics g) {
        super.paint(g);
        for (ColorPoint point: points) {
            g.setColor(point.color);
            g.fillOval(point.x, point.y, point.size, point.size);
        }
    }

    public void removeFromList(int x, int y) {
        ArrayList<ColorPoint> removeList = new ArrayList<ColorPoint>();
        for (ColorPoint point: points) {
            if (Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2)) <= size) {
                removeList.add(point);
            }
        }

        for (ColorPoint point: removeList) {
            points.remove(point);
        }
    }
}
