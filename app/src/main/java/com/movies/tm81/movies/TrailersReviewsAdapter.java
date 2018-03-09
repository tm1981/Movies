package com.movies.tm81.movies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.movies.tm81.movies.model.Review;
import com.movies.tm81.movies.model.Trailer;

import java.util.List;


class TrailersReviewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final List<Trailer> trailers;
    private final List<Review> reviews;
    private final int TRAILERS = 0, REVIEWS = 1, TITLE = 2;
    private final List<String> itemTitles;

    TrailersReviewsAdapter(Context context, List<Trailer> trailers, List<Review> reviews, List<String> itemTitles) {
        this.mInflater = LayoutInflater.from(context);
        this.trailers = trailers;
        this.reviews = reviews;
        this.itemTitles = itemTitles;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        View v1 = mInflater.inflate(R.layout.recycleview_trailer_item, parent, false);
        View v2 =  mInflater.inflate(R.layout.recycleview_review_item, parent, false);
        View v3 = mInflater.inflate(R.layout.recycleview_title_item,parent,false);

        viewHolder = new ViewHolder3(v3);

        switch (viewType) {
            case TRAILERS:
                viewHolder = new ViewHolder1(v1);
                break;
            case REVIEWS:
                viewHolder = new ViewHolder2(v2);
                break;
            case TITLE:
                viewHolder = new ViewHolder3(v3);
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {

        int trailersSize = trailers.size();

        if (trailers.isEmpty() && position == 0  ) {
            return  TITLE;
        } else if (trailers.isEmpty() && position > 0) {
            return REVIEWS;
        } else if (!trailers.isEmpty() && position ==0) {
            return TITLE;
        } else if (!trailers.isEmpty() && position > 0 && trailersSize >= position) {
            return TRAILERS;
        } else if (!trailers.isEmpty() && position == trailersSize+1) {
            return TITLE;
        } else {
            return REVIEWS;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {

            case TRAILERS:
                ViewHolder1 vh1 = (ViewHolder1) viewHolder;
                configureViewHolder1(vh1, position);
                break;
            case REVIEWS:
                ViewHolder2 vh2 = (ViewHolder2) viewHolder;
                configureViewHolder2(vh2, position);
                break;
            case TITLE:
                ViewHolder3 vh3 = (ViewHolder3) viewHolder;
                configureViewHolder3(vh3,position);
        }
    }

    private void configureViewHolder1(ViewHolder1 vh1, int position) {
        vh1.trailerName.setText(trailers.get(position-1).getName());
    }

    private void configureViewHolder2(ViewHolder2 vh2,int position ) {

        int newPosition;

        if (trailers.isEmpty()) {
            newPosition = position - 1;
        } else {
            newPosition = position - trailers.size() -2;
        }

        vh2.reviewTV.setText(reviews.get(newPosition).getContent());
        vh2.authorTV.setText(reviews.get(newPosition).getAuthor());
    }

    private void configureViewHolder3(ViewHolder3 vh3,int position ) {

        if (trailers.isEmpty() && position == 0  ) {
            vh3.titleTV.setText(itemTitles.get(1));
        } else if (!trailers.isEmpty() && position ==0) {
            vh3.titleTV.setText(itemTitles.get(0));
        } else if (!trailers.isEmpty() && position > 0) {
            vh3.titleTV.setText(itemTitles.get(1));
        }

    }

    @Override
    public int getItemCount() {

        if (reviews.isEmpty() && !trailers.isEmpty()) {
            return trailers.size() + itemTitles.size() - 1;

        } else if (!reviews.isEmpty() && trailers.isEmpty()) {
            return reviews.size() + itemTitles.size() - 1;

        } else if (reviews.isEmpty() && trailers.isEmpty()) {
            return 0;

        } else {
            return trailers.size() + reviews.size() + itemTitles.size();
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView trailerName;


        ViewHolder1(View itemView) {
            super(itemView);
            trailerName = itemView.findViewById(R.id.trailer_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(getAdapterPosition());

        }
    }
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public class ViewHolder2 extends RecyclerView.ViewHolder {

        final TextView reviewTV;
        final TextView authorTV;

        ViewHolder2(View itemView) {
            super(itemView);
            reviewTV = itemView.findViewById(R.id.review_tv);
            authorTV = itemView.findViewById(R.id.author_name);
        }
    }

    public class ViewHolder3 extends RecyclerView.ViewHolder {

        final  TextView titleTV;

        ViewHolder3(View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.list_item_title);
        }
    }

}
