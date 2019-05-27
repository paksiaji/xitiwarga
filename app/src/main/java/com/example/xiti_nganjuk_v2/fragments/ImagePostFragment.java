package com.example.xiti_nganjuk_v2.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.ImageUploadInterface;
import com.example.xiti_nganjuk_v2.interfaces.SaveDataInterface;
import com.example.xiti_nganjuk_v2.interfaces.VideoUploadInterface;
import com.example.xiti_nganjuk_v2.models.SaveDataResultModel;
import com.example.xiti_nganjuk_v2.models.UploadResultModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImagePostFragment extends Fragment implements LocationListener {

    private OnFragmentInteractionListener mListener;

    Button btnPost;
    ImageView imgPost;
    String szPhotoPath;
    DatabaseReference ref;
    FirebaseAuth auth;
    EditText etPostTitle, etPostDescription;

    LocationManager locationManager;
    double Latitude = 0;
    double Longitude = 0;
    ProgressDialog builder;

    public ImagePostFragment() {
    }

    public static ImagePostFragment newInstance(String param1, String param2) {
        ImagePostFragment fragment = new ImagePostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_post, container, false);
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

    @Override
    public void onLocationChanged(Location location) {
        builder.dismiss();
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        GetLocation();
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
        initComponentMethod();
        String FileDir = getArguments().getString("filePath");
        Uri photoUri = Uri.fromFile(new File(FileDir));
        Glide.with(getActivity()).load(photoUri).into(imgPost);
    }

    private void initComponent(View v){
        etPostTitle = v.findViewById(R.id.etTitle);
        etPostDescription = v.findViewById(R.id.etDescription);
        imgPost = v.findViewById(R.id.imgPost);
        btnPost = v.findViewById(R.id.btnPostImage);
    }

    private void initComponentMethod(){
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });
    }

    private void uploadPhoto() {
        if(!etPostTitle.getText().toString().isEmpty()){
            if(!etPostDescription.getText().toString().isEmpty()){
                String FileDir = getArguments().getString("filePath");
                Uri photoUri = Uri.fromFile(new File(FileDir));
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Mengupload Gambar, Please Wait...");
                dialog.show();

                File imageFile = new File(getRealPathFromURIPath(photoUri,getActivity()));
                RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"),imageFile);
                MultipartBody.Part iFile = MultipartBody.Part.createFormData("image",imageFile.getName(),imageBody);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ImageUploadInterface imageUploadInterface = retrofit.create(ImageUploadInterface.class);
                Call<UploadResultModel> serverCom = imageUploadInterface.uploadImageToServer(iFile);
                serverCom.enqueue(new Callback<UploadResultModel>() {
                    @Override
                    public void onResponse(Call<UploadResultModel> call, Response<UploadResultModel> response) {
                        dialog.dismiss();
                        UploadResultModel model = response.body();
                        Toast.makeText(getActivity(),"Berhasil Upload Gambar",Toast.LENGTH_SHORT);
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
                etPostDescription.setError("Deskripsi Laporan Harus Diisi");
            }
        }else{
            etPostTitle.setError("Judul Laporan Harus Diisi");
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
        String description = etPostDescription.getText().toString();
        String latitude = String.valueOf(Latitude);
        String longitude = String.valueOf(Longitude);
        String progress = "Pending";
        String typePost = "Gambar";
        String timePost = String.valueOf(date.getTime() / 1000);
        String title = etPostTitle.getText().toString();
        String urlContent = "https://xiti.apps.bentang.id/Images/"+fileName;
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

    void GetLocation() {
        try {
            builder = new ProgressDialog(getActivity());
            builder.setMessage("Sedang Mendapatkan Lokasi Saat Ini...");
            builder.setCancelable(false);
            builder.show();
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, this);
        }catch (SecurityException ex){
            ex.printStackTrace();
        }
    }
}
