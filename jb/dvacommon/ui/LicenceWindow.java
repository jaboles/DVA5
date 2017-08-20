//
//  LicenceWindow.java
//  DVA
//
//  Created by Jonathan Boles on 23/01/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
//
package jb.dvacommon.ui;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import jb.common.ExceptionReporter;
import org.swixml.SwingEngine;

public class LicenceWindow {
    private boolean accepted = false;
    public JDialog frame;
    public JTextPane licencePane;
    public JButton acceptButton;
    public JButton cancelButton;
    public JPanel labelPanel;

    public LicenceWindow() {
        SwingEngine renderer = new SwingEngine(this);
        try {
            frame = (JDialog) renderer.render(LicenceWindow.class.getResource("/jb/dvacommon/ui/resources/licencewindow.xml"));
            licencePane.setPage(LicenceWindow.class.getResource("/resources/licence.html"));
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }

        acceptButton.addActionListener(e -> {
            accepted = true;
            frame.setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            accepted = false;
            frame.setVisible(false);
        });

        frame.pack();
    }

    public void showFirstTime()
    {
        frame.setVisible(true);
    }

    public void showSubsequentTimes()
    {
        acceptButton.setText("Close");
        cancelButton.setVisible(false);
        labelPanel.setVisible(false);
        frame.pack();
        frame.setVisible(true);
    }

    public boolean accepted()
    {
        return accepted;
    }
}
