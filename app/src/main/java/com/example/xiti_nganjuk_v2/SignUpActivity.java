package com.example.xiti_nganjuk_v2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import com.example.xiti_nganjuk_v2.interfaces.SaveUserInterface;
import com.example.xiti_nganjuk_v2.models.SaveUserDataResultModel;
import com.example.xiti_nganjuk_v2.models.UploadResultModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {


    //Variables
    int Year, Month, Day;
    FirebaseAuth auth;
    String szCurrentPhotoPath;
    String fileName;
    String UploadedPath;
    Uri photoPath;
    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_GALERY = 2;
    Calendar myCalendar;

    // Components
    EditText txtFName, txtLName, txtBirthDate, txtEmail, txtPassword, txtConfPassword;
    RadioButton rbMale, rbFemale;
    ImageView imgCalendarPic;
    Button btnRegister;
    TextView lblLogin;
    CircleImageView imgPhotoProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Membuat Instans Dari Firebase Auth
        auth = FirebaseAuth.getInstance();
        //Membuat Instans Dari Calendar
        myCalendar = Calendar.getInstance();
        //Menginisilisasi Component
        InitComponents();
        //Menginisialisasi Method Untuk Setiap Component
        InitComponentsMethod();
    }

    //Fungsi Untuk Menginisialisasi Component
    private void InitComponents(){
        txtFName = findViewById(R.id.txtFirstName);
        txtLName = findViewById(R.id.txtLastName);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtConfPassword = findViewById(R.id.txtConfPassReg);
        txtPassword = findViewById(R.id.txtPasswordReg);
        txtEmail = findViewById(R.id.txtEmailReg);

        rbFemale = findViewById(R.id.rbFemale);
        rbMale = findViewById(R.id.rbMale);

        imgCalendarPic = findViewById(R.id.imgCalendar);
        imgPhotoProfile = findViewById(R.id.imgProfile);

        lblLogin = findViewById(R.id.txtLogin);

        btnRegister = findViewById(R.id.btnRegister);
    }

    //Fungsi Untuk Menginisialiasi Method Untuk Setiap Component
    private void InitComponentsMethod(){
        lblLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                finish();
            }
        });

        imgCalendarPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get Current Date
                DatePickerDialog.OnDateSetListener picker = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR,year);
                        myCalendar.set(Calendar.MONTH,month);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        UpdateLabel();
                    }
                };

                new DatePickerDialog(SignUpActivity.this,picker,myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ValidateUserInput()){
                    if(photoPath != null){
                        //Dijalankan jika ada foto profil yang ingin di upload
                        UploadPhotoProfile();
                    }else{
                        RegisterNewAuth();
                    }
                }
            }
        });

        imgPhotoProfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String[] item = new String[]{"Ambil Foto","Pilih Foto"};
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0 :
                                //Dijalankan jika user memilih untuk mengambil foto menggunakan kamera
                                ShowCamera();
                                break;

                            case 1 :
                                //Dijalankan jika user memilih untuk memilih foto dari gallery
                                PickImage();
                                break;
                        }
                    }
                }).show();
            }
        });
    }

    private void UpdateLabel() {
        String Format = "dd - MM - yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(Format);
        txtBirthDate.setText(sdf.format(myCalendar.getTime()));
    }

    //Fungsi untuk validasi input dari user
    private boolean ValidateUserInput(){
        boolean valid = true;
        if(txtFName.getText().toString().isEmpty()){
            txtFName.setError("Please Fill This Field!");
            valid = false;
        }

        if(txtLName.getText().toString().isEmpty()){
            txtLName.setError("Please Fill This Field!");
            valid = false;
        }

        if(txtBirthDate.getText().toString().isEmpty()){
            txtBirthDate.setError("Please Fill This Field!");
            valid = false;
        }

        if(txtEmail.getText().toString().isEmpty()){
            txtEmail.setError("Please Fill This Field!");
            valid = false;
        }

        if(txtPassword.getText().toString().isEmpty()){
            txtPassword.setError("Please Fill This Field!");
            valid = false;
        }

        if(txtConfPassword.getText().toString().isEmpty()){
            txtConfPassword.setError("Please Fill This Field!");
            valid = false;
        }

        if(!txtPassword.getText().toString().equals(txtConfPassword.getText().toString())){
            txtConfPassword.setError("Password and Confirmation Password Should Be The Same!");
            valid = false;
        }

        return  valid;
    }

    //Untuk Melakukan Registrasi Account ke firebase Auth
    private void RegisterNewAuth(){
        //Mengambil nilai dari username dan password yang diketikkan user
        String Email = txtEmail.getText().toString();
        String Password = txtPassword.getText().toString();
        //Membuat Instans Progress Dialog, mengatur message, dan menampilkan
        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("Registering Account, Please Wait...");
        progressDialog.show();

        //Melakukan registerasi user ke firebase menggunakan email dan password
        auth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    RegisterAccData();
                }else {
                    Toast.makeText(SignUpActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void RegisterAccData(){
        final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setMessage("Menyimpan Data User....");
        dialog.show();
        String UserId = auth.getCurrentUser().getUid();
        String FirstName = txtFName.getText().toString();
        String LastName = txtLName.getText().toString();
        String Gender = rbFemale.isChecked() == true ? "Female" : "Male";
        String ProfilePic = photoPath == null ? "" : UploadedPath;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateBirth = sdf.format(myCalendar.getTime());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SaveUserInterface saveUserInterface = retrofit.create(SaveUserInterface.class);
        Call<SaveUserDataResultModel> call = saveUserInterface.saveUserData(UserId,dateBirth.toString(),FirstName,LastName,ProfilePic,Gender);
        call.enqueue(new Callback<SaveUserDataResultModel>() {
            @Override
            public void onResponse(Call<SaveUserDataResultModel> call, Response<SaveUserDataResultModel> response) {
                SaveUserDataResultModel model = response.body();
                if(model.getStatus().equals("Berhasil Meyimpan Data!")){
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Registrasi Berhasil, Silahkan Login!", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Registrasi Gagal!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SaveUserDataResultModel> call, Throwable t) {
                Log.e("SignUpActivity", "onFailure: "+t.toString());
                Toast.makeText(SignUpActivity.this, "Registrasi Gagal!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Function untuk menghandle fitur pilih gambar dari gallery
    private void PickImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),REQUEST_GALERY);
    }

    //Function untuk menghandle fitur untuk mengambil gambar melalui kamera
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ShowCamera(){
        if(ContextCompat.checkSelfPermission(SignUpActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},1);
        }else{
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(camIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException ex){
                    Log.i("Status ", "ShowCamera: "+ex.toString());
                }

                if(photoFile != null){
                    Uri photoUri = FileProvider.getUriForFile(SignUpActivity.this,
                            "com.paksitamtomoaji.xitinganjuk.fileprovider",
                            photoFile);


                    camIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                    startActivityForResult(camIntent, REQUEST_CAMERA);
                }
            }
        }
    }

    //Function untuk menghandle image
    private File createImageFile() throws IOException {
        String TimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String ImageFileName = "Photo_Profile_"+TimeStamp;
        File StorageDir = SignUpActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File Image = File.createTempFile(
                ImageFileName,
                ".jpg",
                StorageDir
        );

        szCurrentPhotoPath = Image.getAbsolutePath();
        fileName = ImageFileName;
        return Image;
    }

    //Function untuk menghandle activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
            photoPath = Uri.fromFile(new File(szCurrentPhotoPath));
            Glide.with(SignUpActivity.this).load(Uri.fromFile(new File(szCurrentPhotoPath)))
                    .into(imgPhotoProfile);
        }else if(requestCode == REQUEST_GALERY && resultCode == RESULT_OK){
            Uri file = data.getData();
            photoPath = file;
            Glide.with(SignUpActivity.this).load(file)
                    .into(imgPhotoProfile);
        }
    }

    //Function untuk mengupload photo ke firebase
    private void UploadPhotoProfile(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setMessage("Mengupload Foto Profil...");
        dialog.show();

        File imageFile = new File(getRealPathFromURIPath(photoPath,SignUpActivity.this));
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"),imageFile);
        MultipartBody.Part iImage = MultipartBody.Part.createFormData("image",imageFile.getName(),imageBody);

        ImageUploadInterface imageUploadInterface = retrofit.create(ImageUploadInterface.class);
        Call<UploadResultModel> call = imageUploadInterface.uploadImageToServer(iImage);
        call.enqueue(new Callback<UploadResultModel>() {
            @Override
            public void onResponse(Call<UploadResultModel> call, Response<UploadResultModel> response) {
                UploadResultModel model = response.body();
                if(model.getSuccess().equals("Berhasil")){
                    UploadedPath = model.getFileName();
                    dialog.dismiss();
                    RegisterNewAuth();

                }else{
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Foto Profile Gagal Diupload!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResultModel> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Foto Profile Gagal Diupload!", Toast.LENGTH_SHORT).show();
                Log.e("SignUpActivity", "onFailure: "+t.toString() );
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
}
