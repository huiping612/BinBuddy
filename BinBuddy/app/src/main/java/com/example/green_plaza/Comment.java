package com.example.green_plaza;

public class Comment {
    private String commentId;
    private String commentText;
    private String userName;
    private int likes;
    private String itemID;  // Assuming each comment is linked to an item

    // Default constructor
    public Comment() {
    }

    public Comment(String commentId, String commentText, String userName, int likes, String itemID) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.userName = userName;
        this.likes = likes;
        this.itemID = itemID;
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
}
