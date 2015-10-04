package com.mbientlab.abdisc.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;

import com.mbientlab.abdisc.AppState;
import com.mbientlab.abdisc.MainActivity;
import com.mbientlab.abdisc.ProfileFragment;
import com.mbientlab.abdisc.R;
import com.mbientlab.abdisc.model.CrunchPosture;
import com.mbientlab.abdisc.model.StepReading;
import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.DataProcessor;
import com.mbientlab.metawear.api.controller.Logging;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
 * Created by Lance Gleason of Polyglot Programming LLC. on 10/2/15.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */
public class DataDownloaderFragment extends Fragment {

    private MetaWearController mwController;
    private Logging loggingCtrllr;
    private ProgressDialog setupProgress;
    private DataProcessor dataProcessorController;

    private final byte ACTIVITY_DATA_SIZE = 4;
    private final int TIME_DELAY_PERIOD = 60000;
    public final int CRUNCH_POSTURE_SESSION_TIMEOUT_IN_SECONDS = 120;
    private int totalEntryCount;
    private ArrayList<StepReading> stepReadings = new ArrayList<>();
    private ArrayList<CrunchPosture> crunchPostures = new ArrayList<>();
    private AppState appState;
    private String abDiscMode;

    @Override
    public void onAttach(Activity activity){
        super.onResume();
        if (!(activity instanceof AppState)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_app_state)));
        }

        appState = (AppState) activity;
    }

    private final Logging.Callbacks logCallbacks = new Logging.Callbacks() {
        private final float notifyRatio = 0.01f;
        private boolean isDownloading;
        private Logging.ReferenceTick refTick;
        private Logging.LogEntry firstEntry = null;
        private int sedentaryLogId = -1;
        private int sensorOffsetLoggingId = -1;
        private int sensorLogId = -1;
        private byte timeTriggerId = -1;
        private long startCrunchPostureTime = 0;

        @Override
        public void receivedLogEntry(final Logging.LogEntry entry) {
            if (firstEntry == null) {
                firstEntry = entry;
            }

            int activityMilliG = ByteBuffer.wrap(entry.data())
                    .order(ByteOrder.LITTLE_ENDIAN).getInt();

            if(sedentaryLogId == -1)
                sedentaryLogId = appState.getSharedPreferences().getInt(MainActivity.SEDENTARY_LOG_ID, -1);

            if(sensorOffsetLoggingId == -1)
                sensorOffsetLoggingId = appState.getSharedPreferences().getInt(MainActivity.SENSOR_OFFSET_LOGGING_ID, -1);

            if(sensorLogId == -1)
                sensorLogId = appState.getSharedPreferences().getInt(MainActivity.SENSOR_LOG_ID, -1);

            byte tId = entry.triggerId();
            Date entryTime = entry.timestamp(refTick).getTime();

            Calendar localCalendar = Calendar.getInstance();

            long entryTimeInMilliseconds = entryTime.getTime() + (localCalendar.get(Calendar.ZONE_OFFSET) + localCalendar.get(Calendar.DST_OFFSET));
            if (tId == sedentaryLogId) {
                Log.i("DataDownloaderFragment", "Time Trigger Id " + entryTime.toString() + String.valueOf(activityMilliG));//String.format(outputFormat, "Z-Axis", entryTime, Gs));
                Log.i("DataDownloaderFragment", String.format(Locale.US, "%.3f,%.3f",
                        entry.offset(firstEntry) / 1000.0, activityMilliG / 1000.0));
                StepReading stepReading = new StepReading(new java.sql.Date(entryTimeInMilliseconds), (long) activityMilliG, false);
                stepReadings.add(stepReading);
            } else if (tId == sensorOffsetLoggingId) {
                Log.i("DataDownloaderFragment", "Sensor Offset Logging ID startCrunchPostureTime: " + String.valueOf(startCrunchPostureTime));
                CrunchPosture crunchPostureRecord = null;
                if(startCrunchPostureTime == 0){
                    crunchPostureRecord = new CrunchPosture(new java.sql.Date(entryTimeInMilliseconds),
                            abDiscMode, CrunchPosture.STATUS_START, true);
                    startCrunchPostureTime = entryTimeInMilliseconds;
                }else if((entryTimeInMilliseconds - startCrunchPostureTime) > (CRUNCH_POSTURE_SESSION_TIMEOUT_IN_SECONDS * 1000)) {
                    crunchPostureRecord = new CrunchPosture(new java.sql.Date(startCrunchPostureTime + (CRUNCH_POSTURE_SESSION_TIMEOUT_IN_SECONDS * 1000)),
                            abDiscMode, CrunchPosture.STATUS_STOP, true);
                    crunchPostures.add(crunchPostureRecord);
                    crunchPostureRecord = new CrunchPosture(new java.sql.Date(entryTimeInMilliseconds),
                            abDiscMode, CrunchPosture.STATUS_START, true);
                    startCrunchPostureTime = entryTimeInMilliseconds;
                }else{
                    crunchPostureRecord = new CrunchPosture(new java.sql.Date(entryTimeInMilliseconds),
                            abDiscMode, CrunchPosture.STATUS_STOP, true);
                    startCrunchPostureTime = 0;
                }
                crunchPostures.add(crunchPostureRecord);
                Log.i("DataDownloaderFragment", "Sensor Offset Logging ID Time Trigger Id " + entryTime.toString() + String.valueOf(activityMilliG));
                Log.i("DataDownloaderFragment", String.format("Sensor Offset Logging ID, (%d, %s)",
                        tId, Arrays.toString(entry.data())));
            } else if (tId == sensorLogId){
                Log.i("DataDownloaderFragment", "Sensor Log ID Time Trigger Id " + entryTime.toString() + String.valueOf(activityMilliG));
                Log.i("DataDownloaderFragment", String.format("Sensor Log ID, (%d, %s)",
                        tId, Arrays.toString(entry.data())));
            } else {
                Log.i("DataDownloaderFragment", "Unknown Time Trigger Id " + entryTime.toString() + String.valueOf(activityMilliG));
                Log.i("DataDownloaderFragment", String.format("Unkown Trigger ID, (%d, %s)",
                        tId, Arrays.toString(entry.data())));
            }
        }

        @Override
        public void receivedReferenceTick(Logging.ReferenceTick reference) {
            refTick = reference;

            Log.i("LoggingExample", String.format("Received the reference tick = %s, %d", reference, reference.tickCount()));
            // Got the reference tick, make lets get
            // the log entry count
            loggingCtrllr.readTotalEntryCount();
        }

        @Override
        public void receivedTriggerId(byte triggerId) {
            byte triggerArray[] = {triggerId};
            timeTriggerId = triggerId;
            startLog();
            Log.i("receivedTrigger", "Received trigger id " + String.valueOf(triggerId));
            Log.i("encoded trigger", Base64.encodeToString(triggerArray, Base64.NO_WRAP));
            Log.i("decoded trigger", String.valueOf(Base64.decode(Base64.encodeToString(triggerArray, Base64.NO_WRAP), Base64.NO_WRAP)[0]));
        }

        private void startLog() {
            loggingCtrllr.startLogging();
        }

        @Override
        public void receivedTotalEntryCount(int totalEntries) {
            if (!isDownloading && (totalEntries > 0)) {
                totalEntryCount = totalEntries;
                isDownloading = true;
                Log.i("LoggingExample", "Download begin");

                //Got the entry count, lets now download the log
                loggingCtrllr.downloadLog(totalEntries, (int) (totalEntries * notifyRatio));
            } else {
                isDownloading = false;
                Log.i("LoggingExample", "Total Entries count " + String.valueOf(totalEntries));
                mwController.waitToClose(false);
                setupProgress.dismiss();
                setupProgress = null;
            }
        }

        @Override
        public void receivedDownloadProgress(int nEntriesLeft) {
            Log.i("LoggingExample", String.format("Entries remaining= %d", nEntriesLeft));
        }

        @Override
        public void downloadCompleted() {
            isDownloading = false;
            Log.i("removing ", String.valueOf((short) totalEntryCount) + " entries");
            loggingCtrllr.removeLogEntries((short) totalEntryCount);
            Log.i("LoggingExample", "Download completed");

            if(startCrunchPostureTime > 0) {
                CrunchPosture crunchPostureRecord = new CrunchPosture(new java.sql.Date(startCrunchPostureTime + (CRUNCH_POSTURE_SESSION_TIMEOUT_IN_SECONDS * 1000)),
                        abDiscMode, CrunchPosture.STATUS_STOP, true);
                crunchPostures.add(crunchPostureRecord);
            }

            TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(stepReadings)));
            TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(crunchPostures)));
            mwController.waitToClose(false);
            setupProgress.dismiss();
            setupProgress = null;
        }
    };

    private void setupLogginController(MetaWearController mwController) {
        Log.i("DataDownloader", "setting up logging controller");
        if (loggingCtrllr == null) {
            loggingCtrllr = (Logging) mwController.getModuleController(Module.LOGGING);
            mwController.addModuleCallback(logCallbacks);
        }
        if (dataProcessorController == null) {
            dataProcessorController = (DataProcessor) mwController.getModuleController(Module.DATA_PROCESSOR);
        }
    }

    public void startLogDownload(MetaWearController mwController, ProgressDialog setupProgress) {
        /*
           Before actually calling the downloadLog method, we will first gather the required
           data to compute the log timestamps and setup progress notifications.
           This means we will call downloadLog in one of the logging callback functions, and
           will start the callback chain here
         */
        stepReadings = new ArrayList<>();
        crunchPostures = new ArrayList<>();
        this.setupProgress = setupProgress;
        this.mwController = mwController;
        setupLogginController(mwController);
        SharedPreferences sharedPreferences = appState.getSharedPreferences();
        abDiscMode = sharedPreferences.getString(ProfileFragment.PROFILE_AB_DISK_MODE, CrunchPosture.MODE_CRUNCH);
        Log.i("Logging", "Starting Log Download");
        loggingCtrllr.readReferenceTick();
    }

}
