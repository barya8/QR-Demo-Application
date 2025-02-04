package com.example.qr_demo_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton btnSend1, btnSend2, btnSend3, btnSend4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize buttons
        btnSend1 = findViewById(R.id.en_BTN_send1);
        btnSend2 = findViewById(R.id.en_BTN_send2);
        btnSend3 = findViewById(R.id.en_BTN_send3);
        btnSend4 = findViewById(R.id.en_BTN_send4);

        // Set click listeners
        btnSend1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToMainActivity(Constants.Client1ApiKey, "client");
            }
        });

        btnSend2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToMainActivity(Constants.Client2ApiKey, "client");
            }
        });

        btnSend3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToMainActivity(Constants.Client2ApiKey, "admin");
            }
        });

        btnSend4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToMainActivity(Constants.AdminApiKey, "admin");
            }
        });
    }

    private void moveToMainActivity(String key, String mode) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("apiKey", key);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }
}
