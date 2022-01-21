package com.example.mdpapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.textView);
        Button start_app = (Button) findViewById(R.id.startAppButton);
        start_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("tag","Application is started");
                Intent intent = new Intent(MainActivity.this,ApplicationActivity.class);
                startActivity(intent);
            }
        });

        Log.d("tag","In onCreate");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("tag","In onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("tag","In onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("tag","In onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("tag","In onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("tag","In onDestroy");
    }
}