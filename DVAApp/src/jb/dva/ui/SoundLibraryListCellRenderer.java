package jb.dva.ui;
import jb.dva.SoundLibrary;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.*;

// Custom renderer for the sound library list that adds an icon, and count of sounds in the library
public class SoundLibraryListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean expanded) {
        SoundLibrary lib = (SoundLibrary)value;
        String s = "<html><body><b>";
        s = s.concat(lib.getName());
        s = s.concat("</b><br><i><span style=\"font-size: 80%\">(");
        s = s.concat(Integer.toString(lib.size()));
        s = s.concat(" items)</span></i></body></html>");

        JLabel label = (JLabel)super.getListCellRendererComponent(list, s, index, selected, expanded);
        SoundLibrary obj = (SoundLibrary)value;
        if (obj.getIcon() != null)
            label.setIcon(shrinkIcon(obj.getIcon()));
        return label;
    }

    // Shrink icon down to 32x32 size
    private static Icon shrinkIcon(URL u)
    {
        int iconSize = 32;
        ImageIcon resizedIcon = new ImageIcon((new ImageIcon(u).getImage()).getScaledInstance(32, -1, java.awt.Image.SCALE_SMOOTH));
        BufferedImage bi = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        int xOffset = (iconSize - resizedIcon.getIconWidth()) / 2;
        int yOffset = (iconSize - resizedIcon.getIconHeight()) / 2;
        Graphics g = bi.createGraphics();
        g.drawImage(resizedIcon.getImage(), xOffset, yOffset, resizedIcon.getIconWidth(), resizedIcon.getIconHeight(), null);
        return new ImageIcon(bi);
    }}