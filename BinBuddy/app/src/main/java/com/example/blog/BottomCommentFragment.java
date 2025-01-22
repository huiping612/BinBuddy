package com.example.blog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BottomCommentFragment extends BottomSheetDialogFragment {

    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private String contextID;
    private FireStoreHelper fireStoreHelper;
    private String currentUserID; // Replace with dynamic user ID fetching logic if available.

    public BottomCommentFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contextID = getArguments().getString("contextID");
        } else {
            throw new IllegalArgumentException("Context ID is required");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        View view = inflater.inflate(R.layout.fragment_bottom_comment, container, false);

        // Initialize commentList
        commentList = new ArrayList<>();

        // Initialize RecyclerView
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and set it to the RecyclerView
        commentAdapter = new CommentAdapter(requireContext(),commentList);
        commentsRecyclerView.setAdapter(commentAdapter);

        // Initialize EditText and Send Button
        EditText ETComment = view.findViewById(R.id.ETComments);
        ImageButton BtnSend = view.findViewById(R.id.btnSend);

        fireStoreHelper = new FireStoreHelper();
        fetchAndDisplayComments();

        // Handle adding new comments
        BtnSend.setOnClickListener(v -> {
            String newComment = ETComment.getText().toString().trim();
            if (!newComment.isEmpty()) {

                Comment comment = new Comment(currentUserID, newComment, contextID, "com/example/greenplaza"); // Replace "current_user" with actual user
                commentList.add(comment); // Add the new comment to the list immediately
                commentAdapter.notifyItemInserted(commentList.size() - 1); // Notify adapter
                commentsRecyclerView.smoothScrollToPosition(commentList.size() - 1); // Scroll to the new comment

                ETComment.setText(""); // Clear the input field

                // Add the comment to Firestore
                fireStoreHelper.addCommentData(
                        contextID,
                        newComment,
                        "com/example/greenplaza",
                        currentUserID = username               // Replace with the actual username or userID
                );
                fetchAndDisplayComments();
                Toast.makeText(getContext(), "Comment added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter a comment before sending.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void fetchAndDisplayComments() {
        fireStoreHelper.fetchComments(contextID, new FireStoreHelper.FirestoreCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (comments != null) {
                    commentList.clear();
                    commentList.addAll(comments);
                    commentAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firestore", "No comments found for contextID: " + contextID);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), "Error fetching comments: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addNewComment(Comment comment) {
        commentList.add(comment);
        commentAdapter.notifyItemInserted(commentList.size() - 1);
        commentsRecyclerView.smoothScrollToPosition(commentList.size() - 1);
    }
}