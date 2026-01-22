package com.example.volunteerconnectapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.volunteerconnectapp.fragments.HomeFragment;
import com.example.volunteerconnectapp.fragments.NotificationsFragment;
import com.example.volunteerconnectapp.fragments.ProfileFragment;
import com.example.volunteerconnectapp.fragments.RegistrationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    private int volunteerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get the volunteerId from SharedPreferences (saved during login)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        volunteerId = prefs.getInt("user_id", -1);

        // Safety check - if no user_id found, return to login
        if (volunteerId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Load default fragment (HomeFragment) on first launch
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Handle navigation item clicks
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_registrations) {
                    selectedFragment = new RegistrationsFragment();
                }
                else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificationsFragment();
                }
                else if (itemId == R.id.nav_profile) {
                    ProfileFragment profileFragment = new ProfileFragment();

                    // Pass the logged-in userId from HomeActivity
                    Bundle args = new Bundle();
                    args.putInt("volunteerId", volunteerId); // matches key from MainActivity login
                    profileFragment.setArguments(args);

                    selectedFragment = profileFragment;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }

                return false;
            }
        });

        // Set default selected item
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment) {
        try {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            Log.d("HomeActivity", "Fragment loaded: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e("HomeActivity", "Error loading fragment: " + fragment.getClass().getSimpleName(), e);
        }
    }
}