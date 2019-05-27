package com.example.xiti_nganjuk_v2;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.example.xiti_nganjuk_v2.adapters.Post_item_grid_adapter;
import com.example.xiti_nganjuk_v2.models.Post_grid_model_class;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAllPostActivity extends AppCompatActivity {
    RecyclerView rvPost;
    ImageView imgBack;
    List<Post_grid_model_class> listPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_post);
        rvPost = findViewById(R.id.rvPost);
        imgBack = findViewById(R.id.imgBack);
        loadData();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadData(){
        String userId = getIntent().getStringExtra("userId");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("post").orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Post_grid_model_class> listPost = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String postId = snapshot.getKey();
                    String urlContent = snapshot.child("urlContent").getValue().toString();
                    String progress = snapshot.child("progress").getValue().toString();
                    Post_grid_model_class post = new Post_grid_model_class(postId,urlContent,progress);
                    listPost.add(post);
                }
                GridLayoutManager gridLayoutManager = new GridLayoutManager(ViewAllPostActivity.this,3);

                Post_item_grid_adapter adapter = new Post_item_grid_adapter(listPost,ViewAllPostActivity.this);
                int spanCount = 3; // 3 columns
                int spacing = 4; // 50px
                boolean includeEdge = false;
                rvPost.addItemDecoration(new RecyclerView_Decoration(spanCount,spacing,includeEdge));
                rvPost.setLayoutManager(gridLayoutManager);
                rvPost.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
