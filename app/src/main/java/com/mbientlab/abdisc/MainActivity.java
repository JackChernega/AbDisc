/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.mbientlab.abdisc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mbientlab.abdisc.filter.DebugMainActivity;
import com.mbientlab.abdisc.filter.FilterSetup;
import com.mbientlab.abdisc.filter.FilterState;
import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.DataProcessor;
import com.mbientlab.metawear.api.controller.Timer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by etsai on 6/1/2015.
 */
public class MainActivity extends Activity implements ServiceConnection, AppState {
    private final String MW_MAC_ADDRESS= "C8:D2:BA:90:60:03";
    private final static int REQUEST_ENABLE_BT= 0;
    private static final int ACTIVITY_PER_STEP= 20000;

    private short crunchSessionCount;
    private int steps= 0;
    private Fragment activityFrag= null, distanceFrag= null;

    private FilterState filterState;
    private ProgressDialog setupProgress;
    private MetaWearController mwCtrllr;
    private BluetoothDevice btDevice;
    private LocalBroadcastManager broadcastManager= null;

    private final DataProcessor.Callbacks dpModuleCallbacks= new DataProcessor.Callbacks() {
        @Override
        public void receivedFilterOutput(byte filterId, byte[] output) {
            ByteBuffer buffer= ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN);

            if (filterId == filterState.getSedentaryId()) {
                short milliG= buffer.getShort();

                steps+= (milliG / ACTIVITY_PER_STEP);
                ((StepCountFragment) distanceFrag).stepCountUpdated(steps);
            } else if (filterId == filterState.getSessionStartId()) {
                crunchSessionCount++;
                ((CrunchSessionFragment) activityFrag).crunchSessionCountUpdated(crunchSessionCount);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter btAdapter= ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (btAdapter == null) {
            new AlertDialog.Builder(this).setTitle(R.string.error_title)
                    .setMessage(R.string.error_no_bluetooth)
                    .setCancelable(false)
                    .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.finish();
                        }
                    })
                    .create()
                    .show();
        } else {
            btDevice= btAdapter.getRemoteDevice(MW_MAC_ADDRESS);

            if (!btAdapter.isEnabled()) {
                final Intent enableIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                        this, Context.BIND_AUTO_CREATE);
            }
        }

        final FragmentManager fragManager= getFragmentManager();
        findViewById(R.id.tab_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragTransaction= fragManager.beginTransaction();
                if (activityFrag == null) {
                    activityFrag= new CrunchSessionFragment();
                }

                fragTransaction.replace(R.id.app_content, activityFrag).commit();
            }
        });
        findViewById(R.id.tab_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragTransaction= fragManager.beginTransaction();
                if (distanceFrag == null) {
                    distanceFrag= new StepCountFragment();
                }

                fragTransaction.replace(R.id.app_content, distanceFrag).commit();
            }
        });
        findViewById(R.id.start_debug_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent debugIntent= new Intent(MainActivity.this, DebugMainActivity.class);
                debugIntent.putExtra(DebugMainActivity.EXTRA_BT_DEVICE, btDevice);
                startActivity(debugIntent);
            }
        });
        findViewById(R.id.connect_metawear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mwCtrllr.connect();
            }
        });
        findViewById(R.id.upload_filter_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mwCtrllr.isConnected()) {
                    setupProgress= new ProgressDialog(MainActivity.this);
                    setupProgress.setIndeterminate(true);
                    setupProgress.setMessage("Setting up filters...");
                    setupProgress.show();

                    FilterSetup.configure(mwCtrllr, new FilterSetup.SetupListener() {
                        @Override
                        public void ready(FilterState state) {
                            filterState= state;

                            DataProcessor dpCtrllr= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);
                            dpCtrllr.enableFilterNotify(state.getSedentaryId());
                            dpCtrllr.enableFilterNotify(state.getSessionStartId());

                            setupProgress.dismiss();
                            setupProgress= null;

                            Toast.makeText(MainActivity.this, R.string.text_filter_setup_complete, Toast.LENGTH_SHORT).show();
                            Log.i("AbDisc", state.toString());

                            Timer timerCtrllr= (Timer) mwCtrllr.getModuleController(Module.TIMER);
                            timerCtrllr.startTimer(filterState.getSensorTimerId());

                            Accelerometer accelCtrllr = (Accelerometer) mwCtrllr.getModuleController(Module.ACCELEROMETER);
                            accelCtrllr.enableXYZSampling()
                                    .withFullScaleRange(Accelerometer.SamplingConfig.FullScaleRange.FSR_8G)
                                    .withOutputDataRate(Accelerometer.SamplingConfig.OutputDataRate.ODR_100_HZ)
                                    .withHighPassFilter((byte) 2)
                                    .withSilentMode();
                            ///< May want to configure the other options for tap detection
                            accelCtrllr.enableTapDetection(Accelerometer.TapType.DOUBLE_TAP, Accelerometer.Axis.Z)
                                    .withSilentMode();
                            accelCtrllr.startComponents();
                        }
                    }).commit();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish();
                } else {
                    getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                            this, Context.BIND_AUTO_CREATE);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (broadcastManager != null) {
            broadcastManager.unregisterReceiver(MetaWearBleService.getMetaWearBroadcastReceiver());
        }

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    private final MetaWearController.DeviceCallbacks dCallbacks= new MetaWearController.DeviceCallbacks() {
        @Override
        public void connected() {
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            Log.i("AbDisc", "Device connected");
        }

        @Override
        public void disconnected() {
            Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            Log.i("AbDisc", "Connection lost");
        }
    };

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        final MetaWearBleService mwService= ((MetaWearBleService.LocalBinder) iBinder).getService();
        broadcastManager= LocalBroadcastManager.getInstance(mwService);
        broadcastManager.registerReceiver(MetaWearBleService.getMetaWearBroadcastReceiver(),
                MetaWearBleService.getMetaWearIntentFilter());
        mwService.useLocalBroadcastManager(broadcastManager);

        mwCtrllr= mwService.getMetaWearController(btDevice);
        mwCtrllr.addDeviceCallback(dCallbacks).addModuleCallback(dpModuleCallbacks);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public MetaWearController getMetaWearController() {
        return mwCtrllr;
    }

    @Override
    public FilterState getFilterState() {
        return filterState;
    }

    @Override
    public int getStepCount() {
        return steps;
    }

    @Override
    public int getCrunchSessionCount() {
        return crunchSessionCount;
    }

}
