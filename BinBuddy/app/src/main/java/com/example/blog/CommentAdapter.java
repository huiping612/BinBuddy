package com.example.blog;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private Context context;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
        return new CommentViewHolder(view);
    }

    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        Comment comment = commentList.get(position);
        holder.TVUser.setText(comment.getUsername());
        holder.TVComment.setText(comment.getText());
        loadUserProfileImage(comment.getUsername(), holder.ProfileImage);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView TVUser, TVComment;
        private ImageView ProfileImage;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            TVUser = itemView.findViewById(R.id.TVUser);
            TVComment = itemView.findViewById(R.id.TVComment);
            ProfileImage = itemView.findViewById(R.id.IVProfile1);
        }
    }

    private void loadUserProfileImage(String username, ImageView profileImageView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user") // Adjust collection name if needed
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String profileImageUrl = document.getString("profilePic");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Load the profile image using Glide
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.default_profile_image) // Placeholder image
                                    .into(profileImageView);
                        } else {
                            // Set a default image if no profile image exists
                            profileImageView.setImageResource(R.drawable.default_profile_image);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching profile image for " + username, e);
                });
    }

}
