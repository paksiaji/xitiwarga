package com.example.xiti_nganjuk_v2;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.ImageUploadInterface;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.interfaces.SaveUserInterface;
import com.example.xiti_nganjuk_v2.models.SaveUserDataResultModel;
import com.example.xiti_nganjuk_v2.models.UploadResultModel;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileActivity extends AppCompatActivity {
    CircleImageView imgProfilePic;
    TextView txtGantiFoto;
    EditText txtFirstName, txtLastName, txtBirthDate;
    RadioButton rbMale, rbFemale;
    String szCurrentPhotoPath;
    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_GALERY = 2;
    Button btnUbah, btnCancel;
    FirebaseAuth auth;
    Calendar myCalendar;
    ImageView imgCalendar;
    String CurrentPhotoUrl;
    Uri newPhotoPath;
    boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        txtGantiFoto = findViewById(R.id.txtGantiFoto);
        btnUbah = findViewById(R.id.btnUbahProfile);
        btnCancel = findViewById(R.id.btnCancel);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtBirthDate = findViewById(R.id.txtDateOfBirth);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        imgCalendar = findViewById(R.id.imgCalendar);
        myCalendar = Calendar.getInstance();

        btnUbah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener picker = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR,year);
                        myCalendar.set(Calendar.MONTH,month);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        UpdateLabel();
                    }
                };

                new DatePickerDialog(EditProfileActivity.this,picker,myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        txtGantiFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = {"Tangkap Gambar","Pilih Dari Galeri"};
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                ShowCamera();
                                break;
                            case 1:
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent,"Select Picture"),REQUEST_GALERY);
                                break;
                        }
                    }
                }).show();
            }
        });
        LoadCurrenProfile();
    }

    private void UpdateLabel() {
        String Format = "dd - MM - yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(Format);
        txtBirthDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
            newPhotoPath = Uri.fromFile(new File(szCurrentPhotoPath));
            Glide.with(EditProfileActivity.this).load(Uri.fromFile(new File(szCurrentPhotoPath))).into(imgProfilePic);
            changed = true;
        }else if(requestCode == REQUEST_GALERY && resultCode == RESULT_OK){
            Uri uri = data.getData();
            newPhotoPath = uri;
            Glide.with(EditProfileActivity.this).load(uri).into(imgProfilePic);
            changed = true;
        }
    }

    private File CreateImageFile() throws IOException {
        String TimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String ImageFileName = "XITI - "+TimeStamp;
        File storageDir = EditProfileActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                ImageFileName,
                ".jpg",
                storageDir
        );

        szCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private  void ShowCamera(){
        if(ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(EditProfileActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(EditProfileActivity.this.getPackageManager()) != null){
                File photoFile = null;

                try{
                    photoFile = CreateImageFile();
                }catch (IOException ex){
                    Log.i("Status ", "ShowCamera: "+ex.toString());
                }

                if(photoFile != null){
                    Uri photoUri;
                    if(Build.VERSION_CODES.N <= Build.VERSION.SDK_INT){
                        photoUri = FileProvider.getUriForFile(EditProfileActivity.this,"com.example.xiti_nganjuk_v2.fileprovider", photoFile);
                    }else{
                        photoUri = Uri.fromFile(photoFile);
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                    startActivityForResult(takePictureIntent,REQUEST_CAMERA);
                }
            }
        }
    }

    private void LoadCurrenProfile(){
        final ProgressDialog dialog = new ProgressDialog(EditProfileActivity.this);
        dialog.setMessage("Memuat Data...");
        dialog.show();
        auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(auth.getCurrentUser().getUid());

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                dialog.dismiss();
                UserModel model = response.body();
                txtFirstName.setText(model.getFirstName());
                txtLastName.setText(model.getLastName());
                txtBirthDate.setText(model.getBirthDate());
                CurrentPhotoUrl = model.getProfilePic();
                if(model.getGender().equals("Male")){
                    rbMale.setChecked(true);
                }else{
                    rbFemale.setChecked(true);
                }

                if(!model.getProfilePic().equals("")){
                    Glide.with(EditProfileActivity.this).load("https://xiti.apps.bentang.id/Images/"+model.getProfilePic()).into(imgProfilePic);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                dialog.dismiss();
                Log.e("EditProfile", "onFailure: "+t.toString() );
            }
        });
    }

    private void SaveData(){
        auth = FirebaseAuth.getInstance();

        if(changed == true){
            uploadPhoto();
        }else{
            updateUserData("");
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

    private void uploadPhoto(){
        Uri file = newPhotoPath;
        final ProgressDialog dialog = new ProgressDialog(EditProfileActivity.this);
        dialog.setMessage("Mengupload Foto...");
        dialog.show();

        File imageFile = new File(getRealPathFromURIPath(file,EditProfileActivity.this));
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"),imageFile);
        MultipartBody.Part iFile = MultipartBody.Part.createFormData("image",imageFile.getName(),imageBody);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageUploadInterface imageUploadInterface = retrofit.create(ImageUploadInterface.class);
        Call<UploadResultModel> call = imageUploadInterface.uploadImageToServer(iFile);
        call.enqueue(new Callback<UploadResultModel>() {
            @Override
            public void onResponse(Call<UploadResultModel> call, Response<UploadResultModel> response) {
                dialog.dismiss();
                UploadResultModel uploadResultModel = response.body();
                if(uploadResultModel.getSuccess().equals("Berhasil")){
                    dialog.dismiss();
                    updateUserData(uploadResultModel.getFileName());
                }else{
                    dialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Gagal Mengupload Foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResultModel> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Gagal Mengupload Foto", Toast.LENGTH_SHORT).show();
                Log.e("EditProfileActivity", "onFailure: "+t.toString() );
            }
        });
    }

    private void updateUserData(String profilePic){
        final ProgressDialog dialog = new ProgressDialog(EditProfileActivity.this);
        dialog.setMessage("Meyimpan Perubahan...");
        dialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String firstName = txtFirstName.getText().toString();
        String lastName = txtLastName.getText().toString();
        String pic = profilePic != "" ? profilePic : CurrentPhotoUrl;
        String gender = rbMale.isChecked() == true ? "Male" : "Female";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateBirth = sdf.format(myCalendar.getTime());
        Log.e("EditProfileActivity", "updateUserData: "+dateBirth);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SaveUserInterface saveUserInterface = retrofit.create(SaveUserInterface.class);
        Call<SaveUserDataResultModel> call = saveUserInterface.updateUserData(
                auth.getCurrentUser().getUid(),
                dateBirth,
                firstName,
                lastName,
                pic,
                gender
        );

        call.enqueue(new Callback<SaveUserDataResultModel>() {
            @Override
            public void onResponse(Call<SaveUserDataResultModel> call, Response<SaveUserDataResultModel> response) {
                SaveUserDataResultModel model = response.body();
                if(model.getStatus().equals("Berhasil Mengubah Data!")){
                    dialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Berhasil Meyimpan Perubahan...", Toast.LENGTH_SHORT).show();
                    LoadCurrenProfile();
                }else{
                    dialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Gagal Meyimpan Perubahan...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SaveUserDataResultModel> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Gagal Menyimpan Perubahan...", Toast.LENGTH_SHORT).show();
                Log.e("EditProfileActivity", "onFailure: "+t.toString());
            }
        });
    }
}
