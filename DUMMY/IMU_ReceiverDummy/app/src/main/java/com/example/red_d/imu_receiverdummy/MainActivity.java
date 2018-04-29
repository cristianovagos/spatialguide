package com.example.red_d.imu_receiverdummy;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: State off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: State ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: State TURNING off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: State turning on");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (bluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "Receiver2:  Discover On");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "Receiver2: Discover On: Enable to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "onReceive2: Discover Disable");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "onReceive2: Connecting");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "onReceive2: Connected");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                btDevice.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdaptorAdapter = new DeviceListAdapter(context, R.layout.device_list_view, btDevice);
                lvNewDevices.setAdapter(deviceListAdaptorAdapter);
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBluetoothDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    //Broadcast to Receive incoming Transmisison
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: here");
            String text = intent.getStringExtra("theMessage");
            stringBuilder.append(text+ "\n");

            convertData(text);
        }
    };

    //Convert the Data Received to Float
    private void convertData(String data) {
        String [] tmp = data.split("/");
        Log.d(TAG, "convertData: string splitted 0: " + tmp[0]);
        Log.d(TAG, "convertData: string splitted 1: " + tmp[1]);
        Log.d(TAG, "convertData: string splitted 2: " + tmp[2]);

        rotation[0] = Float.valueOf(tmp[0]);
        rotation[1] = Float.valueOf(tmp[1]);
        rotation[2] = Float.valueOf(tmp[2]);
        Log.d(TAG, "convertData: float converted 0: " + rotation[0]);
        Log.d(TAG, "convertData: float converted 1: " + rotation[1]);
        Log.d(TAG, "convertData: float converted 2: " + rotation[2]);

        yawText.setText("Yaw: " + String.valueOf(Math.toDegrees(rotation[1])));
        pitchText.setText("Pitch" + String.valueOf(Math.toDegrees(rotation[0])));
        rollText.setText("Roll" + String.valueOf(Math.toDegrees(rotation[2])));

    }

    public BluetoothConnectionService bluetoothConnectionService;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public ArrayList<BluetoothDevice> btDevice = new ArrayList<>(); //list of bluetooth devices it discovers
    public DeviceListAdapter deviceListAdaptorAdapter;
    ListView lvNewDevices;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    Button button, discoverButton;

    Button startConnection;
    Button send;

    private static final String TAG = "MainActivity";

    private float [] rotation = {0f, 0f, 0f};
    private TextView yawText;
    private  TextView pitchText;
    private TextView rollText;
    private StringBuilder stringBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        yawText = (TextView) findViewById(R.id.yaw);
        pitchText = (TextView) findViewById(R.id.pitch);
        rollText = (TextView) findViewById(R.id.roll);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));
        bluetoothConfig();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver2);
        unregisterReceiver(mReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }


    private void bluetoothConfig()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices = (ListView) findViewById(R.id.list);
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        Button button = (Button) findViewById(R.id.button);
        discoverButton = (Button) findViewById(R.id.discovery);
        startConnection = (Button) findViewById(R.id.startConnection);

        btDevice = new ArrayList<>();

        stringBuilder = new StringBuilder();
        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);



        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleDiscover();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleBT();
            }
        });

        startConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });


    }

    private void startConnection() {
        startBTConnection(mBluetoothDevice, MY_UUID_INSECURE);
    }

    private void toogleDiscover()
    {
        Log.d(TAG, "toogleDiscover: Device discover");
        Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverIntent);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver2, intentFilter);
    }

    public void toogleBT()
    {
        if(bluetoothAdapter == null)
        {
            Log.d(TAG, "toogleBT: No BT device");
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(btIntent);

            IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, btIntentFilter);
        }
        if(!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.disable();
            IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, btIntentFilter);
        }
    }


    public void btnDiscover(View view) {

        if(bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();

            checkBTPermissions();
            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver3, intentFilter);
        }
        if(!bluetoothAdapter.isDiscovering())
        {
            checkBTPermissions();
            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver3, intentFilter);
        }

    }
    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0) {

                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    //SENSOR STUFF


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //cancel discover
        bluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: Clicked");

        String dname = btDevice.get(position).getName();
        String daddress = btDevice.get(position).getAddress();

        //create the bond
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + dname);
            btDevice.get(position).createBond();

            mBluetoothDevice = btDevice.get(position);
            bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);
        }
    }


    public void startBTConnection(BluetoothDevice device, UUID uuid)
    {
        Log.d(TAG, "startConnection: starting connection");
        bluetoothConnectionService.startClient(device, uuid);
    }


}
