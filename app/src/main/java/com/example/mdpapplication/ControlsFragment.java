package com.example.mdpapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ControlsFragment extends Fragment {
    private static final String TAG = "ControlsFragment";

    BluetoothConnectionHelper bluetooth;
    String msg;
    ImageButton sa, sr, sl, f, r, rl, rr, stop;
    ToggleButton explore, fastest;
    TextView exploreText,fastestText;
    PixelGridView pixelGrid;

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
        stop = view.findViewById(R.id.stop);
        explore = view.findViewById(R.id.explore);
        fastest = view.findViewById(R.id.fastest);

        pixelGrid = getActivity().findViewById(R.id.pixelGrid);

        exploreText = view.findViewById(R.id.explorationTitleTextView);
        fastestText  = view.findViewById(R.id.fastestPathTitleTextView);

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
//                msg = "sendArena";
                try{
                    List<PixelGridView.Obstacle> obstacles = new ArrayList<>(pixelGrid.getObstacles());

                    obstacles.sort((o1, o2) -> {
                        if (o1.getId() > o2.getId()) {
                            return 1;
                        } else if (o1.getId() < o2.getId()) {
                            return -1;
                        }
                        return -1;
                    });

                    JSONObject json = new JSONObject();

                    JSONArray array = new JSONArray();

                    for(PixelGridView.Obstacle obstacle: obstacles){
                        JSONObject item = new JSONObject();
                        item.put("X", obstacle.X);
                        item.put("Y", obstacle.Y);
                        item.put("id", obstacle.id);
                        item.put("direction", obstacle.direction);
                        array.put(item);
                    }

                    json.put("Obstacles", array);

                    msg = json.toString();

                    bluetooth.write(msg);
                }catch (JSONException e){
                    Log.e(TAG, "onClick: ", e);
                }
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
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "s";
                bluetooth.write(msg);
            }
        });
        explore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    msg = "beginExplore";
                    bluetooth.write(msg);
                    if(exploreText.getText().equals("Exploration")){
                        exploreText.setText("Exploring");
                    }
                    if(exploreText.getText().equals("Exploration Stopped")){
                        exploreText.setText("Exploring");
                    }
                }
                else{
                    msg = "stopExplore";
                    bluetooth.write(msg);
                    if(exploreText.getText().equals("Exploring")){
                        exploreText.setText("Exploration Stopped");
                    }
                }
            }
        });
        fastest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    msg = "beginFastest";
                    bluetooth.write(msg);
                    if(fastestText.getText().equals("Fastest Path")){
                        fastestText.setText("Fastest Pathing");
                    }
                    if(fastestText.getText().equals("Fastest Stopped")){
                        fastestText.setText("Fastest Pathing");
                    }
                }
                else{
                    msg = "stopFastest";
                    bluetooth.write(msg);
                    if(fastestText.getText().equals("Fastest Pathing")){
                        fastestText.setText("Fastest Stopped");
                    }
                }
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