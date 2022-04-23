package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class DrawPane extends JPanel {
    private ArrayList<ColorPoint> points;
    private ArrayList<ArrayList<ColorPoint>> pointLines;
    private ArrayList<ArrayList<ColorPoint>> lastLines;
    private int size = 20;
    private Color color = Color.blue;
    private boolean rubber = false;
    private final boolean ver;

    public DrawPane() {
        super();
        this.setLayout(null);

        ver = false;

        if (ver) {
            pointLines = new ArrayList<>();
            lastLines = new ArrayList<>();

            JButton backButton = new JButton("back");
            backButton.setBounds(1000, 10, 100, 30);
            backButton.setEnabled(false);


            JButton nextButton = new JButton("next");
            nextButton.setBounds(1100, 10, 100, 30);
            nextButton.setEnabled(false);

            backButton.addActionListener(e -> {
                lastLines.add(pointLines.get(pointLines.size()-1));
                pointLines.remove(pointLines.size()-1);
                nextButton.setEnabled(true);
                if (pointLines.size() <= 0) {
                    backButton.setEnabled(false);
                }
                repaint();
            });

            nextButton.addActionListener(e -> {
                pointLines.add(lastLines.get(lastLines.size()-1));
                lastLines.remove(lastLines.size()-1);
                backButton.setEnabled(true);
                if (lastLines.size() <= 0) {
                    nextButton.setEnabled(false);
                }
                repaint();
            });

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    pointLines.add(new ArrayList<>());
                    pointLines.get(pointLines.size()-1).add(new ColorPoint(e.getX(), e.getY(), size, color));
                    backButton.setEnabled(true);
                    nextButton.setEnabled(false);
                    lastLines.clear();
                    repaint();
                }
            });

            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    pointLines.get(pointLines.size()-1).add(new ColorPoint(e.getX(), e.getY(), size, color));
                    repaint();
                }
            });



            this.add(backButton);
            this.add(nextButton);
        } else {
            points = new ArrayList<>();

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
            switchButton.setBounds(1000, 10, 100, 30);
            switchButton.addActionListener(e -> {
                rubber = !rubber;
            });

            this.add(switchButton);
        }


        JButton colorButton = new JButton("color");
        colorButton.setBounds(900, 10, 100, 30);
        colorButton.addActionListener(e -> {
            color = JColorChooser.showDialog(null, "Choose pen color", color);
        });


        this.add(colorButton);

        JLabel sizeLabel = new JLabel(String.valueOf(size));
        sizeLabel.setBounds(1010, 50, 100, 30);
        this.add(sizeLabel);

        Scrollbar sizeAdjust = new Scrollbar(Scrollbar.HORIZONTAL, 20, 0, 1, 60);
        sizeAdjust.addAdjustmentListener(e -> {
            size = e.getValue();
            sizeLabel.setText(String.valueOf(size));
        });
        sizeAdjust.setBounds(900, 50, 100, 30);
        this.add(sizeAdjust);


    }

    public void paint(Graphics g) {
        super.paint(g);

        if (ver) {
            Graphics2D g2 = (Graphics2D) g;
            for (ArrayList<ColorPoint> line: pointLines) {
                if (line.size() > 1) {
                    g2.setColor(line.get(0).color);
                    g2.setStroke(new BasicStroke(line.get(0).size));
                    for (int i = 0; i < line.size()-1; i++) {
                        g2.drawLine(line.get(i).x, line.get(i).y, line.get(i+1).x, line.get(i+1).y);
                    }
                } else if (line.size() > 0) {
                    g.setColor(line.get(0).color);
                    g.fillOval(line.get(0).x, line.get(0).y, line.get(0).size, line.get(0).size);
                }
            }

        } else {
            for (ColorPoint point: points) {
                g.setColor(point.color);
                g.fillOval(point.x, point.y, point.size, point.size);
            }
        }

    }

    public void removeFromList(int x, int y) {
        ArrayList<ColorPoint> removeList = new ArrayList<>();
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
