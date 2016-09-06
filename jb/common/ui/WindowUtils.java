//
//  WindowUtils.java
//  Caduceus
//
//  Copyright 2005 ICP Firefly Caduceus Development Team (Z. Benitez, J. Boles, L. Constantinescu, M. Lunney).
//	All rights reserved.
//
package jb.common.ui;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * General window utilities class with static methods.
 *
 * This singleton class does not get instantiated more than once, but rather provides a number of static methods usable on any window.
 * It also prevents any window that is registered with it from being moved into the action-bar (the main window, at the top of the Caduceus
 * display). It does this by merely moving it back down if it ever is moved in there.
 */
public class WindowUtils implements ComponentListener {
    private Window window2; // the window that is not to be intruded on by other windows
    private static WindowUtils utils;

    /** 'Singleton' method */
    public synchronized static WindowUtils getInstance() {
        if (utils == null) utils = new WindowUtils();
        return utils;
    }

    /** Center a window on the screen. */
    public static void center(Window f) {
        int screenWidth = (int)f.getGraphicsConfiguration().getBounds().getWidth();
        int screenHeight = (int)f.getGraphicsConfiguration().getBounds().getHeight();
        int windowWidth = (int)f.getBounds().getWidth();
        int windowHeight = (int)f.getBounds().getHeight();

        f.setBounds((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) /  2, windowWidth, windowHeight);
    }

    /** Scale a window's size by a given factor. */
    public static void scale(Window f, double xFactor, double yFactor) {
        int windowWidth = (int)(f.getBounds().getWidth() * xFactor);
        int windowHeight = (int)(f.getBounds().getHeight() * yFactor);

        f.setSize(windowWidth, windowHeight);
    }

    private static void resetPosition(Window window1, Window window2) {
        Rectangle r1 = window1.getBounds();
        Rectangle r2 = window2.getBounds();
        int lowerEdge = (int)r2.getY() + (int)r2.getHeight();
        int rightEdge = (int)r2.getX() + (int)r2.getWidth();
        if ((int)r1.getY() < lowerEdge && (int)r1.getX() < rightEdge) {
            r1.setLocation((int)r1.getX(), lowerEdge);
            window1.setBounds(r1);
        }
    }

    /** Prevents intrusion of window1 on window2's screen area. (Public method called statically) */
    public static void preventIntrusion(Window window1, Window window2) {
        getInstance().preventIntrusion2(window1, window2);
    }

    private void preventIntrusion2(Window window1, Window window2) {
        this.window2 = window2;
        window1.addComponentListener(this);
    }

    // ComponentListener implementation
    public void componentHidden(ComponentEvent e) {}
    public void componentResized(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {
        resetPosition((Window)e.getSource(), window2);
    }
    public void componentShown(ComponentEvent e) {
        resetPosition((Window)e.getSource(), window2);
    }
}

