package com.mbientlab.abdisc.utils;

import android.content.SharedPreferences;

import com.mbientlab.abdisc.ProfileFragment;

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
 * Created by Lance Gleason of Polyglot Programming LLC. on 8/24/15.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */
public class GoalDataUtils {
    public static int calculateStepGoal(SharedPreferences sharedPreferences) {
        int age = sharedPreferences.getInt(ProfileFragment.PROFILE_AGE, 0);
        return (11000 - (age * 75));
    }

    public static int getStepGoal(SharedPreferences sharedPreferences) {
        int steps;
        if (sharedPreferences.getBoolean(ProfileFragment.PROFILE_STEPS_AUTOMATIC, true)) {
            steps = calculateStepGoal(sharedPreferences);
        } else {
            steps = sharedPreferences.getInt(ProfileFragment.PROFILE_STEPS, 0);
        }
        return steps;
    }

    public static int getHeightInInches(SharedPreferences sharedPreferences) {
        int heightInFeet = sharedPreferences.getInt(ProfileFragment.PROFILE_HEIGHT_FEET, 0);
        int heightInInches = sharedPreferences.getInt(ProfileFragment.PROFILE_HEIGHT_INCHES, 0);
        return ((heightInFeet * 12) + heightInInches);
    }

    public static int calculateStride(SharedPreferences sharedPreferences) {
        String gender = sharedPreferences.getString(ProfileFragment.PROFILE_GENDER, "");
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
        return ((Double.valueOf(genderOffset + ((heightInInches - genderHeightOffset) * 0.75)).intValue()));
    }

    public static int getStride(SharedPreferences sharedPreferences) {
        int stride;
        if (sharedPreferences.getBoolean(ProfileFragment.PROFILE_STRIDE_AUTOMATIC, true)) {
            stride = calculateStride(sharedPreferences);
        } else {
            stride = sharedPreferences.getInt(ProfileFragment.PROFILE_STRIDE, 0);
        }
        return stride;
    }
}
