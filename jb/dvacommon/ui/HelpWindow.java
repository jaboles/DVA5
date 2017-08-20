//
//  HelpWindow.java
//  DVA
//
//  Created by Jonathan Boles on 23/01/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
//
package jb.dvacommon.ui;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import jb.common.ExceptionReporter;
import org.swixml.SwingEngine;


public class HelpWindow {
    public JDialog frame;
    public JTextPane helpPane;
    public JButton closeButton;

    public HelpWindow() {
        SwingEngine renderer = new SwingEngine(this);
        try {
            frame = (JDialog) renderer.render(HelpWindow.class.getResource("/jb/dvacommon/ui/resources/helpwindow.xml"));
        } catch (Exception e) {
            ExceptionReporter.reportException(e);
        }

        closeButton.addActionListener(e -> frame.setVisible(false));

        frame.pack();
    }

    public void showHelp()
    {
        frame.setTitle("Help");
        try {
            helpPane.setPage(HelpWindow.class.getResource("/resources/help.html"));
        } catch (IOException ex) {
            ExceptionReporter.reportException(ex);
        }
        frame.setVisible(true);
    }

    public void showVersionHistory()
    {
        frame.setTitle("Version History");
        try {
            helpPane.setPage(HelpWindow.class.getResource("/resources/versionhistory.html"));
        } catch (IOException ex) {
            ExceptionReporter.reportException(ex);
        }
        frame.setVisible(true);
    }
}
