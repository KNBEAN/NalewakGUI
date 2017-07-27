package com.example.swiec.guibluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.triggertrap.seekarc.SeekArc;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.example.swiec.guibluetooth.DeviceConnector;

public class Woda extends AppCompatActivity implements View.OnClickListener {
    private static DeviceConnector connector;
    private static SeekArc mSeekArc, mSeekArc2;
    private ToggleButton mTbLock;
    private static ImageButton lej;
private static ImageButton stoplej;
    private static BluetoothResponseHandler mHandler;
    private static TextView progrestext;
    public static final int MESSAGE_READ = 2;
    static int wateramount;
    //private DeviceConnector connector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHandler = new BluetoothResponseHandler(this);
        connector = DeviceConnector.getInstance( );
        connector.sethandler(mHandler);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_woda);
        lej = (ImageButton) findViewById(R.id.Waterlevel);
        lej.setOnClickListener(this);
        stoplej=(ImageButton) findViewById(R.id.stoplania);
        mSeekArc = (SeekArc) findViewById(R.id.seekArc);
        mSeekArc2 = (SeekArc) findViewById(R.id.seekArcnalewanie);
        progrestext = (TextView) findViewById((R.id.progresslania));
        mSeekArc2.setProgress(200);
        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser) {
                progrestext.setText(String.valueOf(progress) + " ml");
                mSeekArc2.setProgress(1);
                lej.setClickable(true);
                stoplej.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    public void onClick(View v) {
        mSeekArc2.setProgress(1);
        lej.setClickable(false);
        stoplej.setVisibility(View.VISIBLE);
        wateramount=mSeekArc.getProgress();
        if(wateramount<25)
            wateramount=25;
        String amount=Integer.toString(wateramount);
        amount= amount+"\r\n";
        connector.Send(amount);

        /*while (mSeekArc2.getProgress() != 0) {
            mSeekArc2.setProgress(pobierzposteplania());

        }
        */
    }
    public void Stop(View v) {
        connector.Send("Stop\r\n");

    }
    /*public int pobierzposteplania() {
        //tu ustawić pobierania wartość na postęplania
        return 0;
    }*/

    @Override
    public void onBackPressed(){

    }

    static void Receive(String Rec)
    {
        //progrestext.setText(Rec);
        String[] potato = Rec.split("\r\n");
        int sizeOfPotato = potato.length-1;
        if(potato[sizeOfPotato].equals("End"))
        {
            lej.setClickable(true);
            mSeekArc2.setProgress(100);
            stoplej.setVisibility(View.INVISIBLE);
            wateramount=0;

        }
        else if(potato[sizeOfPotato].equals("ERR"))
        {
            progrestext.setText("BLAD");

        }
        else if(Rec.equals("0")) {
        }

        else
        {
           //String[] potato = Rec.split("\r\n");
            int waterget= Integer.parseInt(potato[sizeOfPotato]);
            if (waterget>wateramount)
                waterget=wateramount;
            mSeekArc2.setProgress(wateramount > 0 ? ((waterget*100)/wateramount) : 0);

        }

    }
//private static class
    private static class BluetoothResponseHandler extends Handler {
        private WeakReference<Woda> mActivity;

        public BluetoothResponseHandler(Woda activity) {
            mActivity = new WeakReference<Woda>(activity);
        }

        public void setTarget(Woda target) {
            mActivity.clear();
            mActivity = new WeakReference<Woda>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            Woda activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        final String readMessage = (String) msg.obj;
                        if (readMessage != null) {
                           // progrestext.setText(readMessage);
                           Receive(readMessage);
                        }
                        break;
                }
            }
        }
    }















}