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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.api.MetaWearBleService;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.DataProcessor;
import com.mbientlab.metawear.api.controller.Debug;
import com.mbientlab.metawear.api.controller.GPIO;
import com.mbientlab.metawear.api.controller.Timer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/**
 * Created by etsai on 6/4/2015.
 */
public class DebugFragment extends Fragment implements ServiceConnection {
    private static DebugFragment INSTANCE= null;
    private static final int ACTIVITY_PER_STEP= 20000;

    public static DebugFragment getInstance() {
        if (INSTANCE == null) {
            INSTANCE= new DebugFragment();
        }
        return INSTANCE;
    }

    private DataConnection conn;

    private final DataProcessor.Callbacks dpModuleCallbacks= new DataProcessor.Callbacks() {
        @Override
        public void receivedFilterOutput(byte filterId, byte[] output) {
            FilterState state= conn.getFilterState();

            if (state != null) {
                ByteBuffer buffer = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN);

                if (filterId == conn.getFilterState().getSedentaryId()) {
                    int milliG = buffer.getShort() & 0xffff;

                    sedentaryValue.setText(String.format(Locale.US, "%d", milliG));
                    steps += (milliG / ACTIVITY_PER_STEP);
                    stepCountValue.setText(String.format(Locale.US, "%d", steps));
                } else if (filterId == conn.getFilterState().getSensorId()) {
                    adcValue.setText(String.format(Locale.US, "%d", buffer.getShort()));
                } else if (filterId == conn.getFilterState().getOffsetUpdateId()) {
                    adcOffsetValue.setText(String.format(Locale.US, "%d", buffer.getShort()));
                } else if (filterId == conn.getFilterState().getSessionStartId()) {
                    crunchSessionCount++;
                    crunchSessionValue.setText(String.format(Locale.US, "%d", crunchSessionCount));
                } else if (filterId == conn.getFilterState().getCrunchOffsetId()) {
                    crunchOffsetValue.setText(String.format(Locale.US, "%d", buffer.getShort()));
                }
            }
        }
    };
    private final GPIO.Callbacks gpioModuleCallbacks= new GPIO.Callbacks() {
        @Override
        public void receivedAnalogInputAsSupplyRatio(byte pin, short value) {
            adcReadValue.setText(String.format(Locale.US, "%d", value));
        }
    };
    private MetaWearController mwCtrllr;
    private DataProcessor dpCtrllr;

    private short crunchSessionCount= 0;
    private int steps= 0;
    private TextView sedentaryValue, adcValue, adcOffsetValue, adcReadValue, crunchSessionValue, stepCountValue, crunchOffsetValue;

    public static String getTitle() {
        return "Diagnostics";
    }

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        sedentaryValue= (TextView) view.findViewById(R.id.debug_sedentary_value);
        adcValue= (TextView) view.findViewById(R.id.debug_adc_value);
        adcOffsetValue= (TextView) view.findViewById(R.id.debug_adc_offset_value);
        adcReadValue= (TextView) view.findViewById(R.id.debug_adc_read_value);
        crunchSessionValue= (TextView) view.findViewById(R.id.debug_crunch_session_count);
        stepCountValue= (TextView) view.findViewById(R.id.debug_step_count_value);
        crunchOffsetValue= (TextView) view.findViewById(R.id.debug_crunch_offset_value);

        ((CheckBox) view.findViewById(R.id.debug_stream_adc)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (conn.getFilterState() != null) {
                    if (isChecked) {
                        dpCtrllr.enableFilterNotify(conn.getFilterState().getSensorId());
                    } else {
                        dpCtrllr.disableFilterNotify(conn.getFilterState().getSensorId());
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.text_filter_setup_required, Toast.LENGTH_SHORT).show();
                }
            }
        });
        ((CheckBox) view.findViewById(R.id.debug_stream_adc_offset)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (conn.getFilterState() != null) {
                    if (isChecked) {
                        dpCtrllr.enableFilterNotify(conn.getFilterState().getOffsetUpdateId());
                    } else {
                        dpCtrllr.disableFilterNotify(conn.getFilterState().getOffsetUpdateId());
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.text_filter_setup_required, Toast.LENGTH_SHORT).show();
                }
            }
        });
        ((CheckBox) view.findViewById(R.id.debug_stream_adc_value)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (conn.getFilterState() != null) {
                    if (isChecked) {
                        mwCtrllr.addModuleCallback(gpioModuleCallbacks);
                    } else {
                        mwCtrllr.removeModuleCallback(gpioModuleCallbacks);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.text_filter_setup_required, Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.findViewById(R.id.debug_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conn.getFilterState() != null) {
                    Timer timerCtrllr = (Timer) mwCtrllr.getModuleController(Module.TIMER);
                    timerCtrllr.startTimer(conn.getFilterState().getSensorTimerId());

                    Accelerometer accelCtrllr = (Accelerometer) mwCtrllr.getModuleController(Module.ACCELEROMETER);
                    accelCtrllr.enableXYZSampling()
                            .withFullScaleRange(Accelerometer.SamplingConfig.FullScaleRange.FSR_8G)
                            .withOutputDataRate(Accelerometer.SamplingConfig.OutputDataRate.ODR_100_HZ)
                            .withHighPassFilter((byte) 0)
                            .withSilentMode();
                    ///< May want to configure the other options for tap detection
                    accelCtrllr.enableTapDetection(Accelerometer.TapType.DOUBLE_TAP, Accelerometer.Axis.Z)
                            .withThreshold(conn.getFilterState().getTapThreshold())
                            .withSilentMode();
                    accelCtrllr.startComponents();
                } else {
                    Toast.makeText(getActivity(), R.string.text_filter_setup_required, Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.findViewById(R.id.debug_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conn.getFilterState() != null) {
                    Timer timerCtrllr = (Timer) mwCtrllr.getModuleController(Module.TIMER);
                    timerCtrllr.stopTimer(conn.getFilterState().getSensorTimerId());

                    Accelerometer accelCtrllr = (Accelerometer) mwCtrllr.getModuleController(Module.ACCELEROMETER);
                    accelCtrllr.startComponents();
                } else {
                    Toast.makeText(getActivity(), R.string.text_filter_setup_required, Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.findViewById(R.id.debug_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Debug) mwCtrllr.getModuleController(Module.DEBUG)).resetDevice();
            }
        });
        view.findViewById(R.id.debug_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mwCtrllr.connect();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        final MetaWearBleService mwService= ((MetaWearBleService.LocalBinder) iBinder).getService();
        mwCtrllr= mwService.getMetaWearController(conn.getBluetoothDevice());
        dpCtrllr= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);
        mwCtrllr.addModuleCallback(dpModuleCallbacks);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
}
