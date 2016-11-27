package com.yuq.curtain.supercurtain;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yuq.curtain.supercurtain.utils.RWxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class initapp extends Activity {

    private final int SPLASH_DISPLAY_LENGHT = 2000; //延迟2秒
    private RWxml rWxml;
    public static String filepath="/mnt/sdcard/superCurtain/data.xml";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initapp);
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                createInfoFile();
                Intent mainIntent = new Intent(initapp.this,MainActivity.class);
                initapp.this.startActivity(mainIntent);
                initapp.this.finish();
            }

        }, SPLASH_DISPLAY_LENGHT);
    }

    public void createInfoFile(){
        rWxml=new RWxml(this);
        File xmlFile = new File(filepath);
        try {
            if (!xmlFile.exists()) {
                File dir = new File(xmlFile.getParent());
                dir.mkdirs();
                xmlFile.createNewFile();
                OutputStream os = new FileOutputStream(xmlFile);
                rWxml.writeXml(os, rWxml.readDefaultXml());
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
