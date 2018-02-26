package com.movies.tm81.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movies.tm81.movies.model.Movie;
import com.movies.tm81.movies.utilities.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private List<Movie> movies;


    // data is passed into the constructor
    RecyclerViewAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.movies = movies;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String posterStringUrl = new ImageUrlBuilder().Build(movies.get(position).getPoster_path(),context);
        Picasso.with(context).load(posterStringUrl).placeholder(R.drawable.movie_poster_place_holder).into(holder.poster);
        holder.movieName.setText(movies.get(position).getTitle());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return movies.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView movieName;
        ImageView poster;

        ViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            movieName = itemView.findViewById(R.id.movie_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
//    String getItem(int id) {
//        return "clicked";
//    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
