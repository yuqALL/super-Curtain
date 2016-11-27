package com.yuq.curtain.supercurtain.data;
/*
这个service用于保存以及读取用户操作，更新主界面，以及接收蓝牙信息
功能：
1.启动service后，未连接蓝牙情况下，根据存储好的配置文件（data..xml），初始化界面

2.搜索蓝牙设备，返回设备列表，刷新主界面

3.连接指定蓝牙设备

4.停止按钮，停止搜索蓝牙

5.控制模式改变，发送给窗帘设备，成功后更新配置文件，返回控制模式，并更新界面，更改配置文件

6.手动开启、手动关闭，发送命令到窗帘设备，返回结果，更改窗帘状态，成功后保存配置文件

7.定时状态，开启定时状态后，更改配置列表

8.定时开关，当定时模式开的情况下，点击定时开关，将设置的时间保存在配置文件中，并开始倒计时，每秒更新界面信息

9.接收蓝牙信息，并保存聊天记录，更新聊天界面

10.查看蓝牙开关状态

11.获得绑定过的蓝牙设备列表

12.开蓝牙

13.关蓝牙

 */

import android.app.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yuq.curtain.supercurtain.MainActivity;
import com.yuq.curtain.supercurtain.initapp;
import com.yuq.curtain.supercurtain.utils.RWxml;
import com.yuq.curtain.supercurtain.utils.SerializableMap;
import com.yuq.curtain.supercurtain.utils.SerializableObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class mainService extends Service {

    private static final String ACTION_RECEIVER_DATA = "com.yuq.receiver.data";//从service接收数据
    private static final String ACTION_CTL_SERVICE = "com.yuq.control.service"; //控制service，service接收这个广播
    public int control = -1;

    //回送的消息类型
    public static String SEND_ACTION_VIEW_DATA = "get  the data";
    public static String SEND_ACTION_BLUETOOTH_LIST = "get the device list";
    public static String SEND_ACTION_GET_BLUETOOTH_STATE = "get  the bluetooth state";
    public static String SEND_ACTION_GET_DEFAULT_BLUETOOT = "get  the default bluetooth device";
    public static String SEND_ACTION_RECEIVER_DATA = "receiver  data come from bluetooth";
    public static String SEND_ACTION_SEND_DATA = "send  data to bluetooth";
    public static String SEND_ACTION_CONNECTING = "connecting";
    public static String SEND_ACTION_CONNECTED = "connected";
    public static String SEND_ACTION_CONNECT_FAILED = "connect failed";
    public static String SEND_ACTION_CONNECTION_LOST = "connection lost";
    public static String SEND_ACTION_COLOCK_STATE = "clock state";
    public static String SEND_ACTION_COLOCK_RESET_COLOCK = "00:00";
    public static String SEND_ACTION_CONTROL_MODE = "control mode";
    public static String SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD = "change tempture threshold";
    public static String SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD = "change humidity threshold";
    public static String SEND_ACTION_CHANGE_LIGHT_THRESHOLD = "change light threshold";
    public static String SEND_ACTION_CURTAIN_STATE="curtain state";
    public static String SEND_ACTION_CURRENT_TEMPTURE="current tempture";
    public static String SEND_ACTION_CURRENT_HUMIDITY="current humidity";
    public static String SEND_ACTION_CURRENT_LIGHT="current light";
    public static String SEND_ACTION_CLOCK_MES="clock mes";

    //定义操作码
    public static final int init_view = 0;
    public static final int find_bluetooth = 1;
    public static final int connect_bluetooth = 3;
    public static final int get_bluetooth_state = 10;
    public static final int find_default_bluetooth = 11;
    public static final int open_bluetooth = 12;
    public static final int close_bluetooth = 13;
    public static final int open_curtain = 2;
    public static final int close_curtain = 4;
    public static final int auto_control_mode = 5;
    public static final int manual_control_mode = 6;
    public static final int clock_set_enable = 7;
    public static final int clock_set_disable = 8;
    public static final int send_message = 9;
    public static final int set_tempture_threshold = 14;
    public static final int set_humidity_threshold = 15;
    public static final int set_light_threshold = 16;
    public static final int set_clock_time = 17;
    public static final int set_stop_all = 18;

    private CMDReceiver cmdReceiver;
    private Intent dataIntent = new Intent();
    private final Intent mainIntent = new Intent();

    private RWxml rWxml;
    private Map<String, String> map = null;
    private Map<String, Object> map_bluetooth;

    private SerializableMap myMap = null;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private AcceptThread mAcceptThread;
    private timeThread mTimeClock;

    private int mState;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private BluetoothDevice bluetoothDevice;
    private static final String TAG = "test";

    public static int SCAN_STATE = 0; //0  未搜索  1  正在搜索   2  停止搜索  3  搜索绑定设备  4已绑定设备搜索完成
    public static int INIT_ERROR = 0;//0表示蓝牙设备正常  -1代表找不到蓝牙设备
    public static int BLUETOOTH_STATE = 0;//0 表示蓝牙关  1 表示蓝牙开   2  表示正在打开

    private boolean registerFlag = false;
    private Bundle bundle;

    private List<Map<String, Object>> bluetooth_devices = new ArrayList<Map<String, Object>>();
    private boolean bluetooth_list_change = false;
    private Intent receiverIntent;
    private boolean run = true;

    public mainService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("test", "this come from service onCreate()");
        dataIntent.setAction(ACTION_RECEIVER_DATA);
        mainIntent.setAction(MainActivity.MAIN_RECEIVER);
        registerReceiver();
        start();
    }

    private void registerReceiver() {
        Log.e("test", "this come from service registerReceiver");
        registerFlag = true;
        cmdReceiver = new CMDReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        filter.addAction(ACTION_CTL_SERVICE);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(cmdReceiver, filter);//注册Broadcast Receiver

    }

    //操作
    public void serviceDo() {
        switch (control) {
            case init_view:
                getUIData();
                myMap = new SerializableMap();
                myMap.setMap(map);
                bundle = new Bundle();
                bundle.putSerializable("map", myMap);
                dataIntent.putExtra("info", SEND_ACTION_VIEW_DATA);
                dataIntent.putExtras(bundle);
                sendBroadcast(dataIntent);
                break;
            case find_bluetooth://搜索蓝牙
                Log.e("test", "find bluetooth!");
                bluetooth_devices = getBluetoothList(true);
                new scanNewDevice().start();
                break;
            case connect_bluetooth: //连接指定蓝牙
                Log.e("test", "connect bluetooth!");
                String device = receiverIntent.getStringExtra("device");
                String address = receiverIntent.getStringExtra("address");
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                connect(bluetoothDevice);
                break;
            case find_default_bluetooth://搜索已绑定的蓝牙设备返回列表
                Log.e("test", "find default bluetooth!");
                bluetooth_devices = getBluetoothList(false);
                SerializableObject list = new SerializableObject();
                list.setList(bluetooth_devices);
                bundle = new Bundle();
                bundle.putSerializable("bluetooth_list", list);
                dataIntent.putExtra("info", SEND_ACTION_BLUETOOTH_LIST);
                dataIntent.putExtras(bundle);
                sendBroadcast(dataIntent);
                break;

            case get_bluetooth_state:
                if (check()) {
                    boolean b = blueState();
                    Log.e("test", "get bluetooth state!");
                    dataIntent.putExtra("info", SEND_ACTION_GET_BLUETOOTH_STATE);
                    dataIntent.putExtra("bluetooth_state", b);
                    sendBroadcast(dataIntent);
                }
                break;
            case open_bluetooth:
                if (check()) {
                    openBlueTooth();
                }
                new Thread() {
                    @Override
                    public void run() {
                        while (run) {
                            if (check()) {
                                start();
                                Log.e("test", "you are in thread start Acceptthread");
                                run = false;
                            }
                        }
                    }
                };
                break;
            case close_bluetooth:
                if (check()) {
                    stop();
                    run = true;
                    CloseBlueTooth();
                }
                break;
            case open_curtain:
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("4");
                break;
            case close_curtain:
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("3");
                break;
            case auto_control_mode:
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("0");

                break;
            case manual_control_mode:
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("1");
                break;
            case clock_set_enable:
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("clock_state", "enable");
                setUIData("clock_stop", "00:00");
                dataIntent.putExtra("info", SEND_ACTION_COLOCK_STATE);
                dataIntent.putExtra(SEND_ACTION_COLOCK_STATE, true);
                dataIntent.putExtra(SEND_ACTION_COLOCK_RESET_COLOCK, SEND_ACTION_COLOCK_RESET_COLOCK);
                sendBroadcast(dataIntent);
                break;
            case clock_set_disable:
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("clock_state", "disable");
                setUIData("clock_stop", "00:00");
                dataIntent.putExtra("info", SEND_ACTION_COLOCK_STATE);
                dataIntent.putExtra(SEND_ACTION_COLOCK_STATE, false);
                dataIntent.putExtra(SEND_ACTION_COLOCK_RESET_COLOCK, SEND_ACTION_COLOCK_RESET_COLOCK);
                sendBroadcast(dataIntent);
                break;
            case send_message:
                if (getState() != STATE_CONNECTED) {
                    dataIntent.putExtra("info", SEND_ACTION_SEND_DATA);
                    dataIntent.putExtra("write", "Write failed !");
                    break;
                }
                write(receiverIntent.getStringExtra("sendMes"));
                break;
            case set_tempture_threshold:

                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                float t_threshold = receiverIntent.getFloatExtra("set threshold", 0);
                setUIData("temperature_threshold", t_threshold + "");
                dataIntent.putExtra("info", SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD);
                dataIntent.putExtra(SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD, t_threshold);
                sendBroadcast(dataIntent);
                //发送蓝牙
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("5:"+t_threshold);

                break;
            case set_humidity_threshold:
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                float h_threshold = receiverIntent.getFloatExtra("set threshold", 0);
                setUIData("humidity_threshold", h_threshold + "");
                dataIntent.putExtra("info", SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD);
                dataIntent.putExtra(SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD, h_threshold);
                sendBroadcast(dataIntent);
                //发送蓝牙
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("6:"+h_threshold);

                break;
            case set_light_threshold:
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                float l_threshold = receiverIntent.getFloatExtra("set threshold", 0);
                setUIData("temperature_threshold", l_threshold + "");
                dataIntent.putExtra("info", SEND_ACTION_CHANGE_LIGHT_THRESHOLD);
                dataIntent.putExtra(SEND_ACTION_CHANGE_LIGHT_THRESHOLD, l_threshold);
                sendBroadcast(dataIntent);
                //发送蓝牙
                if (getState() != STATE_CONNECTED) {
                    break;
                }
                write("7:"+l_threshold);

                break;
            case set_clock_time:
                String endTime = receiverIntent.getStringExtra("clock time");
                String clockMes=receiverIntent.getStringExtra("clock mes");
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                Log.e("test","get endtime "+endTime);
                setUIData("clock_stop", endTime);

                dataIntent.putExtra("info", SEND_ACTION_CLOCK_MES);
                dataIntent.putExtra(SEND_ACTION_CLOCK_MES, clockMes);
                sendBroadcast(dataIntent);

                if(mTimeClock!=null)
                {
                    mTimeClock.cancel();
                    mTimeClock=null;
                }
                mTimeClock = new timeThread(endTime, clockMes);
                mTimeClock.start();

                break;
            case set_stop_all:
                stop();
                break;
            default:
                break;
        }
    }

    public void setUIData(String tag, String tagValue) {
        File wfile = new File(initapp.filepath);
        File rfile = new File("/mnt/sdcard/superCurtain/olddata.xml");
        Log.e("test", "set ui data");
        try {
            InputStream is = new FileInputStream(rfile);
            OutputStream os = new FileOutputStream(wfile);
            rWxml.setTag(is, os, tag, tagValue);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class scanNewDevice extends Thread {
        @Override
        public void run() {
            while (SCAN_STATE == 1) {
                try {
                    Thread.sleep(1000);
                    //发送搜索到的蓝牙设备
                    Log.e(TAG, " scan state " + SCAN_STATE);
                    if (bluetooth_list_change) {
                        SerializableObject list = new SerializableObject();
                        list.setList(bluetooth_devices);
                        bundle = new Bundle();
                        bundle.putSerializable("bluetooth_list", list);
                        dataIntent.putExtra("info", SEND_ACTION_BLUETOOTH_LIST);
                        dataIntent.putExtras(bundle);
                        sendBroadcast(dataIntent);
                        bluetooth_list_change = false;
                    }

                } catch (InterruptedException e) {

                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiverIntent = intent;
        //得到请求
        control = intent.getIntExtra("control", -1);
        if (control != -1) serviceDo();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (registerFlag) {
            unregisterReceiver(cmdReceiver);
        }
        this.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public final boolean blueState() {
        return bluetoothAdapter.isEnabled();
    }

    public Map<String, String> getUIData() {
        File file = new File(initapp.filepath);
        try {
            InputStream is = new FileInputStream(file);
            rWxml = new RWxml();
            map = rWxml.readXml(is);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean check() {
        if (bluetoothAdapter != null) {
            INIT_ERROR = 0;
            return true;
        } else {
            INIT_ERROR = -1;
            return false;
        }
    }

    public void openBlueTooth() {
        if (this.blueState()) {
            BLUETOOTH_STATE = 1;
        } else {
            bluetoothAdapter.enable();
            BLUETOOTH_STATE = 1;
        }

    }

    public void CloseBlueTooth() {
        if (this.blueState()) {
            BLUETOOTH_STATE = 0;
            bluetoothAdapter.disable();
        }
    }


    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.e(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        //发送广播通知主界面
        if (mState == STATE_CONNECTING) {
            //正在连接蓝牙
            dataIntent.putExtra("info", SEND_ACTION_CONNECTING);
            sendBroadcast(dataIntent);
        } else if (mState == STATE_CONNECTED) {
            //完成连接后发送
            dataIntent.putExtra("info", SEND_ACTION_CONNECTED);
            sendBroadcast(dataIntent);
        }
    }


    public synchronized int getState() {
        return mState;
    }


    public synchronized void start() {
        Log.e(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread == null) {
            if (blueState()) {
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            }
        }

        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.e("test", "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.e(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.e(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if(mTimeClock!=null)
        {
            mTimeClock.cancel();
            mTimeClock=null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param message
     * @see ConnectedThread#write(byte[])
     */
    public void write(String message) {
        // Get the message bytes and tell the BluetoothChatService to write
        byte[] send = message.getBytes();
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(send);
    }

    private void connectionFailed() {
        dataIntent.putExtra("info", SEND_ACTION_CONNECT_FAILED);
        sendBroadcast(dataIntent);
        mainService.this.start();
    }

    private void connectionLost() {
        dataIntent.putExtra("info", SEND_ACTION_CONNECTION_LOST);
        sendBroadcast(dataIntent);
        mainService.this.start();
    }

    public List<Map<String, Object>> getBluetoothList(boolean scan) {
        findPairedDevices();
        if (scan) {
            scanNewBluetoothDevice();
        }
        if (bluetooth_devices != null) {
            return bluetooth_devices;
        }
        return null;
    }

    //得到绑定过的蓝牙设备
    public List<Map<String, Object>> findPairedDevices() {
        SCAN_STATE = 3;
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        if (bluetooth_devices.size() != 0) {
            bluetooth_devices.clear();
        }
        int size = bluetooth_devices.size();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                map_bluetooth = new HashMap<String, Object>();
                if (device.getName() != null || device.getName() != "") {
                    map_bluetooth.put("device", device.getName());
                    Log.e("test", "device " + device.getName());
                } else {
                    map_bluetooth.put("device", "未知设备");
                }
                if (device.getAddress() != null || device.getAddress() != "") {
                    map_bluetooth.put("address", device.getAddress());
                } else {
                    map_bluetooth.put("device", "获取地址出错");
                }
                bluetooth_devices.add(map_bluetooth);
            }
        }
        if (size != bluetooth_devices.size()) {
            bluetooth_list_change = true;
        }
        SCAN_STATE = 4;
        return bluetooth_devices;
    }

    //寻找新设备
    public void scanNewBluetoothDevice() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            SCAN_STATE = 0;
        }
        bluetoothAdapter.startDiscovery();
        SCAN_STATE = 1;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {

                tmp = device.createRfcommSocketToServiceRecord(
                        MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.e("test", "BEGIN mConnectThread ");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e("test", "unable to close() " +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (mainService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("test", "close() of connect  socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.e("test", "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("test", "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.e("test", "BEGIN mConnectedThread");
            //byte[] buffer = new byte[1024];
            byte[] head = new byte[1];
            String sign = "&";
            String endsign = "%";
            String preMes = "";
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {

                    //寻找消息头
                    // Read from the InputStream
                    //bytes = mmInStream.read(buffer);
                    bytes = mmInStream.read(head, 0, 1);
                    String headSign = new String(head, 0, bytes);
                    while (headSign.equals(sign) == false) {
                        bytes = mmInStream.read(head, 0, 1);
                        headSign = new String(head, 0, bytes);
                        if (headSign.equals(sign)) break;
                    }

                    //寻找消息尾
                    byte[] end = new byte[1];
                    bytes = mmInStream.read(end, 0, 1);
                    String message = "";
                    String endSign = new String(end, 0, bytes);
                    while (endSign.equals(endsign) == false) {
                        message += endSign;
                        bytes = mmInStream.read(end, 0, 1);
                        endSign = new String(end, 0, bytes);
                        if (endSign.equals(endsign)) break;
                    }
                    //屏蔽过多的一样的消息
                    if (!preMes.equals(message)) {
                        analyzeMes(message);
                        dataIntent.putExtra("info", SEND_ACTION_RECEIVER_DATA);
                        dataIntent.putExtra("receiver", message);
                        //处理接收到的信息
                        sendBroadcast(dataIntent);
                        preMes = message;
                    }

                } catch (IOException e) {
                    Log.e("test", "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    mainService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

            } catch (IOException e) {
                Log.e("test", "Exception during write", e);
                // 写消息
                dataIntent.putExtra("info", SEND_ACTION_SEND_DATA);
                dataIntent.putExtra("write", "Write failed !");
                sendBroadcast(dataIntent);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * @param oldPath String 原文件路径
     * @param newPath String 复制后路径
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                Log.e("test", "copy file!");
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Accept",
                        MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.e(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (mainService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.e(TAG, "END mAcceptThread");

        }

        public void cancel() {
            Log.e(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    //消息解析
    public void analyzeMes(String mes) {
        String head = "";
        float num = 0;
        Log.e("test", mes);
        if (mes.contains(":")) {
            int index = mes.indexOf(":");
            head = mes.substring(0, index - 1);
            try {
                num = Float.parseFloat(mes.substring(index + 2));
            }catch(NumberFormatException e)
            {
                e.printStackTrace();
            }
            Log.e("get  some number", "mes : " + head + ":" + num);
        } else {
            head = mes;
        }
        switch (head) {
            case "automatic mode":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("control_mode", "auto");
                dataIntent.putExtra("info", SEND_ACTION_CONTROL_MODE);
                dataIntent.putExtra(SEND_ACTION_CONTROL_MODE, false);
                sendBroadcast(dataIntent);

                break;
            case "manual mode":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("control_mode", "manual");
                dataIntent.putExtra("info", SEND_ACTION_CONTROL_MODE);
                dataIntent.putExtra(SEND_ACTION_CONTROL_MODE, true);
                sendBroadcast(dataIntent);
                break;

            case "Curtain had closed !":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("curtain_state", "close");
                dataIntent.putExtra("info", SEND_ACTION_CURTAIN_STATE);
                dataIntent.putExtra(SEND_ACTION_CURTAIN_STATE, false);
                sendBroadcast(dataIntent);
                break;
            case "Curtain had opened !":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("curtain_state", "open");
                dataIntent.putExtra("info", SEND_ACTION_CURTAIN_STATE);
                dataIntent.putExtra(SEND_ACTION_CURTAIN_STATE, true);
                sendBroadcast(dataIntent);
                break;
            case "The control mode is auto":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("control_mode", "auto");
                dataIntent.putExtra("info", SEND_ACTION_CONTROL_MODE);
                dataIntent.putExtra(SEND_ACTION_CONTROL_MODE, false);
                sendBroadcast(dataIntent);
                break;
            case "The control mode is manual":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("control_mode", "manual");
                dataIntent.putExtra("info", SEND_ACTION_CONTROL_MODE);
                dataIntent.putExtra(SEND_ACTION_CONTROL_MODE, true);
                sendBroadcast(dataIntent);
                break;
            case "Tempture threshold":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("temperature_threshold", num+"");
                dataIntent.putExtra("info", SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD);
                dataIntent.putExtra(SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD, num+"");
                sendBroadcast(dataIntent);
                mainIntent.putExtra("info", "tempture_threshold");
                mainIntent.putExtra("tempture_threshold", num+"");
                sendBroadcast(mainIntent);
                break;
            case "Humidity threshold":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("humidity_threshold",num+"");
                dataIntent.putExtra("info", SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD);
                dataIntent.putExtra(SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD, num+"");
                sendBroadcast(dataIntent);
                mainIntent.putExtra("info", "humidity_threshold");
                mainIntent.putExtra("humidity_threshold", num+"");
                sendBroadcast(mainIntent);
                break;
            case "Light threshold":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("brightness_threshold",num+"");
                dataIntent.putExtra("info", SEND_ACTION_CHANGE_LIGHT_THRESHOLD);
                dataIntent.putExtra(SEND_ACTION_CHANGE_LIGHT_THRESHOLD, num+"");
                sendBroadcast(dataIntent);
                mainIntent.putExtra("info", "light_threshold");
                mainIntent.putExtra("light_threshold",num+"");
                sendBroadcast(mainIntent);
                break;
            case "environment tempture":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("temperature", num+"");
                dataIntent.putExtra("info", SEND_ACTION_CURRENT_TEMPTURE);
                dataIntent.putExtra(SEND_ACTION_CURRENT_TEMPTURE, num+"");
                sendBroadcast(dataIntent);
                break;
            case "environment humidity":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("humidity", num+"");
                dataIntent.putExtra("info", SEND_ACTION_CURRENT_HUMIDITY);
                dataIntent.putExtra(SEND_ACTION_CURRENT_HUMIDITY, num+"");
                sendBroadcast(dataIntent);
                break;
            case "environment light":
                Log.e("get  some ", "mes : " + head);
                copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                setUIData("brightness", num+"");
                dataIntent.putExtra("info", SEND_ACTION_CURRENT_LIGHT);
                dataIntent.putExtra(SEND_ACTION_CURRENT_LIGHT, num+"");
                sendBroadcast(dataIntent);
                break;
        }
    }

    //定时线程
    private class timeThread extends Thread
    {
        int hour=0;
        int minute=0;
        int curHour=0;
        int curMinute=0;
        boolean timerun=true;
        final Calendar mCalendar;
        String message="";
        public timeThread(String time,String mes)
        {
            message=mes;
            String[] timeMes=time.split(":");
            hour=Integer.parseInt(timeMes[0]);
            minute=Integer.parseInt(timeMes[1]);
            mCalendar = Calendar.getInstance();
            curHour = mCalendar.get(Calendar.HOUR);
            curMinute = mCalendar.get(Calendar.MINUTE);
        }

        @Override
        public void run() {
            while (timerun)
            {
                try {
                    Thread.sleep(1000);
                    curHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                    curMinute = mCalendar.get(Calendar.MINUTE);
                    //通过log看出  线程中好像获取不到系统时间？
                    Log.e("test  time thread",hour+":"+minute+" vs "+curHour+":"+curMinute);
                    //此处处理还有问题
                    if(hour==curHour&&minute<curMinute)
                    {
                        //Log.e("test  time thread",hour+":"+minute+" vs "+curHour+":"+curMinute);
                        if(mConnectedThread!=null)
                        {
                            write(message);
                            timerun=false;
                            copyFile(initapp.filepath, "/mnt/sdcard/superCurtain/olddata.xml");
                            setUIData("clock_state", "disable");
                            setUIData("clock_stop", "00:00");
                            dataIntent.putExtra("info", SEND_ACTION_COLOCK_STATE);
                            dataIntent.putExtra(SEND_ACTION_COLOCK_STATE, false);
                            dataIntent.putExtra(SEND_ACTION_COLOCK_RESET_COLOCK, SEND_ACTION_COLOCK_RESET_COLOCK);
                            sendBroadcast(dataIntent);
                        }
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void cancel()
        {
            timerun=false;
        }
    }

    //接收请求信息，传递给service
    public class CMDReceiver extends BroadcastReceiver {

        public CMDReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int size = bluetooth_devices.size();
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                map_bluetooth = new HashMap<String, Object>();
                if (device.getName() != null || device.getName() != "") {
                    map_bluetooth.put("device", device.getName());
                    Log.e("test", "find extra device : " + device.getName());
                } else {
                    map_bluetooth.put("device", "未知设备");
                }
                if (device.getAddress() != null || device.getAddress() != "") {
                    map_bluetooth.put("address", device.getAddress());
                } else {
                    map_bluetooth.put("device", "获取地址出错");
                }
                bluetooth_devices.add(map_bluetooth);
                if (size != bluetooth_devices.size()) {
                    bluetooth_list_change = true;
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                SCAN_STATE = 2;
                Log.e("test", "scan success " + bluetooth_devices.size());
            } else if (ACTION_CTL_SERVICE.equals(action)) {

                int code = intent.getIntExtra("control", -1);

                Log.e("test", "service receive " + code);
                onStartCommand(intent, 0, 0);
            }
        }

    }


}

