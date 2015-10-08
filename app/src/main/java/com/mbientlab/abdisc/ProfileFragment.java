package com.mbientlab.abdisc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.abdisc.utils.GoalDataUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public static final String PROFILE_HEIGHT_FEET = "profile_height_feet";
    public static final String PROFILE_HEIGHT_INCHES = "profile_height_inches";
    public static final String PROFILE_GENDER = "profile_gender";
    public static final String PROFILE_AGE = "profile_age";
    public static final String PROFILE_SESSIONS = "profile_sessions";
    public static final String PROFILE_STEPS = "profile_steps";
    public static final String PROFILE_AB_DISK_MODE = "profile_ab_disk_mode";
    public static final String PROFILE_STRIDE = "profile_stride";
    public static final String PROFILE_SESSIONS_AUTOMATIC = "profile_sessions_automatic";
    public static final String PROFILE_STEPS_AUTOMATIC = "profile_steps_automatic";
    public static final String PROFILE_STRIDE_AUTOMATIC = "profile_stride_automatic";
    public static final String PROFILE_NAME = "profile_name";
    public static final String PROFILE_EMAIL = "profile_email";
    public static final String PROFILE_FACEBOOK = "profile_facebook";
    public static final String PROFILE_TWITTER = "profile_twitter";
    public static final String PROFILE_PASSWORD = "profile_password";
    public static final String PROFILE_WEIGHT = "profile_weight";
    public static final int DEFAULT_SESSIONS_GOAL = 12;


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_IMAGE = 2;
    private AppState appState;
    private MainActivity mainActivity;
    private Intent takePictureIntent;
    private Uri profilePhotoUri;
    private static final int RESULT_OK = -1;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.mbientlab.abdisc.fileprovider";
    private static final String PROFILE_PHOTO_FILE_NAME = "profile_photo.png";
    private File profilePhotoFile;
    private Bitmap profilePhotoBitmap;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof AppState)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_app_state)));
        }

        mainActivity = (MainActivity) activity;
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
        setupNoEditToast(view);
        setupHeader(view, sharedPreferences);
    }

    @Override
    public void onStart() {
        super.onStart();
        appState.setCurrentFragment(this);
    }

    public Bitmap getProfilePhotoBitmap() {
        return profilePhotoBitmap;
    }

    public void setProfilePhotoBitmap(Bitmap profilePhotoBitmap) {
        this.profilePhotoBitmap = profilePhotoBitmap;
        try {
            FileOutputStream profilePhotoOutputStream = new FileOutputStream(profilePhotoFile);
            profilePhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, profilePhotoOutputStream);
            profilePhotoOutputStream.close();
        } catch (FileNotFoundException e){
            Log.e("Profile Fragment Error ", e.toString());
        } catch (IOException e){
            Log.e("Profile Fragmetn Error", e.toString());
        }
    }


    private void setupHeader(View view, SharedPreferences sharedPreferences) {
        File path = new File(mainActivity.getFilesDir(), "images");
        if (!path.exists()) path.mkdirs();
        profilePhotoFile = new File(path, PROFILE_PHOTO_FILE_NAME);

        profilePhotoUri = FileProvider.getUriForFile(mainActivity, CAPTURE_IMAGE_FILE_PROVIDER,
                profilePhotoFile);
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, profilePhotoUri);
        ImageView mImageView = (ImageView) getView().findViewById(R.id.head_photo);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


                final CharSequence[] items = {"Take Photo", "Choose from Library",
                        "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Add Photo!");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        } else if (items[item].equals("Choose from Library")) {
                            Intent intent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select File"),
                                    REQUEST_GALLERY_IMAGE);
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        profilePhotoBitmap = BitmapFactory.decodeFile(profilePhotoFile.getAbsolutePath());

        if (profilePhotoBitmap != null) {
            mImageView.setImageBitmap(profilePhotoBitmap);
        } else {
            mImageView.setImageResource(R.drawable.colbert);
        }

        TextView profileNameTextView = (TextView) view.findViewById(R.id.persons_name);
        profileNameTextView.setText(sharedPreferences.getString(PROFILE_NAME, ""));

        getView().post(new Runnable() {
            @Override
            public void run() {
                LinearLayout header = (LinearLayout) getView().findViewById(R.id.heading);
                LinearLayout.LayoutParams headerParams = (LinearLayout.LayoutParams) header.getLayoutParams();
                ImageView imageView = (ImageView) getView().findViewById(R.id.head_photo);
                headerParams.height = imageView.getWidth() - 60;
                header.setLayoutParams(headerParams);
            }
        });
    }

    private String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String imagePath = null;

        if (requestCode == REQUEST_GALLERY_IMAGE) {
            if (data != null) {
                imagePath = getPath(data.getData(), mainActivity);
                if(imagePath == null){imagePath = data.getDataString();}
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            imagePath = profilePhotoFile.getAbsolutePath();
        }

        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_GALLERY_IMAGE) && resultCode == RESULT_OK && imagePath != null) {
            //Crop.of(profilePhotoUri, profilePhotoUri).asSquare().start(mainActivity);
            //}else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK){
            int targetHeight = getView().getHeight();
            int targetWidth = getView().getWidth();
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            if(imagePath.startsWith("content://com.google.android.apps.photos.content"))
            {
                try {
                    InputStream is = getView().getContext().getContentResolver().openInputStream(data.getData());
                    BitmapFactory.decodeStream(is, null, bmOptions);
                }catch (IOException e){
                    Log.e("Profile Fragment Error ", e.toString());
                }
            } else {
                BitmapFactory.decodeFile(imagePath, bmOptions);
            }

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetWidth, photoH / targetHeight);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            if(imagePath.startsWith("content://com.google.android.apps.photos.content"))
            {
                try {
                    InputStream is = getView().getContext().getContentResolver().openInputStream(data.getData());
                    profilePhotoBitmap = BitmapFactory.decodeStream(is, null, bmOptions);
                }catch (IOException e){
                    Log.e("Profile Fragment Error ", e.toString());
                }
            } else {
                profilePhotoBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
            }
            mainActivity.openCropPhotoFragment();
        }
    }

    private void setupNoEditToast(final View view) {
        view.findViewById(R.id.calories).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Toast noEditMessage = Toast.makeText(view.getContext(), R.string.label_profile_no_edit_calories_text, Toast.LENGTH_LONG);
                noEditMessage.show();
            }
        });
    }

    private void setupConditionalDialog(final View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        // hard coded for stride until others need this

        int labelId;
        String sharedPreferenceKey;

        if (view.getId() == R.id.stride) {
            labelId = R.string.label_profile_stride;
            sharedPreferenceKey = PROFILE_STRIDE;
        } else if (view.getId() == R.id.sessions) {
            int sessionsLabelId = R.string.label_profile_crunch_sessions;
            if (sharedPreferences.getString(PROFILE_AB_DISK_MODE, "").equals("posture")) {
                sessionsLabelId = R.string.label_profile_posture_sessions;
            }
            labelId = sessionsLabelId;
            sharedPreferenceKey = PROFILE_SESSIONS;
        } else {
            labelId = R.string.label_profile_steps;
            sharedPreferenceKey = PROFILE_STEPS;
        }


        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.profile_dialog_with_default, null);
        TextView promptLabel = (TextView) promptView.findViewById(R.id.profileDialogWithDefaultTitle);
        final EditText promptContent = (EditText) promptView.findViewById(R.id.profileDialogWithDefaultText);
        final CheckBox useAutomaticCheckbox = (CheckBox) promptView.findViewById(R.id.profileDialogWithDefaultUseAutoCheckbox);
        promptContent.setInputType(InputType.TYPE_CLASS_NUMBER);
        promptLabel.setText(labelId);
        promptContent.setText(String.valueOf(sharedPreferences.getInt(sharedPreferenceKey, 0)));
        promptContent.setEnabled(true);

        useAutomaticCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useAutomaticCheckbox.isChecked()) {
                    promptContent.setEnabled(false);
                    if (view.getId() == R.id.stride) {
                        promptContent.setText(String.valueOf(GoalDataUtils.calculateStride(sharedPreferences)));
                    } else if (view.getId() == R.id.sessions) {
                        promptContent.setText(String.valueOf(DEFAULT_SESSIONS_GOAL));
                    } else {
                        promptContent.setText(String.valueOf(GoalDataUtils.calculateStepGoal(sharedPreferences)));
                    }
                } else {
                    promptContent.setEnabled(true);
                }
            }
        });

        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.label_profile_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                String promptValue = promptContent.getText().toString();
                                boolean useAutomaticSettings = useAutomaticCheckbox.isChecked();

                                switch (view.getId()) {
                                    case R.id.stride:
                                        editor.putInt(PROFILE_STRIDE, Integer.valueOf(promptValue));
                                        editor.putBoolean(PROFILE_STRIDE_AUTOMATIC, useAutomaticSettings);
                                        TextView strideEntry = (TextView) view.findViewById(R.id.strideEntry);
                                        strideEntry.setText(promptValue + getString(R.string.label_profile_inches));
                                        break;
                                    case R.id.sessions:
                                        editor.putInt(PROFILE_SESSIONS, Integer.valueOf(promptValue));
                                        editor.putBoolean(PROFILE_SESSIONS_AUTOMATIC, useAutomaticSettings);
                                        TextView sessionsEntry = (TextView) view.findViewById(R.id.sessionsEntry);
                                        sessionsEntry.setText(promptValue);
                                        break;
                                    default:
                                        editor.putInt(PROFILE_STEPS, Integer.valueOf(promptValue));
                                        editor.putBoolean(PROFILE_STEPS_AUTOMATIC, useAutomaticSettings);
                                        TextView stepsEntry = (TextView) view.findViewById(R.id.stepsEntry);
                                        stepsEntry.setText(promptValue);
                                }
                                editor.apply();
                                dialog.dismiss();
                            }
                        }
                ).setNegativeButton(R.string.label_profile_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }

        );
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    private void setupYesNoDialogs(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        final int profileFieldIds[] = {R.id.stride, R.id.steps, R.id.sessions};
        final Hashtable<Integer, Integer> alertText = new Hashtable<>();
        alertText.put(R.id.stride, R.string.label_profile_yes_no_text_stride);
        alertText.put(R.id.steps, R.string.label_profile_yes_no_steps);
        alertText.put(R.id.sessions, R.string.label_profile_yes_no_text_sessions);

        TextView strideEntry = (TextView) view.findViewById(R.id.strideEntry);
        TextView stepsEntry = (TextView) view.findViewById(R.id.stepsEntry);
        TextView sessionsLabel = (TextView) view.findViewById(R.id.sessionsLabel);
        TextView sessionsEntry = (TextView) view.findViewById(R.id.sessionsEntry);

        int stride;
        int steps;
        int sessions;

        for (int profileFieldId : profileFieldIds) {
            view.findViewById(profileFieldId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (((view.getId() == R.id.stride) && sharedPreferences.getBoolean(PROFILE_STRIDE_AUTOMATIC, true)) ||
                            (view.getId() == R.id.steps) && sharedPreferences.getBoolean(PROFILE_STEPS_AUTOMATIC, true) ||
                            ((view.getId() == R.id.sessions) && sharedPreferences.getBoolean(PROFILE_SESSIONS_AUTOMATIC, true))) {
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
                    } else {
                        setupConditionalDialog(view, sharedPreferences, alertDialogBuilder);
                    }
                }
            });
        }

        stride = GoalDataUtils.getStride(sharedPreferences);

        steps = GoalDataUtils.getStepGoal(sharedPreferences);

        int sessionsLabelId = R.string.label_profile_crunch_sessions;

        if (sharedPreferences.getString(PROFILE_AB_DISK_MODE, "").equals("posture")) {
            sessionsLabelId = R.string.label_profile_posture_sessions;
        }

        if (sharedPreferences.getBoolean(PROFILE_SESSIONS_AUTOMATIC, true)) {
            sessions = DEFAULT_SESSIONS_GOAL;
        } else {
            sessions = sharedPreferences.getInt(PROFILE_SESSIONS, DEFAULT_SESSIONS_GOAL);
        }

        sessionsLabel.setText(sessionsLabelId);
        sessionsEntry.setText(String.valueOf(sessions));
        strideEntry.setText(String.valueOf(stride) + getString(R.string.label_profile_inches));
        stepsEntry.setText(String.valueOf(steps));
    }

    private void setupTextFieldDialogs(View view, final SharedPreferences sharedPreferences, final AlertDialog.Builder alertDialogBuilder) {
        final int profileFieldIds[] = {R.id.name, R.id.email, R.id.facebook, R.id.twitter, R.id.password, R.id.weight, R.id.age};
        final Hashtable<Integer, Integer> profileFieldNames = new Hashtable<>();
        profileFieldNames.put(R.id.name, R.string.label_profile_name);
        profileFieldNames.put(R.id.email, R.string.label_profile_email);
        profileFieldNames.put(R.id.facebook, R.string.label_profile_facebook);
        profileFieldNames.put(R.id.twitter, R.string.label_profile_twitter);
        profileFieldNames.put(R.id.password, R.string.label_profile_password);
        profileFieldNames.put(R.id.weight, R.string.label_profile_weight);
        profileFieldNames.put(R.id.age, R.string.label_profile_age);
        final int[] profileEntries = {R.id.nameEntry, R.id.emailEntry, R.id.facebookEntry, R.id.twitterEntry, R.id.passwordEntry, R.id.weightEntry, R.id.ageEntry};
        final Hashtable<Integer, String> sharedPreferenceKeys = new Hashtable<>();
        sharedPreferenceKeys.put(R.id.name, PROFILE_NAME);
        sharedPreferenceKeys.put(R.id.email, PROFILE_EMAIL);
        sharedPreferenceKeys.put(R.id.facebook, PROFILE_FACEBOOK);
        sharedPreferenceKeys.put(R.id.twitter, PROFILE_TWITTER);
        sharedPreferenceKeys.put(R.id.password, PROFILE_PASSWORD);
        sharedPreferenceKeys.put(R.id.weight, PROFILE_WEIGHT);
        sharedPreferenceKeys.put(R.id.age, PROFILE_AGE);
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
                        promptContent.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else if (view.getId() == R.id.age) {
                        promptLabel.setText(profileFieldNames.get(view.getId()));
                        promptContent.setInputType(InputType.TYPE_CLASS_NUMBER);
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
                            .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
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
                                        } else if (view.getId() == R.id.name) {
                                            TextView profileNameTextView = (TextView) getView().findViewById(R.id.persons_name);
                                            editor.putString(sharedPreferenceKeys.get(view.getId()), promptValue);
                                            profileNameTextView.setText(promptValue);
                                        } else {
                                            editor.putString(sharedPreferenceKeys.get(view.getId()), promptValue);
                                        }
                                        profileValue.setText(promptValue);
                                    }
                                    editor.apply();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
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
        final Hashtable<Integer, Integer> profileFieldNames = new Hashtable<>();
        profileFieldNames.put(R.id.abDiskMode, R.string.label_profile_ab_disk_mode);
        profileFieldNames.put(R.id.gender, R.string.label_profile_gender);
        final Hashtable<Integer, String> sharedPreferenceKeys = new Hashtable<>();
        sharedPreferenceKeys.put(R.id.abDiskMode, PROFILE_AB_DISK_MODE);
        sharedPreferenceKeys.put(R.id.gender, PROFILE_GENDER);
        final Hashtable<Integer, Integer> profileFieldEntries = new Hashtable<>();
        profileFieldEntries.put(R.id.abDiskMode, R.id.abDiskModeEntry);
        profileFieldEntries.put(R.id.gender, R.id.genderEntry);

        for (int profileFieldId : profileFieldIds) {
            TextView profileEntry = (TextView) view.findViewById(profileFieldEntries.get(profileFieldId));
            String currentSetting = sharedPreferences.getString(sharedPreferenceKeys.get(profileFieldId), "");
            if (currentSetting.length() > 0) {
                currentSetting = currentSetting.substring(0, 1).toUpperCase() + currentSetting.substring(1);
            }
            profileEntry.setText(currentSetting);

            view.findViewById(profileFieldId).setOnClickListener(new View.OnClickListener() {
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
                            .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    int checkedId = radioGroup.getCheckedRadioButtonId();
                                    String checkboxSelection;
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
                            .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
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
        int heightFeet = sharedPreferences.getInt(PROFILE_HEIGHT_FEET, 0);
        int heightInches = sharedPreferences.getInt(PROFILE_HEIGHT_INCHES, 0);
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

                int heightFeet = sharedPreferences.getInt(PROFILE_HEIGHT_FEET, 0);
                int heightInches = sharedPreferences.getInt(PROFILE_HEIGHT_INCHES, 0);

                heightSpinnerFeet.setSelection(heightFeet);
                heightSpinnerInches.setSelection(heightInches);

                alertDialogBuilder.setView(promptView);
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                int heightFeet = heightSpinnerFeet.getSelectedItemPosition();
                                int heightInches = heightSpinnerInches.getSelectedItemPosition();

                                editor.putInt(PROFILE_HEIGHT_FEET, heightFeet);
                                editor.putInt(PROFILE_HEIGHT_INCHES, heightInches);

                                profileEntry.setText(getHeightString(heightFeet, heightInches));
                                editor.apply();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
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
