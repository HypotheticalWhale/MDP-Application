package com.example.mdpapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ApplicationActivity extends AppCompatActivity {
    private static final String TAG = "ApplicationActivity";
    Button b_list, b_scan, b_discover, b_test, b_connect;
    SwitchMaterial bluetoothSwitch;
    ListView pairedList, scannedList;
    BluetoothAdapter bluetoothAdapter;

    ArrayAdapter<String> btArrayAdapter;

    BluetoothConnectionHelper bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        b_list = findViewById(R.id.b_list);
        b_scan = findViewById(R.id.b_scan);
        b_discover = findViewById(R.id.b_discover);
        b_connect = findViewById(R.id.b_connect);
        b_test = findViewById(R.id.b_test);
        pairedList = findViewById(R.id.pairedList);
        scannedList = findViewById(R.id.scannedList);

        bluetoothSwitch = findViewById(R.id.btSwitch);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Bluetooth Page");

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setChecked(true);
        }

        btArrayAdapter = new ArrayAdapter<>(ApplicationActivity.this, android.R.layout.simple_list_item_1);
        scannedList.setAdapter(btArrayAdapter);
        scannedList.setOnItemClickListener(mScannedDeviceClickListener);

        ApplicationActivity.this.registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Context context = getApplicationContext();
        bluetooth = new BluetoothConnectionHelper(context);

        bluetoothSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 000);
                        return;
                    }
                }
                if (bluetoothSwitch.isChecked()) {
                    showToast("Turning On Bluetooth...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                } else {
                    bluetoothAdapter.disable();
                    showToast("Turning Bluetooth Off");
                }
            }
        });

        b_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list paired devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 000);
                        return;
                    }
                }
                if (bluetoothAdapter.isEnabled()) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    ArrayList<String> devices = new ArrayList<String>();
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ApplicationActivity.this, android.R.layout.simple_list_item_1, devices);
                    pairedList.setAdapter(arrayAdapter);
                    pairedList.setOnItemClickListener(mPairedDeviceClickListener);
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice mDevice : pairedDevices) {
                            arrayAdapter.add(mDevice.getName() + "\n"
                                    + mDevice.getAddress());
                        }
                    } else {
                        String mNoDevices = "None Paired";// getResources().getText(R.string.none_paired).toString();
                        arrayAdapter.add(mNoDevices);
                    }
                } else {
                    showToast("Bluetooth is not on!");
                }
            }

        });

        b_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, 000);
                        return;
                    }
                }
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivity(intent);
            }
        });

        b_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list paired devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 000);
                        return;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                        return;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                        return;
                    }
                }
                if (bluetoothAdapter.isEnabled()) {

                    btArrayAdapter.clear();
                    bluetoothAdapter.startDiscovery();
                } else {
                    showToast("Bluetooth is not on!");
                }
            }
        });

        b_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.connectAsClient();
            }
        });

        b_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.write("test");
            }
        });
    }

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 000);
                    return;
                }
            }
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && !checkExist(btArrayAdapter, device.getName() + "\n" + device.getAddress())) {
                    btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    btArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private boolean checkExist(ArrayAdapter a, String exist) {
        int count = a.getCount();
        for (int i = 0; i < count; i++) {
            if (a.getItem(i) == exist)
                return true;
        }
        return false;
    }

    private AdapterView.OnItemClickListener mScannedDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> mAdapterView, View mView,
                                int mPosition, long mLong) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 000);
                    return;
                }
            }
            bluetoothAdapter.cancelDiscovery();
            String mDeviceInfo = ((TextView) mView).getText().toString();
            String mDeviceAddress = mDeviceInfo
                    .substring(mDeviceInfo.length() - 17);
            Log.v("tag", "Device_Address " + mDeviceAddress);

            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress);
            pairDevice(btDevice);
        }
    };

    private AdapterView.OnItemClickListener mPairedDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> mAdapterView, View mView,
                                int mPosition, long mLong) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(ApplicationActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ApplicationActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 000);
                    return;
                }
            }
            bluetoothAdapter.cancelDiscovery();
            String mDeviceInfo = ((TextView) mView).getText().toString();
            String mDeviceAddress = mDeviceInfo
                    .substring(mDeviceInfo.length() - 17);
            Log.v("tag", "Device_Address " + mDeviceAddress);

            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress);
            bluetooth.setDeviceInfo(btDevice.getName(),btDevice.getAddress());
            bluetooth.setRPIDeviceInfo(btDevice.getName(),btDevice.getAddress());
        }
    };

    //For Pairing
    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");
            showToast("Pairing...");
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
    }


    //For UnPairing
    private void unpairDevice(BluetoothDevice device) {
        try {
            Log.d("unpairDevice()", "Start Un-Pairing...");
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("unpairDevice()", "Un-Pairing finished.");
            showToast("Device unpaired!");
        } catch (Exception e) {
            Log.e("unpairDevice()", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(ApplicationActivity.this, MainActivity.class);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("tag", "Bluetooth In onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("tag", "Bluetooth In onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();
        Log.d("tag", "Bluetooth In onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothAdapter.cancelDiscovery();
        Log.d("tag", "Bluetooth In onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        Log.d("tag", "Bluetooth In onDestroy");
    }
}


