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

package com.mbientlab.abdisc.debug;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.mbientlab.abdisc.DataConnection;
import com.mbientlab.abdisc.R;
import com.mbientlab.abdisc.filter.DefaultParameters;
import com.mbientlab.abdisc.filter.FilterParameters;
import com.mbientlab.abdisc.filter.FilterSetup;
import com.mbientlab.abdisc.filter.FilterState;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by etsai on 6/3/2015.
 */
public class FilterConfigFragment extends Fragment {
    private FilterConfigAdapter configAdapter;
    private DataConnection conn;
    private FilterParameters parameterSetup;

    private ProgressDialog setupProgress;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof DataConnection)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_data_connection)));
        }

        conn= (DataConnection) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parameterSetup= FilterSetup.configure(conn.getMetaWearController(), new FilterSetup.SetupListener() {
            @Override
            public void ready(FilterState state) {
                setupProgress.dismiss();
                setupProgress= null;

                Toast.makeText(getActivity(), R.string.text_filter_setup_complete, Toast.LENGTH_SHORT).show();
                Log.i("AbDisc", state.toString());
                conn.receivedFilterState(state);
            }
        });
        configAdapter= new FilterConfigAdapter(getActivity(), R.id.filter_config_entry_layout);
        configAdapter.setNotifyOnChange(true);
        return inflater.inflate(R.layout.filter_config, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView configList= (ListView) view.findViewById(R.id.filter_config_list);
        configList.setAdapter(configAdapter);

        final ArrayList<FilterConfig> configSettings= new ArrayList<>();
        configSettings.add(new FilterConfig("Sensor Data Pin", DefaultParameters.SENSOR_DATA_PIN) {
            @Override
            public void writeSetting() {
                parameterSetup.withSensorDataPin(Byte.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("Sensor Ground Pin", DefaultParameters.SENSOR_GROUND_PIN) {
            @Override
            public void writeSetting() {
                parameterSetup.withSensorGroundPin(Byte.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("Sedentary Reset Threshold", DefaultParameters.SEDENTARY_RESET_THRESHOLD) {
            @Override
            public void writeSetting() {
                parameterSetup.withSedentaryResetThreshold(Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("Sedentary Min Activity Threshold", DefaultParameters.SEDENTARY_MIN_ACTIVITY_THRESHOLD) {
            @Override
            public void writeSetting() {
                parameterSetup.withSedentaryMinActivityThreshold(Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("Crunch Session Duration", DefaultParameters.CRUNCH_SESSION_DURATION) {
            @Override
            public void writeSetting() {
                parameterSetup.withCrunchSessionDuration(Float.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("Crunch Threshold Update Delta", DefaultParameters.CRUNCH_SESSION_THRESHOLD_UPDATE) {
            @Override
            public void writeSetting() {
                parameterSetup.withCrunchThresholdUpdateMinChangeThreshold(Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("L1 Haptic Lower Bound", DefaultParameters.L1_HAPTIC_LOWER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchLower(FilterParameters.HapticLevel.L1, Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("L1 Haptic Upper Bound", DefaultParameters.L1_HAPTIC_UPPER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchUpper(FilterParameters.HapticLevel.L1, Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("L2 Haptic Lower Bound", DefaultParameters.L2_HAPTIC_LOWER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchLower(FilterParameters.HapticLevel.L2, Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("L2 Haptic Upper Bound", DefaultParameters.L2_HAPTIC_UPPER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchUpper(FilterParameters.HapticLevel.L2, Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("L3 Haptic Lower Bound", DefaultParameters.L3_HAPTIC_LOWER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchLower(FilterParameters.HapticLevel.L3, Integer.valueOf(this.value));
            }
        });
        configSettings.add(new FilterConfig("L3 Haptic Upper Bound", DefaultParameters.L3_HAPTIC_UPPER) {
            @Override
            public void writeSetting() {
                parameterSetup.withHapticCrunchUpper(FilterParameters.HapticLevel.L3, Integer.valueOf(this.value));
            }
        });
        configAdapter.addAll(configSettings);

        view.findViewById(R.id.filter_config_program).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupProgress= new ProgressDialog(getActivity());
                setupProgress.setIndeterminate(true);
                setupProgress.setMessage("Setting up filters...");
                setupProgress.show();

                for(FilterConfig filterCfg: configSettings) {
                    filterCfg.writeSetting();
                }
                parameterSetup.commit();
            }
        });
    }
}
