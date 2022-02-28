package com.example.mdpapplication;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity<NameViewModel> extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String STATE_OBSTACLE = "obstacles";
    private static final String STATE_ROBOT = "robot";
    private static final String STATE_ROBOT_DIRECTION = "robot direction";
    private static final String STATE_COUNTER = "counter";

    public static final String EVENT_ROBOT_STATUS = "com.event.EVENT_ROBOT_STATUS";

    private static final String ROBOT_STATUS = "status";

    private BluetoothAdapter mBluetoothAdapter;

    TabLayout tabLayout;
    ViewPager2 viewPager;
    FragmentAdapter adapter;
    PixelGridView pixelGrid;
    BluetoothConnectionHelper bluetooth;
    TextView robotStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        robotStatus = findViewById(R.id.robotStatus);

        pixelGrid = findViewById(R.id.pixelGrid);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        FragmentManager fm = getSupportFragmentManager();
        adapter = new FragmentAdapter(fm, getLifecycle());
        viewPager.setAdapter(adapter);

        Context context = getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();

        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_ROBOT_STATUS));

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            HashSet<PixelGridView.Obstacle> obstacles = new HashSet<>(savedInstanceState.getParcelableArrayList(STATE_OBSTACLE));
            pixelGrid.setObstacles(obstacles);
            float[] curCoord = savedInstanceState.getFloatArray(STATE_ROBOT);
            String direction = savedInstanceState.getString(STATE_ROBOT_DIRECTION);
            pixelGrid.setCurCoord(curCoord[0], curCoord[1], direction);
            pixelGrid.setCounter(savedInstanceState.getInt(STATE_COUNTER));
            robotStatus.setText(savedInstanceState.getString(ROBOT_STATUS));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.bluetooth) {
            Log.d(TAG, "onOptionsItemSelected: Going to Bluetooth Page");
            Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("key");

            if(intent.getAction().equals(EVENT_ROBOT_STATUS)){
                robotStatus.setText(message);
            }
        }
    };

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        ArrayList<PixelGridView.Obstacle> obstacles = new ArrayList<>(pixelGrid.getObstacles());
        savedInstanceState.putParcelableArrayList(STATE_OBSTACLE, obstacles);
        savedInstanceState.putFloatArray(STATE_ROBOT, pixelGrid.getCurCoord());
        savedInstanceState.putString(STATE_ROBOT_DIRECTION, pixelGrid.getRobotDirection());
        savedInstanceState.putInt(STATE_COUNTER, pixelGrid.getCounter());
        savedInstanceState.putString(ROBOT_STATUS, (String) robotStatus.getText());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().commit();

        Log.d(TAG, "onDestroy: ");
    }
}