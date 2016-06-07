package com.ruddell.moviestudio.util;


import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chris on 11/17/15.
 */
public class TMDB_ApiHandler {
    private static final boolean DEBUG = true;
    private static final String TAG = "TMDB_ApiHandler";
    private static String mBaseUrl = "http://api.themoviedb.org/3/";
    private static final String mImageRequestBaseUrl = "http://image.tmdb.org/t/p/";

    private static final String TMDB_API_KEY = "8f53d6e1af4c7cfddfe0ed6bee89d436";
    //8f53d6e1af4c7cfddfe0ed6bee89d436
    //INSERT_API_KEY_HERE

    public static String getTMDBUrl(String imageSize, String imagename) throws InvalidImageSizeException {
        if (!stringFromInterface(imageSize)) {
            Log.e("NetworkManager", "Invalid Image Size - use TMDBImageSize interface!");
            throw new InvalidImageSizeException();
        }
        return mBaseUrl + imageSize + "/" + imagename;
    }

    public static String getImageRequestUrl(String posterPath, String imageSize) {
        return mImageRequestBaseUrl + imageSize + "/" + posterPath;
    }

    public static String getApiKey() {
        return TMDB_API_KEY;
    }

    public static String getTrailerRequestUrl(String movieId) {
        return mBaseUrl + "movie/" + movieId + "/videos?api_key=" + getApiKey();
    }

    public static String getReviewRequestUrl(String movieId) {
        return mBaseUrl + "movie/" + movieId + "/reviews?api_key=" + getApiKey();
    }

    public static String getMovieTrailersUrl(String movieId) {
        return mBaseUrl + "movie/" + movieId + "/videos?api_key=" + getApiKey();
    }

    public static String getMovieDiscoveryUrl(boolean byPopularity, boolean descending) {
        return mBaseUrl + "discover/movie?sort_by=" + (byPopularity ? "popularity" : "vote_average") + (descending ? ".desc" : ".asc") + "&api_key=" + getApiKey();
    }

    public interface TMDBImageSize {
        String xxs = "w92";
        String xs = "w154";
        String s = "w185";
        String m = "w342";
        String l = "w500";
        String xl = "w780";
        String original = "original";

    }

    //post data to script
    public static String performGetRequest(String encodedUrl) throws IOException {
        if (DEBUG) Debug.LOGD(TAG, "performGetRequest(" + encodedUrl + ")");
        URL obj = new URL(encodedUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }


    public static class InvalidImageSizeException extends Exception {

    }

    private static boolean stringFromInterface(String valueToCheck) {
        return (!valueToCheck.equals(TMDBImageSize.xxs) && !valueToCheck.equals(TMDBImageSize.xs) && !valueToCheck.equals(TMDBImageSize.s) && !valueToCheck.equals(TMDBImageSize.m) && !valueToCheck.equals(TMDBImageSize.l) && !valueToCheck.equals(TMDBImageSize.xl) && !valueToCheck.equals(TMDBImageSize.original));
    }
}
