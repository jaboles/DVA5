package jb.plasma.ui;

import jb.dvacommon.DVA;
import jb.plasma.Announcer;
import jb.plasma.data.DepartureData;
import org.swixml.XVBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;

/**
 * Created by jb on 21/2/17.
 */
public class DeparturesPanel extends XVBox {
    private static String[] departurePanelTitles = new String[] { "Next Train:", "2nd Train:", "3rd Train:" };
    private DeparturePanel[] departurePanels = new DeparturePanel[3];
    private JComboBox<Announcer> playAnnouncementVoiceCombobox;

    public DeparturesPanel(DVA dva, JComboBox<Announcer> playAnnouncementVoiceCombobox)
    {
        // Add the DeparturePanels, currently 3 for use with Cityrail
        // indicators.
        for (int i = 0; i < departurePanels.length; i++) {
            departurePanels[i] = new DeparturePanel(departurePanelTitles[i],
                    dva,
                    dva != null ? ((Announcer) playAnnouncementVoiceCombobox.getSelectedItem()).getSoundLibrary().getName() : null);

            if (i >= 1) {
                JSeparator separator = new JSeparator();
                separator.setBorder(new EmptyBorder(0, 0, 0, 0));
                add(separator);
            }
            add(departurePanels[i]);
        }

        playAnnouncementVoiceCombobox.addActionListener(e -> {
            for (DeparturePanel departurePanel : departurePanels) {
                departurePanel.setScriptVoice(((Announcer) playAnnouncementVoiceCombobox.getSelectedItem()).getSoundLibrary().getName());
            }
        });
    }

    public void copyTo(List<DepartureData> dd)
    {
        for (int i = 0; i < departurePanels.length; i++) {
            if (i >= dd.size()) {
                dd.add(new DepartureData());
            }
            dd.set(i, departurePanels[i].getData());
        }
    }

    public void populateFrom(List<DepartureData> dd)
    {
        for (int i = 0; i < departurePanels.length; i++) {
            if (dd.size() > i) {
                departurePanels[i].setData(dd.get(i));
            }
        }
    }

    public void shiftUpwards(DepartureData appendToEnd)
    {
        for (int i = 0; i < departurePanels.length - 1; i++) {
            departurePanels[i].setData(departurePanels[i + 1].getData());
        }
        departurePanels[departurePanels.length - 1].setData(appendToEnd);
    }

    public int count()
    {
        return departurePanels.length;
    }
}
