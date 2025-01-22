package com.example.binbuddy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.binbuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Nullable;

public class LoginFragment extends Fragment {

    private EditText Username;
    private EditText Password;
    private TextView TVRegister;
    private AppCompatButton BtnLogin;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        Username = view.findViewById(R.id.ETUsername);
        Password = view.findViewById(R.id.ETPassword);
        BtnLogin = view.findViewById(R.id.ACBLogin);
        TVRegister = view.findViewById(R.id.TVRegister);


        BtnLogin.setOnClickListener(v -> loginUser());
        TVRegister.setOnClickListener( v-> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment()) // replace with SecondHandPlazaFragment
                    .addToBackStack(null) // optional: allows user to go back to previous fragment
                    .commit();
        });

        return view;
    }

    private void loginUser() {
        String username = Username.getText().toString().trim();
        String password = Password.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("user").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String storedPassword = documentSnapshot.getString("password");

                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Save the username in session
                            UserSessionManager sessionManager = new UserSessionManager(requireContext());
                            sessionManager.saveUserSession(username);

                            // Login successful
                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).onLoginSuccess();
                            }
                        } else {
                            Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "User not found, please register", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get reference to Bottom Navigation View
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        ImageView centerButton = requireActivity().findViewById(R.id.center_button);

        // Hide the Bottom Navigation
        if (bottomNav != null && centerButton != null) {
            bottomNav.setVisibility(View.GONE);
            centerButton.setVisibility(View.GONE);
        }
    }
}


// Use Navigation to move to HomeFragment
//Navigation.findNavController(requireView())
//.navigate(R.id.action_DestLogin_to_DestHomeInfoCenter, bundle);