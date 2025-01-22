package com.example.point_collection;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.example.quiz.Leaderboard;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private String mParam1;
    private FirebaseFirestore db;
    ImageView userImageView;
    TextView usernameTextView, pointNumber, rankingPlace;
    UserSessionManager sessionManager;

    Button redeemButton, viewButton, viewRanking;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String username) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton btnBack = view.findViewById(R.id.BtnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        sessionManager = new UserSessionManager(requireContext());
        mParam1 = sessionManager.getLoggedInUser();

        if (mParam1 != null) {
            usernameTextView = view.findViewById(R.id.usernameText);
            userImageView = view.findViewById(R.id.userImage);
            pointNumber = view.findViewById(R.id.pointNumber);
            redeemButton = view.findViewById(R.id.redeemButton);
            viewButton = view.findViewById(R.id.viewButton);
            rankingPlace = view.findViewById(R.id.rankingPlace2);
            viewRanking= view.findViewById(R.id.viewRankingButton);

            redeemButton.setOnClickListener(v -> {
                // Navigate to a RedemptionFragment
                RedeemFragment redemptionFragment = new RedeemFragment();
                Bundle args = new Bundle();
                args.putString("username", mParam1); // Pass the logged-in username
                redemptionFragment.setArguments(args);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, redemptionFragment) // Replace with your container ID
                        .addToBackStack(null)
                        .commit();
            });

            viewButton.setOnClickListener(v -> {
                // Navigate to a PointsHistoryFragment
                VoucherFragment voucherFragment = new VoucherFragment();
                Bundle args = new Bundle();
                args.putString("username", mParam1); // Pass the logged-in username
                voucherFragment.setArguments(args);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, voucherFragment) // Replace with your container ID
                        .addToBackStack(null)
                        .commit();
                Log.d("ProfileFragment", "View button clicked");
            });

            viewRanking.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), Leaderboard.class);

                // Pass the logged-in username as an extra
                intent.putExtra("USERNAME_KEY", mParam1);

                // Start the LeaderboardActivity
                startActivity(intent);
            });

            fetchUserData(mParam1, usernameTextView, userImageView);
            fetchPointData(mParam1, pointNumber);
            fetchAndDisplayUserRank(mParam1);
        } else {
            Log.e("ProfileFragment", "Username is null");
        }

        return view;
    }

    private void fetchUserData(String username, TextView usernameText, ImageView userImage) {
        db = FirebaseFirestore.getInstance();

        db.collection("user")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String fetchedUsername = document.getString("username");
                            String profileImageURL = document.getString("profilePic");

                            usernameText.setText(fetchedUsername);
                            Glide.with(this)
                                    .load(profileImageURL)
                                    .into(userImage);
                        }
                    } else {
                        Log.d("Firestore", "No user found with username: " + username);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user data", e));
    }

    public void fetchPointData(String username, TextView pointNumber) {
        db = FirebaseFirestore.getInstance();

        db.collection("point")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Object pointData = document.get("pointEarned");

                            if (pointData instanceof Long) {
                                Long totalPoint = (Long) pointData;
                                pointNumber.setText(String.valueOf(totalPoint));
                            } else if (pointData instanceof Double) {
                                Double totalPoint = (Double) pointData;
                                pointNumber.setText(String.valueOf(totalPoint));
                            } else {
                                Log.e("Firestore", "Unexpected data type for pointEarned");
                            }

                        }
                    } else {
                        Log.d("Firestore", "No user found with username: " + username);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user data", e));
    }

    public void fetchAndDisplayUserRank(String targetUsername) {
        db = FirebaseFirestore.getInstance();

        db.collection("point")  // Assuming 'point' collection has the points data
                .orderBy("pointEarned", Query.Direction.DESCENDING)  // Sorting by points in descending order
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int rank = 1;
                        int targetUserPoints = 0;

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String username = document.getString("username");
                            Long points = document.getLong("pointEarned");

                            if (points != null) {
                                // Check if this is the target user
                                if (username != null && username.equals(targetUsername)) {
                                    targetUserPoints = points.intValue();
                                    rankingPlace.setText(String.valueOf(rank));  // Set rank
                                    break;
                                }
                                rank++;  // Increment rank
                            }
                        }
                    } else {
                        Log.d("Firestore", "No user data found");
                        rankingPlace.setText("N/A");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching point data", e));
    }


}
