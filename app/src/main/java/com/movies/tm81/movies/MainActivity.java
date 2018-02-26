package com.movies.tm81.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.movies.tm81.movies.model.Movie;
import com.movies.tm81.movies.utilities.NetworkHelper;
import com.movies.tm81.movies.utilities.NetworkUtils;
import com.movies.tm81.movies.utilities.ParseJson;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,RecyclerViewAdapter.ItemClickListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MOVIES_LOADER = 10;
    private static final String SCHEME = "https";
    private static final String AUTHORITY = "api.themoviedb.org";
    private static final String API_VERSION = "3";
    private static final String KEY_PARAMETER = "api_key";

    private GridLayoutManager gridLayoutManager;
    private List<Movie> movies;
    private ProgressBar progressBar;
    private boolean networkOk = false;
    private CoordinatorLayout coordinatorLayout;
    private boolean loading = true;
    private int currentPageNumber = 1;
    private int numOfPages;
    private RecyclerView recyclerView;
    private int numberOfItems = 10; //define after how much movie items to load next page

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rv_movies);
        recyclerView.addOnScrollListener(scrollListener);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN ); //set color for progressbar for api<21
        coordinatorLayout = findViewById(R.id.coordinator); //for Snackbar

        //set number of movie columns 2 or 4 depending of the device screen
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpWidth > 480){
            //code for big screen (like tablet)
            gridLayoutManager = new GridLayoutManager(this, 4);
            numberOfItems = 5;
        }else{
            gridLayoutManager = new GridLayoutManager(this, 2);
        }

        setupSharedPreferences();
    }



    @Override
    public android.support.v4.content.Loader<String> onCreateLoader(int i, final Bundle bundle) {
        return new MyLoader(this,bundle);

    }


    @Override
    public void onLoadFinished(final android.support.v4.content.Loader<String> loader, String json) {
        Log.d("JSON", "onLoadFinished: " + json);
        ParseJson parseJson = new ParseJson(json);

        if (movies == null) {
            movies = parseJson.getMovieList();
            numOfPages = parseJson.getNumOfPages();
        } else {
            movies.addAll(parseJson.getMovieList());
        }

        recyclerView.setLayoutManager(gridLayoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, movies);

        adapter.setClickListener(this);
        Parcelable recyclerViewState;
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

        progressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<String> loader) {


    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        loadSortFromPreferences(sharedPreferences);
    }

    private void loadSortFromPreferences(SharedPreferences sharedPreferences) {
        String moviePath = sharedPreferences.getString(getString(R.string.sort_key),getString(R.string.pref_sort_value_popular));
        Uri.Builder builder = new Uri.Builder();

        String url = builder.scheme(SCHEME)
                .authority(AUTHORITY).appendPath(API_VERSION)
                .appendEncodedPath(moviePath)
                .appendQueryParameter(KEY_PARAMETER,getString(R.string.api_key))
                .appendQueryParameter("page", String.valueOf(currentPageNumber)).build().toString();

        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        android.support.v4.content.Loader<String> movieApiLoader = loaderManager.getLoader(MOVIES_LOADER);

        networkOk = NetworkHelper.hasNetworkAccess(this);

        Bundle urlBundle = new Bundle();
        urlBundle.putString("URL",url);

        if (networkOk) {
            if (movieApiLoader == null) {
                loaderManager.initLoader(MOVIES_LOADER,urlBundle,this);
            } else {
                loaderManager.restartLoader(MOVIES_LOADER,urlBundle,this);
            }
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            Snackbar.make(coordinatorLayout,R.string.network_not_available, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sort_key))) {
            progressBar.setVisibility(View.VISIBLE);
            movies.clear(); //clear the movies list
            currentPageNumber = 1; //reset the page number
            numberOfItems = 10; //reset the count for the infinite loader

            loadSortFromPreferences(sharedPreferences);
        }

    }

    public static class MyLoader extends AsyncTaskLoader<String> {
        String json;
        Bundle bundle;


        MyLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Nullable
        @Override
        public String loadInBackground() {
            String apiUrlString = bundle.getString("URL");
            if (apiUrlString == null || TextUtils.isEmpty(apiUrlString)) {
                return null;
            }
            try {
                URL movieUrl = new URL(apiUrlString);
                json = NetworkUtils.getResponseFromHttpUrl(movieUrl);
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
            if (json !=null) {
                deliverResult(json);
            }
            else {
                forceLoad();
            }
        }

        @Override
        public void deliverResult(@Nullable String jsonData) {
            super.deliverResult(jsonData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this,SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onItemClick(View view, int position) {
        if (movies != null) {
            Movie singleMovie = movies.get(position); //get single movie
            Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
            intent.putExtra("movie",singleMovie); //send movie information to the DetailActivity
            networkOk = NetworkHelper.hasNetworkAccess(this);
            if (networkOk) {
                startActivity(intent);
            }
            else {
                progressBar.setVisibility(View.INVISIBLE);
                Snackbar.make(coordinatorLayout,R.string.network_not_available, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
            }
        }
    }


    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);


            if (dy>0 && loading) {
                //int visableItemCount = gridLayoutManager.getChildCount();
                //int totalItemCount = gridLayoutManager.getItemCount();
                int pastVisiblesItems = gridLayoutManager.findFirstVisibleItemPosition();


                //if ((visableItemCount + pastVisiblesItems) >= (totalItemCount)) {
                if (pastVisiblesItems >= numberOfItems) {
                    loading = false;
                    numberOfItems = numberOfItems + 20;
                    //get new data;
                    if (currentPageNumber < numOfPages) {
                        currentPageNumber += 1;
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        loadSortFromPreferences(sharedPreferences);
                        loading = true;
                    }

                }
            }

        }
    };
}

