package com.example.volunteerconnectapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.VolunteerRegistrationAdapter;
import com.example.volunteerconnectapp.models.OrganizationRegistrationAdapter;
import com.example.volunteerconnectapp.models.ApiHelper;
import com.example.volunteerconnectapp.models.Opportunity;
import com.example.volunteerconnectapp.models.VolunteerRegistration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistrationsFragment extends Fragment {

    private static final String TAG = "RegistrationsFragment";

    private ListView listView;
    private VolunteerRegistrationAdapter volunteerAdapter;
    private OrganizationRegistrationAdapter organizationAdapter;
    private List<Opportunity> volunteerRegistrationList;
    private List<VolunteerRegistration> organizationRegistrationList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;

    private String userType;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registrations, container, false);

        // Get user type and ID from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userType = prefs.getString("user_type", "volunteer");
        userId = prefs.getInt("user_id", -1);

        Log.d(TAG, "User Type: " + userType + ", User ID: " + userId);

        initViews(view);
        setupListView();
        loadRegistrations();

        return view;
    }

    private void initViews(View view) {
        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this::loadRegistrations);
    }

    private void setupListView() {
        if (userType.equals("volunteer")) {
            // Setup volunteer adapter (read-only list of registered opportunities)
            volunteerRegistrationList = new ArrayList<>();
            volunteerAdapter = new VolunteerRegistrationAdapter(getContext(), volunteerRegistrationList);
            listView.setAdapter(volunteerAdapter);
        } else {
            // Setup organization adapter (pending registrations with Accept/Reject)
            organizationRegistrationList = new ArrayList<>();
            organizationAdapter = new OrganizationRegistrationAdapter(getContext(), organizationRegistrationList,
                    new OrganizationRegistrationAdapter.OnActionClickListener() {
                        @Override
                        public void onAccept(VolunteerRegistration registration, int position) {
                            updateRegistrationStatus(registration.getRegistrationId(), "approved", position);
                        }

                        @Override
                        public void onReject(VolunteerRegistration registration, int position) {
                            updateRegistrationStatus(registration.getRegistrationId(), "rejected", position);
                        }
                    });
            listView.setAdapter(organizationAdapter);
        }
    }

    private void loadRegistrations() {
        showLoading(true);

        if (userType.equals("volunteer")) {
            loadVolunteerRegistrations();
        } else {
            loadOrganizationRegistrations();
        }
    }

    private void loadVolunteerRegistrations() {
        Log.d(TAG, "Loading volunteer registrations...");

        ApiHelper.fetchVolunteerRegistrations(getContext(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Volunteer registrations response: " + response);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray registrations = jsonResponse.getJSONArray("data");
                            Log.d(TAG, "Volunteer registrations count: " + registrations.length());
                            volunteerRegistrationList.clear();

                            for (int i = 0; i < registrations.length(); i++) {
                                JSONObject obj = registrations.getJSONObject(i);

                                Opportunity opportunity = new Opportunity(
                                        obj.getInt("opportunity_id"),
                                        obj.getInt("org_id"),
                                        obj.getString("title"),
                                        obj.getString("description"),
                                        obj.optString("location", "Location not specified"),
                                        obj.optString("start_date", ""),
                                        obj.optString("end_date", ""),
                                        obj.getInt("capacity"),
                                        obj.getString("registration_status"),
                                        obj.optString("image_url", ""),
                                        obj.getString("organization_name"),
                                        obj.optString("organization_logo", ""),
                                        true


                                );
                                volunteerRegistrationList.add(opportunity);
                            }

                            volunteerAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(volunteerRegistrationList.isEmpty() ? View.VISIBLE : View.GONE);
                            if (volunteerRegistrationList.isEmpty()) {
                                tvEmpty.setText("No registrations yet");
                            }
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing volunteer registrations", e);
                        Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading volunteer registrations: " + error);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        });
    }

    private void loadOrganizationRegistrations() {
        Log.d(TAG, "Loading organization registrations...");

        ApiHelper.fetchOrganizationRegistrations(getContext(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Organization registrations response: " + response);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray registrations = jsonResponse.getJSONArray("data");
                            Log.d(TAG, "Organization registrations count: " + registrations.length());
                            organizationRegistrationList.clear();

                            for (int i = 0; i < registrations.length(); i++) {
                                JSONObject obj = registrations.getJSONObject(i);

                                VolunteerRegistration registration = new VolunteerRegistration(
                                        obj.getInt("registration_id"),
                                        obj.getInt("volunteer_id"),
                                        obj.getInt("opportunity_id"),
                                        obj.getString("volunteer_name"),
                                        obj.optString("volunteer_email", ""),
                                        obj.optString("volunteer_phone", ""),
                                        obj.optString("volunteer_skills", ""),
                                        obj.getString("opportunity_title"),
                                        obj.optString("start_date", ""),
                                        obj.optString("location", ""),
                                        obj.getString("status"),
                                        obj.getString("registered_at")
                                );
                                organizationRegistrationList.add(registration);
                            }

                            organizationAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(organizationRegistrationList.isEmpty() ? View.VISIBLE : View.GONE);
                            if (organizationRegistrationList.isEmpty()) {
                                tvEmpty.setText("No pending registrations");
                            }
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing organization registrations", e);
                        Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading organization registrations: " + error);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        });
    }

    private void updateRegistrationStatus(int registrationId, String status, int position) {
        progressBar.setVisibility(View.VISIBLE);

        ApiHelper.updateRegistrationStatus(getContext(), registrationId, status, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(getContext(),
                                    status.equals("approved") ? "Registration approved!" : "Registration rejected",
                                    Toast.LENGTH_SHORT).show();

                            // Remove from list
                            organizationRegistrationList.remove(position);
                            organizationAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(organizationRegistrationList.isEmpty() ? View.VISIBLE : View.GONE);
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
                    Toast.makeText(getContext(), "Error updating status: " + error, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void showLoading(boolean show) {
        swipeRefresh.setRefreshing(show);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        listView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRegistrations(); // Refresh when fragment becomes visible
    }
}