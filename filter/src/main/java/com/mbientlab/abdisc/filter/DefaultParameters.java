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

/**
 * Created by etsai on 6/5/2015.
 */
public class DefaultParameters {
    public static final byte SENSOR_DATA_PIN= 0, SENSOR_GROUND_PIN= 1, SEDENTARY_TIME= 16, ADC_SAMPLE_SIZE= 4;
    public static final float CRUNCH_SESSION_DURATION= 120.f, L1_HAPTIC_STRENGTH= 40.f, L2_HAPTIC_STRENGTH= 70.f, L3_HAPTIC_STRENGTH= 90.f, TAP_THRESHOLD= 4.f;
    public static final int SEDENTARY_RESET_THRESHOLD= 2048, SEDENTARY_MIN_ACTIVITY_THRESHOLD= 2048, CRUNCH_SESSION_THRESHOLD_UPDATE= 20,
            L1_HAPTIC_LOWER= 16, L1_HAPTIC_UPPER= 384, L2_HAPTIC_LOWER= 384, L2_HAPTIC_UPPER= 768, L3_HAPTIC_LOWER= 768, L3_HAPTIC_UPPER= 1023,
            CRUNCH_THRESHOLD_UPDATE_PERIOD= 15000;
}
