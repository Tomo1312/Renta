package com.example.partyrental.Login;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.partyrental.Adapter.MyViewPagerAdapter;
import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.MainActivity;
import com.example.partyrental.Model.User;
import com.example.partyrental.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class WelcomeActivity extends AppCompatActivity {

    LocalBroadcastManager localBroadcastManager;

    CollectionReference barberRef;

    NonSwipeViewPager viewPager;

    Button btn_previous_step, btn_next_step;

    View.OnClickListener goToLogin, goToPrevious;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    ProgressBar progressBar;

    String passwordTmp;
    AlertDialog loading;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int step = intent.getIntExtra(Constants.KEY_STEP, 0);
//            if (step == 1)
//                //Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
            if (step == 1) {
                Common.userPhone = intent.getStringExtra(Constants.KEY_PHONE_NUMBER);
            } else if (step == 3) {
                Common.currentUser = intent.getParcelableExtra(Constants.KEY_USER);
                passwordTmp = intent.getStringExtra(Constants.PASSWORD);
            }
            btn_next_step.setEnabled(true);
            setColorButton();
        }
    };


    private void setColorButton() {
        if (btn_next_step.isEnabled()) {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if (btn_previous_step.isEnabled()) {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Paper.init(this);
        String userId = Paper.book().read(Constants.LOGGED_KEY);
        if(TextUtils.isEmpty(userId)){
            setContentView(R.layout.activity_welcome);

            initView();


        }else{

            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.putExtra(Constants.KEY_USER_ID, userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initView() {
        loading = new SpotsDialog.Builder().setCancelable(false).setContext(WelcomeActivity.this).build();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        viewPager = findViewById(R.id.view_pager);

        btn_next_step = findViewById(R.id.next_step);
        btn_previous_step = findViewById(R.id.previous_step);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Constants.KEY_ENABLE_BUTTON_NEXT));

        setColorButton();

        goToLogin = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        };

        goToPrevious = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.step == 3 || Common.step > 0) {
                    Common.step--;
                    viewPager.setCurrentItem(Common.step);
                    if (Common.step < 3) {
                        btn_next_step.setEnabled(true);
                        setColorButton();
                    }
                }
            }
        };
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    btn_previous_step.setText("Login");
                    btn_next_step.setText("NEXT");
                    btn_previous_step.setOnClickListener(goToLogin);
                } else if (position == 3) {
                    btn_previous_step.setOnClickListener(goToPrevious);
                    btn_previous_step.setText("previous");
                    btn_next_step.setText("REGISTER");
                } else {
                    btn_next_step.setText("NEXT");
                    btn_previous_step.setOnClickListener(goToPrevious);
                    btn_previous_step.setText("previous");
                }
                btn_next_step.setEnabled(false);
                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btn_previous_step.setText("Login");
        btn_previous_step.setOnClickListener(goToLogin);

        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.step < 3 || Common.step == 0) {
                    Common.step++;
                    if (Common.step == 2) {
                        if (Common.userPhone != null) {
                            Log.d("PHONE NUMBER :", "USERPHONE:" + Common.userPhone);
                            verifyPhoneNumber(Common.userPhone);
                        }
                    }
                } else if (Common.step == 3) {
                    if (Common.currentUser != null) {
                        startEmailVerification();
                    }
                }

                btn_next_step.setEnabled(false);
                viewPager.setCurrentItem(Common.step);
            }

        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Common.step = 3;
                            viewPager.setCurrentItem(Common.step);
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void verifyPhoneNumber(String userPhone) {
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(WelcomeActivity.this, "MESSAGE SENT!", Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Common.AuthCredentials = s;
                viewPager.setCurrentItem(Common.step);
            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(userPhone)       // Phone number to verify
                        .setTimeout(10L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void startEmailVerification() {
        loading.show();
        mAuth.createUserWithEmailAndPassword(Common.currentUser.getEmail(), passwordTmp).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendEmailVerification();
                } else {
                    Toast.makeText(WelcomeActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendUserData();
                        Toast.makeText(WelcomeActivity.this, "Verification mail has been send", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(WelcomeActivity.this, "Verification mail has not been send", Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

    private void sendUserData() {
        Common.currentUser.setUserId(mAuth.getUid());
        CollectionReference userRef;
        userRef = FirebaseFirestore.getInstance().collection("Users");
        userRef.document(mAuth.getUid()).set(Common.currentUser);
        if (loading.isShowing())
            loading.dismiss();
        finish();
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.userPhone = "";
        Common.currentUser = null;
        Common.step = 0;

    }
}
