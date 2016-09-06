//
//  Player.java
//  DVA
//
//  Created by Jonathan Boles on 29/05/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package jb.common.sound;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import jb.common.ExceptionReporter;
import jb.common.FileUtilities;
import jb.common.sound.xuggle.MediaConcatenator2;
import jb.common.ui.ProgressWindow;
import jb.dvacommon.ProgressAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player extends Thread {
    List<URL> audioClipList;
    LevelMeterThread levelMeterThread = null;
    BigClip clip = null;
    final static Logger logger = LoggerFactory.getLogger(Player.class);
    Runnable longConcatCallback;
    Runnable afterConcatCallback;
    Timer timer;
    public static final int LongConcatThreshold = 700;

    public Player(List<URL> urlList, LevelMeterPanel levelMeterPanel) {
        this(urlList, null, null, levelMeterPanel);
    }

    public Player(List<URL> urlList, Runnable longConcatCallback, Runnable afterConcatCallback, LevelMeterPanel levelMeterPanel) {
        audioClipList = urlList;
        this.longConcatCallback = longConcatCallback;
        this.afterConcatCallback = afterConcatCallback;
        this.timer = new Timer();
        if (levelMeterPanel != null)
        {
            this.levelMeterThread = new LevelMeterThread(levelMeterPanel);
            this.levelMeterThread.start();
        }
    }

    public static File getCacheDir()
    {
        return new File(System.getProperty("java.io.tmpdir") + File.separator + "dvatmp");
    }

    public static void emptyCache()
    {
        File cacheDir = getCacheDir();
        if (cacheDir.exists()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File f : cacheFiles) {
                    if (f.getName().toLowerCase().endsWith(".wav") || f.getName().toLowerCase().endsWith(".mp3"))
                        f.delete();
                }
            }
        }
    }

    public void run()
    {
        run2();
    }

    public void run2() {
        try
        {
            if (audioClipList.size() > 1)
            {
                File cacheDir = getCacheDir();
                long combinedHash = 17;
                for (Object o : audioClipList)
                {
                    combinedHash = 23 * combinedHash + o.hashCode();
                }
                if (!cacheDir.exists()) {
                    cacheDir.mkdir();
                }
                File[] cacheFiles = cacheDir.listFiles();
                if (cacheFiles != null) {
                    for (File f : cacheFiles) {
                        if ((new Date().getTime() - f.lastModified()) > (60 * 60 * 1000)) {
                            f.delete();
                        }
                    }
                }
                File cacheFile = new File(cacheDir, Long.toString(combinedHash) + ".mp3");
                File tempCacheFile = new File(cacheDir, Long.toString(combinedHash) + ".temp.mp3");
                logger.debug("Using cache file: {}", cacheFile.getPath());
                
                final TimerTask longConcatTimerTask = new TimerTask() {
                    public void run() {
                        if (longConcatCallback != null) {
                            longConcatCallback.run();
                        }
                    }
                };
                
                if (!cacheFile.exists()) {
                    logger.debug("Cache file does not exist, creating");
                    if (longConcatCallback != null) {
                        timer.schedule(longConcatTimerTask, LongConcatThreshold);
                    }
                    MediaConcatenator2.concat(audioClipList, tempCacheFile.getPath(), null);
                    
                    try {
                        if (tempCacheFile.exists())
                            FileUtilities.copyFile(tempCacheFile, cacheFile);
                    } catch (ClosedByInterruptException ex) {
                        if (tempCacheFile.exists())
                            tempCacheFile.delete();
                        if (cacheFile.exists())
                            cacheFile.delete();
                    }
                }
                
                if (cacheFile.exists()) {
                    audioClipList = new LinkedList<>();
                    audioClipList.add(cacheFile.toURI().toURL());
                    logger.debug("Playing cache file");
                }
                
                if (longConcatCallback != null)
                    longConcatTimerTask.cancel();
                if (afterConcatCallback != null)
                    afterConcatCallback.run();
                
            }
            
            if (!isInterrupted())
                run1();
        } catch (IOException e) {
            ExceptionReporter.reportException(e);
            /*} catch (InterruptedException e) {
            DVA.reportException(e);*/
        }

    }

    // This code is a fucking mess, but it is meant to play through a sequence of sound files
    // with as small a gap between them as possible. As much processing of the next file as possible
    // is done while each sound file is playing.
    public void run1() {
        try {
            /*

            URL url = new URL("http://pscode.org/media/leftright.wav");
            BigClip clip = new BigClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip.open(ais);
            clip.start();
            JOptionPane.showMessageDialog(null, "BigClip.start()");
            clip.loop(4);
            JOptionPane.showMessageDialog(null, "BigClip.loop(4)");
            clip.setFastForward(true);
            clip.loop(8);
            // the looping/FF combo. reveals a bug..
            // there is a slight 'click' in the sound that should not be audible
            JOptionPane.showMessageDialog(null, "Are you on speed?");

             */
            Iterator<URL> it = audioClipList.iterator();
            GetDataLineLevelAudioInputStream ais;
            clip = null;
            BigClip nextClip = null;
            double[] nextLevels = null;

            while (true) {
                if (clip != null)
                {
                    // Block til the clip started earlier is done.
                    //clip.drain();

                    playClipAsync(nextClip);
                    clip = nextClip;
                    nextClip = null;
                    if (levelMeterThread != null)
                    {
                        this.levelMeterThread.next(nextLevels);
                    }
                }


                // Process the next file while the current one is playing in the background.
                //if (it.hasNext())
                {
                    // Start processing the next file.
                    if (it.hasNext())
                    {
                        URL u = it.next();
                        //System.out.println("Processing url: " + u.toString());
                        InputStream istream = new BufferedInputStream(u.openStream(), 102400);
                        AudioInputStream in= AudioSystem.getAudioInputStream(istream);

                        //AudioInputStream in= AudioSystem.getAudioInputStream(u);
                        AudioInputStream din;
                        AudioFormat baseFormat = in.getFormat();
                        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                baseFormat.getSampleRate(),
                                16,
                                baseFormat.getChannels(),
                                baseFormat.getChannels() * 2,
                                baseFormat.getSampleRate(),
                                false);
                        din = AudioSystem.getAudioInputStream(decodedFormat, in);
                        ais = new GetDataLineLevelAudioInputStream(din, decodedFormat, din.getFrameLength());

                        // Create the clip
                        nextClip = new BigClip();

                        // This method does not return until the audio file is completely loaded
                        nextClip.open(ais);

                        // Calc the sound power levels
                        ais.process();
                        nextLevels = ais.getLevels();
                        //System.out.println("done processing");
                    }
                }

                // Done processing the next file. Block on the current one, if there's one playing.
                if (clip != null)
                {
                    blockOnSound();
                }

                if (nextClip == null)
                    break;

                clip = nextClip;
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            ExceptionReporter.reportException(e);
        }

        if (levelMeterThread != null)
        {
            this.levelMeterThread.next(null);
        }
    }

    private synchronized void blockOnSound()
    {
        try
        {
            wait();
        }
        catch (InterruptedException e)
        {
            if (this.levelMeterThread != null)
            {
                this.levelMeterThread.interrupt();
            }
        }
    }

    private synchronized void soundStopped() {
        notifyAll();
    }

    private void playClipAsync(Clip clip)
    {
        // Start playing
        clip.start();

        // Set listener to notify and unblock when done.
        clip.addLineListener(e -> {
            if (e.getType().equals(LineEvent.Type.STOP)) {
                soundStopped();
            }
        });
    }

    public void stopPlaying()
    {
        if (clip != null)
        {
            clip.stop();
            clip = null;
        }
        if (levelMeterThread != null)
        {
            this.levelMeterThread.interrupt();
        }
        interrupt();
    }

    public class LevelMeterThread extends Thread
    {
        private double[] levels;
        private LevelMeterPanel levelMeterPanel;

        public LevelMeterThread(LevelMeterPanel levelMeterPanel)
        {
            this.levelMeterPanel = levelMeterPanel;
        }

        public void run()
        {
            try
            {
                do
                {
                    displayLevelsAndWait();
                } while (this.levels != null);
            } catch (InterruptedException ignored) {
            }
            levelMeterPanel.setLevel(0);
        }

        private synchronized void displayLevelsAndWait() throws InterruptedException
        {
            if (levels != null)
            {
                for (double level : levels) {
                    levelMeterPanel.setLevel(level / 70.0);
                    Thread.sleep(1000 / GetDataLineLevelAudioInputStream.REFRESHES_PER_SEC);
                }
                levelMeterPanel.setLevel(0.1);
            }
            wait();
        }

        public synchronized void next(double[] levels)
        {
            this.levels = levels;
            notifyAll();
        }
    }
}
