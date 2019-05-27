package com.example.xiti_nganjuk_v2;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPreviewActivity extends AppCompatActivity {
    VideoView vView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_preview);
        String videoLocation = getIntent().getStringExtra("FileLocation");
        MediaController mediaController = new MediaController(this);

        vView = findViewById(R.id.videoView);
        vView.setVideoURI(Uri.parse(videoLocation));
        vView.setMediaController(mediaController);
        mediaController.setMediaPlayer(vView);

        vView.start();
    }
}
