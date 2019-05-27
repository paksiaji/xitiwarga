package com.example.xiti_nganjuk_v2.models;

import com.google.gson.annotations.SerializedName;

public class Post_model_class {
    private String postId;
    private String userId;
    private String title;
    private String description;
    @SerializedName("typePost")
    private String type;
    private String category;
    private String progress;
    private String timePost;
    private double longitude;
    private double latitude;
    private String urlContent;

    public Post_model_class(){

    }

    public Post_model_class(String postId, String userId, String title, String description, String type, String category, String progress, String timePost, double longitude, double latitude, String urlContent) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.progress = progress;
        this.timePost = timePost;
        this.longitude = longitude;
        this.latitude = latitude;
        this.urlContent = urlContent;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getTimePost() {
        return timePost;
    }

    public void setTimePost(String timePost) {
        this.timePost = timePost;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUrlContent() {
        return urlContent;
    }

    public void setUrlContent(String urlContent) {
        this.urlContent = urlContent;
    }
}
