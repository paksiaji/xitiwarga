package com.example.xiti_nganjuk_v2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    //Variables
    EditText txtEmail, txtPass;
    TextView lblRegister;
    Button btnLogin;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        //Instansiasi Component
        initComponents();
        //Instansiasi Method Untuk Component
        initComponentsMethod();
        //Membuat instans dari firebase auth
        auth = FirebaseAuth.getInstance();

        //Mengecek, apakah user sudah pernah login atau belum, jika sudah akan langsung diarahkan ke main menu
        if(auth.getCurrentUser() != null){
            FirebaseMessaging.getInstance().subscribeToTopic(auth.getCurrentUser().getUid());
            startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
            finish();
        }

    }

    //Function instansiasi component
    private void initComponents(){
        txtEmail = findViewById(R.id.txtEmail);
        txtPass = findViewById(R.id.txtPassword);
        lblRegister = findViewById(R.id.txtSignUp);
        btnLogin = findViewById(R.id.btnLogin);
    }

    //Function untuk instansiasi method ke component
    private void initComponentsMethod(){
        lblRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = txtEmail.getText().toString();
                String Password = txtPass.getText().toString();
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Logging You In, Please Wait...");
                dialog.setCancelable(false);
                dialog.show();
                auth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Successfully Login!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
                            finish();
                        }else{
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed To Login!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("failure", "onFailure: "+e.toString());
                    }
                });
            }
        });
    }
}
