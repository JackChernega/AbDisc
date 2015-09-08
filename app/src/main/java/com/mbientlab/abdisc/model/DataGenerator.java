package com.mbientlab.abdisc.model;


import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.sql.Date;
import java.util.ArrayList;

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
 * Created by Lance Gleason of Polyglot Programming LLC. on 8/6/15.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */
public class DataGenerator {
    public static void generateStepData(){
        long nowInMilliseconds = System.currentTimeMillis();
        int minutesToGenerate = 60*24*3;
        long currentTimeInMilliSeconds;
        LocalDateTime currentDateTime = LocalDateTime.now();
        ArrayList<StepReading> stepReadings = new ArrayList<>();
        ArrayList<CrunchPosture> crunchPostures = new ArrayList<>();

        for(int i = 0; i < minutesToGenerate; i++){
            long val = (long) (Math.random() * (60 * 6700));
            int hour = currentDateTime.minusMinutes(i).getHour();
            if(hour < 9 || hour > 20){
                val = (long) (val * 0.5);
            }
            currentTimeInMilliSeconds = nowInMilliseconds - (60000 * i);
            if((i % 30) == 0 && (Math.random() > 0.8)){
                CrunchPosture crunchPostureStart = new CrunchPosture(new Date(currentTimeInMilliSeconds),
                        CrunchPosture.MODE_CRUNCH,
                        CrunchPosture.STATUS_START);
                CrunchPosture crunchPostureStop = new CrunchPosture(new Date(currentTimeInMilliSeconds + (60000 * 30)),
                        CrunchPosture.MODE_CRUNCH,
                        CrunchPosture.STATUS_STOP);
                crunchPostures.add(crunchPostureStart);
                crunchPostures.add(crunchPostureStop);
            }
            StepReading stepReading = new StepReading((new Date(currentTimeInMilliSeconds)), val, true);
            stepReadings.add(stepReading);
        }

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(stepReadings)));
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(crunchPostures)));

    }
}
