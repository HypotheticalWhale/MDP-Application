package com.example.mdpapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

public class controls extends Fragment {
    BluetoothConnectionHelper bluetooth;
    String msg;
    ImageButton sa,sr,sl,f,r,rl,rr;
    ToggleButton explore,fastest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.fragment_controls,container,false);

        Context context = getActivity().getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();;
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

        return view;    }
}