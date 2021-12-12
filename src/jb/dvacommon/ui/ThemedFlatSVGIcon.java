package jb.dvacommon.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import jb.dvacommon.Settings;

public class ThemedFlatSVGIcon extends FlatSVGIcon {
    public ThemedFlatSVGIcon(String name) {
        super("icons/" + name + (Settings.getLookAndFeelName().toLowerCase().contains("dark") ? "_dark" : "") + ".svg");
    }
}
