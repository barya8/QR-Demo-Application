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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.qrretrofit.api.GenericController;
import com.example.qrretrofit.interfaces.GenericCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
            askForImageSource();
        } else {
            requestPermissions();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askForImageSource() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Image Source")
                .setMessage("Would you like to take a picture or select one from your gallery?")
                .setPositiveButton("Camera", (dialog, which) -> launchCamera())
                .setNegativeButton("Gallery", (dialog, which) -> showImageSourceDialog())
                .setCancelable(false)
                .show();
    }

    private void requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void showImageSourceDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

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
        if (imageUri == null) {
            Log.e("ScanActivity", "Image URI is null.");
            return;
        }

        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytesFromInputStream(imageStream);
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody);

            // Start the API call sequence
            scanBarcode(filePart);

        } catch (IOException e) {
            Log.e("ScanActivity", "Error reading image", e);
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanBarcode(MultipartBody.Part filePart) {
        genericController.scanBarcodeImpl(apiKey, filePart, new GenericCallback() {
            @Override
            public void success(String barcodeResponse) {
                scanQRCode(filePart);
            }

            @Override
            public void error(String error) {
                Log.e("ScanActivity", "Barcode Scan error: " + error);
                showToast("Invalid QR Code");
            }
        });
    }

    private void scanQRCode(MultipartBody.Part filePart) {
        genericController.scanQRCodeImpl(apiKey, filePart, new GenericCallback() {
            @Override
            public void success(String qrResponse) {
                getClientData(qrResponse);
            }

            @Override
            public void error(String error) {
                Log.e("ScanActivity", "QR Scan error: " + error);
                showToast("Cannot Read QR Parameters");
            }
        });
    }

    private void getClientData(String qrResponse) {
        genericController.getDataByClientImpl(apiKey, new GenericCallback() {
            @Override
            public void success(String clientDataResponse) {
                processScannedData(qrResponse, clientDataResponse);
            }

            @Override
            public void error(String error) {
                Log.e("ScanActivity", "Error getting client data: " + error);
                showToast("Internal Server Error");
            }
        });
    }

    private void processScannedData(String qrResponse, String clientDataResponse) {
        try {
            JSONObject qrJson = new JSONObject(qrResponse);
            JSONObject clientDataJson = new JSONObject(clientDataResponse);
            JSONObject dataObject = qrJson.optJSONObject("data");

            String scannedUrl = dataObject != null ? dataObject.optString("fullUrl", "") : "";

            // Get matching item parameters (including QR ID)
            Map<String, String> updateParams = isUrlMatchInClientData(clientDataJson, scannedUrl);
            boolean IsOneTime = updateParams.get("type").equals("3") || updateParams.get("type").equals("1");
            if ((!updateParams.isEmpty()) && (updateParams.get("scanned").equals(false)) && (IsOneTime)) {
                updateParams.put("isScanned", "true");
                updateParams.remove("scanned");
                updateParams.remove("base64Image");
                updateQrStatus(updateParams);
                showToast("Scan processed. URL Matched!");
            } else {
                showToast("Not Active Ticket");
            }

        } catch (JSONException e) {
            Log.e("ScanActivity", "Error parsing JSON", e);
            showToast("Internal Server Error");
        }
    }

    private void updateQrStatus(Map<String, String> updateParams) {
        genericController.updateQrByIdImpl(apiKey, updateParams, new GenericCallback() {
            @Override
            public void success(String response) {
                Log.d("ScanActivity", "QR Update Successful: " + response);
            }

            @Override
            public void error(String error) {
                Log.e("ScanActivity", "QR Update Failed: " + error);
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(ScanActivity.this, message, Toast.LENGTH_SHORT).show());
    }


    // Helper method to check if scanned URL matches any URL in client data
    private Map<String, String> isUrlMatchInClientData(JSONObject clientDataJson, String scannedUrl) {
        Map<String, String> itemParams = new HashMap<>();
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
                        itemParams.put("id", key);
                        // Collect all parameters needed for the update
                        Iterator<String> itemKeys = item.keys();
                        while (itemKeys.hasNext()) {
                            String paramKey = itemKeys.next();
                            itemParams.put(paramKey, item.optString(paramKey, ""));
                        }
                        return itemParams; // Return the first matched item
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("ScanActivity", "Error parsing client data", e);
        }
        return itemParams;
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

