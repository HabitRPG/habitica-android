package com.habitrpg.android.habitica.helpers;

import android.content.Context;

import com.habitrpg.android.habitica.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@Config(constants = BuildConfig.class)
@RunWith(value = RobolectricTestRunner.class)
public class NumberAbbreviatorTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void testThatItDoesntAbbreviatesSmallNumbers() {
        assertEquals("215", NumberAbbreviator.INSTANCE.abbreviate(context, 215));
        assertEquals("2.05", NumberAbbreviator.INSTANCE.abbreviate(context, 2.05));
    }

    @Test
    public void testThatItAbbreviatesThousand() {
        assertEquals("1.55k", NumberAbbreviator.INSTANCE.abbreviate(context, 1550));
    }

    @Test
    public void testThatItAbbreviatesMillion() {
        assertEquals("9.99m", NumberAbbreviator.INSTANCE.abbreviate(context, 9990000));
    }

    @Test
    public void testThatItAbbreviatesBillion() {
        assertEquals("1.99b", NumberAbbreviator.INSTANCE.abbreviate(context, 1990000000));
    }

    @Test
    public void testThatItAbbreviatesThousandWithoutAdditionalDecimals() {
        assertEquals("1k", NumberAbbreviator.INSTANCE.abbreviate(context, 1000));
        assertEquals("1.5k", NumberAbbreviator.INSTANCE.abbreviate(context, 1500));
    }

    @Test
    public void voidtestThatitRoundsCorrectly() {
        assertEquals("9.99k", NumberAbbreviator.INSTANCE.abbreviate(context, 9999));
    }

}