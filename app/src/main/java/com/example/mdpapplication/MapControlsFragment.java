package com.example.mdpapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class MapControlsFragment extends Fragment {
    private static final String TAG = "MapControlsFragment";
    public static final String EVENT_SEND_MOVEMENT = "com.event.EVENT_SEND_MOVEMENT";

    private static final List<String> ValidDirection = Arrays.asList( "N", "E", "S", "W");

    BluetoothConnectionHelper bluetooth;
    TextInputLayout xInput, yInput, directionInput;
    PixelGridView pixelGrid;
    Button b_reset, b_set;
    String x,y,robotDirection;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_controls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        b_reset = view.findViewById(R.id.b_reset);
        b_set = view.findViewById(R.id.b_set);

        xInput = view.findViewById(R.id.xInput);
        yInput = view.findViewById(R.id.yInput);
        directionInput = view.findViewById(R.id.directionInput);
        pixelGrid = getActivity().findViewById(R.id.pixelGrid);

        Context context = getActivity().getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_SEND_MOVEMENT));

        updateTextInput();

        xInput.getEditText().addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(@NonNull CharSequence s, int start,
                                      int before, int count) {
                //get the String from CharSequence with s.toString() and process it to validation
                x = s.toString();
            }
        });

        yInput.getEditText().addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(@NonNull CharSequence s, int start,
                                      int before, int count) {
                //get the String from CharSequence with s.toString() and process it to validation
                y = s.toString();
            }
        });

        directionInput.getEditText().addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(@NonNull CharSequence s, int start,
                                      int before, int count) {
                //get the String from CharSequence with s.toString() and process it to validation
                robotDirection = s.toString();
            }
        });

        b_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelGrid.resetGrid();
                updateTextInput();
            }
        });

        b_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int col = Integer.parseInt(x.replace(" ", ""));
                int row = Integer.parseInt(y.replace(" ", ""));
                String direction = robotDirection.replace(" ", "").toUpperCase();

                if(col >= 0 && col < 20 && row >= 0 && row < 20 && ValidDirection.contains(direction))
                {
                    pixelGrid.setCurCoord(col, row, direction);
                }
                else{
                    showToast("Invalid Input!");
                }
            }
        });

    }
    
    private void updateTextInput(){
        int[] curCoords = pixelGrid.getCurCoord();
        x = String.valueOf(curCoords[0]);
        y = String.valueOf(curCoords[1]);
        robotDirection = pixelGrid.getRobotDirection();

        xInput.getEditText().setText(x);
        yInput.getEditText().setText(y);
        directionInput.getEditText().setText(robotDirection);
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            // Get extra data included in the Intent
            if (intent.getAction().equals(EVENT_SEND_MOVEMENT)) {
                updateTextInput();
            }
        }
    };


    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        super.onDetach();
    }
}