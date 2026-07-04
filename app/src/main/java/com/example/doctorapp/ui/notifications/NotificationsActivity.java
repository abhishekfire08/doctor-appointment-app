package com.example.doctorapp.ui.notifications;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.NotificationAdapter;
import com.example.doctorapp.config.Config;
import com.example.doctorapp.models.AppNotification;
import com.example.doctorapp.network.ApiCallback;
import com.example.doctorapp.network.ApiClient;
import com.example.doctorapp.utils.Constants;
import com.example.doctorapp.utils.SessionManager;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

/** Module 12: Notifications (in-app list; push notifications are a bonus/optional feature). */
public class NotificationsActivity extends AppCompatActivity {

    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeRefreshNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.tvToolbarBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvToolbarTitle)).setText("Notifications");

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        swipeRefreshNotifications = findViewById(R.id.swipeRefreshNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        rvNotifications.setAdapter(adapter);

        swipeRefreshNotifications.setOnRefreshListener(this::loadNotifications);
        loadNotifications();
    }

    private void loadNotifications() {
        String userId = SessionManager.getUserId();
        String url = Config.restEndpoint(Constants.TABLE_NOTIFICATIONS)
                + "?user_id=eq." + userId + "&order=created_at.desc";

        ApiClient.get(url, new ApiCallback() {
            @Override
            public void onSuccess(String responseBody) {
                swipeRefreshNotifications.setRefreshing(false);
                List<AppNotification> notifications = Arrays.asList(new Gson().fromJson(responseBody, AppNotification[].class));
                adapter.setNotifications(notifications);
            }

            @Override
            public void onError(String message) {
                swipeRefreshNotifications.setRefreshing(false);
                Toast.makeText(NotificationsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
