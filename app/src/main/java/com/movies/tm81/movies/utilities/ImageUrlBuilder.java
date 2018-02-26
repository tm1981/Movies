package com.movies.tm81.movies.utilities;

import android.content.Context;
import android.net.Uri;

import com.movies.tm81.movies.R;


public class ImageUrlBuilder {

    public String Build (String imagePath, Context context) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("image.tmdb.org")
                .appendPath("t").appendPath("p").appendPath(context.getString(R.string.image_size))
                .appendEncodedPath(imagePath);
        return builder.build().toString();
    }
}
