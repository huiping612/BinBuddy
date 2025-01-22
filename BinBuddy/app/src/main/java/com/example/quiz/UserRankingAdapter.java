package com.example.quiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.binbuddy.R;

import java.util.List;

public class UserRankingAdapter extends RecyclerView.Adapter<UserRankingAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private FirebaseFirestore db;

    // Constructor for the adapter
    public UserRankingAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.activity_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind data to the views
        User user = userList.get(position);
        holder.tvRanking.setText(String.valueOf(position + 4)); // Setting the rank as position + 4
        holder.tvUsername.setText(user.getUsername());
        holder.tvScore.setText(String.valueOf(user.getPoints()));

        // Fetch and load the profile photo from Firestore
        db.collection("user").document(user.getUsername())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profilePicUrl = documentSnapshot.getString("profilePic");
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(context).load(profilePicUrl).into(holder.ivUserPhoto);
                        } else {
                            Glide.with(context).load(R.drawable.cat).into(holder.ivUserPhoto); // Default profile picture
                        }
                    } else {
                        Glide.with(context).load(R.drawable.cat).into(holder.ivUserPhoto); // Default if user not found
                    }
                })
                .addOnFailureListener(e -> {
                    Glide.with(context).load(R.drawable.cat).into(holder.ivUserPhoto); // Default in case of error
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class to hold references to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRanking, tvUsername, tvScore;
        ShapeableImageView ivUserPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            tvRanking = itemView.findViewById(R.id.tvRanking);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
