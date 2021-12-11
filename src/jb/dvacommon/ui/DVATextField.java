package jb.dvacommon.ui;

import javax.swing.JLabel;
import javax.swing.JTextField;
import jb.dva.Script;
import jb.dvacommon.DVA;

public class DVATextField extends JTextField
{
    private static final long serialVersionUID = 1L;

    private DVATextManager textManager;
    
    public DVATextField()
    {
        textManager = new DVATextManager();
    }

    public void initialize(final DVA controller, final Script currentScript, JLabel indicatorIconLabel)
    {
        textManager.initialize(this, controller, currentScript, indicatorIconLabel);
    }
}
