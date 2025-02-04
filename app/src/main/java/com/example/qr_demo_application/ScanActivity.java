package com.example.qr_demo_application;

import static com.example.qr_demo_application.Constants.BACKEND_URL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.qrretrofit.api.GenericController;
import com.example.qrretrofit.interfaces.GenericCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ScanActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Uri imageUri; // Store the image URI
    private GenericController genericController;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiKey = getIntent().getStringExtra("apiKey");

        genericController = new GenericController(BACKEND_URL, new GenericCallback() {
            @Override
            public void success(String response) {
                Log.d("SDK CALL", response);
// Handle the response from the server
                Toast.makeText(ScanActivity.this, "Upload Successful: " + response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error(String error) {
                Log.d("SDK CALL", error);
                Toast.makeText(ScanActivity.this, "Upload Failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Register the camera intent launcher
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Toast.makeText(this, "Image Captured!", Toast.LENGTH_SHORT).show();
                // Wait for the toast to finish and then call the next function
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Call another function after the toast finishes
                        //proceedWithNextAction();
                    }
                }, Toast.LENGTH_SHORT); // Delay time equals toast duration
                sendImageToApi();

            } else {
                Toast.makeText(this, "Camera cancelled.", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        // Register the gallery intent launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                if (imageUri != null) {
                    sendImageToApi();
                }
            } else {
                Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        // Register the permission request launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        // Request permissions and choose action (camera or gallery)
        if (hasPermissions()) {
            showImageSourceDialog();
        } else {
            requestPermissions();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    private void showImageSourceDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

//        // Check and request camera permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            launchCamera();
//        } else {
//            // Request permissions
//            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
//            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
//            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Create an image file
                File photoFile = createImageFile();
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.qr_demo_application.fileprovider", photoFile);

                // Set the URI for the image capture
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(takePictureIntent);
            } catch (IOException e) {
                Log.e("ScanActivity", "Error creating image file", e);
            }
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void sendImageToApi() {
        if (imageUri != null) {
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = getBytesFromInputStream(imageStream);

                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody);

                // First, scan barcode
                genericController.scanBarcodeImpl(apiKey, filePart, new GenericCallback() {
                    @Override
                    public void success(String barcodeResponse) {
                        // If barcode scan is successful, proceed with QR code scan
                        genericController.scanQRCodeImpl(apiKey, filePart, new GenericCallback() {
                            @Override
                            public void success(String qrResponse) {
                                // Get client data to compare URLs
                                genericController.getDataByClientImpl(apiKey, new GenericCallback() {
                                    @Override
                                    public void success(String clientDataResponse) {
                                        try {
                                            // Parse responses
                                            JSONObject qrJson = new JSONObject(qrResponse);
                                            JSONObject clientDataJson = new JSONObject(clientDataResponse);
                                            JSONObject dataObject = qrJson.optJSONObject("data");

                                            // Extract full URL from QR scan
                                            String scannedUrl = dataObject.optString("fullUrl", "");

                                            // Check if scanned URL matches any URL in client data
                                            boolean isUrlValid = isUrlMatchInClientData(clientDataJson, scannedUrl);

                                            if (isUrlValid) {
                                                // Prepare update parameters
                                                Map<String, String> updateParams = new HashMap<>();
                                                updateParams.put("valid", String.valueOf(!isUrlValid));

                                                // Update QR code status
                                                genericController.updateQrByIdImpl(apiKey, updateParams);

                                                // Log results
                                                Log.d("ScanActivity", "URL Valid: " + !isUrlValid);
                                                Toast.makeText(ScanActivity.this,
                                                        "Scan processed. URL " + (isUrlValid ? "Matched!" : "Not Active Ticket"),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(ScanActivity.this,
                                                        "Not Active Ticket",
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                        } catch (JSONException e) {
                                            Log.e("ScanActivity", "Error parsing JSON", e);
                                            Toast.makeText(ScanActivity.this,
                                                    "Internal Server Error",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void error(String error) {
                                        Log.e("ScanActivity", "Error getting client data: " + error);
                                        Toast.makeText(ScanActivity.this,
                                                "Internal Server Error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void error(String error) {
                                Log.e("ScanActivity", "QR Scan error: " + error);
                                Toast.makeText(ScanActivity.this,
                                        "Cannot Read QR Parameters",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void error(String error) {
                        Log.e("ScanActivity", "Barcode Scan error: " + error);
                        Toast.makeText(ScanActivity.this,
                                "Invalid QR Code",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                Log.e("ScanActivity", "Error reading image", e);
                Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to check if scanned URL matches any URL in client data
    private boolean isUrlMatchInClientData(JSONObject clientDataJson, String scannedUrl) {
        try {
            JSONObject dataObject = clientDataJson.optJSONObject("data");
            if (dataObject != null) {
                // Iterate through the keys (like "0", "1", etc.)
                Iterator<String> keys = dataObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject item = dataObject.getJSONObject(key);

                    String itemUrl = item.optString("url", "");
                    if (itemUrl.equals(scannedUrl)) {
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("ScanActivity", "Error parsing client data", e);
        }
        return false;
    }

    // Helper method to convert InputStream to byte array
    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
}

