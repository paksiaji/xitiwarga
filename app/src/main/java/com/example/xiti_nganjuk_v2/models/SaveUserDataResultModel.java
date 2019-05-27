package com.example.xiti_nganjuk_v2.models;

import com.google.gson.annotations.SerializedName;

public class SaveUserDataResultModel {
    @SerializedName("status")
    private String status;

    public SaveUserDataResultModel(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
