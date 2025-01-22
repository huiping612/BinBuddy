package com.example.point_collection;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.example.green_plaza.CheckOutFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class VoucherFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout voucherContainer;
    private String username;

    public double voucherAmount;

    private UserSessionManager sessionManager;

    public VoucherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voucher, container, false);

        ImageButton btnBack = view.findViewById(R.id.BtnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        db = FirebaseFirestore.getInstance();
        voucherContainer = view.findViewById(R.id.voucherContainer);

        sessionManager = new UserSessionManager(requireContext());
        username = sessionManager.getLoggedInUser();

        if (username != null && !username.isEmpty()) {
            // Load vouchers for the logged-in user
            loadVouchers();
        } else {
            Log.w("VoucherFragment", "No logged-in user found in session.");
        }

        return view;
    }

    private void loadVouchers() {
        if (username == null || username.isEmpty()) {
            Log.w("VoucherFragment", "Username is null or empty. Cannot load vouchers.");
            return;
        }

        db.collection("voucherRedemption")
                .whereEqualTo("username", username) // Filter by logged-in username
                .get()
                .addOnCompleteListener(task -> {
                    Log.d("VoucherFragment", "Username: " + username);
                    Log.d("VoucherFragment", "Query Result: " + task.getResult().size());

                    if (task.isSuccessful()) {
                        voucherContainer.removeAllViews(); // Clear the container to prevent duplication
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String voucherId = document.getString("voucherID");
                            Timestamp redeemDate = document.getTimestamp("redemptionDate");

                            if (voucherId != null && redeemDate != null) {
                                Date validityDate = redeemDate.toDate();
                                String formattedValidity = formatDate(validityDate);

                                // Fetch voucher details using the voucherID
                                fetchVoucherDetails(voucherId);
                            }
                        }
                    } else {
                        Log.w("VoucherFragment", "Error getting documents.", task.getException());
                    }
                });
    }

    public void fetchVoucherDetails(String voucherId) {
        if (voucherId != null && !voucherId.isEmpty()) {
            db.collection("voucher")
                    .document(voucherId)
                    .get()
                    .addOnCompleteListener(task -> {
                        Log.d("VoucherFragment", "Fetching voucher details for ID: " + voucherId);

                        if (task.isSuccessful() && task.getResult() != null) {
                            String title = task.getResult().getString("title");
                            String description = task.getResult().getString("description");
                            Timestamp expiryDate = task.getResult().getTimestamp("expiryDate");

                            Object amountField = task.getResult().get("amount");
                            if (amountField instanceof Number) {
                                voucherAmount = ((Number) amountField).doubleValue();
                            } else {
                                Log.w("VoucherFragment", "Invalid or missing 'amount' field: " + amountField);
                                voucherAmount = 0.0; // Default value
                            }

                            if (expiryDate != null) {
                                Date modifiedExpiryDate = expiryDate.toDate();
                                String formattedDate = formatDate(modifiedExpiryDate);
                                addVoucherCard(title, description, formattedDate, voucherId);
                            }
                        } else {
                            Log.w("VoucherFragment", "Failed to fetch voucher details: " + task.getException());
                        }
                    });
        } else {
            Log.w("VoucherFragment", "Invalid voucherId: " + voucherId);
        }
    }


    private void addVoucherCard(String title, String description, String expiry, String voucherId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View voucherCard = inflater.inflate(R.layout.voucher_item, voucherContainer, false);
        View voucherButton = voucherCard.findViewById(R.id.use_button);

        TextView voucherText = voucherCard.findViewById(R.id.voucher_text);
        voucherText.setText(title + "\n" + description + "\nValid Till: " + expiry);

        voucherButton.setOnClickListener(v -> {

            Bundle args = new Bundle();
            args.putDouble("voucherPrice", voucherAmount); // Set voucher price in the Bundle

            // Create a new instance of CheckOutFragment and pass the arguments
            CheckOutFragment checkOutFragment = new CheckOutFragment();
            checkOutFragment.setArguments(args);

            // Delete the voucher from Firestore
            db.collection("voucherRedemption")
                    .whereEqualTo("voucherID", voucherId)
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("VoucherFragment", "Voucher deleted: " + voucherId);
                                        voucherContainer.removeView(voucherCard); // Remove the card from the UI
                                    })
                                    .addOnFailureListener(e -> Log.e("VoucherFragment", "Failed to delete voucher: " + e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("VoucherFragment", "Error fetching voucher for deletion: " + e.getMessage()));
        });
        // Add the card to the container
        voucherContainer.addView(voucherCard);
        Log.d("VoucherFragment", "Adding voucher card with title: " + title);

    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
