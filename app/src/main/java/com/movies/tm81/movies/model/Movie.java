package com.movies.tm81.movies.model;

import java.io.Serializable;

public class Movie implements Serializable {
    private String title;
    private String poster_path;
    private String overview;
    private float vote_average;
    private String release_date;
    private int id;

    public String getTitle() {
        return title;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public float getVote_average() { return vote_average; }

    public String getRelease_date() {
        return release_date;
    }

    public int getId() { return id; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setVote_average(float vote_average) {
        this.vote_average = vote_average;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public void setId(int id) {
        this.id = id;
    }
}
