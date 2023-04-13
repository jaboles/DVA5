package jb.common.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MacOSUtilities {
    public static boolean isDarkTheme() {
        try {
            Process p = Runtime.getRuntime().exec("defaults read -g AppleInterfaceStyle");
            p.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            return line != null && line.equals("Dark");
        } catch (InterruptedException | IOException e) {
            return false;
        }
    }
}
