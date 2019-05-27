package com.example.xiti_nganjuk_v2.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.R;
import com.example.xiti_nganjuk_v2.VideoPreviewActivity;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.SaveDataInterface;
import com.example.xiti_nganjuk_v2.interfaces.VideoUploadInterface;
import com.example.xiti_nganjuk_v2.models.SaveDataResultModel;
import com.example.xiti_nganjuk_v2.models.UploadResultModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoPostFragment extends Fragment implements LocationListener {
    private OnFragmentInteractionListener mListener;

    Button btnPostVideo;
    FirebaseAuth auth;
    EditText etTitle;
    EditText etDescription;
    ImageView imgPlay;
    ImageView imgVideoThumbnail;
    Uri szCurrentVideoPath;
    ProgressDialog dialog;

    LocationManager locationManager;
    double Latitude = 0;
    double Longitude = 0;

    public VideoPostFragment() {
    }

    public static VideoPostFragment newInstance(String param1, String param2) {
        VideoPostFragment fragment = new VideoPostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_post, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
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
    public void onLocationChanged(Location location) {
        dialog.dismiss();
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();

        Log.d("a", "onLocationChanged: "+location.getLongitude()+" "+Longitude);
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

    private void initComponent(View v){
        btnPostVideo = v.findViewById(R.id.btnPostImage);
        etTitle = v.findViewById(R.id.etTitle);
        etDescription = v.findViewById(R.id.etDescription);
        imgPlay = v.findViewById(R.id.imgPlay);
        imgVideoThumbnail = v.findViewById(R.id.imgVideoThumbNail);
    }

    private void initComponentMethod(){
        btnPostVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });

        imgVideoThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), VideoPreviewActivity.class);
                intent.putExtra("FileLocation",szCurrentVideoPath.toString());
                startActivity(intent);
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), VideoPreviewActivity.class);
                intent.putExtra("FileLocation",szCurrentVideoPath.toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
        initComponentMethod();
        szCurrentVideoPath = Uri.parse(getArguments().getString("filePath"));

        Glide.with(getActivity().getApplicationContext()).load(szCurrentVideoPath).into(imgVideoThumbnail);
    }

    private void uploadVideo(){
        if(!etTitle.getText().toString().isEmpty()){
            if(!etDescription.getText().toString().isEmpty()){
                szCurrentVideoPath = Uri.parse(getArguments().getString("filePath"));
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Mengupload Video, Please Wait...");
                dialog.show();

                File videoFile = new File(getRealPathFromURIPath(szCurrentVideoPath,getActivity()));
                RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"),videoFile);
                MultipartBody.Part vFile = MultipartBody.Part.createFormData("video",videoFile.getName(),videoBody);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                VideoUploadInterface videoUploadInterface = retrofit.create(VideoUploadInterface.class);
                Call<UploadResultModel> serverCom = videoUploadInterface.uploadVideoToServer(vFile);
                serverCom.enqueue(new Callback<UploadResultModel>() {
                    @Override
                    public void onResponse(Call<UploadResultModel> call, Response<UploadResultModel> response) {
                        dialog.dismiss();
                        UploadResultModel model = response.body();
                        Toast.makeText(getActivity(),"Berhasil Upload Video",Toast.LENGTH_SHORT);
                        Log.d("VideoPostFragment", "onResponse : "+model.getSuccess());
                        saveData(model.getFileName());
                    }

                    @Override
                    public void onFailure(Call<UploadResultModel> call, Throwable t) {
                        dialog.dismiss();
                        Log.e("VideoPostFragment",t.toString());
                    }
                });
            }else{
                etDescription.setError("Deskripsi Laporan Harus Diisi");
            }
        }else{
            etTitle.setError("Judul Laporan Harus Diisi");
        }
    }

    private void saveData(String fileName){
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Menyimpan Data Ke Server....");
        dialog.show();

        Date date = new Date();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("post");
        String postId = ref.push().getKey();
        String category = getArguments().getString("category");
        String description = etDescription.getText().toString();
        String latitude = String.valueOf(Latitude);
        String longitude = String.valueOf(Longitude);
        String progress = "Pending";
        String typePost = "Video";
        String timePost = String.valueOf(date.getTime() / 1000);
        String title = etTitle.getText().toString();
        String urlContent = "https://xiti.apps.bentang.id/Videos/"+fileName;
        String userId = auth.getCurrentUser().getUid();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SaveDataInterface saveDataInterface = retrofit.create(SaveDataInterface.class);
        Call<SaveDataResultModel> call = saveDataInterface.saveDataToServer(
          postId,
          category,
          description,
          latitude,
          longitude,
          progress,
          timePost,
          title,
          typePost,
          urlContent,
          userId
        );

        call.enqueue(new Callback<SaveDataResultModel>() {
            @Override
            public void onResponse(Call<SaveDataResultModel> call, Response<SaveDataResultModel> response) {
                SaveDataResultModel model = response.body();
                dialog.dismiss();
                if(!model.getStatus().equals("Gagal Menyimpan Data!")){
                    Toast.makeText(getActivity(), "Berhasil Menyimpan Data!", Toast.LENGTH_SHORT).show();
                    HomeFragment homeFragment = new HomeFragment();
                    Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.home_menu));
                    if(f != null){
                        Log.d("MainMenuActivity","Already Added");
                        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(R.id.home_menu+"stack_item").replace(R.id.flContainer,f,String.valueOf(R.id.home_menu)).commit();
                    }else{
                        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(R.id.home_menu+"stack_item").replace(R.id.flContainer,new HomeFragment(),String.valueOf(R.id.home_menu)).commit();
                    }
                }else{
                    Toast.makeText(getActivity(), "Gagal Menyimpan Data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SaveDataResultModel> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "Gagal Menyimpan Data!", Toast.LENGTH_SHORT).show();
                Log.e("VideoPostFragment", "onFailure: "+t.toString() );
            }
        });
    }

    void GetLocation() {
        try {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Sedang Mendapatkan Lokasi Saat Ini...");
            dialog.setCancelable(false);
            dialog.show();
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, this);
        }catch (SecurityException ex){
            ex.printStackTrace();
        }
    }

    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
}
