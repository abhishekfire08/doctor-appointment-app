package com.example.doctorapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.R;
import com.example.doctorapp.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> list) {
        this.reviews = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.name.setText(review.patient_name != null ? review.patient_name : "Patient");
        holder.rating.setText(buildStars(review.rating));
        holder.text.setText(review.review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private String buildStars(int rating) {
        int count = Math.max(0, Math.min(5, rating));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("\u2605");
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, rating, text;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvReviewerName);
            rating = itemView.findViewById(R.id.tvReviewRating);
            text = itemView.findViewById(R.id.tvReviewText);
        }
    }
}
