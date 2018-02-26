package com.movies.tm81.movies.model;

import java.io.Serializable;

public class Movie implements Serializable {
    private String title;
    private String poster_path;
    private String overview;
    private float vote_average;
    private String release_date;

    public String getTitle() {
        return title;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getOverview() {
        return overview;
    }


    public float getVote_average() {
        return vote_average;
    }


    public String getRelease_date() {
        return release_date;
    }
}
