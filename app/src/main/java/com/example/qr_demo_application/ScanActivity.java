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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
                // Get InputStream from URI
                InputStream imageStream = getContentResolver().openInputStream(imageUri);

                // Manually read the InputStream into a byte array
                byte[] imageBytes = getBytesFromInputStream(imageStream);

                // Convert the byte array to a MultipartBody.Part
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody);

                // Call the new method to handle the barcode and QR code processing
                genericController.scanBarcodeOrQRCode(apiKey, filePart, true, new GenericCallback() {
                    @Override
                    public void success(String response) {
                        // Handle success
                        Log.d("API CALL", "Success: " + response);
                        Toast.makeText(ScanActivity.this, "Success: " + response, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void error(String error) {
                        // Handle error
                        Log.d("API CALL", "Error: " + error);
                        Toast.makeText(ScanActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Log.e("ScanActivity", "Error reading image from URI", e);
            }
        }
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

