package com.example.binbuddy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.binbuddy.R;
import com.example.blog.ViewingActivity;
import com.example.point_collection.ProfileFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText Username, Password, Email, Phone, Address;
    private ImageView ProfilePic;
    private AppCompatButton BtnRegister;
    private Uri selectedImageUri; // To store the selected image URI
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ProfilePic.setImageURI(selectedImageUri); // Display the selected image in ImageView
                }
            });

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize views
        Username = view.findViewById(R.id.ETUsername);
        Password = view.findViewById(R.id.ETPassword);
        Email = view.findViewById(R.id.ETEmail);
        Phone = view.findViewById(R.id.ETPhone);
        Address = view.findViewById(R.id.ETAddress);
        ProfilePic = view.findViewById(R.id.IVProfilePic);
        BtnRegister = view.findViewById(R.id.ACBSignIn);

        // Set click listeners
        BtnRegister.setOnClickListener(v -> registerUser());
        ProfilePic.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void registerUser() {
        boolean isValid = true;

        // Validate Username
        String username = Username.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Username.setError("Username is required");
            isValid = false;
        }

        // Validate Password
        String password = Password.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Password.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Validate Email
        String email = Email.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email.setError("Invalid email address");
            isValid = false;
        }

        // Validate Phone
        String phone = Phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || !phone.matches("\\d{3}-\\d{7}")) {
            Phone.setError("Phone number must be in the format XXX-XXXXXXX");
            isValid = false;
        }

        // Validate Address
        String address = Address.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            Address.setError("Address is required");
            isValid = false;
        }

        // Validate Profile Picture
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please upload a profile picture", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // If validation fails, stop the registration process
        if (!isValid) {
            Toast.makeText(getContext(), "Please correct the errors before proceeding", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for duplicate username
        db.collection("user").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Username already exists
                        Username.setError("Username already taken");
                        Toast.makeText(getContext(), "Username is already in use. Please choose another one.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with uploading the profile picture and saving the user
                        uploadImageToFirebase(selectedImageUri, downloadUrl -> {
                            saveUserToFirestore(username, password, email, phone, address, downloadUrl);
                            createAccountToPoint(username);

                            LoginFragment loginFragment = new LoginFragment();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, loginFragment) // Replace fragment_container with your actual container ID
                                    .addToBackStack(null) // Optional: Allows user to go back to the previous fragment
                                    .commit();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking username. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveUserToFirestore(String username, String password, String email, String phone, String address, @Nullable String profilePicUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("points", 0);

        if (profilePicUrl != null) {
            userData.put("profilePic", profilePicUrl);
        }

        db.collection("user")
                .document(username)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void createAccountToPoint(String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("pointEarned", 0);  // Initialize points to 0 when the account is created
        userData.put("submissionDate", FieldValue.serverTimestamp());

        // Access Firestore database
        db.collection("point")
                .document(username)  // Use username as the document ID
                .set(userData)  // Set data in the document
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    private void uploadImageToFirebase(Uri imageUri, OnSuccessListener<String> onSuccessListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profilePicRef = storageRef.child("profilePicture/" + Username.getText().toString() + ".jpg");

        profilePicRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString(); // Convert Uri to String
                            onSuccessListener.onSuccess(downloadUrl); // Pass the String to the provided callback
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to retrieve image URL", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
