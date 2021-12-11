package jb.dva;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import jb.dva.ui.DVAUI;

// Custom renderer for the sound library list that adds an icon, and count of sounds in the library
public class SoundListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    private Icon inheritedIcon = new ImageIcon(DVAUI.class.getResource("/inherited.gif"));

    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean expanded) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, selected, expanded);
        SoundReference obj = (SoundReference)value;
        if (obj.isFallback)
        {
            label.setIcon(inheritedIcon);
        }
        return label;
    }
}