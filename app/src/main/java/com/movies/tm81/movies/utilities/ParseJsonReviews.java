package com.movies.tm81.movies.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movies.tm81.movies.model.Review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ParseJsonReviews {
    private final List<Review> reviewList = new ArrayList<>();

    public ParseJsonReviews(String jsonData) {
        JsonElement jsonElement = new JsonParser().parse(jsonData);
        JsonObject jobject = jsonElement.getAsJsonObject();
        JsonArray itemsArray = jobject.getAsJsonArray("results");
        String jsonReviews = itemsArray.toString();
        Gson gson = new Gson();
        Review[] reviews = gson.fromJson(jsonReviews, Review[].class);

        Collections.addAll(reviewList, reviews);
    }

    public List<Review> getReviewList() {
        return reviewList;
    }
}
