package com.movies.tm81.movies;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movies.tm81.movies.data.MoviesContract;
import com.movies.tm81.movies.model.Movie;
import com.movies.tm81.movies.model.Review;
import com.movies.tm81.movies.model.Trailer;
import com.movies.tm81.movies.utilities.ImageUrlBuilder;
import com.movies.tm81.movies.utilities.NetworkUtils;
import com.movies.tm81.movies.utilities.ParseJsonReviews;
import com.movies.tm81.movies.utilities.ParseJsonTrailers;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<String>>,TrailersReviewsAdapter.ItemClickListener {

    private final List<Trailer> trailers = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private Movie singleMovie;

    private static final int TRAILER_LOADER = 20;
    private static final String SCHEME = "https";
    private static final String AUTHORITY = "api.themoviedb.org";
    private static final String API_VERSION = "3";
    private static final String KEY_PARAMETER = "api_key";
    private static final String MOVIE_PATH = "movie";
    private boolean removedFromFav = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();

        singleMovie = (Movie) intent.getSerializableExtra("movie");

        setTitle(singleMovie.getTitle()); //set action bar title

        TextView titleTV = findViewById(R.id.movie_title);
        TextView plotTV = findViewById(R.id.movie_plot);
        TextView releaseDateTV = findViewById(R.id.data_tv);
        TextView ratingTV = findViewById(R.id.rating_tv);
        ImageView poster = findViewById(R.id.poster_single);
        ImageView background = findViewById(R.id.background_poster);

        titleTV.setText(singleMovie.getTitle());
        plotTV.setText(singleMovie.getOverview());
        releaseDateTV.setText(singleMovie.getRelease_date());
        ratingTV.setText(Float.toString(singleMovie.getVote_average()));
        Picasso.with(getApplicationContext()).load(new ImageUrlBuilder().Build(singleMovie.getPoster_path(),this)).into(poster);
        Picasso.with(getApplicationContext()).load(new ImageUrlBuilder().Build(singleMovie.getPoster_path(),this)).into(background);




        recyclerView = findViewById(R.id.rv_trailers);

        linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        Uri.Builder trailersBuilder = new Uri.Builder();
        Uri.Builder reviewsBuilder = new Uri.Builder();

        String urlTrailers = trailersBuilder.scheme(SCHEME)
                .authority(AUTHORITY).appendPath(API_VERSION)
                .appendEncodedPath(MOVIE_PATH).appendEncodedPath(String.valueOf(singleMovie.getId())).appendEncodedPath("videos")
                .appendQueryParameter(KEY_PARAMETER,getString(R.string.api_key)).build().toString();

        String urlReviews = reviewsBuilder.scheme(SCHEME)
                .authority(AUTHORITY).appendPath(API_VERSION)
                .appendEncodedPath(MOVIE_PATH).appendEncodedPath(String.valueOf(singleMovie.getId())).appendEncodedPath("reviews")
                .appendQueryParameter(KEY_PARAMETER,getString(R.string.api_key)).build().toString();

        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        android.support.v4.content.Loader<String> loader = loaderManager.getLoader(TRAILER_LOADER);

        Bundle urlBundle = new Bundle();
        urlBundle.putString("URL",urlTrailers);
        urlBundle.putString("urlReviews",urlReviews);

        if (loader == null) {
            loaderManager.initLoader(TRAILER_LOADER,urlBundle,this);
        }
        else {
            loaderManager.restartLoader(TRAILER_LOADER,urlBundle,this);
        }


    }

    private void InsertToDB(Movie movie) {

        if (!CheckIfMovieExists(movie)) {

            new InsertAsyncTask(movie,this).execute();

        }

        else {
            if (DeleteFromDb(movie)) {

                ReloadMainActivityAfterRemovedFromFav();
                removedFromFav = false;
                finish();

                Toast.makeText(getApplicationContext(), R.string.movie_removed_from_fav,Toast.LENGTH_LONG).show();


            }
        }


    }

    private boolean CheckIfMovieExists(Movie movie) {

        ContentResolver resolver = getContentResolver();
        Uri singleUri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI,movie.getId());
        Cursor cursor = resolver.query(singleUri,null,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,null, null);

        String movieId= "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            int movieIdCol = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID);
            movieId = cursor.getString(movieIdCol);

            cursor.close();
        }
        return movieId.equals(String.valueOf(movie.getId()));

    }

    private boolean DeleteFromDb(Movie movie) {
        ContentResolver resolver = getContentResolver();

        String movieId = String.valueOf(movie.getId());
        Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(movieId).build();

        int rowDeleted = resolver.delete(uri,null,null);
        removedFromFav = true;

        return rowDeleted == 1;

    }


    @NonNull
    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle bundle) {
        return new DetailActivityLoader(this,bundle);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<String>> loader, List<String> json) {

        List<Trailer> videos;
        List<Review> reviews = new ArrayList<>();
        if (json != null) {
            ParseJsonTrailers parseJsonTrailers = new ParseJsonTrailers(json.get(0));
            ParseJsonReviews parseJsonReviews = new ParseJsonReviews(json.get(1));

            videos = parseJsonTrailers.getTrailersList();
            reviews = parseJsonReviews.getReviewList();

            for (Trailer video : videos) {

                if (video.getType().equals("Trailer")) {

                    trailers.add(video);
                }
            }
        }


        List<String> itemTitles = new ArrayList<>();
        itemTitles.add("Trailers:");
        itemTitles.add("Reviews:");


        recyclerView.setLayoutManager(linearLayoutManager);
        TrailersReviewsAdapter adapter = new TrailersReviewsAdapter(getApplicationContext(), trailers, reviews,itemTitles);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);


    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<String>> loader) {

    }


    @Override
    public void onItemClick(int position) {
        String youtubeId = trailers.get(position-1).getKey();
        String url = "https://www.youtube.com/watch?v=";

        String youtubeUrl = url + youtubeId;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(youtubeUrl));
        startActivity(intent);

    }

    public static class DetailActivityLoader extends AsyncTaskLoader<List<String>> {
        final List<String> json = new ArrayList<>();
        final Bundle bundle;


        DetailActivityLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Nullable
        @Override
        public List<String> loadInBackground() {
            String firstUrlString = bundle.getString("URL");
            if (firstUrlString == null || TextUtils.isEmpty(firstUrlString)) {
                return null;
            }

            String secondUrlString = bundle.getString("urlReviews");
            if (secondUrlString == null || TextUtils.isEmpty(secondUrlString)) {
                return null;
            }
            try {
                URL url1 = new URL(firstUrlString);
                json.add(NetworkUtils.getResponseFromHttpUrl(url1));

                URL url2 = new URL(secondUrlString);
                json.add(NetworkUtils.getResponseFromHttpUrl(url2));

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return json;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (bundle == null) {
                return;
            }
            if (!json.isEmpty()) {
                deliverResult(json);
            }
            else {
                forceLoad();
            }
        }

        @Override
        public void deliverResult(@Nullable List<String> jsonData) {
            super.deliverResult(jsonData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail,menu);

        if (CheckIfMovieExists(singleMovie)) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_yellow_30dp));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_to_db:

                InsertToDB(singleMovie);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class InsertAsyncTask extends AsyncTask<Void, Void, Uri> {

        final Movie movie;
        private final WeakReference<DetailActivity> activityReference;


        InsertAsyncTask(Movie movie,DetailActivity context) {

            this.movie = movie;
            activityReference = new WeakReference<>(context);


        }

        @Override
        protected Uri doInBackground(Void... params) {

            ContentResolver resolver = activityReference.get().getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME, movie.getTitle());
            cv.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie.getId());
            cv.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, movie.getPoster_path());
            cv.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie.getOverview());
            cv.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie.getRelease_date());
            cv.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVote_average());

            return resolver.insert(MoviesContract.MoviesEntry.CONTENT_URI,cv);

        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);

            if (uri != null) {

                Toast.makeText(activityReference.get(), R.string.movie_added_to_fav,Toast.LENGTH_LONG).show();

            }

            activityReference.get().finish();
        }
    }

    private void ReloadMainActivityAfterRemovedFromFav () {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String moviePath = sharedPreferences.getString(getString(R.string.sort_key),getString(R.string.pref_sort_value_popular));
        if (moviePath.equals(getString(R.string.pref_sort_value_fav))) {
            if (removedFromFav) {
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                removedFromFav = false;
                finish();

            }
        }
    }



}
