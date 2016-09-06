//
//  GetDataLineLevelAudioInputStream.java
//  DVA
//
//  Created by Jonathan Boles on 24/01/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
//

package jb.common.sound;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class GetDataLineLevelAudioInputStream extends AudioInputStream {
    public static final int REFRESHES_PER_SEC = 35;
    private int BYTES_PER_SAMPLE;
    private double[] levels;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private int frameSize;
    private boolean isBigEndian;

    public GetDataLineLevelAudioInputStream(InputStream is, AudioFormat format, long length) {
        super(is, format, length);
        frameSize = format.getFrameSize();
        isBigEndian = format.isBigEndian();
        BYTES_PER_SAMPLE = (int)(format.getFrameRate() * format.getFrameSize() / REFRESHES_PER_SEC);
    }

    public int read() throws IOException {
        int b = super.read();
        bos.write(b);
        return b;
    }

    public int read(byte[] b) throws IOException {
        int retval = super.read(b);
        if (retval > 0) {
            bos.write(b, 0, retval);
        }
        return retval;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int retval = super.read(b, off, len);
        if (retval > 0) {
            bos.write(b, off, retval);
        }
        return retval;
    }

    public void process() {
        byte[] data = bos.toByteArray();
        int sampleValue = 0;

        // sample count
        int sampleCount = (int)((float)data.length / (float)BYTES_PER_SAMPLE);
        levels = new double[sampleCount];

        // Select a bunch of data from the sample range, and round it to the frame size.
        int actualBytesUsedPerSample = BYTES_PER_SAMPLE > 200 ? 200 : BYTES_PER_SAMPLE;
        actualBytesUsedPerSample -= (actualBytesUsedPerSample % frameSize);

        for (int i = 0; i < sampleCount; i++) {
            int sum = 0;
            int startOfSample = i * BYTES_PER_SAMPLE;
            startOfSample -= (startOfSample % frameSize);
            for (int j = 0; j < actualBytesUsedPerSample; j += frameSize) {
                if (frameSize == 1) {
                    sampleValue = data[startOfSample+j];
                } else if (frameSize == 2 && isBigEndian) {
                    sampleValue = (data[startOfSample+j]) << 8 + data[startOfSample + j + 1];
                    sampleValue /= 256.0;
                } else if (frameSize == 2 && !isBigEndian) {
                    sampleValue = data[startOfSample+j] + (data[startOfSample + j + 1]) << 8;
                    sampleValue /= 256.0;
                    //System.out.println(sampleValue);
                }
                sum += Math.pow(sampleValue, 2);
            }

            levels[i] = Math.sqrt((double)sum / (double)actualBytesUsedPerSample);
            /*if (frameSize == 2)
            {
                levels[i] = levels[i] / 256.0;
            }*/
            //System.out.println(levels[i]);
        }
    }

    public double[] getLevels() {
        return levels;
    }
}
