package com.ruddell.moviestudio.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.ruddell.moviestudio.R;
import com.ruddell.moviestudio.object_models.Movie;
import com.ruddell.moviestudio.provider.MovieContract;
import com.ruddell.moviestudio.util.Debug;
import com.ruddell.moviestudio.util.FileHelper;
import com.ruddell.moviestudio.util.PrefUtils;
import com.ruddell.moviestudio.util.TMDB_ApiHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {
    private static final String TAG = "MovieListActivity";
    private static final boolean DEBUG = true;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static final int SORT_BY_POPULARITY = 0;
    private static final int SORT_BY_RATING = 1;
    private static final int SORT_BY_FAVORITES = 2;
    private int SORT_BY = SORT_BY_POPULARITY;
    private ArrayList<Movie> mMovies;
    private MovieAdapter mAdapter;
    private GridView mGridView;

    private static final int ViewByPopularity = 0;
    private static final int ViewByRating = 1;
    private static final int ViewByFavorites = 2;
    private static final String STATE_VIEW_MODE = "STATE_VIEW_MODE";
    private static final String STATE_LIST_POSTITION = "STATE_LIST_POSTITION";

    private int mSelectedView = ViewByPopularity;
    private int mLastPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Debug.LOGD(TAG, "onCreate");
        setContentView(R.layout.activity_movie_list);

        mMovies = new ArrayList<>();
        mGridView = (GridView)findViewById(R.id.movie_list);
        mAdapter = new MovieAdapter(this, android.R.layout.simple_list_item_1);
        mGridView.setAdapter(mAdapter);





        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore state members from saved instance
            mSelectedView = savedInstanceState.getInt(STATE_VIEW_MODE);
            mLastPosition = savedInstanceState.getInt(STATE_LIST_POSTITION);
            if (mSelectedView==ViewByFavorites) queryFavorites();
            else {
                //start with assumption we have no internet access, so show favorites
                //if internet is detected, show popular movies
                int viewToUse = mSelectedView;
                mSelectedView = ViewByFavorites;
                queryFavorites();
                new MoviesAsyncTask(viewToUse).execute();
            }
        } else {
            //start with assumption we have no internet access, so show favorites
            //if internet is detected, show popular movies
            mSelectedView = ViewByFavorites;
            queryFavorites();
            new MoviesAsyncTask(ViewByPopularity).execute();
        }



        //get screen size
        int Measuredwidth = 0;
        int Measuredheight = 0;
        android.graphics.Point size = new android.graphics.Point();
        android.view.WindowManager w = this.getWindowManager();

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            w.getDefaultDisplay().getSize(size);

            Measuredwidth = size.x;
            Measuredheight = size.y;

            Debug.LOGE(TAG,"screen size:" + Measuredwidth + "x" + Measuredheight);
        }
        else
        {

        }

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // tablet layouts.
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (DEBUG) Debug.LOGD(TAG, "onSaveInstanceState:" + mSelectedView + " at position:" + mGridView.getFirstVisiblePosition());
        // Save the user's current game state
        savedInstanceState.putInt(STATE_VIEW_MODE, mSelectedView);
        savedInstanceState.putInt(STATE_LIST_POSTITION, mGridView.getFirstVisiblePosition());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }




    private class MoviesAsyncTask extends AsyncTask<String, Void, String> {
        public int selectedView = ViewByPopularity;

        public MoviesAsyncTask(int selectedView) {
            this.selectedView = selectedView;
        }

        @Override
        protected String doInBackground(String... urls) {
            String data = null;
            try {
                if (DEBUG) Debug.LOGD(TAG, "doInBackground...");
                data = TMDB_ApiHandler.performGetRequest(TMDB_ApiHandler.getMovieDiscoveryUrl(selectedView==ViewByPopularity,true));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result==null) return;
            if (DEBUG) Debug.LOGD(TAG, "onPostExecute(" + result + ")");

            MovieListActivity.this.mSelectedView = selectedView;
            mMovies.clear();

            try {
                JSONObject tmdbResponse = new JSONObject(result);
                JSONArray movies = tmdbResponse.getJSONArray("results");
                final int movieCount = movies.length();
                for (int i=0; i<movieCount; i++) {
                    JSONObject movieData = movies.getJSONObject(i);
                    Movie thisMovie = new Movie(
                            movieData.getString("id"),
                            movieData.getString("title"),
                            movieData.getDouble("vote_average"),
                            movieData.getString("overview"),
                            movieData.getString("poster_path"),
                            movieData.getString("release_date"),
                            movieData.getString("popularity"));
                    mMovies.add(thisMovie);
                }

                if (mMovies.size()>0 && mTwoPane) movieClicked(mMovies.get(0));

                mGridView.smoothScrollToPosition(mLastPosition);
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }






    public class MovieAdapter extends ArrayAdapter {
        private Context mContext;

        public MovieAdapter(Context context, int resource) {
            super(context, resource);
            mContext = context;
        }

        @Override
        public int getCount() {
            if (DEBUG) Debug.LOGD(TAG,"grid items to display:" + mMovies.size());
            return mMovies.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.grid_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) v.findViewById(R.id.imageView);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            final Movie thisMovie = mMovies.get(position);

            String imageUrl = TMDB_ApiHandler.getImageRequestUrl(thisMovie.poster_path, TMDB_ApiHandler.TMDBImageSize.m);
            Log.d(TAG, position + ":requesting image from:" + imageUrl);

            if (mSelectedView==ViewByFavorites) {
                try {
                    Drawable image = FileHelper.getDrawableFromCache(thisMovie.id + ".jpg",MovieListActivity.this);
                    holder.imageView.setImageDrawable(image);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else Picasso.with(mContext).load(imageUrl).into(holder.imageView);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movieClicked(thisMovie);

                }
            });

            return v;
        }
    }

    private void movieClicked(Movie thisMovie) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.ARG_MOVIE, thisMovie);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {

            Intent intent = new Intent(MovieListActivity.this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.ARG_MOVIE, thisMovie);
            MovieListActivity.this.startActivity(intent);
        }
    }

    static class ViewHolder {
        ImageView imageView;
    }

    private void queryFavorites() {
        Cursor cursor = this.getContentResolver().query(MovieContract.Movies.CONTENT_URI,
                MovieContract.Movies.PROJECTION_ALL,
                null, null,
                MovieContract.Movies.DEFAULT_SORT);

        mMovies.clear();
        try {
            while (cursor.moveToNext()) {
                String movieId = cursor.getString(MovieContract.Movies.COLUMN_MOVIE_ID);
                String movieTitle = cursor.getString(MovieContract.Movies.COLUMN_MOVIE_TITLE);
                double movieVote = cursor.getDouble(MovieContract.Movies.COLUMN_MOVIE_RATING);
                String movieOverview = cursor.getString(MovieContract.Movies.COLUMN_MOVIE_OVERVIEW);
                String moviePosterPath = cursor.getString(MovieContract.Movies.COLUMN_MOVIE_POSTER_PATH);
                String movieReleaseDate = cursor.getString(MovieContract.Movies.COLUMN_MOVIE_RELEASE_DATE);
                String moviePopularity = cursor.getString(MovieContract.Movies.COLUMN_MOVIE_POPULARITY);


                Movie thisMovie = new Movie(
                        movieId,
                        movieTitle,
                        movieVote,
                        movieOverview,
                        moviePosterPath,
                        movieReleaseDate,
                        moviePopularity);

                mMovies.add(thisMovie);
            }
            mGridView.smoothScrollToPosition(mLastPosition);
            mAdapter.notifyDataSetChanged();

            if (mMovies.size()>0 && mTwoPane) movieClicked(mMovies.get(0));
        }catch(Exception e){
            Debug.LOGE(TAG, Log.getStackTraceString(e));
        } finally {
            // Ensure cursor is closed and not leaked
            if (cursor!=null) cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 0, "Sort by Popularity").setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add(Menu.NONE, 2, 0, "Sort by Rating").setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add(Menu.NONE, 3, 0, "Favorites").setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMovies.clear();
        mAdapter.notifyDataSetChanged();
        switch (item.getItemId()) {
            case 1:
                mSelectedView = ViewByPopularity;
                SORT_BY = SORT_BY_POPULARITY;
                new MoviesAsyncTask(mSelectedView).execute();
                break;
            case 2:
                mSelectedView = ViewByRating;
                SORT_BY = SORT_BY_RATING;
                new MoviesAsyncTask(mSelectedView).execute();
                break;
            case 3:
                mSelectedView = ViewByFavorites;
                queryFavorites();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
