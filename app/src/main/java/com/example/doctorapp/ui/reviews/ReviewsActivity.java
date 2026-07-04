package com.example.doctorapp.ui.reviews;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.ReviewAdapter;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.Review;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

/** Module 10: Reviews and Ratings. */
public class ReviewsActivity extends AppCompatActivity {

    private String doctorId;
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        doctorId = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_ID);
        String doctorName = getIntent().getStringExtra(Constants.EXTRA_DOCTOR_NAME);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText(
                TextUtils.isEmpty(doctorName) ? "Reviews" : "Reviews \u2022 " + doctorName);

        RecyclerView rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter();
        rvReviews.setAdapter(adapter);

        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText etReviewText = findViewById(R.id.etReviewText);

        findViewById(R.id.btnSubmitReview).setOnClickListener(v ->
                submitReview((int) ratingBar.getRating(), etReviewText.getText().toString().trim()));

        loadReviews();
    }

    private void loadReviews() {
        String url = Config.restEndpoint(Constants.TABLE_REVIEWS)
                + "?doctor_id=eq." + doctorId + "&order=created_at.desc";

        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                List<Review> reviews = Arrays.asList(new Gson().fromJson(responseBody, Review[].class));
                adapter.setReviews(reviews);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReviewsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitReview(int rating, String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Write a short review first", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("patient_id", SessionManager.getUserId());
        body.addProperty("patient_name", SessionManager.getFullName());
        body.addProperty("doctor_id", doctorId);
        body.addProperty("rating", rating);
        body.addProperty("review", text);

        ApiClient.post(Config.restEndpoint(Constants.TABLE_REVIEWS), body.toString(), true, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                Toast.makeText(ReviewsActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                loadReviews();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReviewsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
