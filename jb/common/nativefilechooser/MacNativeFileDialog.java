/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jb.common.nativefilechooser;

import ca.weblite.objc.Proxy;
import static ca.weblite.objc.util.CocoaUtils.dispatch_sync;
import com.sun.jna.Pointer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A wrapper around NSSavePanel and NSOpenPanel with some methods similar to java.awt.FileDialog.
 * 
 * <p>This class includes wrappers for most settings of both NSSavePanel and NSOpen panel so that
 * you have full flexibility (e.g. select directories only, files only, force a certain extension,
 * allow user to add folders, show hidden files, etc...</p>
 * 
 * <h3>Example Save Panel</h3>
 * <code><pre>
 *  NativeFileDialog dlg = new NativeFileDialog("Save as...", FileDialog.SAVE);
    dlg.setMessage("Will save only as .txt file");
    dlg.setExtensionHidden(true);
    dlg.setAllowedFileTypes(Arrays.asList("txt"));
    dlg.setVisible(true);  // this is modal

    String f = dlg.getFile();
    if ( f == null ){
        return;
    }
    File file = new File(f);
    saveFile(file);
 * </pre></code>
 * 
 * <h3>Example Open Panel</h3>
 * 
 * <code><pre>
 *  NativeFileDialog dlg = new NativeFileDialog("Select file to open", FileDialog.LOAD);
    dlg.setVisible(true); // this is modal.
    String f = dlg.getFile();
    if ( f != null ){
        openFile(new File(f));
    }
 * </pre></code>
 * @author shannah
 * @see <a href="https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html">NSSavePanel Class Reference</a>
 * @see <a href="https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nsopenpanel_Class/Reference/Reference.html">NSOpenPanel Class Reference</a>
 * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/awt/FileDialog.html">java.awt.FileDialog API docs</a>
 */
public class MacNativeFileDialog extends MarshalNSObject
{
    public MacNativeFileDialog(){
        super("NSOpenPanel", "openPanel");
    }

    /**
     * Creates a new file dialog with the specified title and mode.
     * @param title The title for the dialog.
     * @param mode Whether to be an open panel or save panel.  Either java.awt.FileDialog.SAVE
     * or java.awt.FileDialog.LOAD
     */
    public MacNativeFileDialog(final String title){
        this();
        setTitle(title);
    }
    
    /**
     * Sets title of the dialog.
     * @param title The title.
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     */
    public void setTitle(String title){
        set("setTitle:", title);
    }
    
    /**
     * Gets the title of the dialog.
     * @return The title
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     */
    public String getTitle(){
        return getString("title");
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param prompt 
     */
    public void setPrompt(String prompt ){
        set("setPrompt:", prompt);
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param label 
     */
    public void setNameFieldLabel(String label){
        set("setNameFieldLabel:", label);
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public String getNameFieldLabel(){
        return getString("nameFieldLabel");
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param message 
     */
    public void setMessage(String message){
        set("setMessage:", message);
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public String getMessage(){
        return getString("message");
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public String getPrompt(){
        return getString("prompt");
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public boolean canCreateDirectories(){
        return getInt("canCreateDirectories") != 0;
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param can 
     */
    public void setCanCreateDirectories(boolean can){
        set("setCanCreateDirectories:", can?1:0);
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public boolean showsHiddenFiles(){
        return getInt("showsHiddenFiles") != 0;
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param shows 
     */
    public void setShowsHiddenFiles(boolean shows){
        set("setShowsHiddenFiles:", shows?1:0);
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public boolean isExtensionHidden(){
        return getInt("isExtensionHidden")!=0;
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param hidden 
     */
    public void setExtensionHidden(boolean hidden){
        set("setExtensionHidden:", hidden?1:0);
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public boolean canSelectHiddenExtension(){
        return getInt("canSelectHiddenExtension")!=0;
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param sel 
     */
    public void setCanSelectHiddenExtension(boolean sel){
        set("setCanSelectHiddenExtension:", sel?1:0);
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param val 
     */
    public void setNameFieldStringValue(String val){
        set("setNameFieldStringValue:", val);
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public List<String> getAllowedFileTypes(){
        return getStringArray("allowedFileTypes");
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param types 
     */
    public void setAllowedFileTypes(final List<String> types){
        set("setAllowedFileTypes:", types);
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public boolean allowsOtherFileTypes(){
        return getInt("allowsOtherFileTypes")!=0;
    }
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param allowed 
     */
    public void setAllowsOtherFileTypes(boolean allowed){
        set("setAllowsOtherFileTypes:", allowed?1:0);
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @return 
     */
    public boolean getTreatsFilePackagesAsDirectories(){
        return getInt("treatsFilePackagesAsDirectories")!=0;
    }
    
    
    /**
     * @see https://developer.apple.com/library/mac/#documentation/Cocoa/Reference/ApplicationKit/Classes/nssavepanel_Class/Reference/Reference.html
     * @param treat 
     */
    public void setTreatsFilePackagesAsDirectories(boolean treat){
        set("setTreatsFilePackagesAsDirectories:", treat?1:0);
    }
    
    
    /**
     * Returns the path to the directory that was selected.
     * @return 
     */
    public String getDirectory(){
        MarshalNSObject dirUrl = getObject("directoryURL");
        if (dirUrl != null)
            return dirUrl.getString("path");
        else
            return null;
    }
    
    /**
     * Gets the path to the file that was selected.
     * @return 
     */
    public String getFile(){
        
        final String[] out = new String[1];
        dispatch_sync(new Runnable(){
            public void run() {
                MarshalNSObject fileUrl = getObject("URL");
                if (fileUrl == null) {
                    out[0] = null;
                } else {
                    out[0] = fileUrl.getString("path");
                }
            }
            
        });
        return out[0];
        
    }
    
    /**
     * Returns an array of files that were selected by the user.
     * @return 
     */
    public File[] getFiles(){
        final List<File> out = new ArrayList<File>();
        dispatch_sync(new Runnable(){
            public void run() {
                Proxy nsArray = get("URLs");
                if ( !nsArray.getPeer().equals(Pointer.NULL)){
                    int size = nsArray.sendInt("count");
                    for ( int i=0; i<size; i++){
                        Proxy url = nsArray.sendProxy("objectAtIndex:", i);
                        String path = url.sendString("path");
                        out.add(new File(path));
                    }
                } 
            }
            
        });
        
        return out.toArray(new File[0]);
        
    }
    
    /**
     * Returns true if the dialog allows multiple selection.
     * @return 
     */
    public boolean isMultipleMode(){
        return getInt("allowsMultipleSelection")!=0;
    }
    
    
    /**
     * Sets whether the dialog allows the user to select multiple files or not.
     * @param enable 
     */
    public void setMultipleMode(boolean enable){
        set("setAllowsMultipleSelection:", enable?1:0);
    }
    
    /**
     * Returns whether the user can select files in this dialog.
     * @return 
     */
    public boolean canChooseFiles(){
        return getInt("canChooseFiles")!=0;
    }
    
    
    /**
     * Sets whether the user can select files in this dialog.
     * @param can 
     */
    public void setCanChooseFiles(boolean can){
        set("setCanChooseFiles:", can?1:0);
    }
    
    public boolean getCanChooseDirectories(){
        return getInt("canChooseDirectories")!=0;
    }
    
    public void setCanChooseDirectories(boolean can){
        set("setCanChooseDirectories:", can?1:0);
    }   
    
    public boolean getResolvesAliases(){
        return getInt("resolvesAliases")!=0;
    }
    
    public void setResolvesAliases(boolean resolves){
        set("setResolvesAliases:", resolves?1:0);
    }
    
    /**
     * Sets the directory that the dialog displays.
     * @param dir 
     */
    public void setDirectory(final String dir){
        dispatch_sync(new Runnable(){
            public void run() {
                Proxy url = getClient().sendProxy("NSURL", "fileURLWithPath:isDirectory:", dir, 1);
                set("setDirectoryURL:", url);
            }
            
        });
    }
    
    public void setFile(String file){
        throw new UnsupportedOperationException("setFile() notimplemented");
    }
    
    public void setMode(int mode){
        throw new UnsupportedOperationException("Can't set mode after initialization");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize(); 
    }
    
    public void setVisible(boolean visible){
        send("runModal");
    }
    
    public static void main(String[] args){
        MacNativeFileDialog dlg = new MacNativeFileDialog("Foo");
        System.out.println("Get title: "+dlg.getTitle());
        dlg.setPrompt("The prompt");
        dlg.setMessage("The message");
        dlg.setCanCreateDirectories(true);
        dlg.setAllowedFileTypes(Arrays.asList("jar"));
        
        dlg.setVisible(true);
        System.out.println(dlg.getFile());
        System.out.println(dlg.getDirectory());
        System.out.println(dlg.isMultipleMode());
        List<String> l = dlg.getAllowedFileTypes();
        for (String o : l)
            System.out.println(o);
        System.out.println("done");
    }
    
}