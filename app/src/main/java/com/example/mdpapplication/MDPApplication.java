package com.example.mdpapplication;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class MDPApplication extends Application {
    @Override
    public void onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this);
        super.onCreate();
    }
}
