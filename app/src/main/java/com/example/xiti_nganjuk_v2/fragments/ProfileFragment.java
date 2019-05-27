package com.example.xiti_nganjuk_v2.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.EditProfileActivity;
import com.example.xiti_nganjuk_v2.MainActivity;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadPostInterface;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.models.PostCountModel;
import com.example.xiti_nganjuk_v2.models.Post_grid_model_class;
import com.example.xiti_nganjuk_v2.R;
import com.example.xiti_nganjuk_v2.RecyclerView_Decoration;
import com.example.xiti_nganjuk_v2.ViewAllPostActivity;
import com.example.xiti_nganjuk_v2.adapters.Post_item_grid_adapter;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    CircleImageView imgProfile;
    TextView txtWaitingCount, txtProcessCount, txtCompleteCount, txtProfileName;
    RecyclerView rvPost;
    Button btnSeeAll;
    ProgressDialog dialog;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void initComponent(View v){
        imgProfile = v.findViewById(R.id.imgProfile);
        txtProfileName = v.findViewById(R.id.txtProfileName);
        txtWaitingCount = v.findViewById(R.id.txtWaitingCount);
        txtProcessCount = v.findViewById(R.id.txtProcessCount);
        txtCompleteCount = v.findViewById(R.id.txtCompleteCount);
        rvPost = v.findViewById(R.id.rvListPostProfile);
        btnSeeAll = v.findViewById(R.id.btnSeeAll);
        btnSeeAll.setEnabled(false);
        //btnSeeAll.setClickable(false);
    }

    private void initComponentMethod(){
        btnSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                Intent intent = new Intent(getActivity(), ViewAllPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userId",auth.getCurrentUser().getUid());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar5);
        toolbar.inflateMenu(R.menu.profile_menu);
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading Profile...");
        dialog.show();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.mnEditProfile){
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Apakah anda yakin ingin logout?");
                    builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            auth.signOut();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                return true;
            }
        });
        initComponent(view);
        initComponentMethod();
        LoadUsername();


    }

    private void LoadUsername(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(auth.getCurrentUser().getUid());
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel model = response.body();
                if(!response.body().getProfilePic().equals("")){
                    Glide.with(getActivity()).load("https://xiti.apps.bentang.id/Images/"+model.getProfilePic()).into(imgProfile);
                }
                txtProfileName.setText(model.getFirstName()+" "+model.getLastName());
                LoadPostStatCount();
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("ProfileFragment", "onFailure: "+t.toString() );
                dialog.dismiss();
            }
        });
    }

    private void LoadPostStatCount(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadPostInterface loadPostInterface = retrofit.create(LoadPostInterface.class);
        Call<List<PostCountModel>> call = loadPostInterface.loadPostCount(auth.getCurrentUser().getUid());
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
                loadPost();
            }

            @Override
            public void onFailure(Call<List<PostCountModel>> call, Throwable t) {
                dialog.dismiss();
                Log.e("ProfileFragment", "onFailure: "+t.toString() );
            }
        });
    }

    private void loadPost(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadPostInterface loadPostInterface = retrofit.create(LoadPostInterface.class);
        Call<List<Post_grid_model_class>> call = loadPostInterface.loadPostForGrid(auth.getCurrentUser().getUid());
        call.enqueue(new Callback<List<Post_grid_model_class>>() {
            @Override
            public void onResponse(Call<List<Post_grid_model_class>> call, Response<List<Post_grid_model_class>> response) {
                List<Post_grid_model_class> listPost = new ArrayList<>();
                for(int i = 0; i < response.body().size(); i++){
                    String postId = response.body().get(i).getPostId();
                    String urlContent = response.body().get(i).getUrlContent();
                    String progress = response.body().get(i).getStatus();

                    Post_grid_model_class post = new Post_grid_model_class(postId,urlContent,progress);
                    listPost.add(post);
                }

                if((Integer.valueOf(txtWaitingCount.getText().toString())+Integer.valueOf(txtProcessCount.getText().toString())+Integer.valueOf(txtCompleteCount.getText().toString())) > 6){
                    btnSeeAll.setVisibility(View.VISIBLE);
                    btnSeeAll.setEnabled(true);
                }
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);

                Post_item_grid_adapter adapter = new Post_item_grid_adapter(listPost,getActivity());
                int spanCount = 3; // 3 columns
                int spacing = 4; // 50px
                boolean includeEdge = false;
                rvPost.addItemDecoration(new RecyclerView_Decoration(spanCount,spacing,includeEdge));
                rvPost.setLayoutManager(gridLayoutManager);
                rvPost.setAdapter(adapter);
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<Post_grid_model_class>> call, Throwable t) {
                dialog.dismiss();
                Log.e("ProfileFragment", "onFailure: "+t.toString());
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.mnEditProfile){
            Toast.makeText(getActivity(), "unch", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
