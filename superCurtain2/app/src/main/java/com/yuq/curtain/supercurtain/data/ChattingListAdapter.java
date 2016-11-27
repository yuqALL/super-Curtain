package com.yuq.curtain.supercurtain.data;

import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuq.curtain.supercurtain.R;

import java.util.ArrayList;
import java.util.List;

public class ChattingListAdapter extends BaseAdapter {

    private final int VIEW_TYPE_COUNT = 12;
    private final int VIEW_TYPE_LEFT_TEXT = 0;
    private final int VIEW_TYPE_RIGHT_TEXT = 1;

    private int mesFlag = 0;
    private int premesFlag = 0;

    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<ImMsgBean> mData;

    public ChattingListAdapter(Activity activity) {
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
    }

    public void addData(List<ImMsgBean> data) {
        if (data == null || data.size() == 0) {
            return;
        }
        if (mData == null) {
            mData = new ArrayList<>();
        }
        for (ImMsgBean bean : data) {
            addData(bean, false, false);
        }
        this.notifyDataSetChanged();
    }

    public void addData(ImMsgBean bean, boolean isNotifyDataSetChanged, boolean isFromHead) {
        if (bean == null) {
            return;
        }
        if (mData == null) {
            mData = new ArrayList<>();
        }

        if (isFromHead) {
            mData.add(0, bean);
        } else {
            mData.add(bean);
        }

        if (isNotifyDataSetChanged) {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getMsgType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImMsgBean bean = mData.get(position);
        int type = getItemViewType(position);
        View holderView = null;
        switch (type) {
            case VIEW_TYPE_LEFT_TEXT:
                ViewHolderLeftText leftholder;
                if (holderView == null || premesFlag != mesFlag) {
                    premesFlag = mesFlag;
                    leftholder = new ViewHolderLeftText();
                    holderView = mInflater.inflate(R.layout.listitem_cha_left_text, null);
                    holderView.setFocusable(true);
                    leftholder.iv_avatar = (ImageView) holderView.findViewById(R.id.iv_avatar_come);
                    leftholder.tv_content = (TextView) holderView.findViewById(R.id.tv_content_come);
                    holderView.setTag(leftholder);
                } else {
                    leftholder = (ViewHolderLeftText) holderView.getTag();
                }
                convertView = holderView;
                disPlayLeftTextView(position, convertView, leftholder, bean);
                break;
            case VIEW_TYPE_RIGHT_TEXT:
                ViewHolderRightText rightholder;
                mesFlag = VIEW_TYPE_RIGHT_TEXT;
                if (holderView == null || premesFlag != mesFlag) {
                    premesFlag = mesFlag;
                    rightholder = new ViewHolderRightText();
                    holderView = mInflater.inflate(R.layout.listitem_chat_right_text, null);
                    holderView.setFocusable(true);
                    rightholder.iv_avatar_right = (ImageView) holderView.findViewById(R.id.iv_avatar_sender);
                    rightholder.tv_content_right = (TextView) holderView.findViewById(R.id.tv_content_sender);
                    holderView.setTag(rightholder);
                } else {
                    rightholder = (ViewHolderRightText) holderView.getTag();
                }
                convertView = holderView;
                disPlayRightTextView(position, convertView, rightholder, bean);
                break;
            default:
                convertView = new View(mActivity);
                break;
        }
        return convertView;
    }

    public void disPlayLeftTextView(int position, View view, ViewHolderLeftText holder, ImMsgBean bean) {
        setContent(holder.tv_content, bean.getContent());
    }

    public void disPlayRightTextView(int position, View view, ViewHolderRightText holder, ImMsgBean bean) {
        setContent(holder.tv_content_right, bean.getContent());
    }

    public void setContent(TextView tv_content, String content) {
        tv_content.setText(content);
    }


    public final class ViewHolderLeftText {
        public ImageView iv_avatar;
        public TextView tv_content;
    }

    public final class ViewHolderRightText {
        public ImageView iv_avatar_right;
        public TextView tv_content_right;
    }

}