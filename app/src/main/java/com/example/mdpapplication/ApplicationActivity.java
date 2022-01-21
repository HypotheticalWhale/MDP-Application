package com.example.mdpapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
//Hello
public class ApplicationActivity extends AppCompatActivity {
    Button b_on,b_off,b_discover,b_list,back_button;
    ListView list;
    BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLED = 0;
    private static final int REQUEST_DISCOVERABLE = 0;
    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // your operation....
                    }
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);
        b_on = (Button) findViewById(R.id.b_on);
        b_off = (Button) findViewById(R.id.b_off);
        b_discover = (Button) findViewById(R.id.b_discover);
        b_list = (Button) findViewById(R.id.b_list);
        list = (ListView) findViewById(R.id.listView);
        back_button = (Button) findViewById(R.id.back_button);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(bluetoothAdapter == null){
//            Toast.makeText(this,"Bluetooth not supported",Toast.LENGTH_SHORT).show();
//            finish();
//        }
        b_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent    = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
        });
        b_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.disable();
            }
        });
        b_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isDiscovering()){
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivity(intent);
                }
            }
        });
        b_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list paired devices
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                ArrayList<String> devices = new ArrayList<String>();
                for(BluetoothDevice bt:pairedDevices){
                    devices.add(bt.getName());
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(ApplicationActivity.this,android.R.layout.simple_list_item_1,devices);
                list.setAdapter(arrayAdapter);
                }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(ApplicationActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

    }
}