package com.mbientlab.abdisc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
        final SharedPreferences sharedPreferences = appState.getSharedPreferences();

        setupTextFieldDialogs(view, sharedPreferences, alertDialogBuilder);
        setupRadioDialogs(view, sharedPreferences, alertDialogBuilder);
        setupSpinnerDialogs(view, sharedPreferences, alertDialogBuilder);
        setupYesNoDialogs(view, sharedPreferences, alertDialogBuilder);
    }

    private int getHeightInInches(SharedPreferences sharedPreferences) {
        int heightInFeet = sharedPreferences.getInt("profile_height_feet", 0);
        int heightInInches = sharedPreferences.getInt("profile_height_inches", 0);
        return ((heightInFeet * 12) + heightInInches);
    }

    private int calculateStride(SharedPreferences sharedPreferences) {
        String gender = sharedPreferences.getString("profile_gender", "");
        int genderHeightOffset = 0;
        int genderOffset = 0;
        int heightInInches = getHeightInInches(sharedPreferences);

        if (heightInInches > 0) {
            if (gender.equals("male")) {
                genderHeightOffset = 70;
                genderOffset = 31;
            } else if (gender.equals("female")) {
                genderHeightOffset = 64;
                genderOffset = 26;
            }
        }
        return ((new Double(genderOffset + ((heightInInches - genderHeightOffset) * 0.75)).intValue()));
    }

    private void setupConditionalDialog(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        // hard coded for stride until others need this

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.profile_dialog_with_default, null);
        TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogWithDefaultTitle);
        final EditText promptContent = (EditText) promptView.findViewById(R.id.profileDialogWithDefaultText);
        final CheckBox useAutomaticCheckbox = (CheckBox) promptView.findViewById(R.id.profileDialogWithDefaultUseAutoCheckbox);
        promptContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        promptLabel.setText(R.string.label_profile_stride);
        promptContent.setText(String.valueOf(sharedPreferences.getInt("profile_stride", 0)));

        useAutomaticCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useAutomaticCheckbox.isChecked()) {
                    promptContent.setEnabled(false);
                    promptContent.setText(String.valueOf(calculateStride(sharedPreferences)));
                } else {
                    promptContent.setEnabled(true);
                }
            }
        });

        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String promptValue = promptContent.getText().toString();
                        boolean useAutomaticSettings = useAutomaticCheckbox.isChecked();

                        editor.putInt("profile_stride", Integer.valueOf(promptValue));
                        editor.putBoolean("profile_stride_automatic", useAutomaticSettings);

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

    private void setupYesNoDialogs(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        final int profileFieldIds[] = {R.id.stride};
        final Hashtable<Integer, Integer> alertText = new Hashtable();
        alertText.put(R.id.stride, R.string.label_profile_yes_no_text_stride);

        for (int i = 0; i < profileFieldIds.length; i++) {

            view.findViewById(profileFieldIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View promptView = layoutInflater.inflate(R.layout.profile_dialog_yes_no, null);
                    TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogYesNoText);
                    promptLabel.setText(alertText.get(view.getId()));

                    alertDialogBuilder.setView(promptView);
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton(R.string.label_profile_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    setupConditionalDialog(view, sharedPreferences, alertDialogBuilder);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.label_profile_no, new DialogInterface.OnClickListener() {
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

    private void setupTextFieldDialogs(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        final int profileFieldIds[] = {R.id.name, R.id.email, R.id.facebook, R.id.twitter, R.id.password, R.id.weight, R.id.age};
        final Hashtable<Integer, Integer> profileFieldNames = new Hashtable();
        profileFieldNames.put(R.id.name, R.string.label_profile_name);
        profileFieldNames.put(R.id.email, R.string.label_profile_email);
        profileFieldNames.put(R.id.facebook, R.string.label_profile_facebook);
        profileFieldNames.put(R.id.twitter, R.string.label_profile_twitter);
        profileFieldNames.put(R.id.password, R.string.label_profile_password);
        profileFieldNames.put(R.id.weight, R.string.label_profile_weight);
        profileFieldNames.put(R.id.age, R.string.label_profile_age);
        final int[] profileEntries = {R.id.nameEntry, R.id.emailEntry, R.id.facebookEntry, R.id.twitterEntry, R.id.passwordEntry, R.id.weightEntry, R.id.ageEntry};
        final Hashtable<Integer, String> sharedPreferenceKeys = new Hashtable<>();
        sharedPreferenceKeys.put(R.id.name, "profile_name");
        sharedPreferenceKeys.put(R.id.email, "profile_email");
        sharedPreferenceKeys.put(R.id.facebook, "profile_facebook");
        sharedPreferenceKeys.put(R.id.twitter, "profile_twitter");
        sharedPreferenceKeys.put(R.id.password, "profile_password");
        sharedPreferenceKeys.put(R.id.weight, "profile_weight");
        sharedPreferenceKeys.put(R.id.age, "profile_age");
        final Hashtable<Integer, Integer> profileFieldEntries = new Hashtable<>();
        profileFieldEntries.put(R.id.name, R.id.nameEntry);
        profileFieldEntries.put(R.id.email, R.id.emailEntry);
        profileFieldEntries.put(R.id.facebook, R.id.facebookEntry);
        profileFieldEntries.put(R.id.twitter, R.id.twitterEntry);
        profileFieldEntries.put(R.id.password, R.id.passwordEntry);
        profileFieldEntries.put(R.id.weight, R.id.weightEntry);
        profileFieldEntries.put(R.id.age, R.id.ageEntry);

        for (int i = 0; i < profileFieldIds.length; i++) {
            TextView profileEntry = (TextView) view.findViewById(profileEntries[i]);
            if (profileEntry.getId() == R.id.weightEntry) {
                profileEntry.setText(String.valueOf(sharedPreferences.getInt(sharedPreferenceKeys.get(profileFieldIds[i]), 0)) + getString(R.string.label_profile_pounds));
            } else if (profileEntry.getId() == R.id.ageEntry) {
                profileEntry.setText(String.valueOf(sharedPreferences.getInt(sharedPreferenceKeys.get(profileFieldIds[i]), 0)));
            } else {
                profileEntry.setText(sharedPreferences.getString(sharedPreferenceKeys.get(profileFieldIds[i]), ""));
            }

            view.findViewById(profileFieldIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View promptView = layoutInflater.inflate(R.layout.profile_dialog, null);
                    TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogTitle);
                    final EditText promptContent = (EditText) promptView.findViewById(R.id.profileDialogText);

                    if (view.getId() == R.id.password) {
                        promptLabel.setText(profileFieldNames.get(view.getId()));
                        promptContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if (view.getId() == R.id.name) {
                        promptLabel.setText(profileFieldNames.get(view.getId()));
                        promptContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    } else if (view.getId() == R.id.weight) {
                        promptLabel.setText(getString(profileFieldNames.get(view.getId())) + getString(R.string.label_profile_in_pounds));
                        promptContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    } else if (view.getId() == R.id.age) {
                        promptLabel.setText(profileFieldNames.get(view.getId()));
                        promptContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    } else {
                        promptLabel.setText(profileFieldNames.get(view.getId()));
                    }

                    if (view.getId() == R.id.weight) {
                        int weight = sharedPreferences.getInt(sharedPreferenceKeys.get(view.getId()), 0);
                        String weightString = String.valueOf(weight);
                        promptContent.setText(weightString);
                    } else if (view.getId() == R.id.age) {
                        promptContent.setText(String.valueOf(sharedPreferences.getInt(sharedPreferenceKeys.get(view.getId()), 0)));
                    } else {
                        promptContent.setText(sharedPreferences.getString(sharedPreferenceKeys.get(view.getId()), ""));
                    }

                    alertDialogBuilder.setView(promptView);
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    String promptValue = promptContent.getText().toString();
                                    editor.putString(sharedPreferenceKeys.get(view.getId()), promptValue);
                                    TextView profileValue = (TextView) view.findViewById(profileFieldEntries.get(view.getId()));
                                    if (view.getId() == R.id.weight) {
                                        profileValue.setText(promptValue + getString(R.string.label_profile_pounds));
                                        editor.putInt(sharedPreferenceKeys.get(view.getId()), Integer.valueOf(promptValue));
                                    } else {
                                        if (view.getId() == R.id.age) {
                                            editor.putInt(sharedPreferenceKeys.get(view.getId()), Integer.valueOf(promptValue));
                                        } else {
                                            editor.putString(sharedPreferenceKeys.get(view.getId()), promptValue);
                                        }
                                        profileValue.setText(promptValue);
                                    }
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

    private void setupRadioDialogs(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        // radio button code
        final int profileFieldIds[] = {R.id.abDiskMode, R.id.gender};
        final Hashtable<Integer, Integer> profileFieldNames = new Hashtable();
        profileFieldNames.put(R.id.abDiskMode, R.string.label_profile_ab_disk_mode);
        profileFieldNames.put(R.id.gender, R.string.label_profile_gender);
        final int[] profileEntries = {R.id.abDiskModeEntry, R.id.genderEntry};
        final Hashtable<Integer, String> sharedPreferenceKeys = new Hashtable<>();
        sharedPreferenceKeys.put(R.id.abDiskMode, "profile_ab_disk_mode");
        sharedPreferenceKeys.put(R.id.gender, "profile_gender");
        final Hashtable<Integer, Integer> profileFieldEntries = new Hashtable<>();
        profileFieldEntries.put(R.id.abDiskMode, R.id.abDiskModeEntry);
        profileFieldEntries.put(R.id.gender, R.id.genderEntry);

        for (int i = 0; i < profileFieldIds.length; i++) {
            TextView profileEntry = (TextView) view.findViewById(profileFieldEntries.get(profileFieldIds[i]));
            String currentSetting = sharedPreferences.getString(sharedPreferenceKeys.get(profileFieldIds[i]), "");
            if (currentSetting.length() > 0) {
                currentSetting = currentSetting.substring(0, 1).toUpperCase() + currentSetting.substring(1);
            }
            profileEntry.setText(currentSetting);

            view.findViewById(profileFieldIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View promptView = layoutInflater.inflate(R.layout.profile_radio_dialog, null);
                    TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogTitle);
                    int viewId = view.getId();
                    int profileFieldId = profileFieldNames.get(viewId);
                    promptLabel.setText(profileFieldId);

                    final RadioGroup radioGroup = (RadioGroup) promptView.findViewById(R.id.profileDialogRadioGroup);
                    String checkboxSelection = sharedPreferences.getString(sharedPreferenceKeys.get(view.getId()), "");

                    if ((view.getId() == R.id.abDiskMode && checkboxSelection.equals("posture")) ||
                            (view.getId() == R.id.gender && checkboxSelection.equals("female"))) {
                        radioGroup.check(R.id.profileDialogButton1);
                    } else {
                        radioGroup.check(R.id.profileDialogButton0);
                    }

                    RadioButton radioButton0 = (RadioButton) promptView.findViewById(R.id.profileDialogButton0);
                    RadioButton radioButton1 = (RadioButton) promptView.findViewById(R.id.profileDialogButton1);

                    if (view.getId() == R.id.abDiskMode) {
                        radioButton0.setText(R.string.label_profile_mode_crunch);
                        radioButton1.setText(R.string.label_profile_mode_posture);
                    } else {
                        radioButton0.setText(R.string.label_profile_gender_male);
                        radioButton1.setText(R.string.label_profile_gender_female);
                    }

                    alertDialogBuilder.setView(promptView);
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    int checkedId = radioGroup.getCheckedRadioButtonId();
                                    String checkboxSelection = "";
                                    if ((checkedId == R.id.profileDialogButton0) && (view.getId() == R.id.abDiskMode)) {
                                        checkboxSelection = "crunch";
                                    } else if ((checkedId == R.id.profileDialogButton0) && (view.getId() == R.id.gender)) {
                                        checkboxSelection = "male";
                                    } else if (view.getId() == R.id.gender) {
                                        checkboxSelection = "female";
                                    } else {
                                        checkboxSelection = "posture";
                                    }
                                    editor.putString(sharedPreferenceKeys.get(view.getId()), checkboxSelection);
                                    TextView profileValue = (TextView) view.findViewById(profileFieldEntries.get(view.getId()));
                                    checkboxSelection = checkboxSelection.substring(0, 1).toUpperCase() + checkboxSelection.substring(1);
                                    profileValue.setText(checkboxSelection);
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

    private String getHeightString(int feet, int inches) {
        return String.valueOf(feet) + "' " + String.valueOf(inches) + "\"";
    }

    private void setupSpinnerDialogs(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        final TextView profileEntry = (TextView) view.findViewById(R.id.heightEntry);
        int heightFeet = sharedPreferences.getInt("profile_height_feet", 0);
        int heightInches = sharedPreferences.getInt("profile_height_inches", 0);
        profileEntry.setText(getHeightString(heightFeet, heightInches));

        view.findViewById(R.id.height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                View promptView = layoutInflater.inflate(R.layout.profile_height_dialog, null);

                final Spinner heightSpinnerFeet = (Spinner) promptView.findViewById(R.id.profileDialogHeightFeet);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> heightAdapterFeet = ArrayAdapter.createFromResource(promptView.getContext(),
                        R.array.height_feet, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                heightAdapterFeet.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                heightSpinnerFeet.setAdapter(heightAdapterFeet);

                final Spinner heightSpinnerInches = (Spinner) promptView.findViewById(R.id.profileDialogHeightInches);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> heightAdapterInches = ArrayAdapter.createFromResource(promptView.getContext(),
                        R.array.height_inches, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                heightAdapterInches.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                heightSpinnerInches.setAdapter(heightAdapterInches);

                int heightFeet = sharedPreferences.getInt("profile_height_feet", 0);
                int heightInches = sharedPreferences.getInt("profile_height_inches", 0);

                heightSpinnerFeet.setSelection(heightFeet);
                heightSpinnerInches.setSelection(heightInches);

                alertDialogBuilder.setView(promptView);
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                int heightFeet = heightSpinnerFeet.getSelectedItemPosition();
                                int heightInches = heightSpinnerInches.getSelectedItemPosition();

                                editor.putInt("profile_height_feet", heightFeet);
                                editor.putInt("profile_height_inches", heightInches);

                                profileEntry.setText(getHeightString(heightFeet, heightInches));
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
