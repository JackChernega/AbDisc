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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mbientlab.abdisc.filter.FilterState;
import com.mbientlab.bletoolbox.scanner.BleScannerFragment;
import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.controller.DataProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by etsai on 6/1/2015.
 */
public class MainActivity extends ActionBarActivity implements ServiceConnection, AppState, BleScannerFragment.ScannerListener, SettingsFragment.OnFragmentSettingsListener {
    private final static int REQUEST_ENABLE_BT = 0;
    private static final int ACTIVITY_PER_STEP = 20000;

    private short crunchSessionCount;
    private int steps = 0;
    private Fragment activityFrag = null, distanceFrag = null;

    private FilterState filterState;
    private ProgressDialog setupProgress;
    private MetaWearBleService mwService;
    private MetaWearController mwCtrllr;
    private BluetoothDevice btDevice;
    private LocalBroadcastManager broadcastManager = null;
    private SettingsFragment mSettingsFragment;
    private ProfileFragment profileFragment;
    private SharedPreferences sharedPreferences;
    private Editor editor;

    private final DataProcessor.Callbacks dpModuleCallbacks = new DataProcessor.Callbacks() {
        @Override
        public void receivedFilterOutput(byte filterId, byte[] output) {
            ByteBuffer buffer = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN);

            if (filterId == filterState.getSedentaryId()) {
                short milliG = buffer.getShort();

                steps += (milliG / ACTIVITY_PER_STEP);
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

        sharedPreferences = getApplicationContext().getSharedPreferences("com.mbientlab.abdisk", 0);
        // commenting out,  cant' find library
        //ExceptionHandler.register(this, "http://tf2n.serverpit.com/server.php");
        BluetoothAdapter btAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

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
            if (!btAdapter.isEnabled()) {
                final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                        this, Context.BIND_AUTO_CREATE);
            }
        }

        final FragmentManager fragManager = getFragmentManager();
        findViewById(R.id.tab_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragTransaction = fragManager.beginTransaction();
                if (activityFrag == null) {
                    activityFrag = new DayActivityFragment();
                }
                mainTabButtonPressed(R.id.tab_activity);
                fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.app_content, activityFrag).commit();
            }
        });
        findViewById(R.id.tab_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragTransaction = fragManager.beginTransaction();
                if (distanceFrag == null) {
                    distanceFrag = new StepCountFragment();
                }
                mainTabButtonPressed(R.id.tab_distance);
                fragTransaction.replace(R.id.app_content, distanceFrag).commit();
            }
        });

        mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings_drawer);
        mSettingsFragment.setUp(
                R.id.settings_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        // eventually make this conditional.
        FragmentTransaction fragTransaction = fragManager.beginTransaction();
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }

        fragTransaction.replace(R.id.app_content, profileFragment).commit();
    }

    @Override
    public void onFragmentSettingsOptionSelected(int optionId){
        mainTabButtonPressed(0);
    }

    public void mainTabButtonPressed(int buttonId) {
        int buttons[] = {R.id.tab_activity, R.id.tab_burn, R.id.tab_crunch, R.id.tab_distance};
        for (int i = 0; i < buttons.length; i++) {
            ImageButton button = (ImageButton) findViewById(buttons[i]);
            if(buttons[i] == buttonId){
                button.setBackgroundColor(getResources().getColor(R.color.ColorGraphHigh));
            } else {
                button.setBackgroundColor(getResources().getColor(R.color.ColorNavBackground));
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSettingsFragment.syncDrawerToggle();
    }


    @Override
    protected void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_gradient));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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

    private final MetaWearController.DeviceCallbacks dCallbacks = new MetaWearController.DeviceCallbacks() {
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

        @Override
        public void receivedGattError(GattOperation gattOp, int status) {
            mwCtrllr.close(true);
            Log.i("AbDisc", "Gatt Error: " + gattOp.toString() + " (status= " + status + ")");
        }


    };

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mwService = ((MetaWearBleService.LocalBinder) iBinder).getService();
        broadcastManager = LocalBroadcastManager.getInstance(mwService);
        broadcastManager.registerReceiver(MetaWearBleService.getMetaWearBroadcastReceiver(),
                MetaWearBleService.getMetaWearIntentFilter());
        mwService.useLocalBroadcastManager(broadcastManager);
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
    public void setFilterState(FilterState filterState) {
        this.filterState = filterState;
    }

    @Override
    public int getStepCount() {
        return steps;
    }

    @Override
    public int getCrunchSessionCount() {
        return crunchSessionCount;
    }

    @Override
    public void btDeviceSelected(BluetoothDevice device) {
        btDevice = device;
        mwCtrllr = mwService.getMetaWearController(btDevice);
        mwCtrllr.addDeviceCallback(dCallbacks);
        mwCtrllr.connect();
    }

    @Override
    public BluetoothDevice getBluetoothDevice() {
        return btDevice;
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public ProfileFragment getProfileFragment() {
        return profileFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /**if (id == R.id.action_settings) {
         return true;
         }*/

        if (item != null && id == android.R.id.home) {
            if (mSettingsFragment.isDrawerOpen(Gravity.START)) {
                mSettingsFragment.closeDrawer(Gravity.START);
            } else {
                mSettingsFragment.openDrawer(Gravity.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
