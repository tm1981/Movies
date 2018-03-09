package com.movies.tm81.movies.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movies.tm81.movies.model.Trailer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ParseJsonTrailers {
    private final List<Trailer> trailersList = new ArrayList<>();

    public  ParseJsonTrailers(String jsonData) {
        JsonElement jsonElement = new JsonParser().parse(jsonData);
        JsonObject jobject = jsonElement.getAsJsonObject();
        JsonArray itemsArray = jobject.getAsJsonArray("results");
        String jsonTrailer = itemsArray.toString();
        Gson gson = new Gson();
        Trailer[] trailers = gson.fromJson(jsonTrailer, Trailer[].class);

        Collections.addAll(trailersList, trailers);
    }

    public List<Trailer> getTrailersList() {
        return trailersList;
    }
}
