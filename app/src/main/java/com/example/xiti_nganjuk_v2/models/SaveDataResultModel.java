package com.example.xiti_nganjuk_v2.models;

import com.google.gson.annotations.SerializedName;

public class SaveDataResultModel {
    @SerializedName("status")
    private String status;

    public SaveDataResultModel(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
