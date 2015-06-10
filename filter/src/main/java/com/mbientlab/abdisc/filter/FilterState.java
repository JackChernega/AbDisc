package com.mbientlab.abdisc.filter;

/**
 * Interface encapsulating the state of the filter after setting up the filter chain.
 * @author Eric Tsai
 */
public interface FilterState {
    /**
     * Get the timer ID that periodically reads ADC values from the GPIO pin
     * @return ID to be used with the Timer class
     * @see com.mbientlab.metawear.api.controller.Timer
     */
    public byte getSensorTimerId();

    public byte getSessionStartId();
    /**
     * Get the filter ID of the sedentary value
     * @return ID to be used with the DataProcessor class
     * @see com.mbientlab.metawear.api.controller.DataProcessor
     */
    public byte getSedentaryId();

    /**
     * Get the filter ID of the ADC values that controls a crunch session
     * @return ID to be used with the DataProcessor class
     * @see com.mbientlab.metawear.api.controller.DataProcessor
     */
    public byte getSensorId();

    /**
     * Get the filter ID of the ADC value offset
     * @return ID to be used with the DataProcessor class
     * @see com.mbientlab.metawear.api.controller.DataProcessor
     */
    public byte getOffsetUpdateId();

    /**
     * Get the log ID that records sedentary values
     * @return ID to be used with the Logging class
     * @see com.mbientlab.metawear.api.controller.Logging
     */
    public byte getSedentaryLogId();

    /**
     * Get the log ID that records ADC values controlling the crunch session
     * @return ID to be used with the Logging class
     * @see com.mbientlab.metawear.api.controller.Logging
     */
    public byte getSensorLogId();

    /**
     * Get the log ID that records the ADC value offset
     * @return ID to be used with the Logging class
     * @see com.mbientlab.metawear.api.controller.Logging
     */
    public byte getSensorOffsetLoggingId();
}
