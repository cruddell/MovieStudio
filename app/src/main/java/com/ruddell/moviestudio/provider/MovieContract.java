package com.ruddell.moviestudio.provider;

/**
 * Created by David Butts on 1/16/15.
 * Contract class for interacting with {@link ExhibitProvider}.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


import com.ruddell.moviestudio.BuildConfig;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID+".provider.MovieProvider";
    private static final boolean DEBUG_LOG = false;
    private static final String TAG = "MovieContract";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_FAVORITES = "favorites";


    public static final String[] TOP_LEVEL_PATHS = {
            PATH_FAVORITES
    };

    interface MovieColumns {
        String MOVIE_ID = "id";
        String MOVIE_TITLE = "title";
        String MOVIE_OVERVIEW = "overview";
        String MOVIE_RATING = "rating";
        String MOVIE_RELEASE_DATE = "release_date";
        String MOVIE_POSTER_PATH = "poster_path";
        String MOVIE_POPULARITY = "popularity";
    }

    // Uri maps to the table in the provider name table_name (FROM table_name)
    // projection is an array of columns that should be included for each row retrieved (col,col,col,...)
    // selection specifies the criteria for selecting rows (WHERE col = value)
    // selectionArgs replace ? placeholders in the selection clause
    // sortOrder specifies the order in which rows appear in the returned cursor
    public static class Movies implements MovieColumns, BaseColumns {


        /**
         * Content URI identifying Items table data
         */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
        public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI, "filter");

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.digitaldocent.movies";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.digitaldocent.movies";

        // Default "ORDER BY" clause (Item barcode in descending order)
        public static final String DEFAULT_SORT = "favorites." + MovieColumns.MOVIE_POPULARITY + " DESC";
        public static final String SORT_RATING = "favorites." + MovieColumns.MOVIE_RATING + " DESC";


        // String array of all table columns
        public static final String[] PROJECTION_ALL =
                {BaseColumns._ID,
                        MOVIE_ID,
                        MOVIE_OVERVIEW,
                        MOVIE_RATING,
                        MOVIE_RELEASE_DATE,
                        MOVIE_POSTER_PATH,
                        MOVIE_POPULARITY,
                        MOVIE_TITLE
                };

        public static final int COLUMN_BASE_ID = 0;
        public static final int COLUMN_MOVIE_ID = 1;
        public static final int COLUMN_MOVIE_OVERVIEW = 2;
        public static final int COLUMN_MOVIE_RATING = 3;
        public static final int COLUMN_MOVIE_RELEASE_DATE = 4;
        public static final int COLUMN_MOVIE_POSTER_PATH = 5;
        public static final int COLUMN_MOVIE_POPULARITY = 6;
        public static final int COLUMN_MOVIE_TITLE = 7;



        public static Uri buildItemUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).build();
        }

        public static String getItemId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

}
