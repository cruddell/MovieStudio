/*
 * Copyright (c) 2015. Museum of the Bible
 *
 * Modification History
 * -Imported core functions from com.google.samples.apps.iosched.provider.ScheduleProvider
 * -Updated data access routines to pair with Exhibit data elements
 * -Significant method updates for application specific customization
 */

/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ruddell.moviestudio.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.ruddell.moviestudio.util.Debug;

import java.util.Arrays;
import java.util.HashSet;

public class MovieProvider extends ContentProvider {
    private final static String TAG = "MovieProvider";
    private final static boolean DEBUG = true;

    // database
    private MovieDatabase mDatabase;
    private static final String FAVORITES_TABLE = MovieDatabase.Tables.FAVORITES;

    // UriMatcher and helper constants
    private static final UriMatcher sURIMatcher;

    // query items
    private static final int FAVORITES = 100;

    private Context mContext;


//        public static final long INVALID = -1;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        sURIMatcher.addURI(authority, "favorites", FAVORITES);                  // returns list of favorites

    }


    public MovieProvider(MovieDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    public MovieProvider(Context context) {
        mContext = context;
        this.mDatabase = MovieDatabase.getInstance(context);
    }

    public MovieProvider() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        Debug.LOGD(TAG, "onCreate()");
        if (mContext == null) mContext = getContext();
        mDatabase = MovieDatabase.getInstance(mContext);
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case FAVORITES:
                return MovieContract.Movies.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        if (DEBUG)
            Debug.LOGD(TAG, "query(uri: " + uri + ", COLUMNS: " + Arrays.toString(projection) + ")");

        checkColumns(FAVORITES_TABLE, projection);

        final SQLiteDatabase db = mDatabase.getReadableDatabase();
        Cursor cursor = db.query(true, FAVORITES_TABLE, projection, selection, selectionArgs, null, null, sortOrder, null);

        return cursor;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(
            Uri uri,
            ContentValues values) {

        if (DEBUG)
            Debug.LOGD(TAG, "insert(" + sURIMatcher.match(uri) + ": uri=" + uri + ", Values=" + values.toString() + ")");


        // Support insert operations
        final SQLiteDatabase db = mDatabase.getWritableDatabase();

        switch (sURIMatcher.match(uri)) {
            case FAVORITES: {
                // Validate columns exist for requested content values
                checkColumns(FAVORITES_TABLE, values.keySet().toArray(new String[values.size()]));
                db.insertOrThrow(MovieDatabase.Tables.FAVORITES, null, values);
                notifyChange(uri);
                return MovieContract.Movies.buildItemUri(values.getAsString(MovieContract.Movies.MOVIE_ID));
            }

            default:
                throw new UnsupportedOperationException("Unknown Insert URI: " + uri);
        }
    }

    public Uri insertOnConflict(Uri uri, ContentValues values, int conflictOption) {
        // Support insert operations
        final SQLiteDatabase db = mDatabase.getWritableDatabase();

        switch (sURIMatcher.match(uri)) {
            case FAVORITES:
                if (DEBUG) Debug.LOGD(TAG, "inserting favorite into db...");
                long insertResult = db.insertWithOnConflict(MovieDatabase.Tables.FAVORITES, null, values, conflictOption);
                if (DEBUG) Debug.LOGD(TAG,"insert result:" + insertResult);
                notifyChange(uri);
                return MovieContract.Movies.buildItemUri(values.getAsString(MovieContract.Movies.MOVIE_ID));

            default:
                throw new UnsupportedOperationException("Unknown Insert URI: " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        Debug.LOGD(TAG, "delete(uri=" + uri.getPath());
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        int response = db.delete(FAVORITES_TABLE, selection, selectionArgs);
        notifyChange(uri);
        return response;
    }

    //not implemented as we have no need for updating the table.  If a user "unfavorites" a movie, it will simply be deleted
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void notifyChange(Uri uri) {
        mContext.getContentResolver().notifyChange(uri, null);
    }

    // Validation: ensure our provider supports requested projection columns
    private void checkColumns(String pTableName, String[] projection) {

        HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
        HashSet<String> existingColumns = new HashSet<>(Arrays.asList(MovieContract.Movies.PROJECTION_ALL));
        // use maps to find if the existing columns contain the projected columns
        if (!existingColumns.containsAll(requestedColumns)) {
            Debug.LOGE(TAG, "Table (" + pTableName + ") does not contain all requested columns!  Looking for missing columns...");
            Debug.LOGE(TAG, "Requested Columns:" + requestedColumns.toString());
            Debug.LOGE(TAG, "Existing Columns:" + existingColumns.toString());
            for (String requestedColumn : requestedColumns) {
                if (!existingColumns.contains(requestedColumn))
                    Debug.LOGE(TAG, "MISSING COLUMN:" + requestedColumn);
            }
            throw new IllegalArgumentException("Table (" + pTableName + ") does not contain all requested columns: " + Arrays.toString(projection));
        }
    }
}
