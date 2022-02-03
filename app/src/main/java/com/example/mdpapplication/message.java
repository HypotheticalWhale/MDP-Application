package com.example.mdpapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class message extends Fragment {

    Button b_send, b_clear;
    TextView tView;
    TextInputLayout tInput;

    BluetoothConnectionHelper bluetooth;
    String msgLog, sendMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view =  lf.inflate(R.layout.fragment_message, container, false); //pass the correct layout name for the fragment

        b_send = view.findViewById(R.id.b_send);
        b_clear = view.findViewById(R.id.b_clear);
        tView = view.findViewById(R.id.textView);
        tInput = view.findViewById(R.id.textInput);

        Context context = getActivity().getApplicationContext();
        bluetooth = new BluetoothConnectionHelper(context);

        context.registerReceiver(mMessageReceiver, new IntentFilter("ReceiveMsg"));

        msgLog = "";

        tView.setText("Application Started");
        tView.setMovementMethod(new ScrollingMovementMethod());

        tInput.getEditText().addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //get the String from CharSequence with s.toString() and process it to validation
                sendMsg = s.toString();
            }
        });

        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendMsg != null){
                    bluetooth.write(sendMsg);
                    logMsg("Message Sent: " + sendMsg);
                    tInput.getEditText().setText("");
                }
                else{
                    showToast("Message box is empty!");
                }
            }
        });

        b_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tView.setText("");
                msgLog = "";
                showToast("Message log cleared!");
            }
        });

        return view;
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();

            String message = intent.getStringExtra("key");
            logMsg("Message Received: " + message);
        }
    };

    private void logMsg(String message){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        msgLog += "[" + dateFormat.format(date)+ "] " + message + "\n";
        tView.setText(msgLog);
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}