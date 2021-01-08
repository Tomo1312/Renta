package com.example.partyrental.Login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.MainActivity;
import com.example.partyrental.Model.User;
import com.example.partyrental.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    TextView txt_email, txt_password;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    Button btn_register, btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();


//        if (firebaseUser != null) {
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//
//        }
    }

    private void initView() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        txt_email = findViewById(R.id.tvEmail);
        txt_password = findViewById(R.id.tvPassword);
        btn_login = findViewById(R.id.btnLogin);
        btn_register = findViewById(R.id.btnRegister);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                finish();
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(txt_email.getText().toString(), txt_password.getText().toString());
            }
        });
    }

    private void validate(@NonNull String userEmail, @NonNull String userPassword) {
        firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    checkEmailVerification();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void checkEmailVerification() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Boolean emailFlag = firebaseUser.isEmailVerified();
        if (emailFlag) {
            Paper.init(this);
            Paper.book().write(Constants.LOGGED_KEY, firebaseUser.getUid());
            Intent intent = new  Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(Constants.KEY_USER_ID, firebaseUser.getUid());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Verify your e-mail", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }
}