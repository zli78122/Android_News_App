package com.csci571hw9.news;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.csci571hw9.news", appContext.getPackageName());
    }

    @Test
    public void testDate() throws  Exception{
        String time = "2020-04-24T04:24:31Z";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = sdf1.parse(time);
            long old = date.getTime() - 7 * 3600 * 1000;
            System.out.println(old);

            long now = new Date().getTime();
            System.out.println(now);
            System.out.println((now - old) / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
