package com.example.blog;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class FireStoreHelper {
    private FirebaseFirestore db;

    public FireStoreHelper() {
        db = FirebaseFirestore.getInstance();
    }


    public void readBlogDataFromCollection(String collection, Consumer<Task<QuerySnapshot>> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            db.collection(collection)
                    .get()
                    .addOnCompleteListener(callback::accept)
                    .addOnFailureListener(e -> Log.e("Firestore", "Error reading data from collection", e));
        }
    }

    public void readBlogData() {
        db.collection("BinBuddy")
                .document("blog")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("Firestore", "Document data: " + document.getData());
                        } else {
                            Log.d("Firestore", "No such document!");
                        }
                    } else {
                        Log.w("Firestore", "Error getting document.", task.getException());
                    }
                });
    }

    public void addBlogData(Context context, String content, int likeCount,int commentCount, String base64Image, FieldValue timeStamp, String title, String userID) {

        // Create a new blog object with title and description
        Map<String, Object> blogData = new HashMap<>();
        blogData.put("content", content);
        blogData.put("likeCount", likeCount);
        blogData.put("commentCount",commentCount);
        blogData.put("postedDate",timeStamp);
        blogData.put("title",title);
        blogData.put("userID",userID);

        // Add Base64 image string only if it exists
        if (base64Image != null && !base64Image.isEmpty()) {
            blogData.put("picture", base64Image);
        }

        // Add a new document with the blog data to the 'blog' collection
        db.collection("blog")
                .add(blogData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Blog posted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to post blog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public void updateLikeCount(String contextID, int newLikeCount, Consumer<Boolean> callback) {
        db.collection("blog").document(contextID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            document.getReference().update("likeCount", newLikeCount)
                                    .addOnSuccessListener(aVoid -> callback.accept(true))
                                    .addOnFailureListener(e -> callback.accept(false));
                        }
                    } else {
                        Log.e("Firestore", "Document not found for contextID: " + contextID);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            callback.accept(false);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching document for update", e);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        callback.accept(false);
                    }
                });
    }


    public void updateCommentCount(String contextD, Consumer<Boolean> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            db.collection("blog").document(contextD)
                    .update("commentCount", FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> callback.accept(true))
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Failed to increment commentCount", e);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            callback.accept(false);
                        }
                    });
        }
    }

    public void addCommentData(String contextID,String contextText, String contextType,String userID) {

        if (contextID == null || contextText == null || contextType == null || userID == null) {
            Log.e("Firestore", "Invalid input: Null parameters in addCommentData");
            return;
        }

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("contextID", contextID);
        commentData.put("contextText",contextText);
        commentData.put("contextType",contextType);
        commentData.put("userID",userID);

        db.collection("comment")
                .add(commentData)  // Automatically generates a unique document ID
                .addOnSuccessListener(documentReference -> {
                    updateCommentCount(contextID, success -> {
                        if (success) {
                            Log.d("Firestore", "Comment added successfully!");
                        } else {
                            Log.e("Firestore", "Failed to update comment count.");
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to add comment", e));
    }

    public void fetchComments(String contextID, FirestoreCallback<List<Comment>> callback) {
        db.collection("comment")
                .whereEqualTo("contextID", contextID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comment.setUsername(doc.getString("userID")); // Fetch username
                            comment.setText(doc.getString("contextText")); // Fetch text
                            comments.add(comment);
                        }
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchBlogDetails(String contextID, Consumer<Map<String, Object>> callback) {
        db.collection("blog").document(contextID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            callback.accept(document.getData());
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            callback.accept(null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch blog details", e);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        callback.accept(null);
                    }
                });
    }

    public void getBlogByDocumentID(String documentID, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("blog").document(documentID).get().addOnCompleteListener(listener);
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

}

