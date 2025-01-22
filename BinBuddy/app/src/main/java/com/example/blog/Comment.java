package com.example.blog;

public class Comment {
    private String username;
    private String text;
    private String contextID;
    private String contextType;

    public Comment() {}
    public Comment(String username, String text, String contextID, String contextType) {
        this.username = username;
        this.text = text;
        this.contextID = contextID;
        this.contextType = contextType;

    }

    public String getUsername() {return username;}


    public String getText() {return text;}

    public String getContextID(){return contextID;}

    public String getContextType(){return contextType;}

    public void setContextID(String contextID) {this.contextID = contextID;}

    public void setUsername(String username) {this.username = username;}

    public void setText(String text) {this.text = text;}
}
