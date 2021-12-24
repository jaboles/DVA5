package jb.dvacommon.ui;

import javax.swing.JLabel;
import javax.swing.JTextField;

import jb.dva.DVAManager;
import jb.dva.Script;
import jb.dvacommon.DVA;

public class DVATextField extends JTextField
{
    private static final long serialVersionUID = 1L;

    private final DVATextManager textManager;

    public DVATextField()
    {
        textManager = new DVATextManager();
    }

    public void initialize(final DVAManager dvaManager, final Script currentScript, JLabel indicatorIconLabel)
    {
        textManager.initialize(this, dvaManager, currentScript, indicatorIconLabel);
    }
}
