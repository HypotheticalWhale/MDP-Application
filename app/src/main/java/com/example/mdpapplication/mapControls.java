package com.example.mdpapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

public class mapControls extends Fragment {

    BluetoothConnectionHelper bluetooth;
    TextInputLayout corrInput;
    PixelGridView pixelGrid;
    Button b_reset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view =  lf.inflate(R.layout.fragment_map_controls, container, false); //pass the correct layout name for the fragment

        b_reset = view.findViewById(R.id.b_reset);

        corrInput = view.findViewById(R.id.corrInput);
        pixelGrid = getActivity().findViewById(R.id.pixelGrid);

        Context context = getActivity().getApplicationContext();
        bluetooth = new BluetoothConnectionHelper(context);

        corrInput.getEditText().addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //get the String from CharSequence with s.toString() and process it to validation
                pixelGrid.setCurCoord(0, 0, "N");
            }
        });

        b_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelGrid.resetGrid();
            }
        });

        return view;
    }
}