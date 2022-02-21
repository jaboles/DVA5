package jb.dvacommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.StreamSupport;

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
    public static final String ExceptionsContainerName = "exceptions";

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
                Arrays.stream(versions).max(VersionComparator.Instance).ifPresent(s -> latestVersion = s);
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
        String connectionString = System.getenv("Azure_Secret") != null
                ? System.getenv("Azure_Secret")
                : FileUtilities.readAllText("azure.secret").trim();
        CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        String cmd = args[0];
        CloudBlobContainer metadataContainer = serviceClient.getContainerReference(MetadataContainerName);
        CloudBlobContainer soundjarsContainer = serviceClient.getContainerReference(SoundJarsContainerName);

        BlobContainerPermissions bcp = new BlobContainerPermissions();
        bcp.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

        metadataContainer.createIfNotExists();
        metadataContainer.uploadPermissions(bcp);

        if (cmd.equals("uploadversion"))
        {
            String version = null;
            if (args.length > 1) {
                version = args[1];
            } else {
                version = "0." + Files.readString(Paths.get("build/Output/version.txt"));
            }
            CloudBlobContainer versionContainer = getVersionContainer(serviceClient, version);

            versionContainer.createIfNotExists();
            versionContainer.uploadPermissions(bcp);

            // Upload an image file.
            File[] artifacts = new File[] {
                    new File("build/Output/new.html"),
                    new File("build/Output/DVA5.dmg"),
                    new File("build/Output/DVA5.dmg.bz2"),
                    new File("build/Output/DVA5Setup.exe"),
                    new File("build/Output/DVA5-x86_64.deb"),
                    new File("build/Output/DVA5-aarch64.deb"),
                };
            for (File f : artifacts)
            {
                if (f.exists())
                    uploadArtifact(f, versionContainer);
            }
            updateVersions(serviceClient, metadataContainer);
        }
        else if (cmd.equals("deleteversion"))
        {
            CloudBlobContainer versionContainer = getVersionContainer(serviceClient, args[1]);
            deleteContainer(versionContainer);
            updateVersions(serviceClient, metadataContainer);
        }
        else if (cmd.equals("updateversion"))
        {
            updateVersions(serviceClient, metadataContainer);
        }
        else if (cmd.equals("listversion"))
        {
            Arrays.stream(getVersions(serviceClient)).forEach(System.out::println);
        }
        else if (cmd.equals("uploadsoundjars"))
        {
            soundjarsContainer.createIfNotExists();
            soundjarsContainer.uploadPermissions(bcp);

            File[] soundJarsFiles = new File("/Users/jb/Software/DVA/build/soundjars").listFiles();
            if (soundJarsFiles != null) {
                File[] jars = Arrays.stream(soundJarsFiles)
                        .filter(File::isFile)
                        .filter(f -> !f.getName().equalsIgnoreCase(".ds_store"))
                        .toArray(File[]::new);

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

        String[] artifactNames = Arrays.stream(files).map(File::getName).toArray(String[]::new);

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

    private static CloudBlobContainer getVersionContainer(CloudBlobClient serviceClient, String version) throws URISyntaxException, StorageException {
        // Container name must be lower case.
        String containerName = version.replace('.',  '-');
        return serviceClient.getContainerReference(containerName);
    }

    private static String[] getVersions(CloudBlobClient serviceClient)
    {
        return StreamSupport.stream(serviceClient.listContainers().spliterator(), false)
                .filter(c -> !c.getName().equals(MetadataContainerName))
                .filter(c -> !c.getName().equals(SoundJarsContainerName))
                .filter(c -> !c.getName().equals(ExceptionsContainerName))
                .map(c -> c.getName().replace('-', '.')).toArray(String[]::new);
    }
}
