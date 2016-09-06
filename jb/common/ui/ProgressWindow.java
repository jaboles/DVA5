//
//  ProgressWindow.java
//  DVA
//
//  Created by Jonathan Boles on 16/02/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
//
package jb.common.ui;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import jb.common.ExceptionReporter;
import org.swixml.SwingEngine;

public class ProgressWindow {
    public JDialog frame;
    public JLabel descriptionLabel;
    public JLabel progressLabel;
    public JProgressBar progressBar;
    public JButton cancelButton;
    public JPanel cancelButtonPanel;
    public Runnable cancelAction;

    public ProgressWindow(String title, String description) {
        SwingEngine renderer = new SwingEngine(this);
        try {
            frame = (JDialog) renderer.render(ProgressWindow.class.getResource("/jb/common/ui/resources/progresswindow.xml"));
            frame.setTitle(title);
            descriptionLabel.setText(description);
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }
    }

    public void setCancelAction(Runnable r) {
        cancelAction = r;
        cancelButtonPanel.setVisible(true);
        cancelButton.setEnabled(r != null);
    }

    public void setValue(int value) {
        progressBar.setValue(value);
    }

    public void setProgressText(String text) {
        progressLabel.setText(text);
    }

    public void show() {
        frame.pack();
        WindowUtils.center(frame);
        cancelButtonPanel.setVisible(cancelAction != null);
        frame.setVisible(true);
    }

    public void dispose() {
        frame.dispose();
    }

    public void repaint() {
        frame.update(frame.getGraphics());
        progressLabel.update(progressLabel.getGraphics());
        progressBar.update(progressBar.getGraphics());
    }

    public void setProgressBarMaximum(int value) {
        progressBar.setMaximum(value);
    }
    
    public int getProgressBarMaximum() {
        return progressBar.getMaximum();
    }

    @SuppressWarnings("serial")
    public Action cancelActionInternal = new AbstractAction("Cancel", null) {
        public void actionPerformed(ActionEvent e) {
            if (cancelAction != null)
                cancelAction.run();
        }
    };
}