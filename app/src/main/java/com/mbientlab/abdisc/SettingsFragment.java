package com.mbientlab.abdisc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.abdisc.filter.DebugMainActivity;
import com.mbientlab.abdisc.filter.FilterSetup;
import com.mbientlab.abdisc.filter.FilterState;
import com.mbientlab.abdisc.model.DataGenerator;
import com.mbientlab.abdisc.utils.DataDownloaderFragment;
import com.mbientlab.bletoolbox.scanner.BleScannerFragment;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.DataProcessor;
import com.mbientlab.metawear.api.controller.Timer;

import java.util.Locale;
import java.util.UUID;

/**
 * Copyright 2014 MbientLab Inc. All rights reserved.
 * <p/>
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
 * <p/>
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
 * <p/>
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 * <p/>
 * <p/>
 * Created by Lance Gleason of Polyglot Programming LLC. on 7/3/15.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */
public class SettingsFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private static final String PREF_USE_DEMO_DATA = "use_demo_data";

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private AppState appState;
    private Activity activity;
    private ProgressDialog setupProgress;
    private DataDownloaderFragment dataDownloaderFragment;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof AppState)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_app_state)));
        }

        // yes this is pedantic
        this.activity = activity;
        appState = (AppState) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        FragmentManager fragManager = getFragmentManager();

        if (savedInstanceState == null) {
            dataDownloaderFragment = new DataDownloaderFragment();
            fragManager.beginTransaction().add(R.id.drawer_layout, dataDownloaderFragment, MainActivity.DATA_DOWNLOADER_FRAGMENT_KEY).commit();
        } else {
            dataDownloaderFragment = (DataDownloaderFragment) fragManager.getFragment(savedInstanceState, MainActivity.DATA_DOWNLOADER_FRAGMENT_KEY);
        }
        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.profileOption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final android.app.FragmentManager fragmentManager = activity.getFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.app_content, ((MainActivity) activity).getProfileFragment()).commit();
                ((MainActivity) activity).onFragmentSettingsOptionSelected(v.getId());
                closeDrawer(Gravity.START);
            }
        });

        view.findViewById(R.id.start_debug_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent debugIntent = new Intent(activity, DebugMainActivity.class);
                debugIntent.putExtra(DebugMainActivity.EXTRA_BT_DEVICE, appState.getBluetoothDevice());

                startActivity(debugIntent);
            }
        });
        view.findViewById(R.id.connect_metawear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleScannerFragment.newInstance(new UUID[]{UUID.fromString("326a9000-85cb-9195-d9dd-464cfbbae75a")})
                        .show(getActivity().getFragmentManager(), "ble_scanner_fragment");
            }
        });


        view.findViewById(R.id.upload_filter_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appState.getMetaWearController().isConnected()) {
                    setupProgress = new ProgressDialog(activity);
                    setupProgress.setIndeterminate(true);
                    setupProgress.setMessage("Setting up filters...");
                    setupProgress.show();

                    FilterSetup.configure(appState.getMetaWearController(), new FilterSetup.SetupListener() {
                        @Override
                        public void ready(FilterState state) {
                            appState.setFilterState(state);

                            DataProcessor dpCtrllr = (DataProcessor) appState.getMetaWearController().getModuleController(Module.DATA_PROCESSOR);
                            dpCtrllr.enableFilterNotify(state.getSedentaryId());
                            dpCtrllr.enableFilterNotify(state.getSessionStartId());

                            setupProgress.dismiss();
                            setupProgress = null;

                            Toast.makeText(activity, R.string.text_filter_setup_complete, Toast.LENGTH_SHORT).show();
                            Log.i("AbDisc", state.toString());

                            Timer timerCtrllr = (Timer) appState.getMetaWearController().getModuleController(Module.TIMER);
                            timerCtrllr.startTimer(state.getSensorTimerId());

                            Accelerometer accelCtrllr = (Accelerometer) appState.getMetaWearController().getModuleController(Module.ACCELEROMETER);
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

        final SharedPreferences sp = appState.getSharedPreferences();
        final TextView forgetDevice = (TextView) view.findViewById(R.id.forget_metawear);

        forgetDevice.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                appState.forgetSavedDevice();
                                                ((TextView) view).setText("");
                                            }
                                        }
        );

        final String macAddress = sp.getString(MainActivity.MAC_ADDRESS, null);

        view.findViewById(R.id.sync_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((appState.getMetaWearController() != null) && appState.getMetaWearController().isConnected()) {
                    setupProgress = new ProgressDialog(activity);
                    setupProgress.setIndeterminate(true);
                    setupProgress.setMessage("Setting up filters...");
                    setupProgress.show();
                    dataDownloaderFragment.startLogDownload(appState.getMetaWearController(), setupProgress);
                } else {
                    if (macAddress != null) {
                        appState.connectToSavedMetawear();
                    }
                }
            }
        });

        final Switch testDataSwitch = (Switch) view.findViewById(R.id.testData);
        testDataSwitch.setChecked(sp.getBoolean(PREF_USE_DEMO_DATA, false));

        testDataSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(PREF_USE_DEMO_DATA, isChecked);
                editor.commit();
            }
        });

        view.findViewById(R.id.populateData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataGenerator.generateStepData();
            }
        });
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void syncDrawerToggle() {
        mDrawerToggle.syncState();
    }

    public boolean isDrawerOpen(int gravity) {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(gravity);
    }

    public void closeDrawer(int gravity) {
        mDrawerLayout.closeDrawer(gravity);
    }

    public void openDrawer(int gravity) {
        mDrawerLayout.openDrawer(gravity);
    }

    public interface OnFragmentSettingsListener {
        // TODO: Update argument type and name
        public void onFragmentSettingsOptionSelected(int optionId);
    }
}
