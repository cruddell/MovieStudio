package com.ruddell.moviestudio.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.ruddell.moviestudio.util.Debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by David Butts on 1/16/15.
 * Contract class for interacting with {@link MovieProvider}.
 */

public class MovieDatabase extends SQLiteOpenHelper {
    private static final String TAG = "MovieDatabase";
    private static final boolean DEBUG_LOG = false;

    private static final String DATABASE_NAME = "popularmovies.db";

    // Database version information
    private static final int DATABASE_VERSION = 1;

    private static MovieDatabase mInstance = null;

    public static MovieDatabase getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (mInstance == null) {
            mInstance = new MovieDatabase(context.getApplicationContext());
        }
        return mInstance;
    }

    private MovieDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (DEBUG_LOG) Debug.LOGD(TAG, "database initialized with version:" + DATABASE_VERSION + " (" + this.getReadableDatabase().getVersion() + ")");
    }

    /* Supported Tables (define table strings in one place) */
    interface Tables {
        String FAVORITES = "favorites";


        List<String> TABLE_LIST = new ArrayList<String>(
                Arrays.asList(new String[]{FAVORITES}));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Debug.LOGI(TAG, "onCreate() checkpoint");

        // POI (Point of Interest) table to track exhibit notables (themes, galleries, cases, artifacts)
        db.execSQL("CREATE TABLE " + Tables.FAVORITES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MovieContract.MovieColumns.MOVIE_ID + " TEXT NOT NULL,"
                + MovieContract.MovieColumns.MOVIE_TITLE + " TEXT NOT NULL,"
                + MovieContract.MovieColumns.MOVIE_OVERVIEW + " TEXT,"
                + MovieContract.MovieColumns.MOVIE_POPULARITY + " TEXT,"
                + MovieContract.MovieColumns.MOVIE_RATING + " TEXT,"
                + MovieContract.MovieColumns.MOVIE_POSTER_PATH + " TEXT,"
                + MovieContract.MovieColumns.MOVIE_RELEASE_DATE + " TEXT,"
                + "UNIQUE (" + MovieContract.MovieColumns.MOVIE_ID + ") ON CONFLICT REPLACE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Debug.LOGI(TAG,"onUpgrade() from " + oldVersion + " to " + newVersion);

        //initial version - no need to perform any logic here
        //in future versions, use this to drop tables if necesary, etc
    }



}