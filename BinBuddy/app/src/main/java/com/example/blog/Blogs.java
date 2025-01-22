package com.example.blog;

public class Blogs {

    private String username;
    private String title;
    private String description;
    private String imageBase64;
    private String contextID;

    public Blogs(){}

    public Blogs(String username,String title, String description, String imageBase64, String contextID) {
        this.username = username;
        this.title = title;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.contextID = contextID;
    }

    public String getUsername(){
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageBase64(){ return  imageBase64;}

    public String getContextID(){return contextID;}

}
