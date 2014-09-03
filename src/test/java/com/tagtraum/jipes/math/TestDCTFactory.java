/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TestDCTFactory.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDCTFactory {

    @Test
    public void testResultLength() {
        final float[] in = new float[] {
                1, 2, 3, 4, 5, 6, 7, 100, 9, 10, 11, 12, 13, 14, 15, 16
        };
        final Transform dct = DCTFactory.getInstance().create(in.length);
        final float[] result = dct.transform(in)[0];
        assertEquals(in.length, result.length);
    }

    @Test
    public void testForwardDCT() {
        final float[] realIn = new float[]{1, 3, 1, 9, -5, 3, 2, 8};
        final float[][] result = DCTFactory.getInstance().create(realIn.length).transform(realIn);

        final float[] real = result[0];
        assertEquals(44f, real[0], 0.0001f);
        assertEquals(-8.827807f, real[1], 0.0001f);
        assertEquals(10.004162f, real[2], 0.0001f);
        assertEquals(-23.663581f, real[3], 0.0001f);
        assertEquals(5.656854f, real[4], 0.0001f);
        assertEquals(12.761234f, real[5], 0.0001f);
        assertEquals(1.979075f, real[6], 0.0001f);
        assertEquals(-34.630271f, real[7], 0.0001f);
    }

    @Test
    public void testZeroDCT() {
        final float[] floats = new float[1024];
        final Transform dct = DCTFactory.getInstance().create(floats.length);
        final float[][] transform = dct.transform(floats);
        for (int i=0; i<floats.length; i++) {
            assertEquals(0f, transform[0][i], 0.0001f);
        }
        assertEquals(floats.length, transform[0].length);
    }

}
