package jb.common.ui;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;

public class WAzureExceptionSink implements IExceptionSink
{
    private String containerSharedAccessString;

    public WAzureExceptionSink(String containerSharedAccessString)
    {
        this.containerSharedAccessString = containerSharedAccessString;
    }

    public void store(String message) throws IOException, URISyntaxException, InvalidKeyException, StorageException {
        /*CloudBlobContainer container = new CloudBlobContainer(new URI(containerSharedAccessString));
        CloudBlockBlob b = container.getBlockBlobReference(new Date().toString() + ".txt");
        b.uploadText(message);*/
    }
}
