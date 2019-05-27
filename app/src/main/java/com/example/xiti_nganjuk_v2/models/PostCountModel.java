package com.example.xiti_nganjuk_v2.models;

import com.google.gson.annotations.SerializedName;

public class PostCountModel {
    @SerializedName("progress")
    private String progress;
    @SerializedName("jumlah")
    private String jumlah;

    public PostCountModel(String progress, String jumlah) {
        this.progress = progress;
        this.jumlah = jumlah;
    }

    public String getProgress() {
        return progress;
    }

    public String getJumlah() {
        return jumlah;
    }
}
