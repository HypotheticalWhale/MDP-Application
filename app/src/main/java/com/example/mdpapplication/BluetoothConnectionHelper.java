package com.example.mdpapplication;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionHelper extends Service {

    /** Variables and Constant
     *
     */
    private static final String TAG = "BluetoothConnectionHelper";

    private static Toast toast;
    @NonNull
    private static Boolean displayToast = true;

    private static Handler mHandler;
    private static Context mContext;

    //BluetoothService states
    public static final int STATE_NONE = 0; //doing nothing
    public static final int STATE_LISTEN = 1; //listening for incoming
    public static final int STATE_CONNECTING = 2; //initiating outgoing
    public static final int STATE_CONNECTED = 3; //connected to a device

    //For broadcast event
    public static final String EVENT_STATE_NONE = "com.event.EVENT_STATE_NONE";
    public static final String EVENT_STATE_LISTEN = "com.event.EVENT_STATE_LISTEN";
    public static final String EVENT_STATE_CONNECTING = "com.event.EVENT_STATE_CONNECTING";
    public static final String EVENT_STATE_CONNECTED = "com.event.EVENT_STATE_CONNECTED";
    public static final String EVENT_MESSAGE_RECEIVED = "com.event.EVENT_MESSAGE_RECEIVED";
    public static final String EVENT_MESSAGE_SENT = "com.event.EVENT_MESSAGE_SENT";
    public static final String EVENT_SEND_MOVEMENT = "com.event.EVENT_SEND_MOVEMENT";
    public static final String EVENT_TARGET_SCANNED = "com.event.EVENT_TARGET_SCANNED";
    public static final String EVENT_ROBOT_MOVES = "com.event.EVENT_ROBOT_MOVES";

    //For showing toast
    private final String BLUETOOTH_NOT_SUPPORTED = "Device does not support bluetooth.";
    private final String BLUETOOTH_NOT_ENABLED = "Device requires Bluetooth to be enabled.";
    private final String BLUETOOTH_NO_REMOTE_DEVICE = "No remote device selected to connect to";
    private final String BLUETOOTH_CONNECTION_FAILED = "Device failed to connect with remote device";

    //For string communication and toast
    public final int MESSAGE_READ = 0020;
    public final int MESSAGE_SENT = 0021;
    public final int MESSAGE_TOAST = 0022;
    private static String receivedMessage;

    @Nullable
    private ConnectThread mConnectThread;
    @Nullable
    private AcceptThread mAcceptThread;
    @Nullable
    private static ConnectedThread mConnectedThread;

    public static int mState = STATE_NONE;

    private static BluetoothAdapter mBluetoothAdapter;
    private static ArrayList<String> arrayList;

    //Standard UUID
    private static final UUID MY_UUID
            = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //For manual connection and reconnection to remote device
    private static String targetMACAddress = "";
    private static String targetDeviceName = "";
    private static final String serverName = "MDP Tablet Group 19";
    private static String connectedMACAddress = "";
    private static String connectedDeviceName = "";
    private static int reconnectAttempt = 0;
    private static boolean isServer = false;

    //For auto connection to remote device
    private static final String RPIMACAddress = "";
    private static final String RPIDeviceName = "";

    private static final List<String> ValidRobotCommands = Arrays.asList( "f", "b", "r",
            "l", "sl", "sr");

    /** Service Binding
     *
     */
    private final IBinder binder = new BluetoothBinder();

    public class BluetoothBinder extends Binder {
        @NonNull
        BluetoothConnectionHelper getBluetooth(){
            return BluetoothConnectionHelper.this;
        }
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /** Constructor
     *
     * */
    public BluetoothConnectionHelper() { }

    public BluetoothConnectionHelper(Context context){
        super();
        arrayList = new ArrayList<String>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case MESSAGE_READ:
                        receivedMessage = new String((byte[])msg.obj);
                        receivedMessage = receivedMessage.trim();

                        Log.d(TAG, "handleMessage: MESSAGE_READ: " + receivedMessage);

                        if(receivedMessage.contains("TARGET")) {
                            sendIntentBroadcastWithMsg(receivedMessage, EVENT_TARGET_SCANNED);
                            sendIntentBroadcastWithMsg(receivedMessage, EVENT_MESSAGE_RECEIVED);
                        }
                        else if(receivedMessage.contains("ROBOT")) {
                            sendIntentBroadcastWithMsg(receivedMessage, EVENT_ROBOT_MOVES);
                            sendIntentBroadcastWithMsg(receivedMessage, EVENT_MESSAGE_RECEIVED);
                        }
                        else if(ValidRobotCommands.contains(receivedMessage)){
                            sendIntentBroadcastWithMsg(receivedMessage, EVENT_SEND_MOVEMENT);
                            sendIntentBroadcastWithMsg(receivedMessage, EVENT_MESSAGE_RECEIVED);
                        }

                        break;
                    case MESSAGE_SENT:
                        String sentMessage = new String((byte[])msg.obj);

                        Log.d(TAG, "handleMessage: MESSAGE_SENT: " + sentMessage);

                        sendIntentBroadcastWithMsg(sentMessage, EVENT_MESSAGE_SENT);

                        if(ValidRobotCommands.contains(sentMessage)){
                            sendIntentBroadcastWithMsg(sentMessage, EVENT_SEND_MOVEMENT);
                        }

                        break;
                    case MESSAGE_TOAST:
                        String toastMessage = (String) msg.obj;
                        showToast(toastMessage);
                        break;
                    default:
                        break;
                }
            }
        };
    }


    /** Setter and Getters
     *
     */

    @SuppressLint("MissingPermission")
    public void setDeviceInfo(String deviceName, String MACAddress){
        BluetoothConnectionHelper.targetMACAddress = MACAddress;
        BluetoothConnectionHelper.targetDeviceName = deviceName;

        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public int getState(){
        return BluetoothConnectionHelper.mState;
    }

    public String getReceivedMsg(){
        return receivedMessage;
    }

    /** Handle button clicks from BluetoothConnection
     *
     */

    public void connectAsServer(){
        isServer = true;

        if (mBluetoothAdapter == null){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                    BLUETOOTH_NOT_SUPPORTED).sendToTarget();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                    BLUETOOTH_NOT_ENABLED).sendToTarget();
            return;
        }

        if (mState == STATE_LISTEN){
            if (mAcceptThread != null){
                mAcceptThread.cancel();
                mAcceptThread = null;
            }
        }

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        setState(STATE_LISTEN, true);
    }

    public void connectAsClient(){
        if (mBluetoothAdapter == null){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                    BLUETOOTH_NOT_SUPPORTED).sendToTarget();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                    BLUETOOTH_NOT_ENABLED).sendToTarget();
            return;
        }
        if (targetMACAddress.equals("")){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                    BLUETOOTH_NO_REMOTE_DEVICE).sendToTarget();
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(
                targetMACAddress);

        if (mState == STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        //Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING, true);
    }

    public void autoConnectAsClient(){
        if (targetMACAddress.equals("")){
            targetMACAddress = RPIMACAddress;
            targetDeviceName = RPIDeviceName;
        }

        connectAsClient();
    }

    private void reconnectAsClient(){
        stopService();
        if (!connectedMACAddress.equals("")){
            targetMACAddress = connectedMACAddress;
        }

        if (targetMACAddress.equals("")){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1, BLUETOOTH_NO_REMOTE_DEVICE)
                    .sendToTarget();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()){
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1, BLUETOOTH_NOT_ENABLED)
                    .sendToTarget();
            return;
        }

        while (mState != STATE_CONNECTED && reconnectAttempt <10
                && reconnectAttempt >= 0 && !isServer){

            reconnectAttempt++;

            String reconnectMsg =String.format(Locale.getDefault(), "Reconnect Attempt: %d / 10",
                    reconnectAttempt);
            mHandler.obtainMessage(MESSAGE_TOAST, 1, -1, reconnectMsg)
                    .sendToTarget();

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(
                    targetMACAddress);
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();

            setState(STATE_CONNECTING, false);
            try {
                Thread.sleep(6000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private synchronized void connected(@NonNull BluetoothSocket mmSocket,
                                        @Nullable BluetoothDevice mmDevice){
        reconnectAttempt = 0;
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mmDevice != null){
            connectedDeviceName = mmDevice.getName();
            connectedMACAddress = mmDevice.getAddress();
        }

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        setState(STATE_CONNECTED, true);
    }


    private void setState(int state, boolean showToast){
        BluetoothConnectionHelper.mState = state;
        switch(state){
            case STATE_NONE:
                sendIntentBroadcast(EVENT_STATE_NONE);
                if (showToast)
                    mHandler.obtainMessage(MESSAGE_TOAST, 0, -1, "Bluetooth is idle.")
                            .sendToTarget();
                break;
            case STATE_LISTEN:
                sendIntentBroadcast(EVENT_STATE_LISTEN);
                if (showToast)
                    mHandler.obtainMessage(MESSAGE_TOAST, 0, -1,
                            "Device is listening for incoming connection").sendToTarget();
                break;
            case STATE_CONNECTING:
                sendIntentBroadcast(EVENT_STATE_CONNECTING);
                if (showToast)
                    mHandler.obtainMessage(MESSAGE_TOAST, 0, -1,
                            "Device is trying to connection to remote device").sendToTarget();
                break;
            case STATE_CONNECTED:
                sendIntentBroadcast(EVENT_STATE_CONNECTED);
                if (showToast)
                    mHandler.obtainMessage(MESSAGE_TOAST, 0, -1,
                            "Device is connected to remote device").sendToTarget();
                break;
            default:
                break;
        }
    }

    public void startToast(){
        displayToast = true;
    }

    public void stopToast(){
        displayToast = false;
        if (toast != null)
            toast.cancel();
    }

    @SuppressLint("MissingPermission")
    private synchronized void stopService(){
        setState(STATE_NONE, true);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    private static final Object obj = new Object();

    public void write(@NonNull String message){
        ConnectedThread r;
        synchronized(obj){
            if (mState != STATE_CONNECTED){
                showToast("Device is not connected to any remote device");
                return;
            }
            r = mConnectedThread;
        }
        if (message.length() > 0){
            byte[] send = message.getBytes();
            r.write(send);
        }
    }

    private class AcceptThread extends Thread{
        @Nullable
        private final BluetoothServerSocket mmServerSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        serverName, MY_UUID);
            }catch(IOException e){
                mHandler.obtainMessage(MESSAGE_TOAST, 1, -1, "Bluetooth failed to create server socket")
                        .sendToTarget();
                stopService();
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run(){
            BluetoothSocket socket;
            while(mState != STATE_CONNECTED){
                try{
                    socket = mmServerSocket.accept();
                }catch (IOException e){
                    mHandler.obtainMessage(MESSAGE_TOAST, 1, -1, BLUETOOTH_CONNECTION_FAILED)
                            .sendToTarget();
                    stopService();
                    break;
                }
                //If a connection was accepted
                if (socket != null){
                    connected(socket, socket.getRemoteDevice());
                    try{
                        mmServerSocket.close();
                    }catch(IOException e){ }
                    break;
                }
            }
        }

        public void cancel(){
            try{
                mmServerSocket.close();
            }catch (IOException e){	}
        }
    }//end of AcceptThread

    private class ConnectThread extends Thread{
        @Nullable
        private final BluetoothSocket mmSocket;
        @NonNull
        private final BluetoothDevice mmDevice;

        @SuppressLint("MissingPermission")
        public ConnectThread(@NonNull BluetoothDevice device){
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try{
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            }catch(Exception e){
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run(){
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();
            try{
                mmSocket.connect();
            }catch(IOException connectException){
                try{
                    mmSocket.close();
                }catch(IOException closeException){
                }
                mHandler.obtainMessage(MESSAGE_TOAST, 1, -1, BLUETOOTH_CONNECTION_FAILED)
                        .sendToTarget();
                stopService();
                return;
            }
            synchronized (BluetoothConnectionHelper.this){
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException closeException){
            }
        }
    }//end of connectThread()

    private class ConnectedThread extends Thread{
        @NonNull
        private final BluetoothSocket mmSocket;
        @Nullable
        private final InputStream mmInStream;
        @Nullable
        private final OutputStream mmOutStream;

        public ConnectedThread(@NonNull BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch(IOException e){
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @SuppressLint("NewApi")
        @Override
        public void run(){

            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try{
                    //Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    buffer[bytes] = '\0';
                    //Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    buffer = new byte[1024];
                }catch(IOException e){
                    mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                            "Bluetooth failed to read from connection").sendToTarget();
                    if (isServer){
                        stopService();
                    }else{
                        reconnectAsClient();
                    }break;
                }
            }
        }
        @SuppressLint("NewApi")
        public void write(@NonNull byte[] buffer){
            try{
                mmOutStream.write(buffer);
                mHandler.obtainMessage(MESSAGE_SENT, buffer.length, -1, buffer)
                        .sendToTarget();
            }catch(IOException e){
                mHandler.obtainMessage(MESSAGE_TOAST, 1, -1,
                        "Bluetooth failed to write to connection").sendToTarget();
                if (isServer){
                    stopService();
                }else{
                    reconnectAsClient();
                }
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){
            }
        }

    }//end of connectedThread

    private void sendIntentBroadcast(String eventCode){
        Intent intent = new Intent();
        intent.setAction(eventCode);
        mContext.getApplicationContext().sendBroadcast(intent);
    }

    private static void sendIntentBroadcastWithMsg(String msg, String eventCode) {
        Intent intent = new Intent();
        intent.setAction(eventCode);
        // You can also include some extra data.
        intent.putExtra("key", msg);
        mContext.getApplicationContext().sendBroadcast(intent);
    }

    //toast message function
    private void showToast(String msg){
        toast = Toast.makeText(mContext.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        if (displayToast){
            toast.show();
            mHandler.postDelayed(new Runnable(){

                @Override
                public void run() {
                    toast.cancel();
                }
            }, 400);
        }
    }
}