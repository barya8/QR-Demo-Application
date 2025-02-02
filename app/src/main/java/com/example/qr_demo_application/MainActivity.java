package com.example.qr_demo_application;

import static com.example.qr_demo_application.Constants.BACKEND_URL;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qr_demo_application.model.QRCode;
import com.example.qrretrofit.api.GenericController;
import com.example.qrretrofit.interfaces.GenericCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Adapter.OnActionListener {
    private RecyclerView recyclerView;
    private FloatingActionButton add, logout, scan, deleteAll;
    private Adapter adapter;
    private List<QRCode> qrCodeList;
    private String apiKey;
    private GenericController genericController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the API key and mode from the Intent
        Intent intent = getIntent();
        apiKey = intent.getStringExtra("apiKey");
        String mode = intent.getStringExtra("mode");
        qrCodeList = new ArrayList<>();

        // Initialize UI elements
        recyclerView = findViewById(R.id.main_LST_qrs);
        add = findViewById(R.id.add);
        logout = findViewById(R.id.logout);
        scan = findViewById(R.id.scan);
        deleteAll = findViewById(R.id.deleteAll);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // You can set your adapter here (for QR codes list)
        // recyclerView.setAdapter(yourAdapter);
        adapter = new Adapter(MainActivity.this, qrCodeList, apiKey);
        adapter.setOnActionListener(this);  // Set the listener here
        recyclerView.setAdapter(adapter);

        // Initialize the generic controller
        //genericController = new GenericController("https://api.restful-api.dev/", new GenericCallback() {
        genericController = new GenericController(BACKEND_URL, new GenericCallback() {
            @Override
            public void success(String response) {
                // Handle success response here
                Log.d("API Success", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject serviceResult = jsonResponse.getJSONObject("serviceResult");
                    String returnCode = serviceResult.getString("returnCode");

                    if (!"0".equals(returnCode)) {
                        String errorMessage = serviceResult.getString("returnMessage");
                        handleError(errorMessage);
                    }
                    JSONObject data = jsonResponse.getJSONObject("data");
                    if ("client".equals(mode)) {
                        parseClientData(data);
                    } else if ("admin".equals(mode)) {
                        parseAdminData(data);
                    } else {
                        // Handle unexpected mode
                        Log.e("Mode Error", "Unexpected mode: " + mode);
                    }
                    // After the data is populated, set the adapter
                    runOnUiThread(() -> {
                        adapter.updateData(qrCodeList);
                        // Update the RecyclerView adapter with new data
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(String error) {
                // Handle error response here
                try {
                    JSONObject jsonResponse = new JSONObject(error);
                    String errorMessage;
                    if (jsonResponse.has("returnMessage"))
                        errorMessage = jsonResponse.getString("returnMessage");
                    else {
                        JSONObject serviceResult = jsonResponse.getJSONObject("serviceResult");
                        errorMessage = serviceResult.getString("returnMessage");
                    }
                    Log.e("API Error", "Error: " + errorMessage);
                    handleError(errorMessage);
                } catch (JSONException e) {

                }
            }
        });

        fetchQRCodeList(apiKey, mode);

        //click on add new qr button- open AddAndEditQRActivity in add mode
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAndEditQRActivity.class);
                intent.putExtra("mode", "add");
                intent.putExtra("apiKey", apiKey);
                startActivity(intent);
            }
        });

        // logout from the current user and go back to LoginActivity
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    intent.putExtra("apiKey", apiKey);
                    startActivity(intent);
                } else {
                    requestCameraPermission(); // Request permission
                }
            }
        })
        ;
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this) // Use MainActivity.this
                        .setTitle("Delete All QR Codes") // More descriptive title
                        .setMessage("Are you sure you want to delete all QR codes?") // More descriptive message
                        .setPositiveButton("Yes", (dialog, which) -> {
                            genericController.deleteAllImpl(apiKey);
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("No", null) // Add a "No" button
                        .show();
            }
        });
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) { // Replace 100 with your request code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the action
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchQRCodeList(String apiKey, String mode) {
        if ("client".equals(mode)) {
            genericController.getDataByClientImpl(apiKey);
        } else if ("admin".equals(mode)) {
            genericController.getAllDataImpl(apiKey);
        } else {
            // Handle unexpected mode
            Log.e("Mode Error", "Unexpected mode: " + mode);
        }
    }

    private void parseClientData(JSONObject data) throws Exception {
        Iterator keys = data.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (data.isNull(key)) {
                // Handle the case where the value is null
                continue; // Skip this iteration
            }
            JSONObject qrData = data.getJSONObject(key);
            QRCode qrCode = new QRCode();
            qrCode.setId(key);
            qrCode.setSize(String.valueOf(qrData.getInt("size")));
            qrCode.setCorrection(qrData.getString("errorCorrection"));
            qrCode.setUrl(qrData.getString("url"));
            qrCode.setBarcodeImage(qrData.getString("base64Image"));
            qrCodeList.add(qrCode);
        }
    }

    private void parseAdminData(JSONObject data) throws Exception {
        JSONObject qrData = data.getJSONObject("QR");
        Iterator<String> keys = qrData.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (qrData.isNull(key)) {
                // Handle the case where the value is null
                continue; // Skip this iteration
            }
            // Get the array associated with the current key (e.g., "key1-client1", "key2-client2")
            JSONArray qrArray = qrData.getJSONArray(key);
            // Iterate through the array and extract the QR code data
            for (int i = 0; i < qrArray.length(); i++) {
                Object item = qrArray.get(i);

                // If the item is not a JSONObject, skip it
                if (!(item instanceof JSONObject)) {
                    Log.e("parseAdminData", "Skipping non-JSONObject item at index " + i);
                    continue;  // Skip this iteration and go to the next item in the array
                }

                // Proceed with processing since the item is a JSONObject
                JSONObject qrObject = (JSONObject) item;
                QRCode qrCode = new QRCode();
                // Set QR code details
                qrCode.setId(String.valueOf(i));
                qrCode.setSize(String.valueOf(qrObject.getInt("size")));
                qrCode.setCorrection(qrObject.getString("errorCorrection"));
                qrCode.setUrl(qrObject.getString("url"));
                qrCode.setBarcodeImage(qrObject.getString("base64Image"));

                // Add the QR code to the list
                qrCodeList.add(qrCode);
            }
        }
    }

    private void handleError(String message) {
        // Show the error message in a Snackbar
        runOnUiThread(() -> {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG);
            snackbar.show();
        });

        // Go back to LoginActivity after a short delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the activity stack
            startActivity(intent);
            finish(); // Close the current activity
        }, 4000); // Wait for 4 seconds to ensure Snackbar is shown before navigating
    }

    @Override
    public void onEdit(QRCode qrCode) {
        Intent intent = new Intent(this, AddAndEditQRActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("apiKey", apiKey);
        intent.putExtra("qrCode", qrCode); // Make sure QRCode implements Serializable
        startActivity(intent);
    }


    @Override
    public void onDelete(QRCode qrCode) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete QR Code")
                .setMessage("Are you sure you want to delete this QR code?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    genericController.deleteQrByIdImpl(apiKey, Integer.parseInt(qrCode.getId()),
                            new com.example.qrretrofit.interfaces.GenericCallback() {
                                @Override
                                public void success(String data) {
                                    runOnUiThread(() -> {
                                        adapter.removeItem(qrCode);
                                        Snackbar.make(recyclerView, "QR code deleted successfully",
                                                Snackbar.LENGTH_LONG).show();
                                    });
                                }

                                @Override
                                public void error(String errorMessage) {
                                    runOnUiThread(() -> {
                                        Snackbar.make(recyclerView, "Error: " + errorMessage,
                                                Snackbar.LENGTH_LONG).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

}