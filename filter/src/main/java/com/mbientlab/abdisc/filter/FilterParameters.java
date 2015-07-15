package com.mbientlab.abdisc.filter;

/**
 * Interface for configuring the crunch filter parameters
 * @author Eric Tsai
 */
public interface FilterParameters {
    public FilterParameters withAdcSampleSize(byte sampleSize);
    /**
     * Sets how long the warning period is before starting a crunch session, defaults to 450ms
     * @param duration Duration of the warning period, in milliseconds
     * @return Calling object
     */
    public FilterParameters withSessionWarningDuration(short duration);

    /**
     * Sets how strong to vibrate the motor during the warning period, defaults to 100%
     * @param strength Vibration strength, between 0% and 100%
     * @return Calling object
     */
    public FilterParameters withSessionWarningStrength(float strength);

    /**
     * Sets the GPIO pin used to ground the circuit, defaults to 1
     * @param pin GPIO pin that will function as ground
     * @return Calling object
     */
    public FilterParameters withSensorGroundPin(byte pin);

    /**
     * Sets the GPIO pin that reads the ADC data, defaults to 0
     * @param pin GPIO pin the sensor is connected to
     * @return Calling object
     */
    public FilterParameters withSensorDataPin(byte pin);

    /**
     * Sets how often to sample ADC data from the GPIO pin, defaults to 500ms
     * @param period Sampling period, in milliseconds, of the sensor GPIO pin
     * @return Calling object
     */
    public FilterParameters withSensorSamplingPeriod(int period);

    /**
     * Sets how often to log the activity rating, default 60 seconds (60000 ms)
     * @param period How often to log activity, in milliseconds
     * @return Calling object
     */
    public FilterParameters withSedentaryStepPeriod(int period);

    /**
     * Sets the units of sedentary step period
     * @param time
     * @return Calling object
     */
    public FilterParameters withSedentaryTime(byte time);

    /**
     * Sets the minimum activity required to restart the sedentary timer
     * @param threshold
     * @return Calling object
     */
    public FilterParameters withSedentaryResetThreshold(int threshold);

    /**
     * Sets the minimum activity level that indicates the device is worn.
     * This is used to stop the sedentary session when the device is not worn
     * @param threshold
     * @return Calling object
     */
    public FilterParameters withSedentaryMinActivityThreshold(int threshold);

    /**
     * Sets how long a crunch session will last, defaults 120 seconds
     * @param duration Crunch session duration, in seconds
     * @return Calling object
     */
    public FilterParameters withCrunchSessionDuration(float duration);

    /**
     * Sets the minimum change in baseline sensor value to update the crunch threshold
     * @param threshold Minimum change in sensor values required to update the crunch threshold
     * @return Calling object
     */
    public FilterParameters withCrunchThresholdUpdateMinChangeThreshold(int threshold);
    public FilterParameters withCrunchThresholdUpdateCheckPeriod(int period);

    /**
     * Enumeration of haptic feedback levels.  Depending on which range the relative ADC value lays in,
     * different settings are used to drive the vibration motor
     */
    public enum HapticLevel {
        /**
         * Haptic feedback level 1.  Defaults to ADC range [16, 384) at 40% strength for 500ms
         */
        L1,
        /**
         * Haptic feedback Level 2.  Defaults to ADC range [384, 768) at 70% strength for 700ms
         */
        L2,
        /**
         * Haptic feedback level 3.  Defaults to ADC range [768, 1024) at 90% strength for 900ms
         */
        L3;
    }

    /**
     * Sets parameters for haptic feedback: lower and upper ADC values, and vibration strength and period.  The ADC ranges
     * are relative to the crunch threshold i.e. input= present measurement - threshold
     * @param level Haptic feedback level to configure
     * @param crunchLower Lower ADC value of haptic range
     * @param crunchUpper Upper ADC value of haptic range
     * @param motorStrength Vibration strength, between [0, 100]
     * @param duration Vibration duration, in milliseconds
     * @return Calling object
     */
    public FilterParameters withHapticSettings(HapticLevel level, int crunchLower, int crunchUpper, float motorStrength, short duration);

    /**
     * Sets parameters for haptic feedback: lower and upper ADC value.  The ADC ranges
     * are relative to the crunch threshold i.e. input= present measurement - threshold
     * @param level Haptic feedback level to configure
     * @param crunchLower Lower ADC value of haptic range
     * @param crunchUpper Upper ADC value of haptic range
     * @return Calling object
     */
    public FilterParameters withHapticSettings(HapticLevel level, int crunchLower, int crunchUpper);

    public FilterParameters withHapticCrunchLower(HapticLevel level, int crunchLower);
    public FilterParameters withHapticCrunchUpper(HapticLevel level, int crunchUpper);
    public FilterParameters withHapticStrength(HapticLevel level, float motorStrength);

    public FilterParameters withTapThreshold(float threshold);
    /**
     * Write filter chain to the board
     */
    public void commit();
}
