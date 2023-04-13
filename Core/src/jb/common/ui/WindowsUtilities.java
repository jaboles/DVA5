package jb.common.ui;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsUtilities {
    public static boolean isDarkTheme() {
        try {
            return (Advapi32Util.registryGetIntValue(
                    WinReg.HKEY_CURRENT_USER,
                    "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "AppsUseLightTheme") == 0);
        } catch (Exception e) {
            return false;
        }
    }
}
