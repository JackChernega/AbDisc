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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mbientlab.abdisc.R;

import java.util.ArrayList;

/**
 * Created by etsai on 6/3/2015.
 */
public class FilterConfigFragment extends Fragment {
    private FilterConfigAdapter configAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        configAdapter= new FilterConfigAdapter(getActivity(), R.id.filter_config_entry_layout);
        configAdapter.setNotifyOnChange(true);
        return inflater.inflate(R.layout.filter_config, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView configList= (ListView) view.findViewById(R.id.filter_config_list);
        configList.setAdapter(configAdapter);

        ArrayList<FilterConfig> configSettings= new ArrayList<>();
        configSettings.add(new FilterConfig("Sensor Data Pin", 0));
        configSettings.add(new FilterConfig("Sensor Ground Pin", 1));
        configSettings.add(new FilterConfig("Sedentary Reset Threshold", 2048));
        configSettings.add(new FilterConfig("Sedentary Min Activity Threshold", 2048));
        configSettings.add(new FilterConfig("Crunch Session Duration", 120.f));
        configSettings.add(new FilterConfig("Crunch Threshold Update Delta", 20));
        configSettings.add(new FilterConfig("L1 Haptic Lower Bound", 0));
        configSettings.add(new FilterConfig("L1 Haptic Upper Bound", 256));
        configSettings.add(new FilterConfig("L2 Haptic Lower Bound", 256));
        configSettings.add(new FilterConfig("L2 Haptic Upper Bound", 640));
        configSettings.add(new FilterConfig("L3 Haptic Lower Bound", 640));
        configSettings.add(new FilterConfig("L3 Haptic Upper Bound", 1023));
        configAdapter.addAll(configSettings);
    }
}
