package jb.common.nativefilechooser;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import jb.common.Utilities;

public class JNativeFileChooser extends JFileChooser
{
    private static final long serialVersionUID = 1L;
    private MacNativeFileDialog _mac;

    public JNativeFileChooser()
    {
        super();
        if (Utilities.isMac())
        {
            _mac = new MacNativeFileDialog();
            _mac.setCanCreateDirectories(true);
        }
    }

    public JNativeFileChooser(File currentDirectory)
    {
        this(currentDirectory.getAbsolutePath());
    }

    public JNativeFileChooser(File currentDirectory, FileSystemView fsv)
    {
        throw new UnsupportedOperationException();
    }

    public JNativeFileChooser(FileSystemView fsv)
    {
        throw new UnsupportedOperationException();
    }

    public JNativeFileChooser(String currentDirectoryPath)
    {
        this();
        if (Utilities.isMac()) { _mac.setDirectory(currentDirectoryPath); }
        else if (Utilities.isWindows()) { }
    }

    public JNativeFileChooser(String currentDirectoryPath, FileSystemView fsv)
    {
        throw new UnsupportedOperationException();
    }

    public String getApproveButtonText()
    {
        if (Utilities.isMac()) { return _mac.getPrompt(); }
        else if (Utilities.isWindows()) { }
        return null;
    }

    public File getCurrentDirectory()
    {
        if (Utilities.isMac()) { return new File(_mac.getDirectory()); }
        else if (Utilities.isWindows()) { }
        return null;
    }

    public String getDialogTitle()
    {
        if (Utilities.isMac()) { return _mac.getTitle(); }
        else if (Utilities.isWindows()) { }
        return null;
    }

    public File getSelectedFile()
    {
        if (Utilities.isMac()) { return new File(_mac.getFile()); }
        else if (Utilities.isWindows()) { }
        return null;
    }

    public File[] getSelectedFiles()
    {
        if (Utilities.isMac()) { return _mac.getFiles(); }
        else if (Utilities.isWindows()) { }
        return null;
    }

    public void setApproveButtonText(String text)
    {
        if (Utilities.isMac()) { _mac.setPrompt(text); }
        else if (Utilities.isWindows()) { }
    }

    public void setCurrentDirectory(File currentDirectory)
    {
        if (Utilities.isMac()) { _mac.setDirectory(currentDirectory.getAbsolutePath()); }
        else if (Utilities.isWindows()) { }
    }

    public void setDialogTitle(String title)
    {
        if (Utilities.isMac()) { _mac.setTitle(title); }
        else if (Utilities.isWindows()) { }
    }

    public void setMultiSelectionEnabled(boolean b)
    {
        if (Utilities.isMac()) { _mac.setMultipleMode(b); }
        else if (Utilities.isWindows()) { }
    }

    public int showOpenDialog(Component parent)
    {

    }

    public int showSaveDialog(Component parent)
    {

    }
}
