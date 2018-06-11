package com.example.red_d.imu_test;

import android.app.Activity;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver socketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: socketReceiver " + intent.getAction());
            if(intent.getAction().equals("SOCKET CLOSED")){
                startConnection();
                sending = false;
            }

            if(intent.getAction().equals("SOCKET CONNECTED"))
                sending = true;

            if(intent.getAction().equals("CONNECTION LOST")) {
                bluetoothConnectionService.kill();
                sending = false;
                startConnection();
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        //Log.d(TAG, "onReceive: State off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //Log.d(TAG, "onReceive: State ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Log.d(TAG, "onReceive: State TURNING off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Log.d(TAG, "onReceive: State turning on");
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
                        //Log.d(TAG, "Receiver2:  Discover On");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        //Log.d(TAG, "Receiver2: Discover On: Enable to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        //Log.d(TAG, "onReceive2: Discover Disable");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        //Log.d(TAG, "onReceive2: Connecting");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        //Log.d(TAG, "onReceive2: Connected");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //Log.d(TAG, "onReceive: ACTION FOUND.");
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                btDevice.add(device);
                //Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
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
            //Log.d(TAG, "onReceive: intent action: " + intent.getAction());

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    //Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBluetoothDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    //Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    //Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    public BluetoothConnectionService bluetoothConnectionService;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public ArrayList<BluetoothDevice> btDevice = new ArrayList<>(); //list of bluetooth devices it discovers
    public DeviceListAdapter deviceListAdaptorAdapter;
    ListView lvNewDevices;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mBluetoothDevice;

    private static final String TAG = "MainActivity";
    
    private SensorManager mSensorManager;
    private Sensor magneticSensor;
    private Sensor accel;

    private float [] magnetometerReading = {0f, 0f, 0f};
    private float [] accelerometerReading = {0f, 0f, 0f};
    private float [] rotation = {0f, 0f, 0f};
    private TextView yawText;
    private TextView pitchText;
    private TextView rollText;

    private boolean sending = false;
    private ImageView headOri;
    private float currentDegree = 0f;

    private static final int REQUEST_ENABLE_BT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sensorConfig();
        bluetoothConfig();
        toogleBT();
        IntentFilter filtro = new IntentFilter("SOCKET CONNECTED");
        registerReceiver(socketReceiver,filtro);
        IntentFilter filtro2 = new IntentFilter("SOCKET CLOSED");
        registerReceiver(socketReceiver,filtro2);
        IntentFilter filtro3 = new IntentFilter("CONNECTION LOST");
        registerReceiver(socketReceiver,filtro3);
        
        IntentFilter sensorFilter = new IntentFilter("imu-service");
        registerReceiver(sensorReceiver, sensorFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, IMUService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver2);
        unregisterReceiver(mReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        unregisterReceiver(socketReceiver);
        unregisterReceiver(sensorReceiver);
    }

    private void sensorConfig()
    {
        //Log.d(TAG, "onCreate: Initialize sensor services");
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        yawText = (TextView) findViewById(R.id.yaw);
        pitchText = (TextView) findViewById(R.id.pitch);
        rollText = (TextView) findViewById(R.id.roll);
        headOri = (ImageView) findViewById(R.id.head);

    }

    private void bluetoothConfig()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if(device.getName().equals("SpatialDemo")) {
                mBluetoothDevice = device;
                bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);
                startConnection();
                break;
            }
        }

        btDevice = new ArrayList<>();
        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);
    }

    private void startConnection() {
        startBTConnection(mBluetoothDevice, MY_UUID_INSECURE);
    }
    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */

    //SENSOR STUFF

    public void toogleBT()
    {
        if(bluetoothAdapter == null)
        {
            //Log.d(TAG, "toogleBT: No BT device");
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

    public void startBTConnection(BluetoothDevice device, UUID uuid)
    {
        //Log.d(TAG, "startConnection: starting connection");
        bluetoothConnectionService.startClient(device, uuid);
    }

    private BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "onReceive: receiving from IMU service");
            
            final float[] values = intent.getFloatArrayExtra("values");
            if(intent.getFloatArrayExtra("accel-values") != null)
                accelerometerReading = intent.getFloatArrayExtra("accel-values");
            if(intent.getFloatArrayExtra("magnetic-values") != null)
                magnetometerReading = intent.getFloatArrayExtra("magnetic-values");

            // Rotation matrix based on current readings from accelerometer and magnetometer.
            final float[] rotationMatrix = new float[9];
            mSensorManager.getRotationMatrix(rotationMatrix, null,
                    accelerometerReading, magnetometerReading);

// Express the updated rotation matrix as three orientation angles.
            final float[] orientationAngles = new float[3];
            mSensorManager.getOrientation(rotationMatrix, orientationAngles);
            rotation = orientationAngles;

            yawText.setText("Pitch: "+Math.toDegrees(orientationAngles[0]));
            pitchText.setText("Yaw: "+Math.toDegrees(orientationAngles[1]));
            rollText.setText("Roll: "+Math.toDegrees(orientationAngles[2]));

            float degree = Math.round(values[1]);
            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            // how long the animation will take place
            ra.setDuration(210);
            // set the animation after the end of the reservation status
            ra.setFillAfter(true);
            // Start the animation
            headOri.startAnimation(ra);
            currentDegree = -degree;
            String test = ("" + String.valueOf(rotation[0]) + "/" + String.valueOf(rotation[1]) + "/" + String.valueOf(rotation[2])+"/");
            //Log.d(TAG, "onClick: " + test);
            byte[] bytes = test.getBytes(Charset.defaultCharset());
            //Log.d(TAG, "SENDING: " + sending);
            if(sending) {
                if (bluetoothConnectionService != null) {
                    bluetoothConnectionService.write(bytes);
                }
            }
        }
    };
}