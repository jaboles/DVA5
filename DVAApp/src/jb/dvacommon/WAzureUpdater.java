package jb.dvacommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.*;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.StringUtilities;
import jb.common.VersionComparator;

public class WAzureUpdater extends BaseUpdater
{
    private String latestVersion = null;

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
                var versions = new ArrayList<String>();
                var container = new CloudBlobContainer(new URI("https://dvaupdate.blob.core.windows.net/update"));
                for (ListBlobItem blob : container.listBlobs())
                {
                    if (blob instanceof CloudBlobDirectory)
                    {
                        var dir = ((CloudBlobDirectory)blob).getPrefix().replace("/", "");
                        versions.add(dir);
                    }
                }

                versions.stream().max(VersionComparator.Instance).ifPresent(s -> latestVersion = s);
            } catch (Exception e) {
                ExceptionReporter.reportException(e);
            }
        }
        return latestVersion;
    }

    public URL getBaseUrl(String version) throws MalformedURLException
    {
        return new URL(baseUrl, "update/" + version + "/");
    }

    public static void main(String[] args) throws InvalidKeyException, URISyntaxException, StorageException, IOException
    {
        /*for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }*/
        String connectionString = System.getenv("AGENT_TEMPDIRECTORY") != null
                ? FileUtilities.readAllText(System.getenv("AGENT_TEMPDIRECTORY") + "/azure.secret").trim()
                : FileUtilities.readAllText("azure.secret").trim();
        CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
        CloudBlobClient serviceClient = account.createCloudBlobClient();

        String cmd = args[0];
        CloudBlobContainer metadataContainer = serviceClient.getContainerReference("metadata");
        CloudBlobContainer soundjarsContainer = serviceClient.getContainerReference("soundjars");

        BlobContainerPermissions bcp = new BlobContainerPermissions();
        bcp.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

        metadataContainer.createIfNotExists();
        metadataContainer.uploadPermissions(bcp);

        if (cmd.equals("uploadsoundjars"))
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
                uploadArtifactList(jars, metadataContainer, "soundjarslist");
            }
        }
        else
        {
            System.err.println("Unknown command '" + cmd + "'");
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

        System.out.print("Uploading artifact list: " + listBlob.getName() + " ... ");
        listBlob.deleteIfExists();
        listBlob.uploadText(StringUtilities.join("\n", artifactNames));
        System.out.println("done");
    }

    private static List<String> getVersions() throws URISyntaxException, StorageException
    {
        var result = new ArrayList<String>();
        var container = new CloudBlobContainer(new URI("https://dvaupdate.blob.core.windows.net/update"));
        for (ListBlobItem blob : container.listBlobs())
        {
            if (blob instanceof CloudBlobDirectory)
            {
                result.add(((CloudBlobDirectory)blob).getPrefix().replace("/", ""));
            }
        }

        return result;
    }
}
