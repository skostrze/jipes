/*
 * =================================================
 * Copyright 2010 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.SignalSource;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TestSlidingWindow.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestSlidingWindow {


    private SignalSource<AudioBuffer> largeBufferSource;
    private SignalSource<AudioBuffer> smallBufferSource;

    @Before
    public void setUp() throws Exception {
        largeBufferSource = new SignalSource<AudioBuffer>() {
            private float[] buf;
            {
                buf = new float[128];
                for (int i=0; i<buf.length; i++) {
                    buf[i] = i;
                }
            }
            public void reset() {
            }

            public AudioBuffer read() throws IOException {
                // return buffer only once
                final float[] b = buf;
                buf = null;
                if (b==null) return null;
                return new RealAudioBuffer(0, b, new AudioFormat(10000, 32, 1, true, true));
            }
        };

        smallBufferSource = new SignalSource<AudioBuffer>() {

            private int count;

            public void reset() {
            }

            public AudioBuffer read() throws IOException {
                if (count < 128) {
                    final AudioBuffer buffer = new RealAudioBuffer(0, new float[]{count, count + 1, count + 2}, new AudioFormat(10000, 32, 1, true, true));
                    count += 3;
                    return buffer;
                }
                return null;
            }
        };
    }

    @Test
    public void testSmallWindowPull() throws IOException {
        final SlidingWindow processor = new SlidingWindow(6, 2);
        processor.connectTo(largeBufferSource);
        float firstValue = 0;
        AudioBuffer buffer;
        while ((buffer = processor.read()) != null) {
            final float[] floats = buffer.getData();
            assertEquals(firstValue, floats[0], 0.00001);
            assertEquals(processor.getSliceLengthInFrames(), floats.length);
            firstValue += processor.getHopSizeInFrames();
        }

        // test reset
        processor.reset();
        // do again
        firstValue = 0;
        while ((buffer = processor.read()) != null) {
            final float[] floats = buffer.getData();
            assertEquals(firstValue, floats[0], 0.00001);
            assertEquals(processor.getSliceLengthInFrames(), floats.length);
            firstValue += processor.getHopSizeInFrames();
        }
    }

    @Test
    public void testSmallWindowPush() throws IOException {
        final SlidingWindow processor = new SlidingWindow(6, 2);
        float firstValue = 0;
        final DataCollector dataCollector = new DataCollector();
        processor.connectTo(dataCollector);
        AudioBuffer buffer;
        while ((buffer = smallBufferSource.read()) != null) {
            processor.process(buffer);
        }
        final List<float[]> results = dataCollector.getOutput();
        System.out.println("results: " + results.size());
        assertFalse(results.isEmpty());
        for (final float[] floats : results) {
            assertEquals(firstValue, floats[0], 0.00001);
            assertEquals(processor.getSliceLengthInFrames(), floats.length);
            firstValue += processor.getHopSizeInFrames();
        }
    }

    @Test
    public void testLargeWindowPush() throws IOException {
        final SlidingWindow processor = new SlidingWindow(13, 11);
        float firstValue = 0;
        final DataCollector dataCollector = new DataCollector();
        processor.connectTo(dataCollector);
        AudioBuffer buffer;
        while ((buffer = smallBufferSource.read()) != null) {
            processor.process(buffer);
        }
        final List<float[]> results = dataCollector.getOutput();
        assertFalse(results.isEmpty());
        System.out.println("results: " + results.size());
        for (final float[] floats : results) {
            assertEquals(firstValue, floats[0], 0.00001);
            assertEquals(processor.getSliceLengthInFrames(), floats.length);
            firstValue += processor.getHopSizeInFrames();
        }
    }

    @Test
    public void testFlushPush() throws IOException {
        final SlidingWindow processor = new SlidingWindow(7, 3);
        float firstValue = 0;
        final DataCollector dataCollector = new DataCollector();
        processor.connectTo(dataCollector);
        AudioBuffer buffer;
        while ((buffer = smallBufferSource.read()) != null) {
            processor.process(buffer);
        }
        processor.flush();
        final List<float[]> results = dataCollector.getOutput();

        assertFalse(results.isEmpty());
        System.out.println("results: " + results.size());

        for (final float[] buf : results) {
            for (final float f : buf) {
                System.out.print(f + "\t");
            }
            System.out.println();
        }

        for (final float[] floats : results) {
            assertEquals(firstValue, floats[0], 0.00001);
            assertEquals(processor.getSliceLengthInFrames(), floats.length);
            firstValue += processor.getHopSizeInFrames();
        }
        final float[] lastResult = results.get(results.size() - 1);
        assertEquals(126f, lastResult[0], 0.0001f);
        assertEquals(127f, lastResult[1], 0.0001f);
        assertEquals(128f, lastResult[2], 0.0001f);
        assertEquals(0f, lastResult[3], 0.0001f);
        assertEquals(0f, lastResult[4], 0.0001f);
        assertEquals(0f, lastResult[5], 0.0001f);
        assertEquals(0f, lastResult[6], 0.0001f);
    }


    @Test
    public void testNullGenerator() throws IOException {
        final SlidingWindow processor = new SlidingWindow();
        processor.connectTo(new NullAudioBufferSource<AudioBuffer>());
        assertNull(processor.read());
    }

    @Test
    public void testEqualsHashCode() {
        final SlidingWindow slidingWindow0 = new SlidingWindow(12, 34);
        final SlidingWindow slidingWindow1 = new SlidingWindow(12, 34);
        final SlidingWindow slidingWindow2 = new SlidingWindow(12, 36);

        assertEquals(slidingWindow0.hashCode(), slidingWindow1.hashCode());
        assertNotEquals(slidingWindow0.hashCode(), slidingWindow2.hashCode());
        assertEquals(slidingWindow0, slidingWindow1);
        assertNotEquals(slidingWindow0, slidingWindow2);
    }


    private static class DataCollector extends AbstractSignalProcessor<AudioBuffer, List<float[]>> {
        private final List<float[]> results = new ArrayList<float[]>();

        @Override
        protected List<float[]> processNext(final AudioBuffer buffer) throws IOException {
            return null;
        }

        @Override
        public void process(final AudioBuffer in) throws IOException {
            results.add(in.getData().clone());
        }

        @Override
        public List<float[]> getOutput() throws IOException {
            return results;
        }
    }
}
