package com.yuq.curtain.supercurtain.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import com.yuq.curtain.supercurtain.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuq32 on 2016/10/3.
 */
public class RWxml {
    private Context context;
    private XmlResourceParser xrp;
    private XmlSerializer xs;
    private Map<String, String> xmlTags;

    public RWxml(Context context) {
        this.context = context;
    }

    public RWxml() {

    }

    //寻找指定默认标签的内容
    public String getDefaultTag(String tag) {
        xrp = context.getResources().getXml(R.xml.data);
        try {

            //如果读到结束标签结束循环
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    //获取标签名
                    String tagName = xrp.getName();
                    if (tagName.equals(tag)) {
                        return xrp.nextText();
                    } else {
                        return "unknown";
                    }
                }

                //获取下一个解析
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "解析出错";
    }

    //获取所有的节点默认数据并返回
    public Map<String, String> readDefaultXml() {

        xmlTags = new HashMap<String, String>();
        xrp = context.getResources().getXml(R.xml.data);
        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    String tagValue=null;
                    //获取标签名
                    String tagName = xrp.getName();
                    if(!"curtain".equals(tagName)) {
                        tagValue = xrp.nextText();
                        xmlTags.put(tagName, tagValue);
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xmlTags;
    }

    public String getTag(InputStream is, String tag) {
        // 利用ANDROID提供的API快速获得pull解析器
        XmlPullParser pullParser = Xml.newPullParser();
        try {
            // 设置需要解析的XML数据
            pullParser.setInput(is, "UTF-8");
            //如果读到结束标签结束循环
            while (pullParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (pullParser.getEventType() == XmlResourceParser.START_TAG) {
                    //获取标签名
                    String tagName = pullParser.getName();
                    if (tagName.equals(tag)) {
                        return pullParser.nextText();
                    } else {
                        return "unknown";
                    }
                }

                //获取下一个解析
                pullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "解析出错";
    }

    public Map<String, String> readXml(InputStream is) {
        // 利用ANDROID提供的API快速获得pull解析器
        XmlPullParser pullParser = Xml.newPullParser();

        xmlTags = new HashMap<String, String>();

        try {
            // 设置需要解析的XML数据
            pullParser.setInput(is, "UTF-8");

            while (pullParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (pullParser.getEventType() == XmlResourceParser.START_TAG) {
                    //获取标签名
                    String tagName = pullParser.getName();
                    String tagValue =null;
                    if(!"curtain".equals(tagName)) {
                        tagValue = pullParser.nextText();
                        xmlTags.put(tagName, tagValue);
                    }

                    xmlTags.put(tagName, tagValue);
                }
                pullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xmlTags;
    }

    //设置指定标签的内容
    public boolean setTag(InputStream is,OutputStream os, String tag, String tagValue) throws Exception {

        xs = Xml.newSerializer();
        xs.setOutput(os, "UTF-8");
        xs.startDocument("UTF-8", true);
        Map<String, String> xmlTags = readXml(is);
        Set set = xmlTags.entrySet();
        Iterator it = set.iterator();
        xs.startTag(null, "curtain");
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            if (((String) me.getKey()).equals(tag)) {
                me.setValue(tagValue);
                xs.startTag(null, (String) me.getKey());
                xs.text((String) me.getValue());
                xs.endTag(null, (String) me.getKey());
                Log.e("test","you change value of "+tag+" :"+tagValue);
            } else {
                if("curtain".equals(me.getKey())) {
                        continue;
                }else{
                    xs.startTag(null, (String) me.getKey());
                    xs.text((String) me.getValue());
                    xs.endTag(null, (String) me.getKey());
                }
            }
        }
        xs.endTag(null, "curtain");
        xs.endDocument();
        os.flush();
        os.close();
        return true;
    }

    //设置xml内容
    public boolean writeXml(OutputStream os, Map<String, String> xmlTags) throws Exception {
        xs = Xml.newSerializer();
        xs.setOutput(os, "UTF-8");
        xs.startDocument("UTF-8", true);
        xs.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        Set set = xmlTags.entrySet();
        Iterator it = set.iterator();
        xs.startTag(null, "curtain");
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            System.out.println(me.getKey() + ":" + me.getValue() + ":" + me.hashCode());
            xs.startTag(null, (String) me.getKey());
            xs.text((String) me.getValue());
            xs.endTag(null, (String) me.getKey());
        }
        xs.endTag(null, "curtain");
        xs.endDocument();
        os.flush();
        os.close();
        return true;
    }

}
