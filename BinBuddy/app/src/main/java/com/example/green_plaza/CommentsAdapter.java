package com.example.green_plaza;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import com.example.binbuddy.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentsList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CommentsAdapter(List<Comment> commentsList) {
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.userName.setText(comment.getUserName());
        holder.commentText.setText(comment.getCommentText());

        holder.likeButton.setOnClickListener(v -> {
            int currentLikes = comment.getLikes();
            comment.setLikes(currentLikes + 1); // Increment likes

            // Update likes in Firestore
            db.collection("secondHandItem")
                    .document(comment.getItemID())
                    .collection("itemComment")
                    .document(comment.getCommentId())
                    .update("likes", comment.getLikes())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("CommentsAdapter", "Like count updated");
                        holder.likesCount.post(() ->
                                holder.likesCount.setText(String.valueOf(comment.getLikes())));
                    })
                    .addOnFailureListener(e -> Log.e("CommentsAdapter", "Error updating likes", e));

        });
        // Set any other views if needed
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentText, likesCount;
        ImageButton likeButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            commentText = itemView.findViewById(R.id.comment_text);
            likesCount = itemView.findViewById(R.id.likes_count);
            likeButton = itemView.findViewById(R.id.like_button);
        }
    }
}

