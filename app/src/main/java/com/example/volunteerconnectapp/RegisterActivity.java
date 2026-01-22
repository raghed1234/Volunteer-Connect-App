package com.example.volunteerconnectapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText fullnameEditText, emailEditText, passwordEditText;
    RadioGroup userTypeGroup;
    Button registerButton;
    TextView loginText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullnameEditText = findViewById(R.id.fullnameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> registerUser());

        loginText = findViewById(R.id.loginText);

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, Activity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String url = "http://192.168.0.107/volunteer-connect/backend/api/register.php"; // Your PC IP

        String fullname = fullnameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        int selectedId = userTypeGroup.getCheckedRadioButtonId();
        RadioButton selectedRadio = findViewById(selectedId);
        String userType = selectedRadio.getText().toString().toLowerCase();

        if(fullname.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if(success){
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }





                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("fullname", fullname);
                params.put("email", email);
                params.put("password", password);
                params.put("user_type", userType);
                return params;
            }
        };

        queue.add(request);
    }
}
