package com.example.green_plaza;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SellingPageFragment extends Fragment {

    private EditText etItemName, etCategory, etPrice, etQuantity, etDescription, etLocation;
    private Button btnUploadItem, btnUploadImage;
    private ImageView ivItemImage;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    public SellingPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_selling_page, container, false);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        ImageButton btnBack = rootView.findViewById(R.id.BtnBack2);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Bind UI elements
        etItemName = rootView.findViewById(R.id.ETItemNameField);
        etCategory = rootView.findViewById(R.id.ETCategoryField);
        etPrice = rootView.findViewById(R.id.ETPriceField);
        etQuantity = rootView.findViewById(R.id.ETQuantityField);
        etDescription = rootView.findViewById(R.id.ETDescriptionField);
        etLocation = rootView.findViewById(R.id.ETLocationField);
        ivItemImage = rootView.findViewById(R.id.ivItemImage); // Correctly bind the ImageView
        btnUploadItem = rootView.findViewById(R.id.BtnSubmit);
        btnUploadImage = rootView.findViewById(R.id.BtnUploadPhoto);

        // Set listeners
        btnUploadItem.setOnClickListener(view -> uploadItemToFirestore());
        btnUploadImage.setOnClickListener(v -> openGallery());

        return rootView;
    }

    private void uploadItemToFirestore() {
        // Get input data from the fields
        String itemName = etItemName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String priceString = etPrice.getText().toString().trim();
        String quantityString = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(category) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Please fill all the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceString);
        int quantity = Integer.parseInt(quantityString);

        // Generate itemID
        String itemID = "item_" + System.currentTimeMillis();

        // Upload image first, then add item to Firestore
        if (imageUri != null) {
            uploadImageToFirebase(imageUrl -> {
                // Create a new Item object with the image URL
                Item newItem = new Item(itemID, category, itemName, description, location, price, quantity,
                        imageUrl, "Available", "userID_example");

                // Add the item to Firestore
                db.collection("secondHandItem").document(itemID)
                        .set(newItem)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Item uploaded successfully!", Toast.LENGTH_SHORT).show();
                            Log.d("SellingPageFragment", "Item uploaded successfully!");
                            clearFields();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to upload item.", Toast.LENGTH_SHORT).show();
                            Log.w("SellingPageFragment", "Error uploading item", e);
                        });
            });
        } else {
            Toast.makeText(getContext(), "Please select an image.", Toast.LENGTH_SHORT).show();
        }
    }

    // Step 1: Open gallery to pick an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivItemImage.setImageURI(imageUri); // Show image preview
            ivItemImage.setVisibility(View.VISIBLE); // Make the image view visible
            uploadImageToFirebase(imageUrl -> {
                // When image upload is successful, continue with Firestore item upload
                uploadItemToFirestore(); // Now we can upload the item to Firestore
            });
        }
    }


    private void uploadImageToFirebase(final OnImageUploadCompleteListener callback) {
        if (imageUri == null) {
            Log.e("Image Upload", "No image selected.");
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "images/" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Image Upload", "Upload successful.");
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d("Image Upload", "Image URL: " + imageUrl);
                        callback.onComplete(imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Image Upload", "Upload failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("imageUrl", imageUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .add(productData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Document added: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding document", e);
                });
    }

    private void clearFields() {
        etItemName.setText("");
        etCategory.setText("");
        etPrice.setText("");
        etQuantity.setText("");
        etDescription.setText("");
        etLocation.setText("");
        ivItemImage.setImageURI(null);  // Reset image preview
        ivItemImage.setVisibility(View.GONE); // Hide the ImageView
        imageUri = null;
    }

    // Callback interface for image upload completion
    private interface OnImageUploadCompleteListener {
        void onComplete(String imageUrl);
    }
}
