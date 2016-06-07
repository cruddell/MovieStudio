/*
 * Copyright (c) 2015. Museum of the Bible
 */

package com.ruddell.moviestudio.object_models;


import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    public String id;
    public String title;
    public double rating;
    public String overview;
    public String poster_path;
    public String release_date;
    public String popularity;

    protected Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        rating = in.readDouble();
        overview = in.readString();
        poster_path = in.readString();
        release_date = in.readString();
        popularity = in.readString();
    }

    public Movie(String id, String title, double rating, String overview, String poster_path, String release_date, String popularity) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.overview = overview;
        this.poster_path = poster_path;
        this.release_date = release_date;
        this.popularity = popularity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeDouble(rating);
        dest.writeString(overview);
        dest.writeString(poster_path);
        dest.writeString(release_date);
        dest.writeString(popularity);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
