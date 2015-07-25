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

package com.mbientlab.abdisc.filter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.DataProcessor;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by etsai on 6/3/2015.
 */
public class FilterConfigFragment extends Fragment implements ServiceConnection {
    private static final String SHARED_PREF_KEY= "com.mbientlab.abdisc.filter.FilterConfigFragment";

    private static FilterConfigFragment INSTANCE;

    public static FilterConfigFragment getInstance() {
        if (INSTANCE == null) {
            INSTANCE= new FilterConfigFragment();
        }
        return INSTANCE;
    }

    public static String getTitle() {
        return "Configuration";
    }

    private ParameterAdapter configAdapter;
    private DataConnection conn;
    private FilterParameters parameterSetup;

    private ProgressDialog setupProgress;
    private MetaWearController mwCtrllr;
    private SharedPreferences sharedPref;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof DataConnection)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_data_connection)));
        }

        conn= (DataConnection) activity;
        activity.getApplicationContext().bindService(new Intent(activity, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);
        sharedPref = activity.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        configAdapter= new ParameterAdapter(getActivity(), R.id.filter_config_entry_layout);
        configAdapter.setNotifyOnChange(true);
        return inflater.inflate(R.layout.filter_config, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final SharedPreferences.Editor editor = sharedPref.edit();

        ListView configList= (ListView) view.findViewById(R.id.filter_config_list);
        configList.setAdapter(configAdapter);

        final Activity owner= getActivity();
        final ArrayList<Parameter> configSettings= new ArrayList<>();
        
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_18),
                owner.getString(R.string.label_filter_config_description_18),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_18), DefaultParameters.ADC_SAMPLE_SIZE),
                DefaultParameters.ADC_SAMPLE_SIZE) {
            @Override
            public void writeSetting() {
                parameterSetup.withAdcSampleSize(Byte.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_18), Byte.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_17),
                owner.getString(R.string.label_filter_config_description_17),
                sharedPref.getFloat(owner.getString(R.string.label_filter_config_setting_17), DefaultParameters.TAP_THRESHOLD),
                DefaultParameters.TAP_THRESHOLD) {
            @Override
            public void writeSetting() {
                parameterSetup.withTapThreshold(Float.valueOf(this.value));
                editor.putFloat(owner.getString(R.string.label_filter_config_setting_17), Float.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_01),
                owner.getString(R.string.label_filter_config_description_01),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_01), DefaultParameters.SENSOR_DATA_PIN),
                DefaultParameters.SENSOR_DATA_PIN) {
            @Override
            public void writeSetting() {
                parameterSetup.withSensorDataPin(Byte.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_01), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_02),
                owner.getString(R.string.label_filter_config_description_02),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_02), DefaultParameters.SENSOR_GROUND_PIN),
                DefaultParameters.SENSOR_GROUND_PIN) {
            @Override
            public void writeSetting() {
                parameterSetup.withSensorGroundPin(Byte.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_02), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_03),
                owner.getString(R.string.label_filter_config_description_03),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_03), DefaultParameters.SEDENTARY_RESET_THRESHOLD),
                DefaultParameters.SEDENTARY_RESET_THRESHOLD) {
            @Override
            public void writeSetting() {
                parameterSetup.withSedentaryResetThreshold(Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_03), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_04),
                owner.getString(R.string.label_filter_config_description_04),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_04), DefaultParameters.SEDENTARY_MIN_ACTIVITY_THRESHOLD),
                DefaultParameters.SEDENTARY_MIN_ACTIVITY_THRESHOLD) {
            @Override
            public void writeSetting() {
                parameterSetup.withSedentaryMinActivityThreshold(Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_04), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_16),
                owner.getString(R.string.label_filter_config_description_16),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_16), DefaultParameters.SEDENTARY_TIME),
                DefaultParameters.SEDENTARY_TIME) {
            @Override
            public void writeSetting() {
                parameterSetup.withSedentaryTime(Byte.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_16), Byte.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_05),
                owner.getString(R.string.label_filter_config_description_05),
                sharedPref.getFloat(owner.getString(R.string.label_filter_config_setting_05), DefaultParameters.CRUNCH_SESSION_DURATION),
                DefaultParameters.CRUNCH_SESSION_DURATION) {
            @Override
            public void writeSetting() {
                parameterSetup.withCrunchSessionDuration(Float.valueOf(this.value));
                editor.putFloat(owner.getString(R.string.label_filter_config_setting_05), Float.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_19),
                owner.getString(R.string.label_filter_config_description_19),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_19), DefaultParameters.CRUNCH_THRESHOLD_UPDATE_PERIOD),
                DefaultParameters.CRUNCH_THRESHOLD_UPDATE_PERIOD) {
            @Override
            public void writeSetting() {
                parameterSetup.withCrunchThresholdUpdateCheckPeriod(Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_19), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_06),
                owner.getString(R.string.label_filter_config_description_06),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_06), DefaultParameters.CRUNCH_SESSION_THRESHOLD_UPDATE),
                DefaultParameters.CRUNCH_SESSION_THRESHOLD_UPDATE) {
            @Override
            public void writeSetting() {
                parameterSetup.withCrunchThresholdUpdateMinChangeThreshold(Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_06), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_07),
                owner.getString(R.string.label_filter_config_description_07),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_07), DefaultParameters.L1_HAPTIC_LOWER),
                DefaultParameters.L1_HAPTIC_LOWER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchLower(FilterParameters.HapticLevel.L1, Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_07), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_08),
                owner.getString(R.string.label_filter_config_description_08),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_08), DefaultParameters.L1_HAPTIC_UPPER),
                DefaultParameters.L1_HAPTIC_UPPER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchUpper(FilterParameters.HapticLevel.L1, Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_08), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_13),
                owner.getString(R.string.label_filter_config_description_13),
                sharedPref.getFloat(owner.getString(R.string.label_filter_config_setting_13), DefaultParameters.L1_HAPTIC_STRENGTH),
                DefaultParameters.L1_HAPTIC_STRENGTH) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticStrength(FilterParameters.HapticLevel.L1, Float.valueOf(this.value));
                editor.putFloat(owner.getString(R.string.label_filter_config_setting_13), Float.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_09),
                owner.getString(R.string.label_filter_config_description_09),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_09), DefaultParameters.L2_HAPTIC_LOWER),
                DefaultParameters.L2_HAPTIC_LOWER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchLower(FilterParameters.HapticLevel.L2, Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_09), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_10),
                owner.getString(R.string.label_filter_config_description_10),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_10), DefaultParameters.L2_HAPTIC_UPPER),
                DefaultParameters.L2_HAPTIC_UPPER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchUpper(FilterParameters.HapticLevel.L2, Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_10), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_14),
                owner.getString(R.string.label_filter_config_description_14),
                sharedPref.getFloat(owner.getString(R.string.label_filter_config_setting_14), DefaultParameters.L2_HAPTIC_STRENGTH),
                DefaultParameters.L2_HAPTIC_STRENGTH) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticStrength(FilterParameters.HapticLevel.L2, Float.valueOf(this.value));
                editor.putFloat(owner.getString(R.string.label_filter_config_setting_14), Float.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_11),
                owner.getString(R.string.label_filter_config_description_11),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_11), DefaultParameters.L3_HAPTIC_LOWER),
                DefaultParameters.L3_HAPTIC_LOWER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchLower(FilterParameters.HapticLevel.L3, Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_11), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_12),
                owner.getString(R.string.label_filter_config_description_12),
                sharedPref.getInt(owner.getString(R.string.label_filter_config_setting_12), DefaultParameters.L3_HAPTIC_UPPER),
                DefaultParameters.L3_HAPTIC_UPPER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchUpper(FilterParameters.HapticLevel.L3, Integer.valueOf(this.value));
                editor.putInt(owner.getString(R.string.label_filter_config_setting_12), Integer.valueOf(this.value));
            }
        });
        configSettings.add(new Parameter(owner.getString(R.string.label_filter_config_setting_15),
                owner.getString(R.string.label_filter_config_description_15),
                sharedPref.getFloat(owner.getString(R.string.label_filter_config_setting_15), DefaultParameters.L3_HAPTIC_STRENGTH),
                DefaultParameters.L3_HAPTIC_STRENGTH) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticStrength(FilterParameters.HapticLevel.L3, Float.valueOf(this.value));
                editor.putFloat(owner.getString(R.string.label_filter_config_setting_15), Float.valueOf(this.value));
            }
        });
        configAdapter.addAll(configSettings);

        view.findViewById(R.id.filter_config_program).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conn.getBluetoothDevice() != null) {
                    setupProgress = new ProgressDialog(getActivity());
                    setupProgress.setIndeterminate(true);
                    setupProgress.setMessage("Setting up filters...");
                    setupProgress.show();

                    for (Parameter filterCfg : configSettings) {
                        filterCfg.writeSetting();
                    }
                    parameterSetup.commit();
                    editor.apply();
                } else {
                    Toast.makeText(getActivity(), R.string.text_select_device, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        final MetaWearBleService mwService= ((MetaWearBleService.LocalBinder) iBinder).getService();
        mwCtrllr= mwService.getMetaWearController(conn.getBluetoothDevice());

        parameterSetup= FilterSetup.configure(mwCtrllr, new FilterSetup.SetupListener() {
            @Override
            public void ready(FilterState state) {
                setupProgress.dismiss();
                setupProgress= null;

                Toast.makeText(getActivity(), R.string.text_filter_setup_complete, Toast.LENGTH_SHORT).show();
                Log.i("AbDisc", state.toString());
                conn.receivedFilterState(state);
                DataProcessor dpCtrllr= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);
                dpCtrllr.enableFilterNotify(state.getSedentaryId());
                dpCtrllr.enableFilterNotify(state.getSessionStartId());
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
