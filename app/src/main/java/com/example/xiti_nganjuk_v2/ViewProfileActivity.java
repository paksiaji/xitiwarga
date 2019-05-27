package com.example.xiti_nganjuk_v2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.adapters.Post_item_grid_adapter;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadPostInterface;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.models.PostCountModel;
import com.example.xiti_nganjuk_v2.models.Post_grid_model_class;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ViewProfileActivity extends AppCompatActivity {
    CircleImageView imgProfile;
    TextView txtWaitingCount, txtProcessCount, txtCompleteCount, txtProfileName;
    Button btnKirimPesan;
    FirebaseAuth auth;
    RecyclerView rvPost;
    Button btnSeeAll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        auth = FirebaseAuth.getInstance();
        imgProfile = findViewById(R.id.imgProfile);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtWaitingCount = findViewById(R.id.txtWaitingCount);
        txtProcessCount = findViewById(R.id.txtProcessCount);
        txtCompleteCount = findViewById(R.id.txtCompleteCount);
        btnKirimPesan = findViewById(R.id.btnKirimPesan);
        rvPost = findViewById(R.id.rvPost);
        btnSeeAll = findViewById(R.id.btnSeeAll);
        btnSeeAll.setEnabled(false);

        btnKirimPesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this,ChatRoomActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userId",getIntent().getStringExtra("userId"));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this,ViewAllPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userId",getIntent().getStringExtra("userId"));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        LoadUsername();
        LoadPostStatCount();
    }

    private void LoadUsername(){
        final ProgressDialog dialog = new ProgressDialog(ViewProfileActivity.this);
        dialog.setMessage("Loading User Profile...");
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(getIntent().getStringExtra("userId"));
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                dialog.dismiss();
                txtProfileName.setText(response.body().getFirstName()+" "+response.body().getLastName());
                if(!response.body().getProfilePic().equals("")){
                    Glide.with(ViewProfileActivity.this).load("https://xiti.apps.bentang.id/Images/"+response.body().getProfilePic()).into(imgProfile);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                dialog.dismiss();
                Log.e("ViewProfileActivity", "onFailure: "+t.toString());
            }
        });
    }

    private void LoadPostStatCount(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadPostInterface loadPostInterface = retrofit.create(LoadPostInterface.class);
        Call<List<PostCountModel>> call = loadPostInterface.loadPostCount(getIntent().getStringExtra("userId"));
        call.enqueue(new Callback<List<PostCountModel>>() {
            @Override
            public void onResponse(Call<List<PostCountModel>> call, Response<List<PostCountModel>> response) {
                for(int i = 0; i < response.body().size(); i++){
                    if(response.body().get(i).getProgress().equals("Pending")){
                        txtWaitingCount.setText(response.body().get(i).getJumlah());
                    }else if(response.body().get(i).getProgress().equals("Process")){
                        txtProcessCount.setText(response.body().get(i).getJumlah());
                    }else{
                        txtCompleteCount.setText(response.body().get(i).getJumlah());
                    }
                }
                loadData();
            }

            @Override
            public void onFailure(Call<List<PostCountModel>> call, Throwable t) {
                Log.e("ProfileFragment", "onFailure: "+t.toString() );
            }
        });
    }

    private void loadData(){
        final ProgressDialog dialog = new ProgressDialog(ViewProfileActivity.this);
        dialog.setMessage("Loading Post...");
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadPostInterface loadPostInterface = retrofit.create(LoadPostInterface.class);
        Call<List<Post_grid_model_class>> call = loadPostInterface.loadPostForGrid(getIntent().getStringExtra("userId"));
        call.enqueue(new Callback<List<Post_grid_model_class>>() {
            @Override
            public void onResponse(Call<List<Post_grid_model_class>> call, Response<List<Post_grid_model_class>> response) {
                dialog.dismiss();
                List<Post_grid_model_class> posts = response.body();
                List<Post_grid_model_class> listPost = new ArrayList<>();
                for(int i = 0; i < posts.size(); i++){
                    String userId = posts.get(i).getPostId();
                    String urlContent = posts.get(i).getUrlContent();
                    String progress = posts.get(i).getStatus();

                    Post_grid_model_class model = new Post_grid_model_class(userId,urlContent,progress);
                    listPost.add(model);
                }

                if((Integer.valueOf(txtCompleteCount.getText().toString())+(Integer.valueOf(txtWaitingCount.getText().toString()))+Integer.valueOf(txtProcessCount.getText().toString())) > 6){
                    btnSeeAll.setEnabled(true);
                }
                GridLayoutManager gridLayoutManager = new GridLayoutManager(ViewProfileActivity.this,3);

                Post_item_grid_adapter adapter = new Post_item_grid_adapter(listPost,ViewProfileActivity.this);
                int spanCount = 3; // 3 columns
                int spacing = 4; // 50px
                boolean includeEdge = false;
                rvPost.addItemDecoration(new RecyclerView_Decoration(spanCount,spacing,includeEdge));
                rvPost.setLayoutManager(gridLayoutManager);
                rvPost.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Post_grid_model_class>> call, Throwable t) {
                dialog.dismiss();
                Log.e("ViewProfileActivity", "onFailure: "+t.toString());
            }
        });
    }
}
