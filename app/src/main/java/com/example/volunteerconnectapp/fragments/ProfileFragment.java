package com.example.volunteerconnectapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.ApiHelper;
import com.example.volunteerconnectapp.models.UserProfile;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "user_id";

    private ImageView ivProfileImage;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvUserType;
    private LinearLayout volunteerSection;
    private LinearLayout organizationSection;
    private TextView tvBio;
    private TextView tvSkills;
    private TextView tvAvailability;
    private TextView tvPhoneVolunteer;
    private TextView tvRegistrationCount;
    private TextView tvOrgName;
    private TextView tvWebsite;
    private TextView tvPhoneOrg;
    private TextView tvAddress;
    private TextView tvOpportunitiesCount;
    private ProgressBar progressBar;

    private int userId;
    private UserProfile userProfile;

    public static ProfileFragment newInstance(int userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ScrollView scrollView = view.findViewById(R.id.scrollView); // give your ScrollView an ID
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));

        if (getArguments() != null) {
            // Preferred key
            userId = getArguments().getInt(ARG_USER_ID, -1);

            // Backward compatibility with MainActivity
            if (userId == -1) {
                userId = getArguments().getInt("volunteerId", -1);
            }
        }


        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (userId == -1) {
            userId = prefs.getInt("user_id", -1);
        }

        if (userId == -1) {
            Log.e(TAG, "No valid user ID found");
            Toast.makeText(getContext(), "Unable to load profile", Toast.LENGTH_SHORT).show();
            return view;
        }



        initViews(view);
        loadProfile();

        return view;
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUserType = view.findViewById(R.id.tvUserType);
        volunteerSection = view.findViewById(R.id.volunteerSection);
        organizationSection = view.findViewById(R.id.organizationSection);
        progressBar = view.findViewById(R.id.progressBar);

        // Volunteer fields
        tvBio = view.findViewById(R.id.tvBio);
        tvSkills = view.findViewById(R.id.tvSkills);
        tvAvailability = view.findViewById(R.id.tvAvailability);
        tvPhoneVolunteer = view.findViewById(R.id.tvPhoneVolunteer);
        tvRegistrationCount = view.findViewById(R.id.tvRegistrationCount);

        // Organization fields
        tvOrgName = view.findViewById(R.id.tvOrgName);
        tvWebsite = view.findViewById(R.id.tvWebsite);
        tvPhoneOrg = view.findViewById(R.id.tvPhoneOrg);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvOpportunitiesCount = view.findViewById(R.id.tvOpportunitiesCount);
    }


    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading profile for user ID: " + userId);

        ApiHelper.fetchUserProfile(getContext(), userId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Profile response: " + response);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONObject data = jsonResponse.getJSONObject("data");

                            userProfile = new UserProfile();
                            userProfile.setId(data.getInt("id"));
                            userProfile.setFullname(data.getString("fullname"));
                            userProfile.setEmail(data.getString("email"));
                            userProfile.setUserType(data.getString("user_type"));
                            userProfile.setCreatedAt(data.optString("created_at", ""));
                            userProfile.setLogo(data.optString("logo", ""));

                            if (data.getString("user_type").equals("volunteer")) {
                                userProfile.setBio(data.optString("bio", ""));
                                userProfile.setSkills(data.optString("skills", ""));
                                userProfile.setAvailability(data.optString("availability", ""));
                                userProfile.setPhoneNumber(data.optString("phone_number", ""));
                                userProfile.setRegistrationCount(data.optInt("registration_count", 0));
                            } else {
                                userProfile.setOrganizationName(data.optString("organization_name", ""));
                                userProfile.setWebsite(data.optString("website", ""));
                                userProfile.setPhoneNumber(data.optString("phone_number", ""));
                                userProfile.setAddress(data.optString("address", ""));
                                userProfile.setOpportunitiesCount(data.optInt("opportunities_count", 0));
                            }

                            displayProfile();
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing profile", e);
                        Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading profile: " + error);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void displayProfile() {
        // Display name
        tvName.setText(userProfile.getDisplayName());
        tvEmail.setText(userProfile.getEmail());

        // Load profile image
        String logoPath = "";
        if (userProfile.getUserType().equals("volunteer")) {
            tvUserType.setText("Volunteer");
            logoPath = "volunteers/";
            volunteerSection.setVisibility(View.VISIBLE);
            organizationSection.setVisibility(View.GONE);
            displayVolunteerInfo();
        } else {
            tvUserType.setText("Organization");
            logoPath = "logos/";
            volunteerSection.setVisibility(View.GONE);
            organizationSection.setVisibility(View.VISIBLE);
            displayOrganizationInfo();
        }

        if (userProfile.getLogo() != null && !userProfile.getLogo().isEmpty()) {
            String imageUrl = "http://192.168.0.103/volunteer-connect/uploads/" + logoPath + userProfile.getLogo();
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(userProfile.getUserType().equals("volunteer") ? R.drawable.ic_volunteer : R.drawable.ic_organization)
                    .error(userProfile.getUserType().equals("volunteer") ? R.drawable.ic_volunteer : R.drawable.ic_organization)
                    .circleCrop()
                    .into(ivProfileImage);
        } else {
            ivProfileImage.setImageResource(userProfile.getUserType().equals("volunteer") ? R.drawable.ic_volunteer : R.drawable.ic_organization);
        }
    }

    private void displayVolunteerInfo() {
        if (userProfile.getBio() != null && !userProfile.getBio().isEmpty()) {
            tvBio.setText(userProfile.getBio());
        } else {
            tvBio.setText("No bio available");
        }

        if (userProfile.getSkills() != null && !userProfile.getSkills().isEmpty()) {
            tvSkills.setText(userProfile.getSkills());
        } else {
            tvSkills.setText("No skills listed");
        }

        if (userProfile.getAvailability() != null && !userProfile.getAvailability().isEmpty()) {
            tvAvailability.setText(userProfile.getAvailability());
        } else {
            tvAvailability.setText("Not specified");
        }

        if (userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().isEmpty()) {
            tvPhoneVolunteer.setText(userProfile.getPhoneNumber());
        } else {
            tvPhoneVolunteer.setText("Not provided");
        }

        tvRegistrationCount.setText(String.valueOf(userProfile.getRegistrationCount()) + " registrations");
    }

    private void displayOrganizationInfo() {
        if (userProfile.getOrganizationName() != null && !userProfile.getOrganizationName().isEmpty()) {
            tvOrgName.setText(userProfile.getOrganizationName());
        } else {
            tvOrgName.setText("Not specified");
        }

        if (userProfile.getWebsite() != null && !userProfile.getWebsite().isEmpty()) {
            tvWebsite.setText(userProfile.getWebsite());
        } else {
            tvWebsite.setText("No website");
        }

        if (userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().isEmpty()) {
            tvPhoneOrg.setText(userProfile.getPhoneNumber());
        } else {
            tvPhoneOrg.setText("Not provided");
        }

        if (userProfile.getAddress() != null && !userProfile.getAddress().isEmpty()) {
            tvAddress.setText(userProfile.getAddress());
        } else {
            tvAddress.setText("Not specified");
        }

        tvOpportunitiesCount.setText(String.valueOf(userProfile.getOpportunitiesCount()) + " opportunities posted");
    }
}