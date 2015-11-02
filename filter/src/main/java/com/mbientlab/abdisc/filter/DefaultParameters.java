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
 * Default parameters used by the filter chain.  These are the values used by the filter upload button from the settings panel
 */
public class DefaultParameters {
    public static final String DEBUG_PANEL_PASSWORD="abdisc";

    /** GPIO pin that reads the ADC data */
    public static final byte SENSOR_DATA_PIN= 0;
    /** GPIO pin used to ground the circuit */
    public static final byte SENSOR_GROUND_PIN= 1;
    /** Number of ADC samples to average */
    public static final byte ADC_SAMPLE_SIZE= 5;

    /** How long a crunch session will last, in seconds*/
    public static final float CRUNCH_SESSION_DURATION= 75.f;
    /** How long (milliseconds) crunch ADC values must remain above or below the threshold to trigger a threshold update */
    public static final int CRUNCH_THRESHOLD_UPDATE_PERIOD= 12000;

    /** Threshold for a tap to register, max 8G */
    public static final float TAP_THRESHOLD= 2.f;

    /** How much activity that results from taking one step */
    public static final int ACTIVITY_PER_STEP= 6700;
    /** units of sedentary step period */
    public static final byte SEDENTARY_TIME= 20;
    /** Minimum activity level that indicates the device is worn.  If activity is less than this value, the step counter will not be updated */
    public static final int SEDENTARY_RESET_THRESHOLD= 180000;
    /** Minimum activity signifying an active user */
    public static final int SEDENTARY_MIN_ACTIVITY_THRESHOLD= 26000;

    /** Motor strength for level 1 feedback, between [0, 100] percent */
    public static final float L1_HAPTIC_STRENGTH= 80.f;
    /** Lower ADC value of haptic range for Level 1 feedback */
    public static final int L1_HAPTIC_LOWER= 3;
    /** Upper ADC value of haptic range for Level 1 feedback */
    public static final int L1_HAPTIC_UPPER= 15;

    /** Motor strength for level 2 feedback, between [0, 100] percent */
    public static final float L2_HAPTIC_STRENGTH= 90.f;
    /** Lower ADC value of haptic range for Level 2 feedback */
    public static final int L2_HAPTIC_LOWER= 16;
    /** Upper ADC value of haptic range for Level 2 feedback */
    public static final int L2_HAPTIC_UPPER= 31;

    /** Motor strength for level 3 feedback, between [0, 100] percent */
    public static final float L3_HAPTIC_STRENGTH= 100.f;
    /** Lower ADC value of haptic range for Level 3 feedback */
    public static final int L3_HAPTIC_LOWER= 32;
    /** Upper ADC value of haptic range for Level 3 feedback */
    public static final int L3_HAPTIC_UPPER= 1023;
}
