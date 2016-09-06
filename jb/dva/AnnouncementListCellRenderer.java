package jb.dva;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import jb.dvacommon.DVA;

// Currently unused
public class AnnouncementListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private DVA controller;

    public AnnouncementListCellRenderer(DVA controller) {
        this.controller = controller;
    }

    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean expanded) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, selected, expanded);
        Script obj = (Script)value;
        Icon i = this.controller.getSoundLibrary(obj.getVoice()).getIcon();
        label.setIcon(i);
        return label;
    }
}