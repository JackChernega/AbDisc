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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by etsai on 6/3/2015.
 */
public class ParameterAdapter extends ArrayAdapter<Parameter> {
    public ParameterAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.filter_config_entry, parent, false);

            viewHolder= new ViewHolder();
            viewHolder.configName= (TextView) convertView.findViewById(R.id.filter_config_setting);
            viewHolder.configValue= (TextView) convertView.findViewById(R.id.filter_config_value);
            viewHolder.configEdit= (ImageButton) convertView.findViewById(R.id.filter_config_edit);
            viewHolder.configRevert= (ImageButton) convertView.findViewById(R.id.filter_config_revert);

            convertView.setTag(viewHolder);
        } else {
            viewHolder= (ViewHolder) convertView.getTag();
        }

        final TextView configEditValue= viewHolder.configValue;
        final Parameter current= getItem(position);

        viewHolder.configName.setText(current.name);
        viewHolder.configValue.setText(current.value);
        viewHolder.configEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogLayout= LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_config_edit, parent, false);
                final EditText configValue= (EditText) dialogLayout.findViewById(R.id.filter_config_edit_value);
                configValue.setText(current.value);

                ((TextView) dialogLayout.findViewById(R.id.filter_config_edit_description)).setText(current.description);

                AlertDialog.Builder builder= new AlertDialog.Builder(getContext()).setTitle(R.string.title_edit_setting)
                        .setPositiveButton(R.string.label_filter_config_commit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                current.value= configValue.getText().toString();
                                configEditValue.setText(current.value);
                            }
                        })
                        .setNegativeButton(R.string.label_cancel, null);

                builder.setView(dialogLayout);
                builder.show();
            }
        });
        viewHolder.configRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current.value= current.defaultValue.toString();
                configEditValue.setText(current.value);
            }
        });

        return convertView;
    }

    private class ViewHolder {
        public TextView configName, configValue;
        public ImageButton configEdit, configRevert;
    }
}
