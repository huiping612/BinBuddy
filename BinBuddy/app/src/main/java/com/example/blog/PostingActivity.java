package com.example.blog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.binbuddy.R;
import com.google.firebase.firestore.FieldValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PostingActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 100;
    private FireStoreHelper fireStoreHelper;
    private ImageView IVPhotoBox, IVIcon;
    private EditText ETTitle, ETContent;
    private ImageButton btnBack;
    private Button btnSubmit;
    private Bitmap selectedBitmap;
    private TextView TVUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        String username = getIntent().getStringExtra("username");

        // Initialize FireStoreHelper
        fireStoreHelper = new FireStoreHelper();

        // Set padding for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.BtnBack2);
        IVPhotoBox = findViewById(R.id.IVPhotoBox);
        ETTitle = findViewById(R.id.ETTitle);
        ETContent = findViewById(R.id.ETContent);
        btnSubmit = findViewById(R.id.BtnSubmit);
        TVUpload = findViewById(R.id.TVUpload);
        IVIcon = findViewById(R.id.IVIcon);


        // Back button functionality
        btnBack.setOnClickListener(v -> finish());

        // Open gallery when clicking IVPhotoBox or TVUpload
        View.OnClickListener openGalleryListener = v -> openGallery();
        IVPhotoBox.setOnClickListener(openGalleryListener);
        TVUpload.setOnClickListener(openGalleryListener);

        // Submit button functionality
        btnSubmit.setOnClickListener(v -> {
            String title = ETTitle.getText().toString().trim();
            String content = ETContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Incomplete Submission")
                        .setMessage("Please fill out all fields before submitting.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                //with actual userID logic
                String userID = username;
                FieldValue timestamp = FieldValue.serverTimestamp();

                // Convert image to Base64, or set it as null if no image is selected
                String base64Image = selectedBitmap != null ? encodeImageToBase64(selectedBitmap) : null;

                // Add blog data to Firestore
                fireStoreHelper.addBlogData(this,content, 0, 0,base64Image, timestamp, title, userID);{

                new AlertDialog.Builder(PostingActivity.this)
                       .setTitle("Success")
                       .setMessage("The blog is successfully added!")
                       .setPositiveButton("OK", (dialog, which) -> {
                           dialog.dismiss();

                           Intent resultIntent = new Intent();
                           setResult(RESULT_OK, resultIntent); // Result to indicate success
                           finish();
                       })
                       .show();
                };

            }
        });

    }

    // Open gallery to select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    // Handle the result from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            try {
                // Convert the selected image to a Bitmap
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                // Display the image in IVPhotoBox
                IVPhotoBox.setImageBitmap(selectedBitmap);
                IVIcon.setVisibility(View.GONE);
                TVUpload.setVisibility(View.GONE);

                Toast.makeText(this, "Photo successfully added!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to add photo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Encode Bitmap to Base64 string
    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}