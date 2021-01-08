package com.example.partyrental.Login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.Model.User;
import com.example.partyrental.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {

    static RegisterFragment instance;

    EditText txt_username, txt_email, txt_password, txt_repeatPassword, txt_name, txt_surname;
    ImageView img_back;
    Button btn_register;
    FirebaseAuth firebaseAuth;

    LocalBroadcastManager localBroadcastManager;
    public static RegisterFragment getInstance() {
        if (instance == null)
            instance = new RegisterFragment();
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);


        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        firebaseAuth = FirebaseAuth.getInstance();

        txt_name = view.findViewById(R.id.tvName);
        txt_surname = view.findViewById(R.id.tvSurname);
        txt_username = view.findViewById(R.id.tvUsername);
        txt_email = view.findViewById(R.id.tvEmail);
        txt_password = view.findViewById(R.id.tvPassword);
        txt_repeatPassword = view.findViewById(R.id.tvRepeatPassword);

        txt_name.addTextChangedListener(registerTextWatcher);
        txt_surname.addTextChangedListener(registerTextWatcher);
        txt_username.addTextChangedListener(registerTextWatcher);
        txt_email.addTextChangedListener(registerTextWatcher);
        txt_password.addTextChangedListener(registerTextWatcher);
        txt_repeatPassword.addTextChangedListener(registerTextWatcher);

        return view;
    }

    private TextWatcher registerTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (checkForPassword() && checkForEmpty()) {
                User user = new User(txt_name.getText().toString(), txt_surname.getText().toString(), txt_username.getText().toString(), Common.userPhone, txt_email.getText().toString());

                Intent intent = new Intent(Constants.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Constants.KEY_USER, user);
                intent.putExtra(Constants.PASSWORD, txt_password.getText().toString());
                intent.putExtra(Constants.KEY_STEP, 3);
                localBroadcastManager.sendBroadcast(intent);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private boolean checkForPassword() {
        if (txt_password.getText().toString().equals(txt_repeatPassword.getText().toString()))
            return true;
        return false;
    }

    private boolean checkForEmpty() {
        if (TextUtils.isEmpty(txt_name.getText())) {
            return false;
        } else if (TextUtils.isEmpty(txt_surname.getText())) {
            return false;
        } else if (TextUtils.isEmpty(txt_username.getText())) {
            return false;
        } else if (TextUtils.isEmpty(txt_email.getText())) {
            return false;
        } else if (TextUtils.isEmpty(txt_password.getText())) {
            return false;
        } else if (TextUtils.isEmpty(txt_repeatPassword.getText())) {
            return false;
        }
        return true;
    }
}