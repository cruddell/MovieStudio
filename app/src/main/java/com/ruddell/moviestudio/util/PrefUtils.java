/*
 * Copyright (c) 2015. Museum of the Bible
 */

package com.ruddell.moviestudio.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PrefUtils {
    private static final String TAG = "PrefUtils";
    private static final String PREF_LAST_VIEW_MODE = "PREF_LAST_VIEW_MODE";

    public static void setPrefLastViewMode(final Context context, int viewMode) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_LAST_VIEW_MODE, viewMode);
    }

    public static int getPrefLastViewMode(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_LAST_VIEW_MODE, 0);
    }



}
