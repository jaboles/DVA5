package jb.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

public class ObjectCache<T>
{
    private String cacheRootName;
    private String cacheUniverse;
    
    public ObjectCache(String cacheRootName, String cacheUniverse)
    {
        this.cacheRootName = cacheRootName;
        this.cacheUniverse = cacheUniverse;
    }
    
    public T load(Class<?> cls, String cacheId, Callable<T> generator) throws Exception
    {
        return load(Long.toString(new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).lastModified()) + "_" + cacheId, generator);        
    }
    
    @SuppressWarnings("unchecked")
    public T load(String cacheId, Callable<T> generator) throws Exception
    {
        File cacheDir = new File(System.getProperty("java.io.tmpdir") + File.separator + cacheRootName);
        cacheDir.mkdirs();
        File cached = new File(cacheDir, cacheUniverse + "_" + cacheId);
        T retval;

        ObjectInputStream ois;
        try
        {
            if (cached.exists())
            {
                ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cached), 102400));
                retval = (T)ois.readObject();
                ois.close();
                return retval;
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }

        retval = generator.call();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cached));
        oos.writeObject(retval);
        oos.close();
        
        return retval;
    }
    
    public T load(Class<?> cls, String cacheId, final T obj) throws Exception
    {
        return load(cls, cacheId, () -> obj);        
    }

    public T load(String cacheId, final T obj) throws Exception
    {
        return load(cacheId, () -> obj);
    }
    
    public void emptyCache()
    {
        File cacheDir = new File(System.getProperty("java.io.tmpdir") + File.separator + cacheRootName);
        if (cacheDir.exists()) {
            for (File f : cacheDir.listFiles()) {
                if (f.getName().toLowerCase().startsWith(cacheUniverse + "_"))
                    f.delete();
            }
        }
    }
}
