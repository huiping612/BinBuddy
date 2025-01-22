package com.example.blog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewingActivity extends AppCompatActivity {
    private FireStoreHelper db;
    private List<Blogs> blogList;
    private List<Blogs> filteredBlogs;
    private ImageButton btnBack, btnAdd;
    private EditText ETSearchingBar;
    private ImageView IVSearch;

    private ImageView IVProfile1;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);

        // Initialize Firestore Helper and lists
        db = new FireStoreHelper();
        blogList = new ArrayList<>();
        filteredBlogs = new ArrayList<>();

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);
        IVProfile1 = findViewById(R.id.IVProfile1);
        ETSearchingBar = findViewById(R.id.ETSearchingBar);
        IVSearch = findViewById(R.id.IVSearch);

        String username = getIntent().getStringExtra("username");
        loadUserProfileImage(username);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBlogs);

        BlogAdapter adapter = new BlogAdapter(this, filteredBlogs, blog -> {
            Intent intent = new Intent(ViewingActivity.this, ArticleDetailsActivity.class);
            intent.putExtra("username", blog.getUsername());
            intent.putExtra("title", blog.getTitle());
            intent.putExtra("description", blog.getDescription());
            intent.putExtra("imageBase64", blog.getImageBase64());
            startActivity(intent);
        });

        // Set GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Update blogs initially (will load after Firestore fetch)
        updateBlogDisplay();

        btnBack.setOnClickListener(v -> finish()); // Go back to the previous activity

        btnAdd.setOnClickListener(v -> {
            // Navigate to PostingActivity
            Intent intent = new Intent(ViewingActivity.this, PostingActivity.class);
            intent.putExtra("username", username);  // Pass the username to PostingActivity
            startActivity(intent);
        });

        IVSearch.setOnClickListener(v -> {
            String query = ETSearchingBar.getText().toString().trim();
            if (!query.isEmpty()) {
                searchBlogs(query);
            } else {
                resetBlogs();
            }
        });

        // Set up the SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Load the latest blogs when refreshed
            loadBlogs();

            // After data is loaded, stop the refreshing animation
            swipeRefreshLayout.setRefreshing(false);
        });
        loadBlogs();
    }

    private void loadBlogs() {
        db.readBlogDataFromCollection("blog", task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                blogList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    String username = document.getString("userID");
                    String title = document.getString("title");
                    String description = document.getString("content");
                    String imageBase64 = document.getString("picture");
                    String contextID = document.getId();  // Get document ID

                    if (imageBase64 == null) {
                        imageBase64 = ""; // Assign an empty string or handle as needed
                    }

                    blogList.add(new Blogs(username,title, description, imageBase64,contextID));
                }
                filteredBlogs = new ArrayList<>(blogList);
                updateBlogDisplay();
            } else {
                Log.w("Firestore", "Error fetching blogs", task.getException());
            }
        });
    }

    private void searchBlogs(String query) {
        db.readBlogDataFromCollection("blog", task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                filteredBlogs.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    String username = document.getString("userID");
                    String title = document.getString("title");
                    String description = document.getString("content");
                    String imageBase64 = document.getString("picture");
                    String contextID = document.getId();

                    if (imageBase64 == null) {
                        imageBase64 = ""; // Assign an empty string or handle as needed
                    }

                    // Check if the title or content contains the query (case-insensitive)
                    if ((title != null && title.toLowerCase().contains(query.toLowerCase())) ||
                            (description != null && description.toLowerCase().contains(query.toLowerCase()))) {
                        filteredBlogs.add(new Blogs(username, title, description, imageBase64,contextID));
                    }
                }
                updateBlogDisplay();
            } else {
                Log.w("Firestore", "Error fetching search results", task.getException());
            }
        });
    }

    private void resetBlogs() {
        filteredBlogs = new ArrayList<>(blogList);
        updateBlogDisplay();
    }

    private void updateBlogDisplay() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBlogs);

        // Set up RecyclerView and Adapter
        BlogAdapter adapter = new BlogAdapter(this, filteredBlogs, blog -> {
            // Create an Intent to navigate to ArticleDetailsActivity
            Intent intent = new Intent(ViewingActivity.this, ArticleDetailsActivity.class);

            // Pass data to the intent
            intent.putExtra("contextID", blog.getContextID()); // Pass the documentID
            intent.putExtra("username", blog.getUsername());
            intent.putExtra("title", blog.getTitle());
            intent.putExtra("description", blog.getDescription());
            intent.putExtra("imageBase64", blog.getImageBase64());

            // Start the ArticleDetailsActivity
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
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
                        String profileImageUrl = document.getString("profilePic");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Load the profile image using Glide
                            Glide.with(ViewingActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.default_profile_image) // Set a placeholder while loading
                                    .into(IVProfile1);
                        } else {
                            // Set a default image if no profile image exists
                            IVProfile1.setImageResource(R.drawable.default_profile_image); // Add a default image in drawable
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting user profile", e);
                });
    }
}