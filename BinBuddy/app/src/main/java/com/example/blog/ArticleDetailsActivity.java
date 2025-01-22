package com.example.blog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class ArticleDetailsActivity extends AppCompatActivity {

    private TextView TVLikeCount, TVCommentCount, TVTitle, TVContent, TVUsername;
    private EditText ETComment;
    private ImageView IVPicture, IVProfilePic;
    private ImageButton BtnSend, BtnBack, BtnLike, BtnComment;
    private int likeCount = 0;
    private int commentCount = 0;
    private FireStoreHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);

        db = new FireStoreHelper();

        // Retrieve data from Intent
        String contextID = getIntent().getStringExtra("contextID");
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String username = getIntent().getStringExtra("userID");
        String imageBase64 = getIntent().getStringExtra("imageBase64");

        // Initialize UI elements
        BtnBack = findViewById(R.id.BtnBack);
        BtnLike = findViewById(R.id.BtnLike);
        BtnComment = findViewById(R.id.btnLikeComment);
        BtnSend = findViewById(R.id.BtnSend);
        TVLikeCount = findViewById(R.id.TVCommentLikeCount);
        TVCommentCount = findViewById(R.id.TVCommentCount);
        ETComment = findViewById(R.id.ETComment);
        TVTitle = findViewById(R.id.TVArticleTitle);
        TVContent = findViewById(R.id.TVContent);
        IVPicture = findViewById(R.id.IVPicture);
        TVUsername = findViewById(R.id.TVUsername);
        IVProfilePic = findViewById(R.id.IVProfilePic);

        // Set data to UI elements
        TVTitle.setText(title);
        TVContent.setText(content);
        TVUsername.setText(username);

        // Decode Base64 image and display it
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            Bitmap image = ImageUtils.decodeBase64ToBitmap(imageBase64);
            IVPicture.setImageBitmap(image);
        }

        // Back button functionality
        BtnBack.setOnClickListener(view -> finish());

        db.fetchBlogDetails(contextID, blogDetails -> {
            if (blogDetails != null) {
                // Update UI with fetched details
                TVTitle.setText((String) blogDetails.get("title"));
                TVContent.setText((String) blogDetails.get("content"));
                TVUsername.setText((String)blogDetails.get("userID"));
                likeCount = ((Long) blogDetails.get("likeCount")).intValue();
                commentCount = ((Long) blogDetails.get("commentCount")).intValue();
                TVLikeCount.setText(String.valueOf(likeCount));
                TVCommentCount.setText(String.valueOf(commentCount));
            } else {
                Toast.makeText(this, "Blog details not found!", Toast.LENGTH_SHORT).show();
            }
        });


        // Like button functionality
        BtnLike.setOnClickListener(view -> {
            likeCount++;
            TVLikeCount.setText(String.valueOf(likeCount));

            db.updateLikeCount(contextID, likeCount, success -> {
                if (success) {
                    Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update like count.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Comment button functionality
        BtnComment.setOnClickListener(view -> {
            BottomCommentFragment bottomCommentFragment = new BottomCommentFragment();
            Bundle args = new Bundle();
            args.putString("contextID", contextID);
            bottomCommentFragment.setArguments(args);
            bottomCommentFragment.show(getSupportFragmentManager(), "BottomCommentFragment");
            //Debug
            Log.d("BottomCommentFragment", "contextID: " + contextID);
        });

        // Send comment functionality
        BtnSend.setOnClickListener(view -> {
            String comment = ETComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                commentCount++;
                TVCommentCount.setText(String.valueOf(commentCount));

                // Add the comment to Firestore
                db.addCommentData(contextID, comment, "Blog", username);

                ETComment.setText(""); // Clear the EditText

                // Notify BottomCommentFragment if it's open
                BottomCommentFragment fragment = (BottomCommentFragment) getSupportFragmentManager()
                        .findFragmentByTag("BottomCommentFragment");
                if (fragment != null && fragment.isVisible()) {
                    fragment.addNewComment(new Comment(username, comment, contextID, "Blog"));
                }
            } else {
                Toast.makeText(this, "Please write a comment before submitting.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (contextID != null) {
            // Fetch blog data from Firestore
            FireStoreHelper fireStoreHelper = new FireStoreHelper();
            fireStoreHelper.getBlogByDocumentID(contextID, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Retrieve blog details
                        String titles = document.getString("title");
                        String contents = document.getString("content");
                        String usernames = document.getString("userID");
                        int likeCount = document.getLong("likeCount").intValue();

                        // Populate the UI
                        TVTitle.setText(titles);
                        TVContent.setText(contents);
                        TVUsername.setText(usernames);
                        TVLikeCount.setText(String.valueOf(likeCount));
                        loadUserProfileImage(usernames);

                    } else {
                        Log.e("ArticleDetail", "Document does not exist!");
                    }
                } else {
                    Log.e("ArticleDetail", "Error fetching document", task.getException());
                }
            });
        } else {
            Log.e("ArticleDetail", "No contextID passed in the intent!");
        }
    }

    private void loadUserProfileImage(String username) {
        // Query Firestore to get user profile data based on username
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user") // Assuming you have a "users" collection
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String profileImage = document.getString("profilePic");

                        if (profileImage != null && !profileImage.isEmpty()) {
                            try {
                                Bitmap profileImageBitmap = ImageUtils.decodeBase64ToBitmap(profileImage);
                                IVProfilePic.setImageBitmap(profileImageBitmap);
                            } catch (IllegalArgumentException e) {
                                // If Base64 decoding fails, treat it as a URL and load the image using Glide
                                Glide.with(ArticleDetailsActivity.this)
                                        .load(profileImage) // Profile image URL
                                        .into(IVProfilePic);
                            }
                        } else {
                            // Set a default image if no profile image exists
                            IVProfilePic.setImageResource(R.drawable.default_profile_image); // Add a default image in drawable
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting user profile", e);
                });
    }
}