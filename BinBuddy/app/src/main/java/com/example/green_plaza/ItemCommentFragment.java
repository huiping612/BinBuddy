package com.example.green_plaza;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.binbuddy.UserSessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import com.example.binbuddy.R;

public class ItemCommentFragment extends BottomSheetDialogFragment {

    private static final String ARG_ITEM_ID = "item_id";
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private View emptyState;  // For showing a message when there are no comments
    private String itemId;


    // Static method to create an instance of ItemCommentFragment with item ID
    public static ItemCommentFragment newInstance(String itemId) {
        ItemCommentFragment fragment = new ItemCommentFragment();
        Bundle args = new Bundle();
        args.putString("itemID", itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        View view = inflater.inflate(R.layout.fragment_item_comment, container, false);
        ImageButton btnSubmitComment = view.findViewById(R.id.BtnSubmitComment);
        EditText commentInput = view.findViewById(R.id.comment_input);



        if (getArguments() != null) {
            itemId = getArguments().getString("itemID");
        }

        // Initialize the RecyclerView
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize empty state view
        emptyState = view.findViewById(R.id.empty_state);

        // Fetch comments from Firestore (or any other source)
        fetchComments(itemId);

        btnSubmitComment.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addCommentToDatabase(commentText, username);
                commentInput.setText(""); // Clear the input field
            }
        });


        return view;
    }

    private void addCommentToDatabase(String commentText, String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare comment data
        String userId = username; // Replace with actual user ID retrieval logic
        String commentId = db.collection("secondHandItem").document(itemId)
                .collection("itemComment").document().getId();

        Comment newComment = new Comment();
        newComment.setCommentText(commentText);
        newComment.setLikes(0);
        newComment.setItemID(itemId);
        newComment.setUserName(userId);

        // Save the comment to Firestore
        db.collection("secondHandItem")
                .document(itemId)
                .collection("itemComment")
                .document(commentId)
                .set(newComment)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ItemCommentFragment", "Comment added successfully");
                    fetchComments(itemId); // Refresh comments
                })
                .addOnFailureListener(e -> Log.d("ItemCommentFragment", "Error adding comment: ", e));
    }


    // Fetch comments from Firestore based on itemId
    private void fetchComments(String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("secondHandItem")
                .document(itemId)
                .collection("itemComment")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comment> commentsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Comment comment = document.toObject(Comment.class);
                            comment.setCommentId(document.getId()); // Make sure to set the ID
                            commentsList.add(comment);

                        }

                        // Update RecyclerView with comments
                        if (commentsList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);  // Show empty state
                        } else {
                            emptyState.setVisibility(View.GONE);  // Hide empty state
                            commentsAdapter = new CommentsAdapter(commentsList);
                            commentsRecyclerView.setAdapter(commentsAdapter);
                        }
                    } else {
                        Log.d("ItemCommentFragment", "Error getting comments: ", task.getException());
                    }
                });
    }

}
