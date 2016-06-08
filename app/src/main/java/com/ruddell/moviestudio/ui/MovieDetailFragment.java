package com.ruddell.moviestudio.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ruddell.moviestudio.R;
import com.ruddell.moviestudio.object_models.Movie;
import com.ruddell.moviestudio.object_models.Rating;
import com.ruddell.moviestudio.object_models.Trailer;
import com.ruddell.moviestudio.provider.MovieContract;
import com.ruddell.moviestudio.provider.MovieProvider;
import com.ruddell.moviestudio.util.Debug;
import com.ruddell.moviestudio.util.FileHelper;
import com.ruddell.moviestudio.util.TMDB_ApiHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
//TODO: Save images to cache when favorite is added
public class MovieDetailFragment extends Fragment implements View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_MOVIE = "movie";
    public static final String ARG_FAVORITE = "favorite";
    private static final String TAG = "MovieDetailFragment";
    private static final boolean DEBUG = true;
    private ArrayList<Trailer> mTrailers;
    private ArrayList<Rating> mRatings;
    private DetailsAdapter mAdapter;
    private boolean mIsFavorite = false;
    private FloatingActionButton mFab = null;
    private ImageView mPoster;
    private Movie mMovie;
    private static final int OptionMenu_ShareTrailer = 100;
    private boolean mOptionMenuNeedsRefreshed = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_MOVIE)) mMovie = getArguments().getParcelable(ARG_MOVIE);
        else mMovie = new Movie("","",0,"","","","");

        mTrailers = new ArrayList<>();
        mRatings = new ArrayList<>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        mFab = (FloatingActionButton)rootView.findViewById(R.id.fabButton);

        mFab.setOnClickListener(this);

        Cursor cursor = getActivity().getContentResolver().query(MovieContract.Movies.CONTENT_URI,
                MovieContract.Movies.PROJECTION_ALL,
                MovieContract.Movies.MOVIE_ID + "='" + mMovie.id + "'", null,
                MovieContract.Movies.DEFAULT_SORT);

        try {
            while(cursor.moveToNext()) {
                mIsFavorite = true;
            }
        }
        catch (Exception e) {

        }
        finally {
            mFab.setImageDrawable(getResources().getDrawable((mIsFavorite ? R.drawable.filled_star : R.drawable.empty_star),null));
        }


        ((TextView)rootView.findViewById(R.id.movieTitle)).setText(mMovie.title);

        mAdapter = new DetailsAdapter(getActivity(),android.R.layout.simple_list_item_1);
        ((ListView)rootView.findViewById(R.id.trailerList)).setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
        new TrailerAsyncTask().execute();
        new RatingAsyncTask().execute();



        return rootView;
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        if (DEBUG) Debug.LOGD(TAG, "onAttach");
        super.onAttach(context);
        if (mOptionMenuNeedsRefreshed) {
            getActivity().invalidateOptionsMenu();
            mOptionMenuNeedsRefreshed = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (DEBUG) Debug.LOGD(TAG,"FAB clicked");
        mIsFavorite = !mIsFavorite;

        mFab.setImageDrawable(getResources().getDrawable((mIsFavorite ? R.drawable.filled_star : R.drawable.empty_star),null));

        if (mIsFavorite) {


            //save image to disk

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //add to database
                        Uri uri = MovieContract.Movies.CONTENT_URI;
                        MovieProvider movieProvider = new MovieProvider(getActivity());


                        ContentValues valuesToAdd = new ContentValues();
                        valuesToAdd.put(MovieContract.Movies.MOVIE_ID, mMovie.id);
                        valuesToAdd.put(MovieContract.Movies.MOVIE_TITLE, mMovie.title);
                        valuesToAdd.put(MovieContract.Movies.MOVIE_OVERVIEW, mMovie.overview);
                        valuesToAdd.put(MovieContract.Movies.MOVIE_RATING, mMovie.rating);
                        valuesToAdd.put(MovieContract.Movies.MOVIE_POPULARITY, mMovie.popularity);
                        valuesToAdd.put(MovieContract.Movies.MOVIE_RELEASE_DATE, mMovie.release_date);
                        valuesToAdd.put(MovieContract.Movies.MOVIE_POSTER_PATH, mMovie.poster_path);

                        Uri newFavoriteUri = movieProvider.insertOnConflict(uri, valuesToAdd, SQLiteDatabase.CONFLICT_REPLACE);
                        if (DEBUG) Debug.LOGD(TAG, "item inserted into db at:" + newFavoriteUri.toString());

                        Bitmap posterAsBitmap = null;
                        try {
                            String imageUrl = TMDB_ApiHandler.getImageRequestUrl(mMovie.poster_path, TMDB_ApiHandler.TMDBImageSize.m);
                            posterAsBitmap = Picasso.with(getActivity()).load(imageUrl).get();
                            FileHelper.saveImageToCache(posterAsBitmap, mMovie.id + ".jpg", getActivity());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

        }
        else {
            //remove from database
            MovieProvider provider = new MovieProvider(getActivity());
            provider.delete(MovieContract.Movies.CONTENT_URI, MovieContract.Movies.MOVIE_ID + " = '" + mMovie.id + "'", null);
        }



    }

    private class TrailerAsyncTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "TrailerAsyncTask";

        @Override
        protected String doInBackground(String... urls) {
            String data = null;
            try {
                if (DEBUG) Debug.LOGD(TAG, "doInBackground...");
                data = TMDB_ApiHandler.performGetRequest(TMDB_ApiHandler.getMovieTrailersUrl(mMovie.id));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result==null) return;
            if (DEBUG) Debug.LOGD(TAG, "onPostExecute(" + result + ")");

            mTrailers.clear();

            try {
                JSONObject tmdbResponse = new JSONObject(result);
                JSONArray trailers = tmdbResponse.getJSONArray("results");
                final int trailerCount = trailers.length();
                for (int i=0; i<trailerCount; i++) {
                    JSONObject trailerData = trailers.getJSONObject(i);
                    Trailer thisTrailer = new Trailer(
                            trailerData.getString("id"),
                            trailerData.getString("key"),
                            trailerData.getString("name"),
                            trailerData.getString("site")

                    );
                    if (thisTrailer.site.equalsIgnoreCase("youtube") && trailerData.getString("type").equalsIgnoreCase("Trailer")) mTrailers.add(thisTrailer);
                }

                mAdapter.notifyDataSetChanged();
                if (getActivity()==null) mOptionMenuNeedsRefreshed = true;
                else getActivity().invalidateOptionsMenu();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class RatingAsyncTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "RatingAsyncTask";

        @Override
        protected String doInBackground(String... urls) {
            String data = null;
            try {
                if (DEBUG) Debug.LOGD(TAG, "doInBackground...");
                data = TMDB_ApiHandler.performGetRequest(TMDB_ApiHandler.getReviewRequestUrl(mMovie.id));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result==null) return;
            if (DEBUG) Debug.LOGD(TAG, "onPostExecute(" + result + ")");

            mRatings.clear();

            try {
                JSONObject tmdbResponse = new JSONObject(result);
                JSONArray ratings = tmdbResponse.getJSONArray("results");
                final int ratingsCount = ratings.length();
                for (int i=0; i<ratingsCount; i++) {
                    JSONObject ratingData = ratings.getJSONObject(i);
                    Rating thisRating = new Rating(
                            ratingData.getString("id"),
                            ratingData.getString("author"),
                            ratingData.getString("url"),
                            ratingData.getString("content")
                    );

                    mRatings.add(thisRating);

                    if (DEBUG) Debug.LOGD(TAG, "rating found:" + ratingData.toString());

                }

                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DetailsAdapter extends ArrayAdapter {

        public DetailsAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return 1 + mTrailers.size() + mRatings.size();
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            if (position==0) return 0;
            else if (position<=mTrailers.size()) return 1;
            else return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position==0) return getHeaderView(position, convertView, parent);
            else if (position<=mTrailers.size()) return getTrailerView(position, convertView, parent);
            else return getRatingsView(position,convertView,parent);
        }

        private View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView==null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.movie_detail_header, parent, false);
            }

            //save poster image to member variable so we can grab the image and save for offline use when favoriting
            mPoster = (ImageView) convertView.findViewById(R.id.poster_image);

            if (mIsFavorite) {
                try {
                    Drawable image = FileHelper.getDrawableFromCache(mMovie.id + ".jpg",MovieDetailFragment.this.getActivity());
                    mPoster.setImageDrawable(image);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                String imageUrl = TMDB_ApiHandler.getImageRequestUrl(mMovie.poster_path, TMDB_ApiHandler.TMDBImageSize.m);
                Picasso.with(getActivity()).load(imageUrl).into(mPoster);
            }


            //set header content for movie
            ((TextView)convertView.findViewById(R.id.releaseDate)).setText("Release Date: " + mMovie.release_date);
            ((TextView)convertView.findViewById(R.id.ratingView)).setText("Rating: " + String.format("%.1f", mMovie.rating) + "/10");
            ((TextView)convertView.findViewById(R.id.description)).setText(mMovie.overview);

            return convertView;
        }

        private View getTrailerView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            TrailerViewHolder holder;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.trailer_item, parent, false);
                holder = new TrailerViewHolder();
                holder.playButton = v.findViewById(R.id.button);
                holder.titleLabel = (TextView) v.findViewById(R.id.trailerText);
                v.setTag(holder);
            } else {
                holder = (TrailerViewHolder) v.getTag();
            }

            final Trailer thisTrailer = mTrailers.get(position-1);
            holder.titleLabel.setText(thisTrailer.name);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DEBUG) Debug.LOGD(TAG,"trailer:" + thisTrailer.id + " clicked");
                    String videoUrl = "http://www.youtube.com/watch_popup?v=" + thisTrailer.key + "&autoplay=1";
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(videoUrl));

                    //ask user for the best app to use...
                    startActivity(Intent.createChooser(intent, getString(R.string.openWithWhatApp)));
                }
            });

            return v;
        }

        private View getRatingsView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            RatingViewHolder holder;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.ratings_item, parent, false);
                holder = new RatingViewHolder();
                holder.ratingAuthor = (TextView)v.findViewById(R.id.ratingAuthor);
                holder.ratingDescription = (TextView)v.findViewById(R.id.ratingDescription);

                v.setTag(holder);
            } else {
                holder = (RatingViewHolder) v.getTag();
            }

            final Rating thisRating = mRatings.get(position-1-mTrailers.size());
            holder.ratingAuthor.setText(thisRating.author);
            holder.ratingDescription.setText(thisRating.description);




            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DEBUG) Debug.LOGD(TAG,"rating:" + thisRating.id + " clicked");
                    //TODO: Do something with this click?
                }
            });

            return v;
        }
    }

    static class TrailerViewHolder {
        View playButton;
        TextView titleLabel;
    }

    static class RatingViewHolder {
        TextView ratingAuthor;
        TextView ratingDescription;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==OptionMenu_ShareTrailer) {
            Trailer firstTrailer = mTrailers.get(0);
            String videoUrl = "http://www.youtube.com/watch_popup?v=" + firstTrailer.key + "&autoplay=1";
            if (DEBUG) Debug.LOGD(TAG, "Sharing trailer:" + videoUrl);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl);
            startActivity(Intent.createChooser(shareIntent,getString(R.string.openWithWhatSharing)));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mTrailers.size()>0) {
            MenuItem menuItem = menu.add(Menu.NONE, OptionMenu_ShareTrailer, Menu.NONE, "Share First Trailer");
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }


        super.onCreateOptionsMenu(menu, inflater);
    }
}
