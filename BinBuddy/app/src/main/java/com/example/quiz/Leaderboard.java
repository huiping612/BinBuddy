package com.example.quiz;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Leaderboard extends AppCompatActivity {

    private TextView name1, name2, name3, no1, no2, no3, TVRanking, bottom_rank, bottom_name, bottom_score;
    private ImageView image1, image2, image3, bottom_image;
    private RecyclerView recyclerViewRanking;
    private ArrayList<User> userList;
    private UserRankingAdapter adapter;
    private FirebaseFirestore db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        UserSessionManager sessionManager = new UserSessionManager(this);
        username = sessionManager.getLoggedInUser();

        // Initialize views
        name1 = findViewById(R.id.name1);
        name2 = findViewById(R.id.name2);
        name3 = findViewById(R.id.name3);
        no1 = findViewById(R.id.no1);
        no2 = findViewById(R.id.no2);
        no3 = findViewById(R.id.no3);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        recyclerViewRanking = findViewById(R.id.recyclerViewRanking);
        TVRanking = findViewById(R.id.TVRanking);
        bottom_rank = findViewById(R.id.bottom_rank);
        bottom_name = findViewById(R.id.bottom_name);
        bottom_image = findViewById(R.id.bottom_image);
        bottom_score = findViewById(R.id.bottom_score);

        TVRanking.setText("Ranking");

        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserRankingAdapter(this, userList);
        recyclerViewRanking.setAdapter(adapter);

        fetchLeaderboardData();

        // Set up the back button functionality
        ImageView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> finish()); // Navigate back to MainActivity
    }

    private void fetchLeaderboardData() {
        CollectionReference pointsRef = db.collection("point"); // Points collection

        pointsRef.orderBy("pointEarned", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String username = document.getId();
                            Integer pointEarned = document.getLong("pointEarned").intValue();

                            User user = new User(username);
                            user.setPoints(pointEarned);
                            userList.add(user);

                            Log.d("Leaderboard", "Username: " + username + ", Points: " + pointEarned);
                        }

                        Collections.sort(userList, Comparator.comparingInt(User::getPoints).reversed());

                        runOnUiThread(() -> {
                            updateTop3Users();
                            updateRecyclerView();
                            displayUserRank();
                        });
                    } else {
                        Log.e("Leaderboard", "Error getting documents: ", task.getException());
                        Toast.makeText(Leaderboard.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchProfilePic(String username, ImageView imageView) {
        db.collection("user").document(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String profilePicUrl = task.getResult().getString("profilePic");
                        if (profilePicUrl != null) {
                            Glide.with(this).load(profilePicUrl).into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.cat); // Default image if no URL
                        }
                    } else {
                        imageView.setImageResource(R.drawable.cat); // Default image if error
                    }
                });
    }

    private void updateTop3Users() {
        if (userList.size() > 0) {
            User firstUser = userList.get(0);
            name1.setText(firstUser.getUsername());
            no1.setText(String.valueOf(firstUser.getPoints()));
            fetchProfilePic(firstUser.getUsername(), image1);
        }
        if (userList.size() > 1) {
            User secondUser = userList.get(1);
            name2.setText(secondUser.getUsername());
            no2.setText(String.valueOf(secondUser.getPoints()));
            fetchProfilePic(secondUser.getUsername(), image2);
        }
        if (userList.size() > 2) {
            User thirdUser = userList.get(2);
            name3.setText(thirdUser.getUsername());
            no3.setText(String.valueOf(thirdUser.getPoints()));
            fetchProfilePic(thirdUser.getUsername(), image3);
        }
    }

    private void updateRecyclerView() {
        ArrayList<User> top8Users = new ArrayList<>();
        for (int i = 3; i < userList.size() && i < 8; i++) {
            top8Users.add(userList.get(i));
        }

        adapter = new UserRankingAdapter(this, top8Users);
        recyclerViewRanking.setAdapter(adapter);

        if (top8Users.isEmpty()) {
            Toast.makeText(this, "Not enough users to display", Toast.LENGTH_SHORT).show();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    public void displayUserRank() {
        String targetUsername = username; // Replace with the target username
        int rank = -1;
        int points = 0;

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equals(targetUsername)) {
                rank = i + 1;
                points = userList.get(i).getPoints();
                break;
            }
        }

        if (rank != -1) {
            bottom_rank.setText(String.valueOf(rank));
            bottom_name.setText(targetUsername);
            bottom_score.setText(String.valueOf(points));
            fetchProfilePic(targetUsername, bottom_image);
        } else {
            bottom_rank.setText("N/A");
            bottom_name.setText("N/A");
            bottom_score.setText("N/A");
            bottom_image.setImageResource(R.drawable.cat);
        }
    }

}
