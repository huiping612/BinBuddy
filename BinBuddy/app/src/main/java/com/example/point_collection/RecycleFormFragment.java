package com.example.point_collection;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.binbuddy.HomePageFragment;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecycleFormFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText nameTextbox, recyclingCentreTextbox, recyclingItemsTextbox;
    private Button submitButton;
    private ImageView selectedImageView;
    private Uri selectedImageUri;
    private UserSessionManager sessionManager;

    private String username;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public RecycleFormFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecycleFormFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecycleFormFragment newInstance(String param1, String param2) {
        RecycleFormFragment fragment = new RecycleFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle_form, container, false);

        ImageButton btnBack = view.findViewById(R.id.BtnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Initialize Firestore and Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        sessionManager = new UserSessionManager(requireContext());
        username = sessionManager.getLoggedInUser();

        // Get references to UI elements
        nameTextbox = view.findViewById(R.id.nameTextbox);
        recyclingCentreTextbox = view.findViewById(R.id.recyclingCentreTextbox);
        recyclingItemsTextbox = view.findViewById(R.id.recyclingItemsTextbox);
        selectedImageView = view.findViewById(R.id.selectedImageView);
        submitButton = view.findViewById(R.id.submitButton);

        // Set up the upload image button
        selectedImageView.setOnClickListener(v -> openImagePicker());

        // Set up the submit button
        submitButton.setOnClickListener(v -> saveDataToFirestoreAndGoBack());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            selectedImageView.setImageURI(selectedImageUri); // Preview the selected image
        }
    }

    private void saveDataToFirestoreAndGoBack() {
        // Get user input
        String name = nameTextbox.getText().toString().trim();
        String recyclingCentre = recyclingCentreTextbox.getText().toString().trim();
        String recyclingItems = recyclingItemsTextbox.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || recyclingCentre.isEmpty() || recyclingItems.isEmpty()) {
            // Show a toast message prompting the user to fill in the fields
            Toast.makeText(getContext(), "Please fill in all fields before submitting", Toast.LENGTH_SHORT).show();
            return; // Stop the process if fields are empty
        }

        // Validate if an image is selected
        if (selectedImageUri == null) {
            // Show a toast message prompting the user to upload an image
            Toast.makeText(getContext(), "Please upload an image before submitting", Toast.LENGTH_SHORT).show();
            return; // Stop the process if no image is selected
        }

        // Prepare data for Firestore
        Map<String, Object> recyclingData = new HashMap<>();
        recyclingData.put("Name", name);
        recyclingData.put("RecyclingCentre", recyclingCentre);
        recyclingData.put("RecyclingItems", recyclingItems);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";
        recyclingData.put("userId", username);

        // Save data to Firestore
        db.collection("recyclingForm")
                .add(recyclingData)
                .addOnSuccessListener(documentReference -> {
                    showConfirmationDialog(username,documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void clearFormFields() {
        nameTextbox.setText("");
        recyclingCentreTextbox.setText("");
        recyclingItemsTextbox.setText("");
        selectedImageView.setImageURI(null);
        selectedImageUri = null;
    }

    private void returnToHomepageFragment() {
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomePageFragment()); // Replace with your homepage fragment
            transaction.commit();
        }
    }


    private void showConfirmationDialog(String userId, String documentReference) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this data?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Call saveDataToFirestore and finish the fragment on success
                    Toast.makeText(requireContext(), "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    uploadImageToStorage(documentReference);
                    updatePointsDatabase(userId, 10);
                    clearFormFields();

                    // Return to the home page fragment
                    //returnToHomepageFragment();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void uploadImageToStorage(String documentId) {
        if (selectedImageUri == null) return;

        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString()); // Save in the "images/" folder

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update Firestore document with image URL
                        db.collection("recyclingForm").document(documentId)
                                .update("imageUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updatePointsDatabase(String userId, int pointsToAdd) {
        // Reference the `point` collection and the specific document
        db.collection("point").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long currentPoints = documentSnapshot.contains("pointEarned")
                                ? documentSnapshot.getLong("pointEarned")
                                : 0;

                        // Increment the `pointEarned` field
                        db.collection("point").document(userId)
                                .update("pointEarned", currentPoints + pointsToAdd)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Points updated successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update points: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Create a new document if it doesn't exist
                        Map<String, Object> pointData = new HashMap<>();
                        pointData.put("pointEarned", pointsToAdd);
                        pointData.put("submissionDate", System.currentTimeMillis());
                        pointData.put("username", username ); // Add username if available

                        db.collection("point").document(userId)
                                .set(pointData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Points added successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to create points document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch points: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

