package com.example.qr_demo_application;

import static com.example.qr_demo_application.Constants.BACKEND_URL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.qrretrofit.api.GenericController;
import com.example.qrretrofit.interfaces.GenericCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.sql.Date;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_and_edit_qr_activity);
        findViews();
        apiKey = getIntent().getStringExtra("apiKey");
        mode = getIntent().getStringExtra("mode");

        genericController = new GenericController(BACKEND_URL, new GenericCallback() {

            @Override
            public void success(String response) {
                Log.d("API Success", response);
                Intent intent = new Intent(AddAndEditQRActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void error(String error) {
                Log.d("API Fail", error);
                runOnUiThread(() -> {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error: " + error, Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
            }
        });



        //check that the client fill all the fields in the form
        en_BTN_send.setOnClickListener(v -> {
            en_CB_valid.setChecked(true);
            boolean allFieldsValid = true;
            if (!checkIfEmpty(en_ET_qr_code_url)) {
                allFieldsValid = false;
            }
            if (!checkIfEmpty(en_ET_type)) {
                allFieldsValid = false;
            }

            if (allFieldsValid) {
                if (mode.equals("edit")){
                    en_ET_id.setVisibility(View.VISIBLE);
                    en_LBL_id.setVisibility(View.VISIBLE);
                    if (!checkIfEmpty(en_ET_id))
                        Snackbar.make(v, "Please fill in all the required fields", Snackbar.LENGTH_LONG).show();
                    editQRCode();
                }
                else {
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

    //set error when there was send with empty fields
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
        String size= en_ET_size.getText().toString();
        String correction = en_ET_correction.getText().toString();
        Date start_date = parseDate(en_ET_start_date.getText().toString());
        Date end_date= parseDate(en_ET_start_date.getText().toString());;

        Map<String, String> params = new HashMap<>();
        params.put("text", en_ET_qr_code_url.getText().toString());
        params.put("type", en_ET_type.getText().toString());
        params.put("valid", String.valueOf(en_CB_valid.isChecked())); // Boolean converted to String
        if (!(size.isEmpty())) {
            params.put("size", size); // Integer converted to String
        }
        if (!(correction.isEmpty())) {
            params.put("correction", correction);
        }
        if (start_date != null) {
            params.put("start_date", start_date.toString()); // Include start date if valid
        }
        if (end_date != null) {
            params.put("end_date", end_date.toString()); // Include end date if valid
        }
        genericController.generateQRCodeImpl(apiKey,params);
        Log.d("params",params.toString());
    }

    public void editQRCode() {
        String size= en_ET_size.getText().toString();
        String correction = en_ET_correction.getText().toString();
        Boolean valid= en_CB_valid.isChecked();
        Date start_date = parseDate(en_ET_start_date.getText().toString());
        Date end_date= parseDate(en_ET_end_date.getText().toString());;

        Map<String, String> params = new HashMap<>();
        params.put("text", en_ET_qr_code_url.getText().toString());
        params.put("type", en_ET_type.getText().toString());
        params.put("id", en_ET_id.getText().toString()); // Include ID if available
        if (!(size.equals(""))) {
            params.put("size", size); // Integer converted to String
        }
        if (!(correction.equals(""))) {
            params.put("correction", correction);
        }
        if (valid != null)
            params.put("valid", String.valueOf(valid)); // Boolean converted to String
        if (start_date != null) {
            params.put("start_date", String.valueOf(start_date)); // Include start date if valid
        }
        if (end_date != null) {
            params.put("end_date", String.valueOf(end_date)); // Include end date if valid
        }
        Log.d("params",params.toString());
        genericController.updateQrByIdImpl(apiKey,params);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null; // Handle empty or null date string
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new java.sql.Date(sdf.parse(dateStr).getTime()); // Fix: Proper conversion
        } catch (ParseException e) {
            Log.e("ParseError", "Invalid date format: " + dateStr);
            return null;
        }
    }
}
