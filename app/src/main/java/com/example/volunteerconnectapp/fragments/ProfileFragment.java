package com.example.volunteerconnectapp.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.ApiHelper;
import com.example.volunteerconnectapp.models.UserProfile;
import com.example.volunteerconnectapp.models.VolleyMultipartRequest;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "user_id";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_PROFILE_IMAGE_REQUEST = 2;

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
    private Button btnAddOpportunity;

    // Edit-related views
    private CardView cardBio, cardDetails, cardOrgDetails;

    private int userId;
    private int loggedInUserId;
    private String loggedInUserType;
    private UserProfile userProfile;
    private boolean isOwnProfile = false;

    // Dialog components for Add Opportunity
    private Dialog addOpportunityDialog;
    private ImageView ivOpportunityImage;
    private TextInputEditText etTitle, etDescription, etLocation, etCapacity, etStartDate, etEndDate;
    private Button btnSelectImage, btnSubmit, btnCancel;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private Calendar startCalendar, endCalendar;

    // Profile image
    private Bitmap selectedProfileImageBitmap;

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
        ScrollView scrollView = view.findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));

        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getInt("user_id", -1);
        loggedInUserType = prefs.getString("user_type", "");

        Log.d(TAG, "Logged in user ID: " + loggedInUserId + ", Type: " + loggedInUserType);

        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_USER_ID, -1);
            if (userId == -1) {
                userId = getArguments().getInt("volunteerId", -1);
            }
        }

        if (userId == -1) {
            userId = loggedInUserId;
        }

        isOwnProfile = (userId == loggedInUserId);
        Log.d(TAG, "Viewing profile of user ID: " + userId + ", isOwnProfile: " + isOwnProfile);

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
        btnAddOpportunity = view.findViewById(R.id.btnAddOpportunity);

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

        // Cards for editing
        cardBio = view.findViewById(R.id.cardBio);
        cardDetails = view.findViewById(R.id.cardDetails);
        cardOrgDetails = view.findViewById(R.id.cardOrgDetails);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
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
        tvName.setText(userProfile.getDisplayName());
        tvEmail.setText(userProfile.getEmail());

        if (userProfile.getUserType().equals("volunteer")) {
            tvUserType.setText("Volunteer");
            volunteerSection.setVisibility(View.VISIBLE);
            organizationSection.setVisibility(View.GONE);
            btnAddOpportunity.setVisibility(View.GONE);
            displayVolunteerInfo();

            // Enable editing for own profile
            if (isOwnProfile) {
                setupVolunteerEditing();
            }
        } else {
            tvUserType.setText("Organization");
            volunteerSection.setVisibility(View.GONE);
            organizationSection.setVisibility(View.VISIBLE);

            if (isOwnProfile && loggedInUserType.equals("organization")) {
                btnAddOpportunity.setVisibility(View.VISIBLE);
                btnAddOpportunity.setOnClickListener(v -> showAddOpportunityDialog());
                setupOrganizationEditing();
            } else {
                btnAddOpportunity.setVisibility(View.GONE);
            }

            displayOrganizationInfo();
        }

        // Profile image click to change (only for own profile)
        if (isOwnProfile) {
            ivProfileImage.setOnClickListener(v -> openProfileImagePicker());
        }

        // Load profile image
        if (userProfile.getLogo() != null && !userProfile.getLogo().isEmpty()) {
            String imageUrl = ApiHelper.getProfileImageUrl(userProfile.getUserType(), userProfile.getLogo());
            Log.d(TAG, "Loading image from: " + imageUrl);

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

    private void setupVolunteerEditing() {
        // Make cards clickable to edit
        if (cardBio != null) {
            cardBio.setOnClickListener(v -> showEditVolunteerDialog());
        }
        if (cardDetails != null) {
            cardDetails.setOnClickListener(v -> showEditVolunteerDialog());
        }
    }

    private void setupOrganizationEditing() {
        // Make card clickable to edit
        if (cardOrgDetails != null) {
            cardOrgDetails.setOnClickListener(v -> showEditOrganizationDialog());
        }
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PROFILE_IMAGE_REQUEST);
    }

    private void showEditVolunteerDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_volunteer);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText etFullName = dialog.findViewById(R.id.etFullName);
        TextInputEditText etBio = dialog.findViewById(R.id.etBio);
        TextInputEditText etSkills = dialog.findViewById(R.id.etSkills);
        TextInputEditText etAvailability = dialog.findViewById(R.id.etAvailability);
        TextInputEditText etPhone = dialog.findViewById(R.id.etPhone);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Pre-fill with current data
        etFullName.setText(userProfile.getFullname());
        etBio.setText(userProfile.getBio());
        etSkills.setText(userProfile.getSkills());
        etAvailability.setText(userProfile.getAvailability());
        etPhone.setText(userProfile.getPhoneNumber());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String fullname = etFullName.getText().toString().trim();
            String bio = etBio.getText().toString().trim();
            String skills = etSkills.getText().toString().trim();
            String availability = etAvailability.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (fullname.isEmpty()) {
                etFullName.setError("Name is required");
                return;
            }

            updateProfile(fullname, bio, skills, availability, phone, null, null, null, null);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditOrganizationDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_organization);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText etFullName = dialog.findViewById(R.id.etFullName);
        TextInputEditText etOrgName = dialog.findViewById(R.id.etOrgName);
        TextInputEditText etWebsite = dialog.findViewById(R.id.etWebsite);
        TextInputEditText etPhone = dialog.findViewById(R.id.etPhone);
        TextInputEditText etAddress = dialog.findViewById(R.id.etAddress);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Pre-fill with current data
        etFullName.setText(userProfile.getFullname());
        etOrgName.setText(userProfile.getOrganizationName());
        etWebsite.setText(userProfile.getWebsite());
        etPhone.setText(userProfile.getPhoneNumber());
        etAddress.setText(userProfile.getAddress());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String fullname = etFullName.getText().toString().trim();
            String orgName = etOrgName.getText().toString().trim();
            String website = etWebsite.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (fullname.isEmpty()) {
                etFullName.setError("Name is required");
                return;
            }

            updateProfile(fullname, null, null, null, phone, orgName, website, address, null);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateProfile(String fullname, String bio, String skills, String availability,
                               String phone, String orgName, String website, String address, String unusedParam) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String url = ApiHelper.getUpdateProfileUrl();
                java.net.URL obj = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) obj.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                StringBuilder postData = new StringBuilder();
                postData.append("user_id=").append(userId);
                postData.append("&user_type=").append(userProfile.getUserType());
                postData.append("&fullname=").append(java.net.URLEncoder.encode(fullname, "UTF-8"));

                if (bio != null) postData.append("&bio=").append(java.net.URLEncoder.encode(bio, "UTF-8"));
                if (skills != null) postData.append("&skills=").append(java.net.URLEncoder.encode(skills, "UTF-8"));
                if (availability != null) postData.append("&availability=").append(java.net.URLEncoder.encode(availability, "UTF-8"));
                if (phone != null) postData.append("&phone_number=").append(java.net.URLEncoder.encode(phone, "UTF-8"));
                if (orgName != null) postData.append("&organization_name=").append(java.net.URLEncoder.encode(orgName, "UTF-8"));
                if (website != null) postData.append("&website=").append(java.net.URLEncoder.encode(website, "UTF-8"));
                if (address != null) postData.append("&address=").append(java.net.URLEncoder.encode(address, "UTF-8"));

                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                conn.getOutputStream().write(postDataBytes);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    getActivity().runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                loadProfile(); // Reload to show updated data
                            } else {
                                Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response", e);
                        }
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating profile", e);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void uploadProfileImage(Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);

        String url = ApiHelper.getUpdateProfileImageUrl();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    try {
                        String responseString = new String(response.data);
                        Log.d(TAG, "Image upload response: " + responseString);

                        JSONObject jsonResponse = new JSONObject(responseString);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(getContext(), "Profile image updated!", Toast.LENGTH_SHORT).show();
                            loadProfile(); // Reload to show new image
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                },
                error -> {
                    Log.e(TAG, "Upload error: " + error.toString());
                    Toast.makeText(getContext(), "Upload failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("user_type", userProfile.getUserType());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                if (bitmap != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    params.put("image", new DataPart("profile_" + System.currentTimeMillis() + ".jpg", imageBytes, "image/jpeg"));
                }

                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(multipartRequest);
    }

    private void displayVolunteerInfo() {
        tvBio.setText(userProfile.getBio() != null && !userProfile.getBio().isEmpty() ? userProfile.getBio() : "No bio available");
        tvSkills.setText(userProfile.getSkills() != null && !userProfile.getSkills().isEmpty() ? userProfile.getSkills() : "No skills listed");
        tvAvailability.setText(userProfile.getAvailability() != null && !userProfile.getAvailability().isEmpty() ? userProfile.getAvailability() : "Not specified");
        tvPhoneVolunteer.setText(userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().isEmpty() ? userProfile.getPhoneNumber() : "Not provided");
        tvRegistrationCount.setText(String.valueOf(userProfile.getRegistrationCount()) + " registrations");
    }

    private void displayOrganizationInfo() {
        tvOrgName.setText(userProfile.getOrganizationName() != null && !userProfile.getOrganizationName().isEmpty() ? userProfile.getOrganizationName() : "Not specified");
        tvWebsite.setText(userProfile.getWebsite() != null && !userProfile.getWebsite().isEmpty() ? userProfile.getWebsite() : "No website");
        tvPhoneOrg.setText(userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().isEmpty() ? userProfile.getPhoneNumber() : "Not provided");
        tvAddress.setText(userProfile.getAddress() != null && !userProfile.getAddress().isEmpty() ? userProfile.getAddress() : "Not specified");
        tvOpportunitiesCount.setText(String.valueOf(userProfile.getOpportunitiesCount()) + " opportunities posted");
    }

    // Add Opportunity methods (existing code)
    private void showAddOpportunityDialog() {
        addOpportunityDialog = new Dialog(getContext());
        addOpportunityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addOpportunityDialog.setContentView(R.layout.dialog_add_opportunity);
        addOpportunityDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        initDialogViews();
        setupDialogListeners();
        addOpportunityDialog.show();
    }

    private void initDialogViews() {
        ivOpportunityImage = addOpportunityDialog.findViewById(R.id.ivOpportunityImage);
        etTitle = addOpportunityDialog.findViewById(R.id.etTitle);
        etDescription = addOpportunityDialog.findViewById(R.id.etDescription);
        etLocation = addOpportunityDialog.findViewById(R.id.etLocation);
        etCapacity = addOpportunityDialog.findViewById(R.id.etCapacity);
        etStartDate = addOpportunityDialog.findViewById(R.id.etStartDate);
        etEndDate = addOpportunityDialog.findViewById(R.id.etEndDate);
        btnSelectImage = addOpportunityDialog.findViewById(R.id.btnSelectImage);
        btnSubmit = addOpportunityDialog.findViewById(R.id.btnSubmit);
        btnCancel = addOpportunityDialog.findViewById(R.id.btnCancel);
    }

    private void setupDialogListeners() {
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        btnCancel.setOnClickListener(v -> {
            resetDialogFields();
            addOpportunityDialog.dismiss();
        });
        btnSubmit.setOnClickListener(v -> validateAndSubmitOpportunity());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedDate = sdf.format(calendar.getTime());

                    if (isStartDate) {
                        etStartDate.setText(formattedDate);
                    } else {
                        etEndDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                    ivOpportunityImage.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image", e);
                    Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == PICK_PROFILE_IMAGE_REQUEST) {
                Uri profileImageUri = data.getData();
                try {
                    selectedProfileImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), profileImageUri);
                    uploadProfileImage(selectedProfileImageBitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading profile image", e);
                    Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void validateAndSubmitOpportunity() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return;
        }

        if (capacityStr.isEmpty()) {
            etCapacity.setError("Capacity is required");
            etCapacity.requestFocus();
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                etCapacity.setError("Capacity must be greater than 0");
                etCapacity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCapacity.setError("Invalid capacity");
            etCapacity.requestFocus();
            return;
        }

        if (startDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select start date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select end date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageBitmap == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadOpportunity(title, description, location, capacity, startDate, endDate);
    }

    private void uploadOpportunity(String title, String description, String location, int capacity, String startDate, String endDate) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Uploading...");

        String uploadUrl = ApiHelper.getUploadOpportunityUrl();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, uploadUrl,
                response -> {
                    try {
                        String responseString = new String(response.data);
                        JSONObject jsonResponse = new JSONObject(responseString);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(getContext(), "Opportunity created successfully!", Toast.LENGTH_LONG).show();
                            resetDialogFields();
                            addOpportunityDialog.dismiss();
                            loadProfile();
                        } else {
                            Toast.makeText(getContext(), "Error: " + jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Create Opportunity");
                },
                error -> {
                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Create Opportunity");
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("org_id", String.valueOf(userId));
                params.put("title", title);
                params.put("description", description);
                params.put("location", location);
                params.put("capacity", String.valueOf(capacity));
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (selectedImageBitmap != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    params.put("image", new DataPart("opportunity_" + System.currentTimeMillis() + ".jpg", imageBytes, "image/jpeg"));
                }
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(multipartRequest);
    }

    private void resetDialogFields() {
        if (etTitle != null) etTitle.setText("");
        if (etDescription != null) etDescription.setText("");
        if (etLocation != null) etLocation.setText("");
        if (etCapacity != null) etCapacity.setText("");
        if (etStartDate != null) etStartDate.setText("");
        if (etEndDate != null) etEndDate.setText("");
        if (ivOpportunityImage != null) ivOpportunityImage.setImageResource(R.drawable.ic_organization);
        selectedImageUri = null;
        selectedImageBitmap = null;
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
    }
}