package com.yuq.curtain.supercurtain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.yuq.curtain.supercurtain.data.ChattingListAdapter;
import com.yuq.curtain.supercurtain.data.ImMsgBean;
import com.yuq.curtain.supercurtain.data.blueToothAdapter;
import com.yuq.curtain.supercurtain.data.mainService;
import com.yuq.curtain.supercurtain.utils.SerializableMap;
import com.yuq.curtain.supercurtain.utils.SerializableObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private final Intent serviceIntent = new Intent();//创建Intent对象
    private final Intent updateIntent = new Intent();
    private static final String ACTION_CTL_SERVICE = "com.yuq.control.service"; //控制service，service接收这个广播
    private mainReceiver receiver;
    public static final String MAIN_RECEIVER = "com.yuq.main.receiver";
    private double temptureThreshold = 0, humidityThreshold = 0, lightThreshold = 0;
    private float temptureThresholdStart = 0, temptureThresholdEnd = 50,
            humidityThresholdStart = 0, humidityThresholdEnd = 1500,
            lightThresholdStart = 0, lightThresholdEnd = 3000,
            defaultTemptureThreshold = 25, defaultHumidityThreshold = 1000, defaultLightThreshold = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateIntent.setAction(ACTION_CTL_SERVICE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        receiver = new mainReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        filter.addAction(MAIN_RECEIVER);
        registerReceiver(receiver, filter);//注册Broadcast Receiver

        //启动service并绑定
        Log.e("test", "you start service");
        serviceIntent.setClass(MainActivity.this, mainService.class);
        this.startService(serviceIntent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {//重写onStart方法

        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.set_menu_tempture_threshold) {
            int progress = (int) (temptureThreshold / (temptureThresholdEnd - temptureThresholdStart) * 100);
            createSettingDialog("温度阀值", R.drawable.setting, R.layout.my_alertdialog_setting,
                    progress, temptureThresholdStart, temptureThresholdEnd, mainService.set_tempture_threshold).show();
            return true;
        } else if (id == R.id.set_menu_humidity_threshold) {
            int progress = (int) (humidityThreshold / (humidityThresholdEnd - humidityThresholdStart) * 100);
            createSettingDialog("湿度阀值", R.drawable.setting, R.layout.my_alertdialog_setting,
                    progress, humidityThresholdStart, humidityThresholdEnd, mainService.set_humidity_threshold).show();
            return true;
        } else if (id == R.id.set_menu_light_threshold) {
            int progress = (int) (lightThreshold / (lightThresholdEnd - lightThresholdStart) * 100);
            createSettingDialog("亮度阀值", R.drawable.setting, R.layout.my_alertdialog_setting,
                    progress, lightThresholdStart, lightThresholdEnd, mainService.set_light_threshold).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public AlertDialog createSettingDialog(String title, int icon, int layout, int progress, final float start, final float end, final int control) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(getResources().getDrawable(icon));
        builder.setTitle(title);
        LinearLayout mAlertLayout = (LinearLayout) getLayoutInflater().inflate(layout, null);
        builder.setView(mAlertLayout);
        //透明
        final AlertDialog dialog = builder.create();
        final SeekBar seekBar = (SeekBar) mAlertLayout.findViewById(R.id.seekBar);
        seekBar.setProgress(progress);
        TextView tvStart = (TextView) mAlertLayout.findViewById(R.id.set_start);
        TextView tvEnd = (TextView) mAlertLayout.findViewById(R.id.set_end);
        tvStart.setText(start + "");
        tvEnd.setText(end + "");
        Button btnSet = (Button) mAlertLayout.findViewById(R.id.btn_alert_setting);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float threshold = (float) (seekBar.getProgress() / 100.0) * (end - start);
                Log.e("test", threshold + " threshold");
                updateIntent.putExtra("control", control);
                updateIntent.putExtra("set threshold", threshold);
                sendBroadcast(updateIntent);
                dialog.cancel();
            }
        });
        Button btnCancel = (Button) mAlertLayout.findViewById(R.id.btn_alert_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        Button btnDefault = (Button) mAlertLayout.findViewById(R.id.btn_alert_default);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateIntent.putExtra("control", control);
                if (control == mainService.set_light_threshold) {
                    updateIntent.putExtra("set threshold", defaultLightThreshold);
                } else if (control == mainService.set_humidity_threshold) {
                    updateIntent.putExtra("set threshold", defaultHumidityThreshold);
                } else if (control == mainService.set_tempture_threshold) {
                    updateIntent.putExtra("set threshold", defaultTemptureThreshold);
                }
                sendBroadcast(updateIntent);
                dialog.cancel();
            }
        });
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.8f;
        window.setAttributes(lp);
        return dialog;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //this.stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ACTION_RECEIVER_DATA = "com.yuq.receiver.data";//从service接收数据
        private static final String ACTION_CTL_SERVICE = "com.yuq.control.service"; //控制service，service接收这个广播
        private DataReceiver dataReceiver;
        private final Intent updateIntent = new Intent();
        private final Intent mainIntent = new Intent();

        public Map<String, String> uiInfo;

        private ListView mBluetoothDeviceListview;
        private Button btnQuery, btnStop, btnstart, btnclose, btnClock;

        //初始化界面所需字符串
        private static final String strTempture = "temperature";
        private static final String strHumidity = "humidity";
        private static final String strBrightness = "brightness";
        private static final String strCurtainState = "curtain_state";
        private static final String strControlMode = "control_mode";
        private static final String strClockState = "clock_state";
        private static final String strTemptureThreshold = "temperature_threshold";
        private static final String strHumidityThreshold = "humidity_threshold";
        private static final String strBrightnessThreshold = "brightness_threshold";
        private static final String strClockMes = "clock_mes";
        private static final String strClockStop = "clock_stop";

        private blueToothAdapter adapter;
        private List<Map<String, Object>> bluetoothList;

        //界面组件
        private Switch switchBluetooth, switchCurtainState, switchControlMode, switchClockState;//蓝牙开关,窗帘状态，控制模式，定时状态
        private TextView temptureThreshold, tVtempture, airHumidityThreshold, tvAirHumidity, tvLightThreshold,
                tVLight, tvCurtainState, tvControlMode, tvClockState, tvClockMes;
        int sectionNumber;
        private ListView mChatListView;
        private ChattingListAdapter mChatAdapter;
        private EditText mChatEditText;
        private Button btnSend;
        private int setHour, setMinute, curHour, curMinute;

        private int progressbarFlagPosition = 0;
        private int preFlagPosition = 0;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            updateIntent.setAction(ACTION_CTL_SERVICE);
            mainIntent.setAction(MainActivity.MAIN_RECEIVER);
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);

            if (sectionNumber == 1) {
                rootView = inflater.inflate(R.layout.fragment_info, container, false);

                //注册监听
                Log.e("test", "you start broadcast");
                dataReceiver = new DataReceiver();
                IntentFilter filter = new IntentFilter();//创建IntentFilter对象
                filter.addAction(ACTION_RECEIVER_DATA);
                getActivity().registerReceiver(dataReceiver, filter);//注册Broadcast Receiver


                mBluetoothDeviceListview = (ListView) rootView.findViewById(R.id.my_bluetooth_device_listview);
                mBluetoothDeviceListview.setOnItemClickListener(this);

                switchBluetooth = (Switch) rootView.findViewById(R.id.switchBluetooth);
                switchCurtainState = (Switch) rootView.findViewById(R.id.switchCurtainState);
                switchControlMode = (Switch) rootView.findViewById(R.id.switchControlMode);
                switchClockState = (Switch) rootView.findViewById(R.id.switchClockState);

                btnQuery = (Button) rootView.findViewById(R.id.btnQuery);
                btnStop = (Button) rootView.findViewById(R.id.btnStop);
                btnstart = (Button) rootView.findViewById(R.id.btnstart);
                btnclose = (Button) rootView.findViewById(R.id.btnclose);
                btnClock = (Button) rootView.findViewById(R.id.btnClock);

                temptureThreshold = (TextView) rootView.findViewById(R.id.tempture_threshold);
                tVtempture = (TextView) rootView.findViewById(R.id.tVtempture);
                airHumidityThreshold = (TextView) rootView.findViewById(R.id.air_humidity_threshold);
                tvAirHumidity = (TextView) rootView.findViewById(R.id.tvAirHumidity);
                tvLightThreshold = (TextView) rootView.findViewById(R.id.light_threshold);
                tVLight = (TextView) rootView.findViewById(R.id.tVLight);
                tvCurtainState = (TextView) rootView.findViewById(R.id.tv_curtain_state);
                tvControlMode = (TextView) rootView.findViewById(R.id.tv_control_mode);
                tvClockState = (TextView) rootView.findViewById(R.id.tv_clock_state);
                tvClockMes = (TextView) rootView.findViewById(R.id.tv_clock_mes);

                switchBluetooth.setOnClickListener(this);
                switchControlMode.setOnClickListener(this);
                switchClockState.setOnClickListener(this);
                btnQuery.setOnClickListener(this);
                btnStop.setOnClickListener(this);
                btnstart.setOnClickListener(this);
                btnclose.setOnClickListener(this);
                btnClock.setOnClickListener(this);

                Log.e("test", "you send message");
                //广播请求初始化界面

                updateIntent.putExtra("control", mainService.init_view);
                getActivity().sendBroadcast(updateIntent);

                updateIntent.putExtra("control", mainService.find_default_bluetooth);
                getActivity().sendBroadcast(updateIntent);

                updateIntent.putExtra("control", mainService.get_bluetooth_state);
                getActivity().sendBroadcast(updateIntent);


            } else if (sectionNumber == 2) {
                rootView = inflater.inflate(R.layout.fragment_serial, container, false);

                //注册监听
                Log.e("test", "you start broadcast");
                dataReceiver = new DataReceiver();
                IntentFilter filter = new IntentFilter();//创建IntentFilter对象
                filter.addAction(ACTION_RECEIVER_DATA);
                getActivity().registerReceiver(dataReceiver, filter);//注册Broadcast Receiver

                mChatListView = (ListView) rootView.findViewById(R.id.lv_chat);
                mChatEditText = (EditText) rootView.findViewById(R.id.et_chat);
                btnSend = (Button) rootView.findViewById(R.id.btn_send);

                mChatAdapter = new ChattingListAdapter(getActivity());


                mChatListView.setAdapter(mChatAdapter);

                mChatEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) {
                            btnSend.setBackground(getResources().getDrawable(R.drawable.btn_send_have_text));
                        } else {
                            btnSend.setBackground(getResources().getDrawable(R.drawable.btn_send_bg_disable));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String msg = mChatEditText.getText().toString();
                        addOneMes(msg, 1);
                        mChatEditText.setText("");
                        updateIntent.putExtra("control", mainService.send_message);
                        updateIntent.putExtra("sendMes", msg);
                        getActivity().sendBroadcast(updateIntent);
                    }
                });

                ArrayList<String> mes = new ArrayList<String>();

                mes.add("欢迎回来");
                mes.add("智能窗帘");
                mes.add("此界面为蓝牙串口");
                mes.add("连接蓝牙后可与蓝牙通讯");

                addArrayMes(mes, 0);
            }


            return rootView;
        }

        private void scrollToBottom() {
            mChatListView.requestLayout();
            mChatListView.post(new Runnable() {
                @Override
                public void run() {
                    mChatListView.setSelection(mChatListView.getBottom());
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();

            if (sectionNumber == 1) {
                updateIntent.putExtra("control", mainService.init_view);
                getActivity().sendBroadcast(updateIntent);

            }
            if (sectionNumber == 2) {

            }
        }

        @Override
        public void onPause() {
            super.onPause();

        }

        @Override
        public void onResume() {
            super.onResume();

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getActivity().unregisterReceiver(dataReceiver);
        }

        private void init_widget() {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            if (sectionNumber == 1) {
                uiInfo = dataReceiver.getMapView();
                if (uiInfo != null) {
                    Set set = uiInfo.entrySet();
                    Iterator it = set.iterator();
                    while (it.hasNext()) {
                        Map.Entry me = (Map.Entry) it.next();
                        Log.e("test", me.getKey() + ":" + me.getValue() + ":" + me.hashCode());
                        switch (me.getKey().toString()) {
                            case strTempture:
                                tVtempture.setText(me.getValue().toString() + "℃");
                                break;
                            case strHumidity:
                                tvAirHumidity.setText(me.getValue().toString());
                                break;
                            case strBrightness:
                                tVLight.setText(me.getValue().toString());
                                break;
                            case strTemptureThreshold:
                                temptureThreshold.setText(me.getValue().toString() + "℃");
                                mainIntent.putExtra("info", "tempture_threshold");
                                mainIntent.putExtra("tempture_threshold", me.getValue().toString());
                                getContext().sendBroadcast(mainIntent);
                                break;
                            case strHumidityThreshold:
                                airHumidityThreshold.setText(me.getValue().toString());
                                mainIntent.putExtra("info", "humidity_threshold");
                                mainIntent.putExtra("humidity_threshold", me.getValue().toString());
                                getContext().sendBroadcast(mainIntent);
                                break;
                            case strBrightnessThreshold:
                                tvLightThreshold.setText(me.getValue().toString());
                                mainIntent.putExtra("info", "light_threshold");
                                mainIntent.putExtra("light_threshold", me.getValue().toString());
                                getContext().sendBroadcast(mainIntent);
                                break;
                            case strClockState:
                                if ("disable".equals(me.getValue().toString())) {
                                    switchClockState.setChecked(false);
                                    tvClockState.setText("00:00");
                                } else if ("enable".equals(me.getValue().toString())) {
                                    switchClockState.setChecked(true);
                                }
                                break;
                            case strControlMode:
                                if ("auto".equals(me.getValue().toString())) {
                                    tvControlMode.setText("自动模式");
                                    switchControlMode.setChecked(false);
                                } else if ("manual".equals(me.getValue().toString())) {
                                    tvControlMode.setText("手动模式");
                                    switchControlMode.setChecked(true);
                                }
                                break;
                            case strCurtainState:
                                if ("close".equals(me.getValue().toString())) {
                                    tvCurtainState.setText("窗帘已关");
                                    switchCurtainState.setChecked(false);
                                } else if ("open".equals(me.getValue().toString())) {
                                    tvControlMode.setText("窗帘已开");
                                    switchCurtainState.setChecked(true);
                                }
                                break;
                            case strClockMes:
                                if ("3".equals(me.getValue().toString())) {
                                    tvClockMes.setText("关窗帘");
                                } else if ("4".equals(me.getValue().toString())) {
                                    tvClockMes.setText("开窗帘");
                                }
                                break;
                            case strClockStop:
                                tvClockState.setText(me.getValue().toString());
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else if (sectionNumber == 2) {


            }

        }

        //获取蓝牙列表高度并通过代码设置（解决ScrollView与ListView冲突）
        private int getListViewHeight(blueToothAdapter adapter) {
            int totalHeight = 0;
            for (int i = 0; i < adapter.getCount(); i++) {
                View listItem = adapter.getView(i, null, mBluetoothDeviceListview);
                listItem.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                totalHeight += listItem.getMeasuredHeight();
            }
            return totalHeight;
        }

        @Override
        public void onClick(View view) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            switch (view.getId()) {
                case R.id.switchBluetooth:
                    if (switchBluetooth.isChecked()) {
                        updateIntent.putExtra("control", mainService.open_bluetooth);
                        getActivity().sendBroadcast(updateIntent);
                    } else {
                        updateIntent.putExtra("control", mainService.close_bluetooth);
                        getActivity().sendBroadcast(updateIntent);
                    }
                    break;
                case R.id.switchControlMode:
                    if (switchControlMode.isChecked()) {
                        updateIntent.putExtra("control", mainService.manual_control_mode);
                        getActivity().sendBroadcast(updateIntent);
                    } else {
                        updateIntent.putExtra("control", mainService.auto_control_mode);
                        getActivity().sendBroadcast(updateIntent);
                    }
                    break;
                case R.id.switchClockState:
                    if (switchClockState.isChecked()) {
                        updateIntent.putExtra("control", mainService.clock_set_enable);
                        getActivity().sendBroadcast(updateIntent);
                    } else {
                        updateIntent.putExtra("control", mainService.clock_set_disable);
                        getActivity().sendBroadcast(updateIntent);
                    }
                    break;
                case R.id.btnQuery:
                    if (sectionNumber == 1) {
                        //广播请求搜索蓝牙
                        updateIntent.putExtra("control", mainService.find_bluetooth);
                        getActivity().sendBroadcast(updateIntent);
                    }
                    break;

                case R.id.btnStop:
                    //断开蓝牙，关闭所有thread
                    updateIntent.putExtra("control", mainService.set_stop_all);
                    getActivity().sendBroadcast(updateIntent);
                    break;
                case R.id.btnstart:
                    if (switchControlMode.isChecked()) {
                        //发送开窗命令
                        updateIntent.putExtra("control", mainService.open_curtain);
                        getContext().sendBroadcast(updateIntent);
                    } else {
                        Toast.makeText(getContext().getApplicationContext(), "您必须打开控制模式开关", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnclose:
                    if (switchControlMode.isChecked()) {
                        //发送关窗命令
                        updateIntent.putExtra("control", mainService.close_curtain);
                        getContext().sendBroadcast(updateIntent);
                    } else {
                        Toast.makeText(getContext().getApplicationContext(), "您必须打开控制模式开关", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnClock:
                    if (switchClockState.isChecked()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        LinearLayout mAlertLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.my_time_set_layout, null);
                        builder.setView(mAlertLayout);
                        //透明
                        final AlertDialog dialog = builder.create();
                        TimePicker timePicker = (TimePicker) mAlertLayout.findViewById(R.id.timePicker);
                        timePicker.setIs24HourView(true);
                        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                setHour = hourOfDay;
                                setMinute = minute;
                                Log.e("test", "you set time " + setHour + ":" + setMinute);
                            }
                        });
                        final RadioButton rbOpen = (RadioButton) mAlertLayout.findViewById(R.id.clockOpen);
                        final RadioButton rbClose = (RadioButton) mAlertLayout.findViewById(R.id.clockClose);
                        Button btnSet = (Button) mAlertLayout.findViewById(R.id.btn_alert_setting);
                        btnSet.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String hour, minute;
                                if (setHour < 10) {
                                    hour = "0" + setHour;
                                } else {
                                    hour = setHour + "";
                                }
                                if (setMinute < 10) {
                                    minute = "0" + setHour;
                                } else {
                                    minute = setHour + "";
                                }
                                Log.e("test", "you send broadcast time " + setHour + ":" + setMinute);
                                updateIntent.putExtra("control", mainService.set_clock_time);
                                updateIntent.putExtra("clock time", hour + ":" + minute);
                                if (rbOpen.isChecked()) {
                                    updateIntent.putExtra("clock mes", "4");
                                } else if (rbClose.isChecked()) {
                                    updateIntent.putExtra("clock mes", "3");
                                }
                                getContext().sendBroadcast(updateIntent);
                                tvClockState.setText(hour + ":" + minute);
                                dialog.cancel();
                            }
                        });
                        Button btnCancel = (Button) mAlertLayout.findViewById(R.id.btn_alert_cancel);
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        Window window = dialog.getWindow();
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.alpha = 0.8f;
                        window.setAttributes(lp);

                        dialog.show();

                    } else {
                        Toast.makeText(getContext().getApplicationContext(), "您必须打开定时开关", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView name = (TextView) view.findViewById(R.id.bluetooth_device_name);
            TextView tvAddress = (TextView) view.findViewById(R.id.bluetooth_device_address);
            ProgressBar bluetoothProgressBar = (ProgressBar) view.findViewById(R.id.bluetooth_connect_progressbar);
            bluetoothProgressBar.setVisibility(View.VISIBLE);
            adapter.updateItemView(preFlagPosition, mBluetoothDeviceListview, 0);
            Log.e("test", "you click position:" + position);
            progressbarFlagPosition = position;
            String device = name.getText().toString();
            String address = tvAddress.getText().toString();
            String info = name.getText().toString() + " : " + tvAddress.getText().toString();

            updateIntent.putExtra("control", mainService.connect_bluetooth);
            updateIntent.putExtra("device", device);
            updateIntent.putExtra("address", address);
            getActivity().sendBroadcast(updateIntent);
            Log.e("test", "try connect : " + info);
        }

        public void addOneMes(String mes, int mesType) {
            if (sectionNumber == 2) {
                if (!TextUtils.isEmpty(mes)) {
                    ImMsgBean bean = new ImMsgBean();
                    bean.setContent(mes);
                    bean.setMsgType(mesType);
                    mChatAdapter.addData(bean, true, false);

                    scrollToBottom();
                }
            }
        }

        public void addArrayMes(ArrayList<String> arrayMes, int mesType) {
            if (sectionNumber == 2) {
                List<ImMsgBean> beanList = new ArrayList<>();
                for (int i = 0; i < arrayMes.size(); i++) {
                    ImMsgBean bean = new ImMsgBean();
                    bean.setContent(arrayMes.get(i));
                    bean.setMsgType(mesType);
                    beanList.add(bean);
                }
                mChatAdapter.addData(beanList);
                scrollToBottom();
            }
        }

        public class DataReceiver extends BroadcastReceiver {
            private Map<String, String> mapView = null;

            public DataReceiver() {

            }

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: This method is called when the BroadcastReceiver is receiving
                String data = intent.getStringExtra("info");
                if (data.equals(mainService.SEND_ACTION_VIEW_DATA)) {
                    Bundle bundle = intent.getExtras();
                    SerializableMap myMap = (SerializableMap) bundle.get("map");
                    mapView = myMap.getMap();
                    init_widget();

                } else if (data.equals(mainService.SEND_ACTION_BLUETOOTH_LIST)) {
                    Bundle bundle = intent.getExtras();
                    SerializableObject mbluetoothList = (SerializableObject) bundle.get("bluetooth_list");
                    bluetoothList = mbluetoothList.getList();
                    Log.e("test", bluetoothList.size() + " receivered");
                    if (sectionNumber == 1) {
                        adapter = new blueToothAdapter(getContext(), bluetoothList);

                        int heightList = getListViewHeight(adapter) + 100;
                        mBluetoothDeviceListview.setAdapter(adapter);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBluetoothDeviceListview.getLayoutParams();
                        params.height = heightList;
                        mBluetoothDeviceListview.setLayoutParams(params);
                        mBluetoothDeviceListview.setFocusable(false);

                    }
                } else if (data.equals(mainService.SEND_ACTION_GET_BLUETOOTH_STATE)) {
                    boolean b = intent.getBooleanExtra("bluetooth_state", false);
                    if (sectionNumber == 1) {
                        switchBluetooth.setChecked(b);
                    }

                } else if (data.equals(mainService.SEND_ACTION_GET_DEFAULT_BLUETOOT)) {
                    Bundle bundle = intent.getExtras();
                    SerializableObject mbluetoothList = (SerializableObject) bundle.get("bluetooth_list");
                    bluetoothList = mbluetoothList.getList();
                    Log.e("test", bluetoothList.size() + " receivered");
                    if (sectionNumber == 1) {
                        adapter = new blueToothAdapter(getContext(), bluetoothList);

                        int heightList = getListViewHeight(adapter) + 100;
                        mBluetoothDeviceListview.setAdapter(adapter);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBluetoothDeviceListview.getLayoutParams();
                        params.height = heightList;
                        mBluetoothDeviceListview.setLayoutParams(params);
                        mBluetoothDeviceListview.setFocusable(false);
                        ((ScrollView) getView().findViewById(R.id.sv_info)).smoothScrollTo(0, 0);

                    }
                } else if (data.equals(mainService.SEND_ACTION_CONNECTING)) {
                    Log.e("test", mainService.SEND_ACTION_CONNECTING);
                    //Toast.makeText(getContext().getApplicationContext(), "正在连接", Toast.LENGTH_SHORT).show();
                    addOneMes("正在连接", 0);
                    if (sectionNumber == 1) {
                        adapter.updateItemView(progressbarFlagPosition, mBluetoothDeviceListview, 1);
                    }
                } else if (data.equals(mainService.SEND_ACTION_CONNECTED)) {
                    //Toast.makeText(getContext().getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                    Log.e("test", mainService.SEND_ACTION_CONNECTED);
                    addOneMes("连接成功", 0);
                    preFlagPosition = progressbarFlagPosition;
                    if (sectionNumber == 1) {
                        adapter.updateItemView(progressbarFlagPosition, mBluetoothDeviceListview, 2);
                    }
                } else if (data.equals(mainService.SEND_ACTION_RECEIVER_DATA)) {
                    //Log.e("test", mainService.SEND_ACTION_RECEIVER_DATA);
                    String mes = intent.getStringExtra("receiver");
                    addOneMes(mes, 0);

                } else if (data.equals(mainService.SEND_ACTION_SEND_DATA)) {
                    Log.e("test", mainService.SEND_ACTION_SEND_DATA);
                    String writeMessage = intent.getStringExtra("write");
                    addOneMes(writeMessage, 0);
                } else if (data.equals(mainService.SEND_ACTION_CONNECT_FAILED)) {
                    Log.e("test", mainService.SEND_ACTION_CONNECT_FAILED);
                    addOneMes("连接失败", 0);
                    if (sectionNumber == 1) {
                        adapter.updateItemView(progressbarFlagPosition, mBluetoothDeviceListview, 0);
                    }
                    Toast.makeText(getContext().getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                } else if (data.equals(mainService.SEND_ACTION_CONNECTION_LOST)) {
                    Log.e("test", mainService.SEND_ACTION_CONNECTION_LOST);
                    addOneMes("连接丢失", 0);
                    if (sectionNumber == 1) {
                        adapter.updateItemView(progressbarFlagPosition, mBluetoothDeviceListview, 0);
                    }
                    Toast.makeText(getContext().getApplicationContext(), "连接丢失", Toast.LENGTH_SHORT).show();
                } else if (data.equals(mainService.SEND_ACTION_COLOCK_STATE)) {
                    if (sectionNumber == 1) {
                        switchClockState.setChecked(intent.getBooleanExtra(data, false));
                        tvClockState.setText(intent.getStringExtra(mainService.SEND_ACTION_COLOCK_RESET_COLOCK));
                    }
                } else if (data.equals(mainService.SEND_ACTION_CONTROL_MODE)) {
                    if (sectionNumber == 1) {
                        if (intent.getBooleanExtra(mainService.SEND_ACTION_CONTROL_MODE, false)) {
                            switchControlMode.setChecked(true);
                            tvControlMode.setText("手动模式");
                        } else {
                            switchControlMode.setChecked(false);
                            tvControlMode.setText("自动模式");
                        }
                    }
                } else if (data.equals(mainService.SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD)) {
                    if (sectionNumber == 1)
                        temptureThreshold.setText(intent.getStringExtra(mainService.SEND_ACTION_CHANGE_TEMPTURE_THRESHOLD) + "℃");
                } else if (data.equals(mainService.SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD)) {
                    if (sectionNumber == 1)
                        airHumidityThreshold.setText(intent.getStringExtra(mainService.SEND_ACTION_CHANGE_HUMIDITY_THRESHOLD) + "");
                } else if (data.equals(mainService.SEND_ACTION_CHANGE_LIGHT_THRESHOLD)) {
                    if (sectionNumber == 1)
                        tvLightThreshold.setText(intent.getStringExtra(mainService.SEND_ACTION_CHANGE_LIGHT_THRESHOLD) + "");
                } else if (data.equals(mainService.SEND_ACTION_CURRENT_TEMPTURE)) {
                    if (sectionNumber == 1)
                        tVtempture.setText(intent.getStringExtra(mainService.SEND_ACTION_CURRENT_TEMPTURE) + "℃");
                } else if (data.equals(mainService.SEND_ACTION_CURRENT_HUMIDITY)) {
                    if (sectionNumber == 1)
                        tvAirHumidity.setText(intent.getStringExtra(mainService.SEND_ACTION_CURRENT_HUMIDITY));
                } else if (data.equals(mainService.SEND_ACTION_CURRENT_LIGHT)) {
                    if (sectionNumber == 1)
                        tVLight.setText(intent.getStringExtra(mainService.SEND_ACTION_CURRENT_LIGHT));
                } else if (data.equals(mainService.SEND_ACTION_CURTAIN_STATE)) {
                    if (sectionNumber == 1) {
                        boolean curtainState = intent.getBooleanExtra(mainService.SEND_ACTION_CURTAIN_STATE, false);
                        switchCurtainState.setChecked(curtainState);
                        if (curtainState) {
                            tvCurtainState.setText("窗帘已开");
                        } else {
                            tvCurtainState.setText("窗帘已关");
                        }
                    }
                } else if (data.equals(mainService.SEND_ACTION_CLOCK_MES)) {
                    if (sectionNumber == 1) {
                        if (intent.getStringExtra(mainService.SEND_ACTION_CLOCK_MES).equals("3")) {
                            tvClockMes.setText("关窗帘");
                        } else if (intent.getStringExtra(mainService.SEND_ACTION_CLOCK_MES).equals("4")) {
                            tvClockMes.setText("开窗帘");
                        }
                    }
                }
            }

            public Map<String, String> getMapView() {
                return mapView;
            }
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }


    }

    public class mainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("info");
            switch (data) {
                case "tempture_threshold":
                    temptureThreshold = Float.parseFloat(intent.getStringExtra(data).toString());
                    Log.e("test", data);
                    break;
                case "humidity_threshold":
                    humidityThreshold = Float.parseFloat(intent.getStringExtra(data).toString());
                    Log.e("test", data);
                    break;
                case "light_threshold":
                    Log.e("test", data);
                    lightThreshold = Float.parseFloat(intent.getStringExtra(data).toString());
                    break;
                default:
                    break;
            }
        }
    }
}
