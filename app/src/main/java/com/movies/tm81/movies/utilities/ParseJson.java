package com.movies.tm81.movies.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movies.tm81.movies.model.Movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParseJson {

    private final List<Movie> movieList = new ArrayList<>();
    private final int numOfPages;

    public  ParseJson(String jsonData) {
        JsonElement jsonElement = new JsonParser().parse(jsonData);
        JsonObject jobject = jsonElement.getAsJsonObject();
        JsonArray itemsArray = jobject.getAsJsonArray("results");
        String jsonMovie = itemsArray.toString();
        Gson gson = new Gson();
        Movie[] movies = gson.fromJson(jsonMovie, Movie[].class);

        Collections.addAll(movieList, movies);

        JsonElement pagesFromElement = jobject.get("total_pages");
        numOfPages = Integer.parseInt(pagesFromElement.getAsString());
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    public int getNumOfPages() {
        return numOfPages;
    }
}
