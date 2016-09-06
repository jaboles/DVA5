/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *
 * This file is part of Xuggle-Xuggler-Main.
 *
 * Xuggle-Xuggler-Main is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Main is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Main.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

/*
 * Modified by Andrew Kallmeyer in 2012
 */

package jb.common.sound.xuggle;

import com.xuggle.mediatool.IMediaGenerator;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.AudioSamplesEvent;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.ICloseCoderEvent;
import com.xuggle.mediatool.event.ICloseEvent;
import com.xuggle.mediatool.event.IOpenCoderEvent;
import com.xuggle.mediatool.event.IOpenEvent;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;

public class MediaConcatenator extends MediaToolAdapter {
    int outputSampleRate;
    int numberOutputChannels;
    IAudioSamples.Format outputFormat;
    // the current offset

    private long mOffset = 0;

    // the next audio timestamp

    private long mNextAudio = 0;

    // the index of the audio stream

    private final int mAudoStreamIndex;

    private IAudioResampler audioResampler;

    private IMediaGenerator source = null;

    /**
     * Create a concatenator.
     *
     * @param audioStreamIndex
     *            index of audio stream
     * @param videoStreamIndex
     *            index of video stream
     */

    public MediaConcatenator(int sampleRate, int channels, IAudioSamples.Format outputFormat) {
        this.outputSampleRate = sampleRate;
        this.numberOutputChannels = channels;
        this.outputFormat = outputFormat;
        mAudoStreamIndex = 0;
    }

    public void onAudioSamples(IAudioSamplesEvent event) {
        IAudioSamples samples = event.getAudioSamples();
        if (this.source != event.getSource()) {

            // The output sample rate is hard coded!!!
            if (this.audioResampler != null) {
                //this.audioResampler.release();
            }
            this.source = event.getSource();
            this.audioResampler = IAudioResampler.make(numberOutputChannels, samples.getChannels(), outputSampleRate, samples.getSampleRate(), outputFormat, samples.getFormat());
        }

        IAudioSamples out = samples;
        if (samples.getNumSamples() > 0 && (samples.getSampleRate() != outputSampleRate || samples.getChannels() !=  numberOutputChannels || samples.getFormat() != outputFormat)) {
            out = IAudioSamples.make(samples.getNumSamples(), numberOutputChannels);
            this.audioResampler.resample(out, samples, samples.getNumSamples());

            //AudioSamplesEvent asc = new AudioSamplesEvent(event.getSource(), out, event.getStreamIndex());
            //super.onAudioSamples(asc);
            //out.delete();
        }

        // set the new time stamp to the original plus the offset
        // established
        // for this media file

        long newTimeStamp = out.getTimeStamp() + mOffset;

        // keep track of predicted time of the next audio samples, if the
        // end
        // of the media file is encountered, then the offset will be
        // adjusted
        // to this time.

        mNextAudio = out.getNextPts();

        // set the new timestamp on audio samples

        out.setTimeStamp(newTimeStamp);

        // create a new audio samples event with the one true audio stream
        // index

        super.onAudioSamples(new AudioSamplesEvent(this, out, mAudoStreamIndex));
        out.delete();
    }

    //		public void onVideoPicture(IVideoPictureEvent event) {
    //			IVideoPicture picture = event.getMediaData();
    //			long originalTimeStamp = picture.getTimeStamp();
    //
    //			// set the new time stamp to the original plus the offset
    //			// established
    //			// for this media file
    //
    //			long newTimeStamp = originalTimeStamp + mOffset;
    //
    //			// keep track of predicted time of the next video picture, if the
    //			// end
    //			// of the media file is encountered, then the offset will be
    //			// adjusted
    //			// to this this time.
    //			//
    //			// You'll note in the audio samples listener above we used
    //			// a method called getNextPts(). Video pictures don't have
    //			// a similar method because frame-rates can be variable, so
    //			// we don't now. The minimum thing we do know though (since
    //			// all media containers require media to have monotonically
    //			// increasing time stamps), is that the next video timestamp
    //			// should be at least one tick ahead. So, we fake it.
    //
    //			mNextVideo = originalTimeStamp + 1;
    //
    //			// set the new timestamp on video samples
    //
    //			picture.setTimeStamp(newTimeStamp);
    //
    //			// create a new video picture event with the one true video stream
    //			// index
    //
    //			super.onVideoPicture(new VideoPictureEvent(this, picture, mVideoStreamIndex));
    //		}

    public void onClose(ICloseEvent event) {
        // update the offset by the larger of the next expected audio or
        // video
        // frame time

        mOffset = mNextAudio;
    }

    public void onAddStream(IAddStreamEvent event) {
        // overridden to ensure that add stream events are not passed down
        // the tool chain to the writer, which could cause problems
    }

    public void onOpen(IOpenEvent event) {
        // overridden to ensure that open events are not passed down the
        // tool
        // chain to the writer, which could cause problems
    }

    public void onOpenCoder(IOpenCoderEvent event) {
        // overridden to ensure that open coder events are not passed down
        // the
        // tool chain to the writer, which could cause problems
    }

    public void onCloseCoder(ICloseCoderEvent event) {
        // overridden to ensure that close coder events are not passed down
        // the
        // tool chain to the writer, which could cause problems
    }
}