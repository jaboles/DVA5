package jb.common.sound.xuggle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import jb.common.ExceptionReporter;
import jb.dvacommon.ProgressAdapter;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IStreamCoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MediaConcatenator2
{
    final static Logger logger = LogManager.getLogger(MediaConcatenator2.class);

    public static void concat(List<URL> urlList, String outputFile, ProgressAdapter pa)
    {
        if (pa != null) {
            pa.show();
            pa.updateProgress(0, 0, "Processing...", null);
        }
        final int sampleRate = 44100;
        final int channels = 2;
        final IAudioSamples.Format outputFormat = IAudioSamples.Format.FMT_S16;
        final ICodec.ID codecId;
        int bitRateTemp = 0;
        if (outputFile.toLowerCase().endsWith(".wav")) {
            codecId = ICodec.ID.CODEC_ID_PCM_S16LE;
        } else if (outputFile.toLowerCase().endsWith(".mp3")) {
            codecId = ICodec.ID.CODEC_ID_MP3;
            bitRateTemp = 128000;
        } else {
            throw new RuntimeException("Error -- cannot export to unknown file type " + outputFile);
        }
        final int bitRate = bitRateTemp;

        logger.info("Concatenating {} items", urlList.size());
        logger.info("Output format: {} Hz, {} channels, {} format, {} codec, {} bps", sampleRate, channels, outputFormat, codecId, bitRate);

        try
        {
            Iterator<URL> it = urlList.iterator();

            IMediaWriter writer = ToolFactory.makeWriter(outputFile);
            writer.addAudioStream(0, 0, ICodec.findEncodingCodec(codecId), channels, sampleRate);
            writer.addListener(new MediaListenerAdapter() {
                @Override
                public void onAddStream(IAddStreamEvent event) {
                    IStreamCoder streamCoder = event.getSource().getContainer().getStream(event.getStreamIndex()).getStreamCoder();
                    streamCoder.setCodec(ICodec.findEncodingCodec(codecId));
                    streamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
                    streamCoder.setSampleRate(sampleRate);
                    streamCoder.setBitRateTolerance(0);
                    streamCoder.setChannels(channels);
                    if (bitRate > 0) {
                        streamCoder.setBitRate(bitRate);
                    }
                }
            });
            MediaConcatenator concat = new MediaConcatenator(sampleRate, channels, outputFormat);
            concat.addListener(writer);
            while (it.hasNext())
            {
                URL u = it.next();
                logger.debug("Concatenating: {}", u);
                if (pa != null) pa.updateProgress(0, 0, "Processing...", u.toString());
                appendFile(u.openStream(), concat);
            }
            writer.close();
        } catch (IOException e) {
            ExceptionReporter.reportException(e);
        }
        if (pa != null) pa.dispose();
    }

    private static void appendFile(InputStream stream, MediaConcatenator concat)
    {
        IContainer container = IContainer.make();
        container.open(stream, IContainerFormat.make());
        IMediaReader reader = ToolFactory.makeReader(container);
        reader.addListener(concat);
        try
        {
            while (reader.readPacket() == null);
        } catch (RuntimeException ex) {            
        } finally {
            reader.close();            
        }
    }
}