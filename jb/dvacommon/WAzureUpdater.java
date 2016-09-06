package jb.dvacommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.HashMap;
import com.innahema.collections.query.queriables.Queryable;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.StringUtilities;
import jb.common.VersionComparator;

public class WAzureUpdater extends BaseUpdater
{
    private String latestVersion = null;
    
    public static final String VersionsListName = "versionslist";
    public static final String SoundJarsList = "soundjarslist";
    public static final String MetadataContainerName = "metadata";
    public static final String SoundJarsContainerName = "soundjars"; 
    
    public static final String PersistedLastModifiedTimestamp = "PersistedLastModifiedTimestamp";
    
    public WAzureUpdater(URL baseUrl)
    {
        super(baseUrl);
    }
    
    public String getLatestVersion()
    {
        if (latestVersion == null)
        {
            try
            {
                String[] versions = FileUtilities.readFromUrl(new URL(baseUrl, MetadataContainerName + "/" + VersionsListName)).split("\r?\n");
                latestVersion = Queryable.from(versions).max(VersionComparator.Instance);
            } catch (Exception e) {
                ExceptionReporter.reportException(e);
            }
        }
        return latestVersion;
    }

    public URL getBaseUrl(String version) throws MalformedURLException
    {
        String container = version.replace('.', '-');
        return new URL(baseUrl, container + "/");
    }

    public static void main(String[] args) throws InvalidKeyException, URISyntaxException, StorageException, IOException
    {
        String connectionString = FileUtilities.readAllText("azure.secret").trim();
        CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        String cmd = args[0];
        
        CloudBlobContainer versionContainer = null;
        if (args.length > 1) {
            String version = args[1];
            // Container name must be lower case.
            String containerName = version.replace('.',  '-');
            versionContainer = serviceClient.getContainerReference(containerName);
        }
        
        CloudBlobContainer metadataContainer = serviceClient.getContainerReference(MetadataContainerName);
        CloudBlobContainer soundjarsContainer = serviceClient.getContainerReference(SoundJarsContainerName);

        BlobContainerPermissions bcp = new BlobContainerPermissions();
        bcp.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        
        metadataContainer.createIfNotExists();
        metadataContainer.uploadPermissions(bcp);

        if (cmd.equals("uploadversion") && versionContainer != null)
        {
            versionContainer.createIfNotExists();
            versionContainer.uploadPermissions(bcp);
            
            // Upload an image file.
            File[] artifacts = new File[] {
                    new File("/Users/jb/Software/DVA/new.html"),
                    new File("/Users/jb/Software/DVA/build/Debug/DVA5.dmg.bz2"),
                    new File("/Users/jb/Software/DVA/build/Debug/DVA5Setup.exe"),
                };
            for (File f : artifacts)
            {
                uploadArtifact(f, versionContainer);
            }
            updateVersions(serviceClient, metadataContainer);
        }
        else if (cmd.equals("deleteversion"))
        {
            deleteContainer(versionContainer);
            updateVersions(serviceClient, metadataContainer);
        }
        else if (cmd.equals("updateversion"))
        {
            updateVersions(serviceClient, metadataContainer);
        }
        else if (cmd.equals("listversion"))
        {
            Queryable.from(getVersions(serviceClient)).forEach(System.out::println);
        }
        else if (cmd.equals("uploadsoundjars"))
        {
            soundjarsContainer.createIfNotExists();
            soundjarsContainer.uploadPermissions(bcp);

            File[] soundJarsFiles = new File("/Users/jb/Software/DVA/build/soundjars").listFiles();
            if (soundJarsFiles != null) {
                File[] jars = Queryable.from(soundJarsFiles)
                        .where(File::isFile)
                        .where(f -> !f.getName().toLowerCase().equals(".ds_store"))
                        .toArray();

                for (File jar : jars) {
                    uploadArtifact(jar, soundjarsContainer);
                }
                uploadArtifactList(jars, metadataContainer, SoundJarsList);
            }
        }
        else if (cmd.equals("deletesoundjars"))
        {
            deleteContainer(soundjarsContainer);
            metadataContainer.getBlockBlobReference(SoundJarsList).deleteIfExists();
        }
    }
    
    private static void uploadArtifact(File f, CloudBlobContainer c) throws StorageException, IOException, URISyntaxException
    {
        CloudBlockBlob blob = c.getBlockBlobReference(f.getName());
        System.out.print("Uploading " + f.getName() + " ... ");
        HashMap<String,String> metadata = new HashMap<>();
        metadata.put(WAzureUpdater.PersistedLastModifiedTimestamp, Long.toString(f.lastModified()));
        blob.upload(new FileInputStream(f), f.length());
        blob.setMetadata(metadata);
        blob.uploadMetadata();
        System.out.println("done");
    }
    
    private static void uploadArtifactList(File[] files, CloudBlobContainer metadataContainer, String listBlobName) throws StorageException, IOException, URISyntaxException
    {
        CloudBlockBlob listBlob = metadataContainer.getBlockBlobReference(listBlobName);

        String[] artifactNames = Queryable.from(files).map(File::getName).toArray();

        System.out.print("Uploading artifact list" + listBlob.getName() + " ... ");
        listBlob.deleteIfExists();
        listBlob.uploadText(StringUtilities.join("\n", artifactNames));
        System.out.println("done");
    }
    
    private static void updateVersions(CloudBlobClient serviceClient, CloudBlobContainer metadataContainer) throws StorageException, URISyntaxException, IOException
    {
        CloudBlockBlob versionsList = metadataContainer.getBlockBlobReference(VersionsListName);

        String[] versions = getVersions(serviceClient);

        System.out.print("Uploading version list" + versionsList.getName() + " ... ");
        versionsList.deleteIfExists();
        versionsList.uploadText(StringUtilities.join("\n", versions));
        System.out.println("done");
    }
    
    private static void deleteContainer(CloudBlobContainer versionContainer) throws StorageException
    {
        System.out.print("Deleting " + versionContainer.getName() + " ... ");
        versionContainer.delete();
        System.out.println("done");
    }
    private static String[] getVersions(CloudBlobClient serviceClient)
    {
        return Queryable.from(serviceClient.listContainers().iterator())
                .filter(c -> !c.getName().equals(MetadataContainerName))
                .filter(c -> !c.getName().equals(SoundJarsContainerName))
                .map(c -> c.getName().replace('-', '.')).toArray();
    }
}
