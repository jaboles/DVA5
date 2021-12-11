package jb.common.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class ColorComboBox extends JComboBox<Object>
{
    private static final long serialVersionUID = 1L;
    private Color lastSelected = Color.white;
    private LinkedList<Color> addedColors = new LinkedList<Color>();

    public ColorComboBox()
    {
        super();
        setRenderer(new ColorComboBoxRenderer());
        setEditable(false);
        addItem("...");

        addActionListener(e -> {
            Object o = getSelectedItem();
            if (o instanceof String && o.equals("..."))
            {
                Color color = JColorChooser.showDialog(ColorComboBox.this, "Color Chooser", lastSelected);
                if (color != null) {
                    addItem(color);
                    setSelectedItem(color);
                    setBackground(color);
                }
                else
                {
                    setBackground(new Color(0,0,0,0));
                }
            }
            else if (o instanceof Color)
            {
                lastSelected = ((Color)getSelectedItem());
                setBackground(lastSelected);
            }
        });
    }

    public void setSelectedItem(Color c)
    {
        addItem(c);
        super.setSelectedItem(c);
    }

    public void addItem(Color c)
    {
        if (!addedColors.contains(c))
        {
            insertItemAt(c, getItemCount() - 1);
            addedColors.add(c);
        }
    }

    public class ColorComboBoxRenderer extends BasicComboBoxRenderer
    {
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Color)
            {
                setBackground((Color)value);
                setText(" ");
            }
            else if (value instanceof String)
            {
                setBackground(Color.white);
            }

            return this;
        }
    }
}
