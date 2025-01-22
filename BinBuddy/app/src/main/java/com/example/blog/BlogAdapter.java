package com.example.blog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private Context context;
    private List<Blogs> blogList;
    private OnBlogClickListener listener;

    private ImageView IVProfileImage1;


    public interface OnBlogClickListener {
        void onBlogClick(Blogs blog);
    }
    public BlogAdapter(Context context, List<Blogs> blogList, OnBlogClickListener listener) {
        this.context = context;
        this.blogList = blogList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blog_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blogs blog = blogList.get(position);

        // Bind data to the views
        holder.blogUser.setText(blog.getUsername());
        holder.blogTitle.setText(blog.getTitle());
        holder.blogDescription.setText(blog.getDescription());

        loadProfileImage(blog.getUsername(), holder.profileImage);

        // Decode Base64 string and set as image
        if (blog.getImageBase64() != null && !blog.getImageBase64().isEmpty()) {
            Bitmap decodedImage = ImageUtils.decodeBase64ToBitmap(blog.getImageBase64());
            holder.blogPhoto.setImageBitmap(decodedImage);
        }

        // Set click listener on TVBlogDescription to navigate to article detail page
        holder.blogDescription.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailsActivity.class);
            intent.putExtra("contextID", blog.getContextID());
            intent.putExtra("userID", blog.getUsername());
            intent.putExtra("title", blog.getTitle());
            intent.putExtra("username", blog.getUsername());
            intent.putExtra("title", blog.getTitle());
            intent.putExtra("content", blog.getDescription());
            intent.putExtra("imageBase64", blog.getImageBase64());
            context.startActivity(intent);
        });

        // Set click listener on itemView
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBlogClick(blog);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    // ViewHolder class
    static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView blogUser, blogTitle, blogDescription;
        ShapeableImageView profileImage;
        ImageView blogPhoto;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            blogUser = itemView.findViewById(R.id.TVBlogUser);
            blogTitle = itemView.findViewById(R.id.TVBlogTitle);
            blogDescription = itemView.findViewById(R.id.TVBlogDescription);
            profileImage = itemView.findViewById(R.id.IVProfileImage1);
            blogPhoto = itemView.findViewById(R.id.IVPhoto);
        }
    }

    private void loadProfileImage(String username, ShapeableImageView profileImageView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user") // Assuming you have a "user" collection
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String profileImageUrl = document.getString("profilePic");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // If the profile image is stored as a URL, load it using Glide
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.default_profile_image) // Placeholder while loading
                                    .into(profileImageView);
                        } else {
                            // If no URL is found, set a default image
                            profileImageView.setImageResource(R.drawable.default_profile_image);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user profile", e);
                    profileImageView.setImageResource(R.drawable.default_profile_image); // Fallback to default image
                });
    }



}