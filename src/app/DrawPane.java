package app;

import app.UI_util.ColorLine;

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
    private ArrayList<ColorLine> lines = new ArrayList<>();
    private ArrayList<ColorLine> removedLines = new ArrayList<>();
    private int size = 20;
    private Color color = Color.blue;
    private boolean rubber = false;
    private final int ver = 3;
    private Image mouse;
    private Toolkit tk;
    private Cursor cu;
    private int x = -1;
    private int y = -1;
    private int round;
    private int turn;
    JLabel wordLabel;
    JButton submitButton;

    public DrawPane() {
        super();
        this.setLayout(null);

        wordLabel = new JLabel();
        wordLabel.setBounds(550, 20, 200, 50);
        this.add(wordLabel);

        submitButton = new JButton("submit");
        submitButton.setBounds(1030, 50, 150, 50);
        submitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                JButton source = (JButton) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                x = -1;
                y = -1;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                JButton source = (JButton) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        submitButton.addActionListener(e -> {
            DrawandGuess.self.drawingList.add(lines);
            WhiteBoardGUI.moveToWait(this);
        });

        this.add(submitButton);

        if (ver != 0) {
            pointLines = new ArrayList<>();
            lastLines = new ArrayList<>();

            JButton backButton = new JButton("back");
            backButton.setBounds(1000, 10, 100, 30);
            backButton.setEnabled(false);
            backButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    JButton source = (JButton) e.getSource();
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    x = -1;
                    y = -1;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    JButton source = (JButton) e.getSource();
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            });


            JButton nextButton = new JButton("next");
            nextButton.setBounds(1100, 10, 100, 30);
            nextButton.setEnabled(false);
            nextButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    JButton source = (JButton) e.getSource();
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    x = -1;
                    y = -1;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    JButton source = (JButton) e.getSource();
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            });

            backButton.addActionListener(e -> {
                if (ver == 3) {
                    removedLines.add(lines.get(lines.size()-1));
                    lines.remove(lines.size()-1);
                    nextButton.setEnabled(true);
                    if (lines.size() <= 0) {
                        backButton.setEnabled(false);
                    }
                } else {
                    lastLines.add(pointLines.get(pointLines.size()-1));
                    pointLines.remove(pointLines.size()-1);
                    nextButton.setEnabled(true);
                    if (pointLines.size() <= 0) {
                        backButton.setEnabled(false);
                    }
                }
                repaint();
            });

            nextButton.addActionListener(e -> {
                if (ver == 3) {
                    lines.add(removedLines.get(removedLines.size()-1));
                    removedLines.remove(removedLines.size()-1);
                    backButton.setEnabled(true);
                    if (removedLines.size() <= 0) {
                        nextButton.setEnabled(false);
                    }
                } else {
                    pointLines.add(lastLines.get(lastLines.size()-1));
                    lastLines.remove(lastLines.size()-1);
                    backButton.setEnabled(true);
                    if (lastLines.size() <= 0) {
                        nextButton.setEnabled(false);
                    }
                }
                repaint();
            });

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    x = e.getX() - size/2;
                    y = e.getY() - size/2;
                    if (ver == 3) {
                        lines.add(new ColorLine(size, color.getRGB()));
                        lines.get(lines.size()-1).x.add(x);
                        lines.get(lines.size()-1).y.add(y);
                        removedLines.clear();
                    } else {
                        pointLines.add(new ArrayList<>());
                        pointLines.get(pointLines.size()-1).add(new ColorPoint(x, y, size, color.getRGB()));
                        lastLines.clear();
                    }
                    backButton.setEnabled(true);
                    nextButton.setEnabled(false);

                    repaint();
                }
            });

            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    x = e.getX() - size/2;
                    y = e.getY() - size/2;
                    if (ver == 3) {
                        lines.get(lines.size()-1).x.add(x);
                        lines.get(lines.size()-1).y.add(y);
                    } else {
                        pointLines.get(pointLines.size()-1).add(new ColorPoint(x, y, size, color.getRGB()));
                    }
                    repaint();
                }
            });


            this.add(backButton);
            this.add(nextButton);
        } else if (ver == 0) {
            points = new ArrayList<>();

            this.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    x = e.getX() - size/2;
                    y = e.getY() - size/2;
                    if (rubber) {
                        removeFromList(x, y);
                    } else {
                        points.add(new ColorPoint(x, y, size, color.getRGB()));
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

//        tk = Toolkit.getDefaultToolkit();
//        mouse = new ImageIcon("src/png-clipart-computer-icons-pencil-drawing-pencil-angle-pencil.png").getImage();
//        cu = tk.createCustomCursor(mouse, new Point(20, 10), "pen");
//        this.setCursor(cu);

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                x = e.getX() - size/2;
                y = e.getY() - size/2;
                repaint();
            }
        });

        JButton colorButton = new JButton("color");
        colorButton.setBounds(900, 10, 100, 30);
        colorButton.addActionListener(e -> {
            color = JColorChooser.showDialog(null, "Choose pen color", color);
        });
        colorButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                JButton source = (JButton) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                x = -1;
                y = -1;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                JButton source = (JButton) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
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
        sizeAdjust.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                Scrollbar source = (Scrollbar) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                x = -1;
                y = -1;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                Scrollbar source = (Scrollbar) e.getSource();
                source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        this.add(sizeAdjust);


    }


    public void paint(Graphics g) {
        super.paint(g);

        if (x != -1 && y != -1) {
            g.setColor(color);
            g.drawOval(x, y, size, size);
        }

        if (ver == 3) {
            Graphics2D g2 = (Graphics2D) g;
            for (ColorLine line: lines) {
                g.setColor(line.getColor());
                g2.setColor(line.getColor());
                g2.setStroke(new BasicStroke((float) (line.size*0.85)));
                for (int i = 0; i < line.x.size(); i++) {
                    if (i != line.x.size()-1) {
                        g2.drawLine(line.x.get(i) + line.size/2, line.y.get(i)+ line.size/2,
                                line.x.get(i+1)+ line.size/2, line.y.get(i+1)+ line.size/2);
                    }
                    g.fillOval(line.x.get(i), line.y.get(i), line.size, line.size);
                }
            }

        } else if (ver == 1) {
            Graphics2D g2 = (Graphics2D) g;
            for (ArrayList<ColorPoint> line: pointLines) {
                if (line.size() > 1) {
                    g2.setColor(line.get(0).getColor());
                    g2.setStroke(new BasicStroke(line.get(0).size));
                    for (int i = 0; i < line.size()-1; i++) {
                        g2.drawLine(line.get(i).x + line.get(i).size/2, line.get(i).y+ line.get(i).size/2,
                                line.get(i+1).x+ line.get(i).size/2, line.get(i+1).y+ line.get(i).size/2);
                    }
                } else if (line.size() > 0) {
                    g.setColor(line.get(0).getColor());
                    g.fillOval(line.get(0).x, line.get(0).y, line.get(0).size, line.get(0).size);
                }
            }

        } else if (ver == 0){
            for (ColorPoint point: points) {
                g.setColor(point.getColor());
                g.fillOval(point.x, point.y, point.size, point.size);
            }
        } else if (ver == 2) {
            Graphics2D g2 = (Graphics2D) g;
            for (ArrayList<ColorPoint> line: pointLines) {
                if (line.size() > 0) {
                    g.setColor(line.get(0).getColor());
                    g2.setColor(line.get(0).getColor());
                    g2.setStroke(new BasicStroke((float) (line.get(0).size*0.85)));
                    for (int i = 0; i < line.size(); i++) {
                        if (i != line.size()-1) {
                            g2.drawLine(line.get(i).x + line.get(i).size/2, line.get(i).y+ line.get(i).size/2,
                                    line.get(i+1).x+ line.get(i).size/2, line.get(i+1).y+ line.get(i).size/2);
                        }
                        g.fillOval(line.get(i).x, line.get(i).y, line.get(i).size, line.get(i).size);
                    }
                }
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

    public void setWord(String word) {
        wordLabel.setText(word);
    }
}
