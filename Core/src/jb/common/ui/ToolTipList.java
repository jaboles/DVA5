//
//  ToolTipList.java
//  DVA
//
//  Created by Jonathan Boles on 9/08/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package jb.common.ui;
import java.awt.event.MouseEvent;
import javax.swing.JList;

/** A JList that uses each item as its own tool tip. */
public class ToolTipList<T> extends JList<T> {
    private static final long serialVersionUID = 1L;

    public String getToolTipText(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index < 0) return "";
        T item = getModel().getElementAt(index);
        String toolTipText = item.toString();

        if (getGraphics().getFontMetrics().stringWidth(toolTipText) > getParent().getSize().getWidth()) {
            if (toolTipText.contains("<html>"))
            {
                return toolTipText;
            }
            else
            {
                return "<html>" + wrap(toolTipText, 30).replaceAll("\n", "<br>") + "</html>";
            }
        } else {
            return null;
        }
    }

    private String wrap(String s, int n) {
        String[] d = s.split(" ");
        StringBuilder sb = new StringBuilder(d[0]);
        int lcc = d[0].length();

        for (int i = 1; i < d.length; i++) {
            if (lcc > n) {
                sb.append("\n");
                lcc = 0;
            }
            sb.append(" ").append(d[i]);
            lcc += d[i].length();
        }
        return sb.toString();
    }
}
