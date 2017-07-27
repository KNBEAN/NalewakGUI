package com.example.swiec.guibluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.support.v7.app.ActionBarActivity;
/**
 * Created by Maciek on 6/16/2017.
 */

class DeviceConnector {
    private static final DeviceConnector ourInstance = new DeviceConnector();

    static DeviceConnector getInstance() {
        return ourInstance;
    }

    private DeviceConnector() {

    }


    private int mState;
    private BluetoothAdapter btAdapter;
    private ThreadConnected myThreadConnected;
    private Handler mHandler;
    private String deviceName;
    private BluetoothSocket btSocket;
    private BluetoothDevice device=null;
    private boolean isBtConnected=false;
    private String Msg_Received=null;

    public void connect(BluetoothDevice dev)
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        device=dev;
        new ConnectBT().execute();
    }
    public void sethandler(Handler h)
    {
        mHandler=h;

    }


    public void Send(String message){
        byte[] send=message.getBytes();
        try {
            btSocket.getOutputStream().write(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            // Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    try {
                        Class class1 = device.getClass();
                        Class aclass[] = new Class[1];
                        aclass[0] = Integer.TYPE;
                        Method method = class1.getMethod("createRfcommSocket", aclass);
                        Object aobj[] = new Object[1];
                        aobj[0] = Integer.valueOf(1);

                        btSocket = (BluetoothSocket) method.invoke(device, aobj);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        //if (D) Log.e(TAG, "createRfcommSocket() failed", e);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        //if (D) Log.e(TAG, "createRfcommSocket() failed", e);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        //if (D) Log.e(TAG, "createRfcommSocket() failed", e);
                    }
                    btSocket.connect();//start connection
                    //tmpIn = btSocket.getInputStream();

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                //msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                //finish();
            }
            else
            {
                isBtConnected = true;
                myThreadConnected = new ThreadConnected(btSocket);
                myThreadConnected.start();
            }
        }



    }

    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            StringBuilder readMessage = new StringBuilder();
            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    readMessage.append(strReceived);
                    final String msgReceived=readMessage.toString();
                    //readMessage.append(readed);




                    if (strReceived.contains("\n")) {
                        readMessage.setLength(0);
                        final String msgbt=msgReceived.substring(0,msgReceived.length()-2);
                        mHandler.obtainMessage(Woda.MESSAGE_READ, bytes, -1, msgbt).sendToTarget();
                    }




                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                }
            }
        }


    }













}
