package com.example.xiti_nganjuk_v2.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xiti_nganjuk_v2.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AddFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    ImageView imgKdrt, imgPohonTumbang, imgJalanRusak, imgBencana, imgKriminal, imgKebakaran;
    TextView txtKdrt, txtPohonTumbang, txtJalanRusak, txtBencana, txtKriminal, txtKebakaran;
    String type, fileName, szCurrentPhotoPath;

    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_VIDEO = 2;

    public AddFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
        initComponentMethod();
    }

    private void initComponent(View v){
        imgKdrt = v.findViewById(R.id.imgKdrt);
        imgBencana = v.findViewById(R.id.imgBencanaAlam);
        imgPohonTumbang = v.findViewById(R.id.imgPohonTumbang);
        imgJalanRusak = v.findViewById(R.id.imgJalanRusak);
        imgBencana = v.findViewById(R.id.imgBencanaAlam);
        imgKriminal = v.findViewById(R.id.imgKriminalitas);
        imgKebakaran = v.findViewById(R.id.imgKebakaran);

        txtKdrt = v.findViewById(R.id.txtKdrt);
        txtBencana = v.findViewById(R.id.txtBencanaAlam);
        txtPohonTumbang = v.findViewById(R.id.txtPohonTumbang);
        txtJalanRusak = v.findViewById(R.id.txtJalanRusak);
        txtBencana = v.findViewById(R.id.txtBencanaAlam);
        txtKriminal = v.findViewById(R.id.txtKriminalitas);
        txtKebakaran = v.findViewById(R.id.txtKebakaran);
    }

    private void initComponentMethod(){
        imgKdrt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "kdrt";
                posting();
            }
        });

        txtKdrt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "kdrt";
                posting();
            }
        });

        imgBencana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "bencana";
                posting();
            }
        });

        txtBencana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "bencana";
                posting();
            }
        });

        imgPohonTumbang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "pohon tumbang";
                posting();
            }
        });

        txtPohonTumbang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "pohon tumbang";
                posting();
            }
        });

        imgJalanRusak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "jalan rusak";
                posting();
            }
        });

        txtJalanRusak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "jalan rusak";
                posting();
            }
        });

        imgKriminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "kriminal";
                posting();
            }
        });

        txtKriminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "kriminal";
                posting();
            }
        });

        imgKebakaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "kebakaran";
                posting();
            }
        });

        txtKebakaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "kebakaran";
                posting();
            }
        });
    }

    private void posting(){
        //Check if user GPS is Enabled Or Not
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle("GPS Tidak Aktif");
            builder.setMessage("GPS Kamu Tidak Aktif, Apakah Kamu Ingin Mengaktifkannya?");
            builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }else{
            String[] items ={"Foto","Video"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0 :
                            showCameraIntent();
                            break;
                        case 1 :
                            showVideoIntent();
                            break;
                    }
                }
            }).show();
        }
    }

    private void ReqPermission(){
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void showCameraIntent(){
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ReqPermission();
        }else{
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(cameraIntent.resolveActivity(getActivity().getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException ex){
                    Log.i("Status","ShowCamera : "+ex.toString());
                }

                if(photoFile != null){
                    Uri photoUri;
                    if(Build.VERSION_CODES.N <= Build.VERSION.SDK_INT){
                        photoUri = FileProvider.getUriForFile(getActivity(),"com.example.xiti_nganjuk_v2.fileprovider", photoFile);
                    }else{
                        photoUri = Uri.fromFile(photoFile);
                    }

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                    startActivityForResult(cameraIntent,REQUEST_CAMERA);
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
            Bundle bundle = new Bundle();
            bundle.putString("filePath",szCurrentPhotoPath);
            bundle.putString("category",type);
            ImagePostFragment imagePostFragment = new ImagePostFragment();
            imagePostFragment.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.flContainer, imagePostFragment);
            transaction.commit();
        }else if(requestCode == REQUEST_VIDEO && resultCode == RESULT_OK){
            Bundle bundle = new Bundle();
            bundle.putString("filePath",data.getData().toString());
            bundle.putString("category",type);
            VideoPostFragment videoPostFragment = new VideoPostFragment();
            videoPostFragment.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.flContainer, videoPostFragment);
            transaction.commit();
        }
    }

    private void showVideoIntent(){
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ReqPermission();
        }else{
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if(videoIntent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivityForResult(videoIntent,REQUEST_VIDEO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String TimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String ImageFileName = "Post_Content_"+TimeStamp;
        File StorageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File Image = File.createTempFile(
                ImageFileName,
                ".jpg",
                StorageDir
        );

        szCurrentPhotoPath = Image.getAbsolutePath();
        fileName = ImageFileName;
        return Image;
    }
}
