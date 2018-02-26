package com.movies.tm81.movies;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.movies.tm81.movies.model.Movie;
import com.movies.tm81.movies.utilities.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        Movie singleMovie = (Movie) intent.getSerializableExtra("movie");

        setTitle(singleMovie.getTitle()); //set action bar title

        TextView titleTV = findViewById(R.id.movie_title);
        TextView plotTV = findViewById(R.id.movie_plot);
        TextView releaseDateTV = findViewById(R.id.data_tv);
        TextView ratingTV = findViewById(R.id.rating_tv);
        ImageView poster = findViewById(R.id.poster_single);
        ImageView background = findViewById(R.id.backgroud_poster);

        titleTV.setText(singleMovie.getTitle());
        plotTV.setText(singleMovie.getOverview());
        releaseDateTV.setText(singleMovie.getRelease_date());
        ratingTV.setText(Float.toString(singleMovie.getVote_average()));
        Picasso.with(getApplicationContext()).load(new ImageUrlBuilder().Build(singleMovie.getPoster_path(),this)).into(poster);
        Picasso.with(getApplicationContext()).load(new ImageUrlBuilder().Build(singleMovie.getPoster_path(),this)).into(background);
    }
}
