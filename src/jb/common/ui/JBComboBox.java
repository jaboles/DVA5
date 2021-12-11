package jb.common.ui;

import javax.swing.JComboBox;

public class JBComboBox<T> extends JComboBox<T>
{
    private static final long serialVersionUID = 1L;

    public void replaceItems(T[] items)
    {
        T selected = getSelectedItemTyped();
        T newSelected = null;
        removeAllItems();
        for (T o : items)
        {
            addItem(o);
            if (o.equals(selected)) newSelected = o;
        }
        if (newSelected != null) setSelectedItem(newSelected);
    }

    public T getSelectedItemTyped()
    {
        return getItemAt(getSelectedIndex());
    }
}
