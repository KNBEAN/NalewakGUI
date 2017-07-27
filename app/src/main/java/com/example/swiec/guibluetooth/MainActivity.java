package com.example.swiec.guibluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


//Blokowanie powrotu
//Unpair
//przycisk do anulowania
//coś doe errorów





public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    //private static DeviceConnector connector;
    private static final String TAG = "MainActivity";
    BluetoothAdapter blutu;
    BluetoothSocket BTsocket=null;
    BluetoothSocket tmp=null;
    InputStream tmpIn=null;
    public ArrayList<BluetoothDevice> urzadzeniablutu = new ArrayList<>();
    public DeviceListAdapter adapterblutu;
    ListView lvnewDevices;
    int polaczoneblu;
    private boolean isBtConnected = false;

    private final BroadcastReceiver mBroadCastReciver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(blutu.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, blutu.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onRecive: State off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadCastReciver1: State turning off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadCastReciver1: State turning on");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadCastReciver1: State on");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver BroadCastReciver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive:ACTION FOUND");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice urzadzenie = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                urzadzeniablutu.add(urzadzenie);
                Log.d(TAG, "onReceive: " + urzadzenie.getName() + ":" + urzadzenie.getAddress());
                adapterblutu = new DeviceListAdapter(context, R.layout.device_adapter_view, urzadzeniablutu);
                lvnewDevices.setAdapter(adapterblutu);
            }
        }
    };
    private final BroadcastReceiver BroadCastReciver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action=intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState()==BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "onReceive: BOND_BONDED");

                    //Intent intente = new Intent(MainActivity.this, Woda.class);
                    //startActivity(intente);
                }
                if (mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "onReceive: BOND_BONDING");
                }
                if (mDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "onReceive: BOND_NONE");
                }
            }

        }};
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        unregisterReceiver(mBroadCastReciver1);
        unregisterReceiver(BroadCastReciver4);
        unregisterReceiver(BroadCastReciver3);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton ButonnONOFF = (ImageButton) findViewById(R.id.BtnONOFF);
        blutu = BluetoothAdapter.getDefaultAdapter();

        lvnewDevices = (ListView) findViewById(R.id.lvNewDevices);
        urzadzeniablutu = new ArrayList<>();
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        lvnewDevices.setOnItemClickListener(MainActivity.this);
        registerReceiver(BroadCastReciver4,filter);




        ButonnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling");
                BluetoothDevice device = urzadzeniablutu.get(polaczoneblu);
                //connector = new DeviceConnector();
                DeviceConnector connector = DeviceConnector.getInstance( );
                connector.connect(device);
                Intent intente = new Intent(MainActivity.this, Woda.class);
               // intente.putExtra("EXTRA_SESSION_ID",connector);//intente.putExtra("MyClass", connector);
                //intente.putExtra("BT", (Parcelable) connector);
                startActivity(intente);

            }
        });

    }



    public void enableDisableBT() {
        if (blutu == null) {
            Log.d(TAG, "enableDisableBT: Does not have bluetooth capabilities");
        }
        if (!blutu.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT");
            Intent enableDisableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableDisableBT);
            finish();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadCastReciver1, BTIntent);

        }
        if (blutu.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling BT");
            blutu.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadCastReciver1, BTIntent);

        }
    }

    public void btnDiscover(View view) {
        Log.d(TAG, "BtnDiscover: Loooking for unpaired devices");
        String szukam="Szukam urządzeń...";
        Toast.makeText(MainActivity.this, szukam, Toast.LENGTH_LONG).show();
        if (blutu.isDiscovering()) {
            blutu.cancelDiscovery();
            Log.d(TAG, "BtnDiscover:Canceling discover");

        }
        if (!blutu.isDiscovering()) {
            checkBTPermissions();
            blutu.startDiscovery();
            IntentFilter discoverdevicesintent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(BroadCastReciver3, discoverdevicesintent);
        }

    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    blutu.cancelDiscovery();
        Log.d(TAG, "onItemClick: You clicked device");
        String devicename=urzadzeniablutu.get(i).getName();
        String deviceaddress=urzadzeniablutu.get(i).getAddress();
        Log.d(TAG, "onItemClick: device name"+devicename);
        Log.d(TAG, "onItemClick: device adress"+deviceaddress);
        if (urzadzeniablutu.get(i).getBondState() == BluetoothDevice.BOND_BONDED) {
            try {
                Method method = urzadzeniablutu.get(i).getClass().getMethod("removeBond", (Class[]) null);
                method.invoke(urzadzeniablutu.get(i), (Object[]) null);

            } catch (Exception e) {
                e.printStackTrace();
            }
            String message="Rozparowywanie z "+devicename+"...";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        else {
            try {
                Method method =  urzadzeniablutu.get(i).getClass().getMethod("createBond", (Class[]) null);
                method.invoke( urzadzeniablutu.get(i), (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String message="Próba parowania z "+devicename+"...";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            polaczoneblu=i;

        }

    }



    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0) {

                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            } else {
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }

    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            // Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (BTsocket == null || !isBtConnected) {
                    BluetoothDevice device = urzadzeniablutu.get(polaczoneblu);
                    try {
                        Class class1 = device.getClass();
                        Class aclass[] = new Class[1];
                        aclass[0] = Integer.TYPE;
                        Method method = class1.getMethod("createRfcommSocket", aclass);
                        Object aobj[] = new Object[1];
                        aobj[0] = Integer.valueOf(1);

                        tmp = (BluetoothSocket) method.invoke(device, aobj);
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
                    tmp.connect();//start connection

                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
                Intent intente = new Intent(MainActivity.this, Woda.class);
                startActivity(intente);
                //startThreadConnected(tmp);
                //run();

            }
        }

    }



    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    }