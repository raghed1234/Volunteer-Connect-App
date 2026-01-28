package com.example.volunteerconnectapp;
import android.app.Activity;
import android.widget.EditText;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> loginUser(emailEditText, passwordEditText));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }


    private void loginUser(EditText emailEditText, EditText passwordEditText) {
        String url = "http://10.102.238.29/volunteer-connect/backend/api/login.php";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            JSONObject user = json.getJSONObject("user"); // <-- get the user object
                            int userId = user.getInt("id");
                            String userType = user.getString("user_type");
                            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", userId)
                                    .putString("user_type", userType)
                                    .apply();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.putExtra("volunteerId", userId);
                            startActivity(intent);
                            finish(); // prevents going back to login
                        }



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", emailEditText.getText().toString());
                params.put("password", passwordEditText.getText().toString());
                return params;
            }
        };

        queue.add(request);
    }
}
