package jb.common.ui;

import javax.swing.*;
import java.awt.*;

public class Filler extends Box.Filler {
    public Filler() {
        super(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    }

    public void setWidth(int w) {
        setPreferredSize(new Dimension(w, 0));
        setMinimumSize(new Dimension(w, 0));
    }

    public void setHeight(int h) {
        setPreferredSize(new Dimension(0, h));
        setMinimumSize(new Dimension(0, h));
    }
}