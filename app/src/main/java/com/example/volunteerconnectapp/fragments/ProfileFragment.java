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

    private int userId;
    private int loggedInUserId;
    private String loggedInUserType;
    private UserProfile userProfile;


    private Dialog addOpportunityDialog;
    private ImageView ivOpportunityImage;
    private TextInputEditText etTitle, etDescription, etLocation, etCapacity, etStartDate, etEndDate;
    private Button btnSelectImage, btnSubmit, btnCancel;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private Calendar startCalendar, endCalendar;

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

        // Get logged-in user info from SharedPreferences
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

        Log.d(TAG, "Viewing profile of user ID: " + userId);

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

        // Initialize calendars
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
        } else {
            tvUserType.setText("Organization");
            volunteerSection.setVisibility(View.GONE);
            organizationSection.setVisibility(View.VISIBLE);


            boolean isOwnProfile = (userId == loggedInUserId);
            boolean isOrganization = loggedInUserType.equals("organization");

            Log.d(TAG, "Button visibility check - isOwnProfile: " + isOwnProfile +
                    ", isOrganization: " + isOrganization +
                    ", userId: " + userId +
                    ", loggedInUserId: " + loggedInUserId);

            if (isOwnProfile && isOrganization) {
                btnAddOpportunity.setVisibility(View.VISIBLE);
                btnAddOpportunity.setOnClickListener(v -> showAddOpportunityDialog());
                Log.d(TAG, "Add Opportunity button is VISIBLE");
            } else {
                btnAddOpportunity.setVisibility(View.GONE);
                Log.d(TAG, "Add Opportunity button is HIDDEN");
            }

            displayOrganizationInfo();
        }


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

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                ivOpportunityImage.setImageBitmap(selectedImageBitmap);
                Log.d(TAG, "Image selected successfully");
            } catch (IOException e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "Starting upload to: " + uploadUrl);
        Log.d(TAG, "Org ID: " + userId);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, uploadUrl,
                response -> {
                    try {
                        String responseString = new String(response.data);
                        Log.d(TAG, "Upload response: " + responseString);

                        JSONObject jsonResponse = new JSONObject(responseString);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(getContext(), "Opportunity created successfully!", Toast.LENGTH_LONG).show();
                            resetDialogFields();
                            addOpportunityDialog.dismiss();
                            loadProfile(); // Reload profile to update opportunities count
                        } else {
                            Toast.makeText(getContext(), "Error: " + jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Create Opportunity");
                },
                error -> {
                    Log.e(TAG, "Upload error: " + error.toString());

                    String errorMessage = "Upload failed";
                    if (error.networkResponse != null) {
                        errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error response: " + responseBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not parse error response", e);
                        }
                    } else if (error.getMessage() != null) {
                        errorMessage += ": " + error.getMessage();
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
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

                Log.d(TAG, "Upload params: " + params.toString());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                if (selectedImageBitmap != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    Log.d(TAG, "Image size: " + imageBytes.length + " bytes");
                    params.put("image", new DataPart("opportunity_" + System.currentTimeMillis() + ".jpg", imageBytes, "image/jpeg"));
                }

                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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