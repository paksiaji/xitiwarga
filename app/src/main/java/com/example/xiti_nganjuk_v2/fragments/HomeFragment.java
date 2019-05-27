package com.example.xiti_nganjuk_v2.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.xiti_nganjuk_v2.ChatListActivity;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadPostInterface;
import com.example.xiti_nganjuk_v2.models.Post_model_class;
import com.example.xiti_nganjuk_v2.R;
import com.example.xiti_nganjuk_v2.adapters.Post_item_adapter;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements LocationListener {
    private OnFragmentInteractionListener mListener;

    private List<Post_model_class> listPost = new ArrayList<>();
//    private Post_item_adapter adapter;
//    private Parcelable recyclerviewState;
//    static String username = "";
//    String lastItemId = "";
    RecyclerView rv;
    SwipeRefreshLayout rLayout;
    ImageView imgMessageIcon;
    LocationManager locationManager;
    double Longitude = 0.0;
    double Latitude = 0.0;
    ProgressDialog dialog;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgMessageIcon = view.findViewById(R.id.imgPesan);
        imgMessageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChatListActivity.class);
                startActivity(intent);
            }
        });
        rv = view.findViewById(R.id.rvPost);
        rLayout = view.findViewById(R.id.rLayout);
        rLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadPost();
            }
        });
        if(Longitude != 0.0 && Latitude != 0.0){
            LoadPost();
        }else{
            GetLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(Longitude == 0.0 && Latitude == 0.0){
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();

            Log.d("a", "onLocationChanged: "+location.getLongitude()+" "+Longitude);

            LoadPost();
            Log.d("PostLoad","ulala");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void LoadPost(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadPostInterface loadPostInterface = retrofit.create(LoadPostInterface.class);
        Call<List<Post_model_class>> call = loadPostInterface.loadPostHome(String.valueOf(Latitude),String.valueOf(Longitude));
        call.enqueue(new Callback<List<Post_model_class>>() {
            @Override
            public void onResponse(Call<List<Post_model_class>> call, Response<List<Post_model_class>> response) {
                List<Post_model_class> posts = response.body();
                listPost.clear();
                for(int i = 0; i < posts.size();i++){
                    String postId = posts.get(i).getPostId();
                    String userId = posts.get(i).getUserId();
                    String title = posts.get(i).getTitle();
                    String description = posts.get(i).getDescription();
                    String type = posts.get(i).getType();
                    String urlContent = posts.get(i).getUrlContent();
                    String category = posts.get(i).getCategory();
                    String progress = posts.get(i).getProgress();
                    String timePost = posts.get(i).getTimePost();
                    String longitude = String.valueOf(posts.get(i).getLongitude());
                    String latitude = String.valueOf(posts.get(i).getLatitude());
                    Post_model_class model = new Post_model_class(postId,userId,title,description,type,category,progress,timePost,Double.valueOf(longitude),Double.valueOf(latitude),urlContent);
                    listPost.add(model);
                }

                Post_item_adapter adapter = new Post_item_adapter(listPost,getActivity());
                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                rv.setLayoutManager(manager);
                rv.setAdapter(adapter);
                rLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Post_model_class>> call, Throwable t) {
            }
        });
    }

    void GetLocation(){
        try {
            rLayout.setRefreshing(true);
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1,this);
        }catch (SecurityException ex){
            ex.printStackTrace();
        }
    }
}
