package com.example.xiti_nganjuk_v2.models;

import com.google.gson.annotations.SerializedName;

public class UploadResultModel {
    @SerializedName("status")
    private String success;
    @SerializedName("file_name")
    private String fileName;

    public UploadResultModel(String success, String fileName) {
        this.success = success;
        this.fileName = fileName;
    }

    public String getSuccess() {
        return success;
    }

    public String getFileName() {
        return fileName;
    }
}
