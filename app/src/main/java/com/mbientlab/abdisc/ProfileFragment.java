package com.mbientlab.abdisc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.DialogPreference;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Hashtable;
import java.util.Locale;

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
 * Created by Lance Gleason of Polyglot Programming LLC. on 7/5/15.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */
public class ProfileFragment extends Fragment {
    AppState appState;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof AppState)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_app_state)));
        }

        appState = (AppState) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        final int profileFieldIds[] = {R.id.name, R.id.email, R.id.facebook, R.id.twitter, R.id.password};
        final Hashtable<Integer, Integer> profileFieldNames = new Hashtable();
        profileFieldNames.put(R.id.name, R.string.label_profile_name);
        profileFieldNames.put(R.id.email, R.string.label_profile_email);
        profileFieldNames.put(R.id.facebook, R.string.label_profile_facebook);
        profileFieldNames.put(R.id.twitter, R.string.label_profile_twitter);
        profileFieldNames.put(R.id.password, R.string.label_profile_password);
        final int[] profileEntries = {R.id.nameEntry, R.id.emailEntry, R.id.facebookEntry, R.id.twitterEntry, R.id.passwordEntry};
        final Hashtable<Integer, String> editorKeys = new Hashtable<>();
        editorKeys.put(R.id.name, "label_profile_name");
        editorKeys.put(R.id.email, "label_profile_email");
        editorKeys.put(R.id.facebook, "label_profile_facebook");
        editorKeys.put(R.id.twitter, "label_profile_twitter");
        editorKeys.put(R.id.password, "label_profile_password");
        final Hashtable<Integer, Integer> profileFieldEntries = new Hashtable<>();
        profileFieldEntries.put(R.id.name, R.id.nameEntry);
        profileFieldEntries.put(R.id.email, R.id.emailEntry);
        profileFieldEntries.put(R.id.facebook, R.id.facebookEntry);
        profileFieldEntries.put(R.id.twitter, R.id.twitterEntry);
        profileFieldEntries.put(R.id.password, R.id.passwordEntry);

        final SharedPreferences sharedPreferences = appState.getSharedPreferences();

        for (int i = 0; i < profileFieldIds.length; i++) {
            TextView profileEntry = (TextView) view.findViewById(profileEntries[i]);
            profileEntry.setText(sharedPreferences.getString(editorKeys.get(profileFieldIds[i]), ""));

            view.findViewById(profileFieldIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View promptView = layoutInflater.inflate(R.layout.profile_dialog, null);
                    TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogTitle);
                    promptLabel.setText(profileFieldNames.get(view.getId()));
                    final EditText promptContent = (EditText) promptView.findViewById(R.id.profileDialogText);
                    promptContent.setText(sharedPreferences.getString(editorKeys.get(view.getId()), ""));

                    if (view.getId() == R.id.password) {
                        promptContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if (view.getId() == R.id.name) {
                        promptContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    }

                    alertDialogBuilder.setView(promptView);
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    String promptValue = promptContent.getText().toString();
                                    editor.putString(editorKeys.get(view.getId()), promptValue);
                                    TextView profileValue = (TextView) view.findViewById(profileFieldEntries.get(view.getId()));
                                    profileValue.setText(promptValue);
                                    editor.apply();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }
            });
        }

        // radio button code
        TextView profileEntry = (TextView) view.findViewById(R.id.abDiskModeEntry);
        String abDiskMode = sharedPreferences.getString("ab_disk_mode", "");
        abDiskMode = abDiskMode.substring(0,1).toUpperCase() + abDiskMode.substring(1);
        profileEntry.setText(abDiskMode);

        view.findViewById(R.id.abDiskMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                View promptView = layoutInflater.inflate(R.layout.profile_radio_dialog, null);
                TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogTitle);
                promptLabel.setText(R.string.label_profile_ab_disk_mode);

                final RadioGroup abDiskRadio = (RadioGroup) promptView.findViewById(R.id.profileDialogRadioGroup);
                String abDiskMode = sharedPreferences.getString("ab_disk_mode", "");

                if (abDiskMode.equals("posture")) {
                    abDiskRadio.check(R.id.profileDialogButton1);
                } else {
                    abDiskRadio.check(R.id.profileDialogButton0);
                }

                alertDialogBuilder.setView(promptView);
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                int checkedId = abDiskRadio.getCheckedRadioButtonId();
                                String abDiskMode = "";
                                if (checkedId == R.id.profileDialogButton0) {
                                    abDiskMode = "crunch";
                                } else {
                                    abDiskMode = "posture";
                                }
                                editor.putString("ab_disk_mode", abDiskMode);
                                TextView profileValue = (TextView) view.findViewById(R.id.abDiskModeEntry);
                                abDiskMode = abDiskMode.substring(0,1).toUpperCase() + abDiskMode.substring(1);
                                profileValue.setText(abDiskMode);
                                editor.apply();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });

    }
}
