package jb.common.nativefilechooser;

import java.util.ArrayList;
import java.util.List;
import com.sun.jna.Pointer;
import ca.weblite.objc.NSObject;
import ca.weblite.objc.Proxy;
import static ca.weblite.objc.util.CocoaUtils.dispatch_sync;
import static ca.weblite.objc.RuntimeUtils.sel;

public class MarshalNSObject extends NSObject
{

    private Proxy peer;
    
    public MarshalNSObject(Proxy peer)
    {
        super(peer.getPeer());
        this.peer = peer;
    }

    public MarshalNSObject(final String className, final String initSelector)
    {
        super("NSObject");
        dispatch_sync(new Runnable(){

            public void run() {
                peer = getClient().sendProxy(className, initSelector);
                peer.send("retain");
            }

        });
    }

    public void set(final String selector, final Proxy value){
        dispatch_sync(new Runnable(){
    
            public void run() {
                peer.send(selector, value);
            }
            
        });
    }
    
    /*public void set(final String selector, final Pointer value){
        dispatch_sync(new Runnable(){
    
            @Override
            public void run() {
                peer.send(selector, value);
            }
            
        });
    }*/
    
    public void set(final String selector, final List<?> array){
        dispatch_sync(new Runnable(){

            public void run() {
                Proxy mutableArray = getClient().sendProxy("NSMutableArray", "arrayWithCapacity:", array.size());
                for (Object o: array){
                    mutableArray.send("addObject:", o);
                }
                peer.send(selector, mutableArray);
                //mutableArray.send("release");
            }
            
        });
    }

    /**
     * Sets a given selector with a string value on the main thread.
     * @param selector An objective-c selector on the NSSavePanel object. e.g. "setTitle:"
     * @param value The value to set the selector.
     */
    public void set(final String selector, final String value){
        dispatch_sync(new Runnable(){
    
            public void run() {
                peer.send(selector, value);
            }
            
        });
    }
    
    
    /**
     * Sets a given selector with an int value on the main thread.
     * @param selector An objective-c selector on the NSSavePanel object. e.g. "setShowsHiddenFiles:"
     * @param value The int value to set.
     */
    public void set(final String selector, final int value){
        dispatch_sync(new Runnable(){
    
            public void run() {
                peer.send(selector, value);
            }
            
        });
    }
    
    public MarshalNSObject getObject(final String selector)
    {
        final Proxy[] out = new Proxy[1];
        dispatch_sync(new Runnable(){

            public void run() {
                out[0] = peer.getProxy(selector);
                if ( out[0] != null && out[0].getPeer().equals(Pointer.NULL)){
                    out[0] = null;
                }
                
            }
            
        });
        if (out[0] != null)
            return new MarshalNSObject(out[0]);
        else
            return null;
    }
    
    public List<String> getStringArray(final String selector){
        final List<String> out = new ArrayList<String>(); 
        dispatch_sync(new Runnable(){

            public void run() {
                Proxy array = peer.getProxy(selector);
                int size = array.getInt("count");
                for ( int i=0; i<size; i++){
                    String nex = array.sendString("objectAtIndex:", i);
                    out.add(nex);
                }
            }
            
        });
        
        return out;
    }

    
    /**
     * Returns the result of a selector  on the main thread for the NSSavePanel
     * object.
     * @param selector The selector to be run.  e.g. "title".
     * @return The result of calling the given selector on the NSSavePanel object.
     */
    public String getString(final String selector){
        final String[] out = new String[1];
        dispatch_sync(new Runnable(){
    
            public void run() {
                out[0] = peer.sendString(selector);
            }
            
        });
        return out[0];
    }
    
    /**
     * Returns the result of running a selector on the NSSavePanel on the main thread.
     * @param selector The selector to be run.  E.g. "showsHiddenFiles"
     * @return The int result.
     */
    public int getInt(final String selector){
        final int[] out = new int[1];
        dispatch_sync(new Runnable(){
    
            public void run() {
                out[0] = peer.sendInt(sel(selector));
            }
            
        });
        return out[0];
    }
    
    public void send(final String selector){
        dispatch_sync(new Runnable(){
    
            public void run() {
                peer.send(selector);
            }
            
        });
    }
    
    public Proxy get(final String selector){
        final Proxy[] out = new Proxy[1];
        dispatch_sync(new Runnable(){
    
            public void run() {
                out[0] = peer.sendProxy(selector);
            }
            
        });
        return out[0];
    }

}
