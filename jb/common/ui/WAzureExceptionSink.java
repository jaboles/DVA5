package jb.common.ui;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import jb.common.FileUtilities;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;

public class WAzureExceptionSink implements IExceptionSink
{
    public static final String ExceptionsContainer = "Exceptions";

    public void store(String message) throws IOException, URISyntaxException, InvalidKeyException, StorageException {
        String connectionString = FileUtilities.readAllText("azure.secret").trim();
        CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        CloudBlobContainer container = serviceClient.getContainerReference(ExceptionsContainer);
        BlobContainerPermissions bcp = new BlobContainerPermissions();
        bcp.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

        container.createIfNotExists();
        container.uploadPermissions(bcp);
        CloudBlockBlob b = container.getBlockBlobReference(new Date().toString() + ".txt");
        b.uploadText(message);
    }
}
