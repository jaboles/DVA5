package jb.dva;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

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
        label.setIcon(obj.getIcon());
        return label;
    }
}