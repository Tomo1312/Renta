package com.example.partyrental.Login;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.HouseActivity;
import com.example.partyrental.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.Executor;

import dmax.dialog.SpotsDialog;

public class VerifyNumberFragment extends Fragment {

    static VerifyNumberFragment instance;
    TextView txt_otp, txt_message;

    Button btn_submit;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    AlertDialog loading;
    LocalBroadcastManager localBroadcastManager;

    public static VerifyNumberFragment getInstance() {
        if (instance == null)
            instance = new VerifyNumberFragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verify_number, container, false);
        loading = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        txt_otp = view.findViewById(R.id.txt_otp);
        txt_message = view.findViewById(R.id.txt_message);
        btn_submit = view.findViewById(R.id.btn_submit);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.show();
                txt_message.setVisibility(View.GONE);
                String otp = txt_otp.getText().toString();
                if(otp.isEmpty()){
                    if(loading.isShowing())
                        loading.dismiss();
                    txt_message.setVisibility(View.VISIBLE);
                    txt_message.setText(getResources().getString(R.string.empty_otp));
                }else{
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(Common.AuthCredentials, otp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        return view;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");

                            Intent intent = new Intent(Constants.KEY_ENABLE_BUTTON_NEXT);
                            intent.putExtra(Constants.KEY_STEP, 2);
                            localBroadcastManager.sendBroadcast(intent);
                            txt_message.setVisibility(View.VISIBLE);
                            txt_message.setText(getResources().getString(R.string.success));
                            //FirebaseUser user = task.getResult().getUser();
                        } else {
                            // Sign in failed, display a message and update the UI
                            // Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                txt_message.setVisibility(View.VISIBLE);
                                txt_message.setText(getResources().getString(R.string.wrong_otp));
                                // The verification code entered was invalid
                            }
                        }
                        if(loading.isShowing())
                            loading.dismiss();
                    }
                });
    }
}