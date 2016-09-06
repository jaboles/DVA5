package jb.common.jna.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ex extends W32APIOptions {
    User32Ex INSTANCE = (User32Ex) Native.loadLibrary("user32", User32Ex.class, DEFAULT_OPTIONS);
    
    int SWP_NOACTIVATE = 0x10;
    
    boolean GetClientRect(WinDef.HWND hWnd, WinDef.RECT rect);
}
