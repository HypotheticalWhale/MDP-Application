package com.example.mdpapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class ControlsFragment extends Fragment {
    private static final String TAG = "ControlsFragment";

    BluetoothConnectionHelper bluetooth;
    String msg;
    ImageButton sa, sr, sl, f, r, rl, rr;
    ToggleButton explore, fastest;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = getActivity().getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();

        sa = view.findViewById(R.id.send_arena);
        sr = view.findViewById(R.id.s_right);
        sl = view.findViewById(R.id.s_left);
        f = view.findViewById(R.id.forward);
        r = view.findViewById(R.id.reverse);
        rl = view.findViewById(R.id.r_left);
        rr = view.findViewById(R.id.r_right);
        explore = view.findViewById(R.id.explore);
        fastest = view.findViewById(R.id.fastest);

        /**
         *  f - Forward
         *  b - Reverse
         *  l - turn left
         *  r - turn right
         *  s - stop
         */
        sa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "sendArena";
                bluetooth.write(msg);
            }
        });
        sr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "sr";
                bluetooth.write(msg);
            }
        });
        sl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "sl";
                bluetooth.write(msg);
            }
        });
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "f";
                bluetooth.write(msg);
            }
        });
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "b";
                bluetooth.write(msg);
            }
        });
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "l";
                bluetooth.write(msg);
            }
        });
        rr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "r";
                bluetooth.write(msg);
            }
        });
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "beginExplore";
                bluetooth.write(msg);
            }
        });
        fastest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "beginFastest";
                bluetooth.write(msg);
            }
        });
    }

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