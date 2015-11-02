package com.mbientlab.abdisc.utils;

import com.mbientlab.abdisc.DayActivityFragment;
import com.mbientlab.abdisc.filter.DefaultParameters;
import com.mbientlab.abdisc.model.StepReading;
import com.mbientlab.abdisc.model.StepReading$Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * Created by Lance Gleason of Polyglot Programming LLC. on 10/5/15.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */
public class StepDataUtils {
    public static HashMap getStepsByHourForDay(LocalDate date, boolean getTestData) {
        LocalDateTime startOfDay = date.atStartOfDay();
        List<Integer> stepsByHour = new ArrayList<>();
        int maxValue = 0;
        int activeMinutes = 0;

        for (int i = 0; i < 24; i++) {
            List<StepReading> hourSteps = new Select().from(StepReading.class)
                    .where(Condition.column(StepReading$Table.DATETIME)
                                    .between(startOfDay.plusHours(i).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli())
                                    .and(startOfDay.plusHours(i + 1).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()),
                            Condition.column(StepReading$Table.ISTESTDATA).eq(getTestData))
                    .queryList();
            int steps = 0;
            for (StepReading stepReading : hourSteps) {
                if (stepReading.getMilliG() > DefaultParameters.SEDENTARY_RESET_THRESHOLD) {
                    steps += (stepReading.getMilliG() / DefaultParameters.ACTIVITY_PER_STEP);
                    activeMinutes++;
                    
                    /*
                    if (stepsThisMinute > 5) {
                        steps += stepsThisMinute;
                        activeMinutes++;
                    }
                    */
                }
            }
            if (maxValue < steps) {
                maxValue = steps;
            }
            stepsByHour.add(steps);
        }

        maxValue = maxValue > 100 ? maxValue : 100;

        HashMap returnValues = new HashMap();
        returnValues.put("stepsByHour", stepsByHour);
        returnValues.put("maxValue", maxValue);
        returnValues.put("activeMinutes", activeMinutes);
        return returnValues;
    }
}
