package com.movies.tm81.movies.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class MoviesContract {



    public static final String AUTHORITY = "com.movies.tm81.movies";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "favoritelist";

    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "favoritelist";

        public static final String COLUMN_MOVIE_NAME = "movieName";

        public static final String COLUMN_MOVIE_ID = "movieId";

        public static final String COLUMN_POSTER_PATH = "posterPath";

        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";

        public static final String COLUMN_RELEASE_DATE = "releaseDate";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_TIMESTAMP = "timestamp";


    }

}
