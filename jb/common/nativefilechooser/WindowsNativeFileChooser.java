package jb.common.nativefilechooser;

import java.io.File;
import java.net.URISyntaxException;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.LibraryLoader;

public class WindowsNativeFileChooser
{
    public static void main(String[] args)
    {
        new WindowsNativeFileChooser();
        
    }
    
    public WindowsNativeFileChooser()
    {
        try
        {
            String dllPath = new File(new File(WindowsNativeFileChooser.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "jacob-1.18-M2-x64.dll").getAbsolutePath();
            System.setProperty(LibraryLoader.JACOB_DLL_PATH, dllPath);
            LibraryLoader.loadJacobLibrary();
            
            ActiveXComponent a = new ActiveXComponent("File Open Dialog");
            a.equals(new Object());
            
            //int out = a.invoke("GetOptions").getInt();
          //System.out.println(out);
            
        }
        catch (URISyntaxException e) { }
    }
    
    public static String CLSID_FileOpenDialog = "DC1C5A9C-E88A-4dde-A5A1-60F82A20AEF7";
    public static String CLSID_FileSaveDialog = "C0B4E2F3-BA21-4773-8DBA-335EC946EB8B";
    public static String IID_IFileOpenDialog = "d57c7288-d4ad-4768-be02-9d969532d960";
    public static String IID_IFileSaveDialog = "84bccd23-5fde-4cdb-aea4-af64b83d78ab";
    public static String IID_IFileDialogEvents = "973510db-7d7f-452b-8975-74a85828d354";
    public static String IID_IShellItem = "43826d1e-e718-42ee-bc55-a1e261c37bfe";
}
