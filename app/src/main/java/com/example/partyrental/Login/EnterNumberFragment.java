package com.example.partyrental.Login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.R;

public class EnterNumberFragment extends Fragment {
    EditText txt_phone_number;
    LocalBroadcastManager localBroadcastManager;

    TextView txt_message;
    static EnterNumberFragment instance;

    public static Fragment getInstance() {
        if (instance == null)
            instance = new EnterNumberFragment();
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
        View view = inflater.inflate(R.layout.fragment_enter_number, container, false);
        txt_phone_number = view.findViewById(R.id.txt_phone_number);
        txt_message = view.findViewById(R.id.txt_message);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        txt_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                txt_message.setVisibility(View.GONE);
                if (txt_phone_number.getText().toString().startsWith("+")) {
                    Intent intent = new Intent(Constants.KEY_ENABLE_BUTTON_NEXT);
                    intent.putExtra(Constants.KEY_PHONE_NUMBER, txt_phone_number.getText().toString());
                    intent.putExtra(Constants.KEY_STEP, 1);
                    localBroadcastManager.sendBroadcast(intent);
                }else{
                    txt_message.setVisibility(View.VISIBLE);
                    txt_message.setText("Broj treba kretati sa + (npr. +385913456789)");
                }
            }
        });
        return view;
    }
}