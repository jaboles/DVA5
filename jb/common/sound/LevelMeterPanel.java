//
//  LevelMeterPanel.java
//  DVA
//
//  Created by Jonathan Boles on 9/08/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package jb.common.sound;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.JPanel;

public class LevelMeterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private double currentLevel;
    private int orientation;
    private static final int ORIENTATION_HORIZONTAL = 0;
    private static final int ORIENTATION_VERTICAL = 1;
    private static final int DIV_WIDTH = 3;
    private static final int DIV_GAP = 1;
    private static final double YELLOW_THRESHOLD = 0.5;
    private static final double RED_THRESHOLD = 0.8;

    private static final Color RED = new Color(128,0,0);
    private static final Color GREEN = new Color(0,128,0);
    private static final Color YELLOW = new Color(128,128,0);


    public LevelMeterPanel() {
        super();
        this.orientation = ORIENTATION_VERTICAL;
    }

    public void paintComponent(Graphics g) {
        Insets insets = getInsets();
        int width = (int)getSize().getWidth();
        int height = (int)getSize().getHeight();
        int divCount = 0;
        if (this.orientation == ORIENTATION_HORIZONTAL)
            divCount = (width - insets.left - insets.right) / (DIV_WIDTH + DIV_GAP);
        else
            divCount = (height - insets.top - insets.bottom) / (DIV_WIDTH + DIV_GAP);

        super.paintComponent(g);


        ((Graphics2D)g).setColor(Color.black);
        ((Graphics2D)g).drawRect(insets.left-2, insets.top-2, width-insets.left-insets.right+4, height-insets.top-insets.bottom+4);

        // Green
        ((Graphics2D)g).setColor(GREEN);
        for (int i = 0; i < min((int)(currentLevel*divCount), (int)(YELLOW_THRESHOLD*divCount)); i++) {
            if (this.orientation == ORIENTATION_HORIZONTAL)
                ((Graphics2D)g).fillRect(i * (DIV_WIDTH+DIV_GAP) + insets.left, insets.top, DIV_WIDTH, height - insets.top - insets.bottom);
            else
                ((Graphics2D)g).fillRect(insets.left, height - insets.bottom - DIV_WIDTH - (i * (DIV_WIDTH+DIV_GAP)), width - insets.left - insets.right, DIV_WIDTH);
        }

        // Yellow
        ((Graphics2D)g).setColor(YELLOW);
        for (int i = (int)(divCount*YELLOW_THRESHOLD); i < min((int)(currentLevel*divCount), (int)(RED_THRESHOLD*divCount)); i++) {
            if (this.orientation == ORIENTATION_HORIZONTAL)
                ((Graphics2D)g).fillRect(i * (DIV_WIDTH+DIV_GAP) + insets.left, insets.top, DIV_WIDTH, height - insets.top - insets.bottom);
            else
                ((Graphics2D)g).fillRect(insets.left, height - insets.bottom - DIV_WIDTH - (i * (DIV_WIDTH+DIV_GAP)), width - insets.left - insets.right, DIV_WIDTH);
        }

        // Red
        ((Graphics2D)g).setColor(RED);
        for (int i = (int)(divCount*RED_THRESHOLD); i < (currentLevel*divCount); i++) {
            if (this.orientation == ORIENTATION_HORIZONTAL)
                ((Graphics2D)g).fillRect(i * (DIV_WIDTH+DIV_GAP) + insets.left, insets.top, DIV_WIDTH, height - insets.top - insets.bottom);
            else
                ((Graphics2D)g).fillRect(insets.left, height - insets.bottom - DIV_WIDTH - (i * (DIV_WIDTH+DIV_GAP)), width - insets.left - insets.right, DIV_WIDTH);
        }
    }

    public void setLevel(double d) {
        currentLevel = d;
        repaint();
    }

    private int min(int a, int b) {
        return a<b? a : b;
    }
}
