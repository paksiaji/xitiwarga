package com.example.xiti_nganjuk_v2.models;

import com.google.gson.annotations.SerializedName;

public class Post_grid_model_class {
    private String postId;
    private String urlContent;
    @SerializedName("progress")
    private String status;

    public Post_grid_model_class(String postId, String urlContent, String status) {
        this.postId = postId;
        this.urlContent = urlContent;
        this.status = status;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUrlContent() {
        return urlContent;
    }

    public void setUrlContent(String urlContent) {
        this.urlContent = urlContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
