package jb.common.jna.windows;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

public interface Shell32Ex extends W32APIOptions
{
    Shell32Ex INSTANCE = (Shell32Ex) Native.loadLibrary("shell32", Shell32Ex.class, DEFAULT_OPTIONS);
    WinNT.HRESULT SetCurrentProcessExplicitAppUserModelID(WString aumi);
}
