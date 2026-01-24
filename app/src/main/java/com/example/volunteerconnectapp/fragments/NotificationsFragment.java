package com.example.volunteerconnectapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.NotificationAdapter;
import com.example.volunteerconnectapp.models.ApiHelper;
import com.example.volunteerconnectapp.models.Notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";

    private ListView listViewNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private SwipeRefreshLayout swipeRefresh;
    private TextView btnMarkAllRead;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        initViews(view);
        setupListView();
        loadNotifications();

        return view;
    }

    private void initViews(View view) {
        listViewNotifications = view.findViewById(R.id.listViewNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);

        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void setupListView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        listViewNotifications.setAdapter(adapter);


        listViewNotifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notification notification = notificationList.get(position);
                if (!notification.isRead()) {
                    markNotificationAsRead(notification, position);
                }
            }
        });
    }

    private void loadNotifications() {
        showLoading(true);
        Log.d(TAG, "Loading notifications...");

        ApiHelper.fetchNotifications(getContext(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Notifications response: " + response);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray notifications = jsonResponse.getJSONArray("data");
                            Log.d(TAG, "Notifications count: " + notifications.length());
                            notificationList.clear();

                            for (int i = 0; i < notifications.length(); i++) {
                                JSONObject obj = notifications.getJSONObject(i);

                                Notification notification = new Notification(
                                        obj.getInt("id"),
                                        obj.getInt("user_id"),
                                        obj.getString("message"),
                                        obj.getBoolean("is_read"),
                                        obj.getString("created_at")
                                );
                                notificationList.add(notification);
                            }

                            adapter.notifyDataSetChanged();

                            // Show/hide empty state
                            if (notificationList.isEmpty()) {
                                emptyLayout.setVisibility(View.VISIBLE);
                                listViewNotifications.setVisibility(View.GONE);
                                btnMarkAllRead.setVisibility(View.GONE);
                            } else {
                                emptyLayout.setVisibility(View.GONE);
                                listViewNotifications.setVisibility(View.VISIBLE);
                                btnMarkAllRead.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing notifications", e);
                        Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading notifications: " + error);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        });
    }

    private void markNotificationAsRead(Notification notification, int position) {
        ApiHelper.markNotificationRead(getContext(), notification.getId(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            notification.setRead(true);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error marking notification as read", e);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error marking notification as read: " + error);
            }
        });
    }

    private void markAllAsRead() {
        // Check if there are any unread notifications
        boolean hasUnread = false;
        for (Notification notif : notificationList) {
            if (!notif.isRead()) {
                hasUnread = true;
                break;
            }
        }

        if (!hasUnread) {
            Toast.makeText(getContext(), "All notifications already read", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ApiHelper.markAllNotificationsRead(getContext(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // Mark all notifications as read in the list
                            for (Notification notif : notificationList) {
                                notif.setRead(true);
                            }
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error marking all as read: " + error, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void showLoading(boolean show) {
        swipeRefresh.setRefreshing(show);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications(); // Refresh when fragment becomes visible
    }
}