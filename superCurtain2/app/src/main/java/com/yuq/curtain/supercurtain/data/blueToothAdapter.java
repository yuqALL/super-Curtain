package com.yuq.curtain.supercurtain.data;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yuq.curtain.supercurtain.R;

import java.util.List;
import java.util.Map;

/**
 * Created by yuq32 on 2016/10/3.
 */
public class blueToothAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater = null;
    private List<Map<String, Object>> data;

    public blueToothAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }


    @Override
    public String getItem(int i) {
        return (String) data.get(i).get("address");//返回蓝牙设备地址
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.bluetooth_device_list_item, null);
            holder.BluetoothDeviceName = (TextView) view.findViewById(R.id.bluetooth_device_name);
            holder.BluetoothDeviceAddress = (TextView) view.findViewById(R.id.bluetooth_device_address);
            holder.BluetoothConnectProgressbar = (ProgressBar) view.findViewById(R.id.bluetooth_connect_progressbar);
            holder.connectedImg = (ImageView) view.findViewById(R.id.connectedImg);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.BluetoothDeviceName.setText((String) data.get(i).get("device"));
        holder.BluetoothDeviceAddress.setText((String) data.get(i).get("address"));

        return view;
    }

    static class ViewHolder {
        public TextView BluetoothDeviceName;
        public TextView BluetoothDeviceAddress;
        public ProgressBar BluetoothConnectProgressbar;
        public ImageView connectedImg;
    }

    public void updateItemView(int position, ListView mListView, int workState) {
        int visibleFirstPosi = mListView.getFirstVisiblePosition();
        int visibleLastPosi = mListView.getLastVisiblePosition();

        if (position >= visibleFirstPosi && position <= visibleLastPosi) {
            View view = mListView.getChildAt(position - visibleFirstPosi);
//            if (workState == 1) {
//                view.setBackgroundResource(R.drawable.bluetooth_connecting);
//            } else if (workState == 2) {
//                view.setBackgroundResource(R.drawable.bluetooth_connect_bac);
//            }else if(workState==0){
//                view.setBackgroundResource(R.drawable.bluetooth_device_bac);
//            }

            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null) {
                holder.BluetoothConnectProgressbar.setVisibility(View.INVISIBLE);
                if (workState == 2) {
                    holder.connectedImg.setVisibility(View.VISIBLE);
                } else if (workState == 0) {
                    holder.connectedImg.setVisibility(View.INVISIBLE);
                }
            }
        } else {

        }
    }
}
