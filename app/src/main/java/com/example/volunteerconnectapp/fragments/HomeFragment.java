package com.example.volunteerconnectapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.OpportunityAdapter;
import com.example.volunteerconnectapp.models.ApiHelper;
import com.example.volunteerconnectapp.models.Opportunity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private OpportunityAdapter adapter;
    private List<Opportunity> opportunityList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        loadOpportunities();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this::loadOpportunities);
    }

    private void setupRecyclerView() {
        opportunityList = new ArrayList<>();
        adapter = new OpportunityAdapter(getContext(), opportunityList, new OpportunityAdapter.OnRegisterClickListener() {
            @Override
            public void onRegisterClick(Opportunity opportunity, int position) {
                registerForOpportunity(opportunity, position);
            }
        });


        adapter.setOnOpportunityDeletedListener(this::loadOpportunities);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadOpportunities() {
        showLoading(true);
        Log.d(TAG, "Starting to load opportunities...");

        ApiHelper.fetchOpportunities(getContext(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Response received: " + response);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d(TAG, "Success: " + jsonResponse.getBoolean("success"));
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray opportunities = jsonResponse.getJSONArray("data");
                            Log.d(TAG, "Opportunities count: " + opportunities.length());
                            opportunityList.clear();

                            for (int i = 0; i < opportunities.length(); i++) {
                                JSONObject obj = opportunities.getJSONObject(i);

                                Opportunity opportunity = new Opportunity(
                                        obj.getInt("id"),
                                        obj.getInt("org_id"),
                                        obj.getString("title"),
                                        obj.getString("description"),
                                        obj.optString("location", "Location not specified"),
                                        obj.optString("start_date", ""),
                                        obj.optString("end_date", ""),
                                        obj.getInt("capacity"),
                                        obj.getString("status"),
                                        obj.optString("image_url", ""),
                                        obj.getString("organization_name"),
                                        obj.optString("organization_logo", ""),
                                        obj.getBoolean("is_registered")
                                );
                                opportunityList.add(opportunity);
                            }

                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(opportunityList.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing data", e);
                        Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        });
    }

    private void registerForOpportunity(Opportunity opportunity, int position) {
        progressBar.setVisibility(View.VISIBLE);

        ApiHelper.registerForOpportunity(getContext(), opportunity.getId(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            opportunity.setRegistered(true);
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), "Successfully registered!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Registration failed: " + error, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void showLoading(boolean show) {
        swipeRefresh.setRefreshing(show);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOpportunities();
    }
}