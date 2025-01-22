package com.example.point_collection;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RedeemFragment extends Fragment implements AfterRedeemFragment.OnRedeemConfirmListener {

    private FirebaseFirestore db;
    private LinearLayout voucherContainer;
    UserSessionManager sessionManager;
    private long currentPoints = 0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView pointNumber;

    public RedeemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RedeemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RedeemFragment newInstance(String param1, String param2) {
        RedeemFragment fragment = new RedeemFragment();
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

        sessionManager = new UserSessionManager(requireContext());
        mParam1 = sessionManager.getLoggedInUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_redeem, container, false);

        ImageButton btnBack = view.findViewById(R.id.BtnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Initialize Firestore and UI elements
        db = FirebaseFirestore.getInstance();
        voucherContainer = view.findViewById(R.id.voucherContainer);

        pointNumber = view.findViewById(R.id.pointNumber);
        ProfileFragment profileFragment= new ProfileFragment();
        profileFragment.fetchPointData(mParam1, pointNumber);

        // Load vouchers from Firestore
        loadVouchers();

        return view;
    }

    private void loadVouchers() {
        db.collection("voucher")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        voucherContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String voucherId = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            Timestamp validityTimestamp = document.getTimestamp("expiryDate");
                            Boolean isRedeemed = document.getBoolean("status");

                            if (validityTimestamp != null) {
                                Date validityDate = validityTimestamp.toDate();
                                String formattedValidity = formatDate(validityDate);
                                addVoucherCard(voucherId, title, description, formattedValidity, isRedeemed);
                            }
                        }
                    } else {
                        Log.w("RedeemFragment", "Error getting documents.", task.getException());
                    }
                });
    }


    private void addVoucherCard(String voucherId, String title, String description, String validity, Boolean isRedeemed) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View voucherCard = inflater.inflate(R.layout.voucher_card, null, false);

        TextView voucherText = voucherCard.findViewById(R.id.voucher_text);
        voucherText.setText(title + "\n" + description + "\nValid Till: " + validity);

        if (isRedeemed) {
            voucherCard.findViewById(R.id.punch_hole).setBackgroundResource(R.drawable.circle_background_clicked); // Change background
            voucherCard.findViewById(R.id.punch_hole).setEnabled(false);  // Disable the button
        }

        voucherCard.findViewById(R.id.punch_hole).setOnClickListener(v -> {
                AfterRedeemFragment afterRedeemFragment = AfterRedeemFragment.newInstance(voucherId, title);
                afterRedeemFragment.setOnRedeemConfirmListener(this);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(android.R.id.content, afterRedeemFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
        });

        voucherContainer.addView(voucherCard);
    }


    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public void onRedeemConfirmed(String voucherId) {
        // Fetch the points first
        fetchUserPoints(getCurrentUserID(), points -> {
            // After points are fetched, check if they are sufficient
            long pointsToDeduct = 30; // Points required for the voucher
            if (points >= pointsToDeduct) {
                redeemVoucher(voucherId);
                deductUserPoints(getCurrentUserID()); // Pass the points to deduct
                saveToVoucherRedemption(voucherId, getCurrentUserID());
            } else {
                Toast.makeText(getContext(), "Not enough points to redeem the voucher.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Redeemed the voucher and change the status in database as true to show redeemed
    private void redeemVoucher(String voucherId) {
        db.collection("voucher")
                .document(voucherId)
                .update("status", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RedeemFragment", "Voucher redeemed.");
                    loadVouchers();
                    fetchUserPoints(getCurrentUserID(), points -> {
                        pointNumber.setText(String.valueOf(points));  // Update points display
                    });
                })
                .addOnFailureListener(e -> {
                    Log.w("RedeemFragment", "Error redeeming voucher.", e);
                });
    }

    public void saveToVoucherRedemption(String voucherId, String userId) {
        Map<String, Object> redemptionData = new HashMap<>();
        redemptionData.put("voucherID", voucherId); // Foreign Key
        redemptionData.put("redemptionDate", new Timestamp(new Date())); // Current date/time
        redemptionData.put("username", userId); // User who redeemed

        db.collection("voucherRedemption")
                .add(redemptionData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("RedeemFragment", "Voucher redemption successfully recorded with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("RedeemFragment", "Error recording voucher redemption.", e);
                });
    }
    private void fetchUserPoints(String userId, PointsCallback callback) {
        db.collection("point")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentPoints = documentSnapshot.getLong("pointEarned") != null ? documentSnapshot.getLong("pointEarned") : 0;
                        callback.onPointsFetched(currentPoints);
                    } else {
                        currentPoints = 0;
                        Log.w("RedeemFragment", "User document does not exist.");
                        callback.onPointsFetched(currentPoints); // Pass the currentPoints even if they are 0
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("RedeemFragment", "Error fetching user points.", e);
                    callback.onPointsFetched(0); // If error, pass 0 points
                });
    }

    // Define a callback interface for points fetching
    interface PointsCallback {
        void onPointsFetched(long points);
    }


    private void deductUserPoints(String userId) {
        db.collection("point")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentPoints = documentSnapshot.getLong("pointEarned") != null ? documentSnapshot.getLong("pointEarned") : 0;
                        long pointsToDeduct = 30; // Points required for the voucher

                        if (currentPoints >= pointsToDeduct) {
                            // Deduct points
                            db.collection("point")
                                    .document(userId)
                                    .update("pointEarned", currentPoints - pointsToDeduct)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("RedeemFragment", "Points successfully deducted.");
                                        currentPoints -= pointsToDeduct; // Update the local value
                                    })
                                    .addOnFailureListener(e -> Log.w("RedeemFragment", "Error deducting points.", e));
                        } else {
                            Log.w("RedeemFragment", "Not enough points to redeem the voucher.");
                            Toast.makeText(getContext(), "Not enough points to redeem the voucher.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("RedeemFragment", "User document does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.w("RedeemFragment", "Error fetching user document.", e));
    }



    private String getCurrentUserID() {
        return mParam1;
    }
}