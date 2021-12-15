package jb.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger logger = LogManager.getLogger(ObjectCache.class);
    private final File cacheDir;
    private final String cacheUniverse;

    public ObjectCache(File tempDir, String cacheUniverse)
    {
        cacheDir = new File(tempDir, "ObjectCache");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {logger.warn("Failed to mkdir {}", cacheDir);}
        this.cacheUniverse = cacheUniverse;
    }

    public T load(Class<?> cls, String cacheId, Callable<T> generator) throws Exception
    {
        return load(new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).lastModified() + "_" + cacheId, generator);
    }

    @SuppressWarnings("unchecked")
    public T load(String cacheId, Callable<T> generator) throws Exception
    {
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
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().toLowerCase().startsWith(cacheUniverse + "_"))
                        f.delete();
                }
            }
        }
    }
}