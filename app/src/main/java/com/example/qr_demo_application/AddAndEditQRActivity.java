package com.example.qr_demo_application;

import static com.example.qr_demo_application.Constants.BACKEND_URL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.qrretrofit.api.GenericController;
import com.example.qrretrofit.interfaces.GenericCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class AddAndEditQRActivity extends AppCompatActivity {
    private AppCompatEditText en_ET_qr_code_url, en_ET_size, en_ET_correction;
    private AppCompatEditText en_ET_id, en_ET_start_date, en_ET_end_date, en_ET_type;
    private AppCompatCheckBox en_CB_valid;
    private MaterialTextView en_LBL_id;
    private MaterialButton en_BTN_send;
    private String mode, apiKey;
    private GenericController genericController;
    private GenericCallback genericCallback;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_and_edit_qr_activity);
        findViews();
        apiKey = getIntent().getStringExtra("apiKey");
        mode = getIntent().getStringExtra("mode");

        genericCallback = new GenericCallback() {
            @Override
            public void success(String response) {
                Log.d("API Success", response);
                Toast.makeText(AddAndEditQRActivity.this, "The action finished successfully", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    Toast.makeText(AddAndEditQRActivity.this, "QR Code added successfully", Toast.LENGTH_SHORT).show();
                });
                finish(); // Optional, depending on your navigation logic
            }

            @Override
            public void error(String error) {
                Log.d("API Fail", error);
                runOnUiThread(() -> {
                    Toast.makeText(AddAndEditQRActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
                finish(); // Optional, depending on your navigation logic
            }
        };

        genericController = new GenericController(BACKEND_URL, genericCallback);

        en_BTN_send.setOnClickListener(v -> {
            boolean allFieldsValid = checkIfEmpty(en_ET_qr_code_url) && checkIfEmpty(en_ET_type);

            if (allFieldsValid) {
                if (mode.equals("edit")) {
                    en_ET_id.setVisibility(View.VISIBLE);
                    en_LBL_id.setVisibility(View.VISIBLE);
                    if (!checkIfEmpty(en_ET_id)) {
                        Snackbar.make(v, "Please fill in all the required fields", Snackbar.LENGTH_LONG).show();
                    } else {
                        editQRCode();
                    }
                } else {
                    en_ET_id.setVisibility(View.GONE);
                    en_LBL_id.setVisibility(View.GONE);
                    addQRCode();
                }
            } else {
                Snackbar.make(v, "Please fill in all the required fields", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void findViews() {
        en_ET_qr_code_url = findViewById(R.id.en_ET_qr_code_url);
        en_ET_size = findViewById(R.id.en_ET_size);
        en_ET_correction = findViewById(R.id.en_ET_correction);
        en_ET_id = findViewById(R.id.en_ET_id);
        en_ET_start_date = findViewById(R.id.en_ET_start_date);
        en_ET_end_date = findViewById(R.id.en_ET_end_date);
        en_ET_type = findViewById(R.id.en_ET_type);
        en_CB_valid = findViewById(R.id.en_CB_valid);
        en_LBL_id = findViewById(R.id.en_LBL_id);
        en_BTN_send = findViewById(R.id.en_BTN_send);
    }

    private boolean checkIfEmpty(AppCompatEditText en_ET_someText) {
        String query = en_ET_someText.getText().toString();
        if (query.isEmpty()) {
            en_ET_someText.setError("This field cannot be blank");
            return false;
        } else {
            en_ET_someText.setError(null);
            return true;
        }
    }

    private void addQRCode() {
        String size = en_ET_size.getText().toString();
        String correction = en_ET_correction.getText().toString();
        String start_date = parseDate(en_ET_start_date.getText().toString());
        String end_date = parseDate(en_ET_end_date.getText().toString());

        Map<String, String> params = new HashMap<>();
        params.put("url", en_ET_qr_code_url.getText().toString());
        params.put("type", en_ET_type.getText().toString());
        params.put("isScanned", String.valueOf(en_CB_valid.isChecked()));
        if (!size.isEmpty()) params.put("size", size);
        if (!correction.isEmpty()) params.put("correction", correction);
        if (start_date != null) params.put("start_date", start_date);
        if (end_date != null) params.put("end_date", end_date);

        genericController.generateQRCodeImpl(apiKey, params, genericCallback);
    }

    public void editQRCode() {
        String size = en_ET_size.getText().toString();
        String correction = en_ET_correction.getText().toString();
        String start_date = parseDate(en_ET_start_date.getText().toString());
        String end_date = parseDate(en_ET_end_date.getText().toString());

        Map<String, String> params = new HashMap<>();
        params.put("url", en_ET_qr_code_url.getText().toString());
        params.put("type", en_ET_type.getText().toString());
        params.put("id", en_ET_id.getText().toString());
        if (!size.isEmpty()) params.put("size", size);
        if (!correction.isEmpty()) params.put("correction", correction);
        params.put("isScanned", String.valueOf(en_CB_valid.isChecked()));
        if (start_date != null) params.put("start_date", start_date);
        if (end_date != null) params.put("end_date", end_date);

        genericController.updateQrByIdImpl(apiKey, params, genericCallback);
    }

    private String parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            return sdf.format(sdf.parse(dateStr));
        } catch (ParseException e) {
            Log.e("ParseError", "Invalid date format: " + dateStr);
            return null;
        }
    }
}
