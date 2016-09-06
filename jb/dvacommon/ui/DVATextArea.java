package jb.dvacommon.ui;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import jb.dva.Script;
import jb.dva.SoundReference;
import jb.dva.ui.SoundListModel;
import jb.dvacommon.DVA;

public class DVATextArea extends JTextArea
{
    private static final long serialVersionUID = 1L;
    
    private DVATextManager textManager;
    
    public DVATextArea()
    {
        textManager = new DVATextManager();
    }

    public void initialize(final DVA controller, final Script currentScript, JLabel indicatorIconLabel)
    {
        textManager.initialize(this, controller, currentScript, indicatorIconLabel);
    }
    
    public void initialize(final DVA controller, final Script currentScript, JLabel indicatorIconLabel, DVATextVerifyListener listener, final JList<SoundReference> suggestedSoundList, SoundListModel suggestedSoundListModel)
    {
        textManager.initialize(this, controller, currentScript, indicatorIconLabel, listener, suggestedSoundList, suggestedSoundListModel);
    }
    
    public void autoComplete() { textManager.autoComplete(); }
    
    public void canonicalise() { textManager.canonicalise(); }
    
    public int verify() { return textManager.verify(); }
}
