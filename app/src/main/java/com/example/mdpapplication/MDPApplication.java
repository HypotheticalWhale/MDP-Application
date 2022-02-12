package com.example.mdpapplication;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class MDPApplication extends Application {
    private static BluetoothConnectionHelper bluetooth;

    @Override
    public void onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this);
        bluetooth = new BluetoothConnectionHelper(getApplicationContext());
        super.onCreate();
    }

    public static BluetoothConnectionHelper getBluetooth() {
        return bluetooth;
    }
}
