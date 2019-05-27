package com.example.xiti_nganjuk_v2.models;

public class Comment_model_class {
    private String commentId;
    private String commentText;
    private String userId;
    private String time;

    public Comment_model_class(){

    }

    public Comment_model_class(String commentId, String commentText, String userId, String time) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.userId = userId;
        this.time = time;
    }


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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
