/*
 * Copyright (c) 2015. Museum of the Bible
 */

package com.ruddell.moviestudio;

import android.os.Build;

import com.ruddell.moviestudio.util.Debug;

import java.util.TimeZone;

/**
 * General configuration information.
 */
public class Config {
    public final static String TAG = "Config";
    public final static boolean DEBUG = false;

    public static final String BOOTSTRAP_DATA_TIMESTAMP = "Mon, 1 Jun 2015 00:42:42 GMT";

//    public static final long[] EXHIBIT_DAYS = new long[] {
//            // start and end of exhibit
//            parseTime("2015-06-25T07:00:00.000Z"),
//            parseTime("2014-06-26T06:59:59.999Z")
//    };

//    private static final Time sTime = new Time();
//    public static long parseTime(String time) {
//        sTime.parse3339(time);
//        return sTime.toMillis(false);
//    }

    public static final TimeZone EXHIBIT_TIMEZONE = TimeZone.getTimeZone("America/Los_Angeles");

    public static boolean isCustomRom() {
        if(DEBUG) Debug.LOGD(TAG, "Build=" + Build.DISPLAY.toLowerCase() + "  isCustomRom=" + Build.DISPLAY.toLowerCase().contains("motb"));
        return Build.DISPLAY.toLowerCase().contains("motb");
    }
}
