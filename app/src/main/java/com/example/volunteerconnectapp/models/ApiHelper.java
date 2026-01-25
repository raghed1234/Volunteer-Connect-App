package com.example.volunteerconnectapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiHelper {
    private static final String BASE_URL = "http://192.168.0.108/volunteer-connect/backend/api/";
    private static final String TAG = "ApiHelper";

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    public static void fetchOpportunities(Context context, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                String fullUrl = BASE_URL + "get_opportunities.php?user_id=" + userId;
                Log.e(TAG, "==========================================");
                Log.e(TAG, "USER ID: " + userId);
                Log.e(TAG, "FULL URL: " + fullUrl);
                Log.e(TAG, "==========================================");

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Server returned code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching opportunities", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void registerForOpportunity(Context context, int opportunityId, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                String fullUrl = BASE_URL + "register_opportunity.php";
                Log.e(TAG, "Registration URL: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("volunteer_id", userId);
                json.put("opportunity_id", opportunityId);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Registration failed with code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error registering for opportunity", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void fetchRegistrations(Context context, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                URL url = new URL(BASE_URL + "get_registrations.php?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Server returned code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching registrations", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // NEW METHOD: Fetch volunteer's own registrations
    public static void fetchVolunteerRegistrations(Context context, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                String fullUrl = BASE_URL + "get_volunteer_registrations.php?user_id=" + userId;
                Log.d(TAG, "Fetching volunteer registrations from: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Server returned code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching volunteer registrations", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // NEW METHOD: Fetch organization's pending registrations
    public static void fetchOrganizationRegistrations(Context context, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                String fullUrl = BASE_URL + "get_organization_registrations.php?user_id=" + userId;
                Log.d(TAG, "Fetching organization registrations from: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Server returned code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching organization registrations", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // NEW METHOD: Update registration status (approve/reject)
    public static void updateRegistrationStatus(Context context, int registrationId, String status, ApiCallback callback) {
        new Thread(() -> {
            try {
                String fullUrl = BASE_URL + "update_registration_status.php";
                Log.d(TAG, "Updating registration status at: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("registration_id", registrationId);
                json.put("status", status);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Update failed with code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error updating registration status", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Fetch notifications for user
    public static void fetchNotifications(Context context, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                String fullUrl = BASE_URL + "get_notifications.php?user_id=" + userId;
                Log.d(TAG, "Fetching notifications from: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Server returned code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching notifications", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Mark single notification as read
    public static void markNotificationRead(Context context, int notificationId, ApiCallback callback) {
        new Thread(() -> {
            try {
                String fullUrl = BASE_URL + "mark_notification_read.php";
                Log.d(TAG, "Marking notification read: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("notification_id", notificationId);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Failed with code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error marking notification read", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Mark all notifications as read
    public static void markAllNotificationsRead(Context context, ApiCallback callback) {
        new Thread(() -> {
            try {
                int userId = getUserId(context);
                String fullUrl = BASE_URL + "mark_all_notifications_read.php";
                Log.d(TAG, "Marking all notifications read: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("user_id", userId);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Failed with code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error marking all read", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }


    // Fetch user profile
    public static void fetchUserProfile(Context context, int userId, ApiCallback callback) {
        new Thread(() -> {
            try {
                String fullUrl = BASE_URL + "get_user_profile.php?user_id=" + userId;
                Log.d(TAG, "Fetching user profile from: " + fullUrl);

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onSuccess(response.toString());
                } else {
                    callback.onError("Server returned code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching user profile", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ============================================
    // HELPER METHODS FOR PROFILE IMAGES
    // ============================================

    /**
     * Get the full image URL for profile pictures
     */
    public static String getProfileImageUrl(String userType, String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return "";
        }

        String uploadsPath = BASE_URL.replace("/backend/api/", "/uploads/");

        if (userType.equals("volunteer")) {
            return uploadsPath + "volunteers/" + imageName;
        } else {
            return uploadsPath + "logos/" + imageName;
        }
    }

    /**
     * Get the opportunity image URL
     * Works for both manually added images and uploaded images
     */
    public static String getOpportunityImageUrl(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return "";
        }

        String uploadsPath = BASE_URL.replace("/backend/api/", "/uploads/");
        return uploadsPath + "opportunities/" + imageName;
    }

    /**
     * Get the upload opportunity endpoint URL
     */
    public static String getUploadOpportunityUrl() {
        return BASE_URL + "upload_opportunity.php";
    }

    /**
     * Get the update profile endpoint URL
     */
    public static String getUpdateProfileUrl() {
        return BASE_URL + "update_profile.php";
    }

    /**
     * Get the update profile image endpoint URL
     */
    public static String getUpdateProfileImageUrl() {
        return BASE_URL + "update_profile_image.php";
    }

    /**
     * Get base uploads URL (for debugging)
     */
    public static String getUploadsBaseUrl() {
        return BASE_URL.replace("/backend/api/", "/uploads/");
    }

}