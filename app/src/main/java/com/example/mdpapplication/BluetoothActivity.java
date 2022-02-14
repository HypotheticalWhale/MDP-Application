package com.example.mdpapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";

    public static final String EVENT_STATE_CONNECTED = "com.event.EVENT_STATE_CONNECTED";
    public static final String EVENT_STATE_NONE = "com.event.EVENT_STATE_NONE";

    Button btn_list, btn_scan, btn_discover;
    SwitchMaterial bluetoothSwitch;
    ListView pairedList, scannedList;
    BluetoothAdapter bluetoothAdapter;
    ProgressBar loadingBar, connectingLoadingBar;
    TextView deviceStatus;
    View pairedSelected;

    SharedPreferences sharedPref;
    String connectedDevice;
    Boolean connected;

    ArrayAdapter<String> btArrayAdapter;

    BluetoothConnectionHelper bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        btn_list = findViewById(R.id.btn_list);
        btn_scan = findViewById(R.id.btn_scan);
        btn_discover = findViewById(R.id.btn_discover);
        pairedList = findViewById(R.id.pairedList);
        scannedList = findViewById(R.id.scannedList);
        loadingBar = findViewById(R.id.loadingBar);
        connectingLoadingBar = findViewById(R.id.connectingLoadingBar);
        bluetoothSwitch = findViewById(R.id.btSwitch);
        deviceStatus = findViewById(R.id.deviceStatus);

        sharedPref = getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE);

        connected = sharedPref.getBoolean("DeviceStatus", false);
        connectedDevice = sharedPref.getString("DeviceConnected", connectedDevice);

        setDeviceStatus(connected, connectedDevice);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().commit();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Bluetooth Page");

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        checkBluetoothOn();

        btArrayAdapter = new ArrayAdapter<>(BluetoothActivity.this, android.R.layout.simple_selectable_list_item);
        scannedList.setAdapter(btArrayAdapter);
        scannedList.setOnItemClickListener(mScannedDeviceClickListener);

        Context context = getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_STATE_CONNECTED));
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_STATE_NONE));

        context.registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(BluetoothStatusReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        bluetoothSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
                        return;
                    }
                }
                if (bluetoothSwitch.isChecked()) {
                    showToast("Turning On Bluetooth...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                } else {
                    bluetoothAdapter.disable();
                    showToast("Turning Off Bluetooth...");
                }
            }
        });

        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list paired devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
                        return;
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                        return;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                        return;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                        return;
                    }
                }
                if (bluetoothAdapter.isEnabled()) {

                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    ArrayList<String> devices = new ArrayList<String>();
                    ArrayAdapter<String>  pairedArray = new ArrayAdapter<>(BluetoothActivity.this, android.R.layout.simple_selectable_list_item, devices);
                    pairedList.setAdapter(pairedArray);
                    pairedList.setOnItemClickListener(mPairedDeviceClickListener);

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice mDevice : pairedDevices) {
                            pairedArray.add(mDevice.getName() + "\n"
                                    + mDevice.getAddress());
                        }
                    } else {
                        String mNoDevices = "None Paired";// getResources().getText(R.string.none_paired).toString();
                        pairedArray.add(mNoDevices);
                    }
                } else {
                    showToast("Bluetooth is not on!");
                }
            }

        });

        btn_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, 0);
                        return;
                    }
                }
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivity(intent);
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list paired devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                        return;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                        return;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                        return;
                    }
                }
                if (bluetoothAdapter.isEnabled()) {
                    btArrayAdapter.clear();
                    loadingBar.setVisibility(View.VISIBLE);
                    bluetoothAdapter.startDiscovery();
                    btn_scan.setEnabled(false);
                    loadingBar.postDelayed(new Runnable() {
                        public void run() {
                            loadingBar.setVisibility(View.INVISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                                    return;
                                }
                            }
                            bluetoothAdapter.cancelDiscovery();
                            btn_scan.setEnabled(true);
                        }
                    }, 12000);

                } else {
                    showToast("Bluetooth is not on!");
                }
            }
        });
    }

    private final AdapterView.OnItemClickListener mScannedDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> mAdapterView, @NonNull View mView,
                                int mPosition, long mLong) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                    return;
                }
            }

            bluetoothAdapter.cancelDiscovery();
            String mDeviceInfo = ((TextView) mView).getText().toString();
            String mDeviceAddress = mDeviceInfo
                    .substring(mDeviceInfo.length() - 17);
            Log.v(TAG, "Device_Address " + mDeviceAddress);

            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress);
            pairDevice(btDevice);

        }
    };

    private final AdapterView.OnItemClickListener mPairedDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> mAdapterView, @NonNull View mView,
                                int mPosition, long mLong) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                    return;
                }
            }
            bluetoothAdapter.cancelDiscovery();
            if (mView.isEnabled()) {
                String mDeviceInfo = ((TextView) mView).getText().toString();
                if (!mDeviceInfo.equals("None Paired")) {
                    String mDeviceAddress = mDeviceInfo
                            .substring(mDeviceInfo.length() - 17);
                    Log.v(TAG, "Device_Address " + mDeviceAddress);

                    BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    bluetooth.setDeviceInfo(btDevice.getName(), btDevice.getAddress());
                    connectedDevice = btDevice.getName();
                    bluetooth.connectAsClient();
                    pairedSelected = mView;
                    connectingLoadingBar.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
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

    private final BroadcastReceiver BluetoothStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();

            try {
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            bluetoothSwitch.setChecked(false);
                            connected = false;
                            setDeviceStatus(connected, "");
                            if(pairedSelected != null){
                                pairedSelected.setEnabled(true);
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            bluetoothSwitch.setChecked(true);
                            if(pairedSelected != null){
                                pairedSelected.setEnabled(true);
                            }
                            break;
                        case BluetoothAdapter.STATE_DISCONNECTED:
                            connected = false;
                            setDeviceStatus(connected, "");
                            if(pairedSelected != null){
                                pairedSelected.setEnabled(true);
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "onReceive: ", e);
            }
        }
    };

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if(intent.getAction().equals(EVENT_STATE_CONNECTED)){
                connected = true;
                setDeviceStatus(connected, connectedDevice);
                if(pairedSelected != null){
                    pairedSelected.setEnabled(false);
                }
            }
            else if(intent.getAction().equals(EVENT_STATE_NONE)){
                connected = false;
                setDeviceStatus(connected, "");
                if(pairedSelected != null){
                    pairedSelected.setEnabled(true);
                }
            }
        }
    };

    private boolean checkExist(@NonNull ArrayAdapter<String> a, String exist) {
        int count = a.getCount();
        for (int i = 0; i < count; i++) {
            if (a.getItem(i).equals(exist))
                return true;
        }
        return false;
    }

    private void setDeviceStatus(boolean connect, String deviceName){
        if(connect){
            deviceStatus.setText("CONNECTED TO" + deviceName);
            deviceStatus.setTextColor(Color.GREEN);
        }
        else{
            deviceStatus.setText(R.string.device_disconnected);
            deviceStatus.setTextColor(Color.RED);
        }
        connectingLoadingBar.setVisibility(View.INVISIBLE);
    }

    private void checkBluetoothOn(){
        bluetoothSwitch.setChecked(bluetoothAdapter.isEnabled());
    }

    //For Pairing
    private void pairDevice(@NonNull BluetoothDevice device) {
        try {
            Log.d(TAG, "pairDevice: Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d(TAG, "pairDevice: Pairing finished.");
            showToast("Pairing...");
        } catch (Exception e) {
            Log.e(TAG, "pairDevice: " + e.getMessage());
        }
    }

    //For UnPairing
    private void unpairDevice(@NonNull BluetoothDevice device) {
        try {
            Log.d(TAG, "unpairDevice: Start Un-Pairing...");
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d(TAG, "unpairDevice: Un-Pairing finished.");
            showToast("Device unpaired!");
        } catch (Exception e) {
            Log.e(TAG, "unpairDevice: " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                return;
            }
        }
        bluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                return;
            }
        }
        bluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
                return;
            }
        }
        bluetoothAdapter.cancelDiscovery();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("DeviceStatus", connected);
        editor.putString("DeviceConnected", connectedDevice);
        editor.commit();

        Log.d(TAG, "onDestroy: DeviceStatus: " + connected);
    }
}


