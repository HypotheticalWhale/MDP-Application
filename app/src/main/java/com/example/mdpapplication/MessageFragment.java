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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageFragment extends Fragment {
    private static final String TAG = "MessageFragment";

    public static final String EVENT_MESSAGE_RECEIVED = "com.event.EVENT_MESSAGE_RECEIVED";
    public static final String EVENT_MESSAGE_SENT = "com.event.EVENT_MESSAGE_SENT";

    private static final String STATE_LOG = "log";

    Button btn_send, btn_clear;
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

        btn_send = view.findViewById(R.id.btn_send);
        btn_clear = view.findViewById(R.id.btn_clear);
        tView = view.findViewById(R.id.textView);
        tInput = view.findViewById(R.id.textInput);

        Context context = getActivity().getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_MESSAGE_RECEIVED));
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_MESSAGE_SENT));

        tView.setText("Application Started");
        msgLog = "";

        if(savedInstanceState != null){
            msgLog = savedInstanceState.getString(STATE_LOG);
            if(msgLog != ""){
                tView.setText(msgLog);
            }
        }

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

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendMsg != null){
                    bluetooth.write(sendMsg);
                    tInput.getEditText().setText("");
                }
                else{
                    showToast("Message box is empty!");
                }
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
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

            if(intent.getAction().equals(EVENT_MESSAGE_RECEIVED)){
                String message = intent.getStringExtra("key");
                logMsg("Message Received: " + message);
            }
            else if(intent.getAction().equals(EVENT_MESSAGE_SENT)){
                String message = intent.getStringExtra("key");
                logMsg("Message Sent: " + message);
            }
        }
    };

    public void logMsg(String message){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        msgLog += "[" + dateFormat.format(date)+ "] " + message + "\n";
        tView.setText(msgLog);
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_LOG, msgLog);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}