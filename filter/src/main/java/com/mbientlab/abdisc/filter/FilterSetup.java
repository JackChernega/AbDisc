package com.mbientlab.abdisc.filter;

import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.controller.Accelerometer;
import com.mbientlab.metawear.api.controller.DataProcessor;
import com.mbientlab.metawear.api.controller.Event;
import com.mbientlab.metawear.api.controller.GPIO;
import com.mbientlab.metawear.api.controller.Haptic;
import com.mbientlab.metawear.api.controller.Logging;
import com.mbientlab.metawear.api.controller.Timer;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.AccumulatorBuilder;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.ComparatorBuilder;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.LowPassBuilder;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.MathBuilder;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.PassthroughBuilder;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.RMSBuilder;
import com.mbientlab.metawear.api.util.FilterConfigBuilder.TimeDelayBuilder;
import com.mbientlab.metawear.api.util.TriggerBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/**
 * Sets up the filter chain for the crunch session
 * @author Eric Tsai
 */
public class FilterSetup {
    private static final byte RMS_DATA_SIZE= 4;
    private static final short SESSION_WARNING_NUM_PULSES= 2, SESSION_START_DELAY= 2000;
    private static final int CRUNCH_THRESHOLD_CHECK_PERIOD= 3000, CRUNCH_OFFSET_UPDATE_COUNT= 16;

    /**
     * Interface definition for a callback when the filter chain is setup
     */
    public interface SetupListener {
        /**
         * Called when the filter chain is ready to be used
         * @param state State of the filter exposed to the user
         */
        public void ready(FilterState state);
    }

    /**
     * Configures a MetaWear board with the AbDisc filter chain
     * @param mwCtrllr Controller of the MetaWear board to manipulate
     * @param listener Callback to be executed after setup is complete
     * @return Config object to modify the parameters of the filter
     */
    public static FilterParameters configure(final MetaWearController mwCtrllr, final SetupListener listener) {
        return new FilterParameters() {
            private byte sensorGPIOPin= DefaultParameters.SENSOR_DATA_PIN, sensorPulldownPin= DefaultParameters.SENSOR_GROUND_PIN, sedentaryTime= DefaultParameters.SEDENTARY_TIME,
                    adcSampleSize= DefaultParameters.ADC_SAMPLE_SIZE;
            private int sensorSamplingPeriod= 500, sedentaryStepPeriod= 60000, sedentaryDeltaAvgReset= DefaultParameters.SEDENTARY_RESET_THRESHOLD,
                    sedentaryThreshold= DefaultParameters.SEDENTARY_MIN_ACTIVITY_THRESHOLD, sensorThreshold= DefaultParameters.CRUNCH_SESSION_THRESHOLD_UPDATE,
                    crunchThresholdUpdatePeriod= DefaultParameters.CRUNCH_THRESHOLD_UPDATE_PERIOD;
            private float crunchSessionDuration= DefaultParameters.CRUNCH_SESSION_DURATION, sessionWarningStrength = 100.f,
                    tapThreshold= DefaultParameters.TAP_THRESHOLD;

            private int l1HapticLower= DefaultParameters.L1_HAPTIC_LOWER, l1HapticUpper= DefaultParameters.L1_HAPTIC_UPPER,
                    l2HapticLower= DefaultParameters.L2_HAPTIC_LOWER, l2HapticUpper= DefaultParameters.L2_HAPTIC_UPPER,
                    l3HapticLower= DefaultParameters.L3_HAPTIC_LOWER, l3HapticUpper= DefaultParameters.L3_HAPTIC_UPPER;

            private short l1HapticPeriod= 500, l2HapticPeriod= 700, l3HapticPeriod= 900, sessionWarningDuration= 450;
            private float l1HapticStrength= DefaultParameters.L1_HAPTIC_STRENGTH, l2HapticStrength= DefaultParameters.L2_HAPTIC_STRENGTH,
                    l3HapticStrength= DefaultParameters.L3_HAPTIC_STRENGTH;

            private byte sensorDataId, sedentaryId;

            /** These IDs need to be exposed to the user */
            private byte sensorTimerId= -1, activityDiffLoggingId= -1, sensorDataLoggingId= -1, sensorThresholdLoggingId= -1,
                    differentialId= -1, sensorPassthroughId= -1, feedbackId= -1;

            @Override
            public FilterParameters withAdcSampleSize(byte sampleSize) {
                adcSampleSize= sampleSize;
                return this;
            }

            public FilterParameters withSessionWarningDuration(short duration) {
                sessionWarningDuration= duration;
                return this;
            }

            public FilterParameters withSessionWarningStrength(float strength) {
                sessionWarningStrength = strength;
                return this;
            }

            @Override
            public FilterParameters withSensorGroundPin(byte pin) {
                sensorPulldownPin= pin;
                return this;
            }

            @Override
            public FilterParameters withSensorDataPin(byte pin) {
                sensorGPIOPin= pin;
                return this;
            }

            @Override
            public FilterParameters withSensorSamplingPeriod(int period) {
                sensorSamplingPeriod= period;
                return this;
            }

            @Override
            public FilterParameters withCrunchThresholdUpdateMinChangeThreshold(int threshold) {
                sensorThreshold= threshold;
                return this;
            }

            @Override
            public FilterParameters withCrunchThresholdUpdateCheckPeriod(int period) {
                crunchThresholdUpdatePeriod= period;
                return this;
            }

            @Override
            public FilterParameters withSedentaryStepPeriod(int period) {
                sedentaryStepPeriod= period;
                return this;
            }

            @Override
            public FilterParameters withSedentaryTime(byte time) {
                sedentaryTime= time;
                return this;
            }

            @Override
            public FilterParameters withSedentaryResetThreshold(int threshold) {
                sedentaryDeltaAvgReset= threshold;
                return this;
            }

            @Override
            public FilterParameters withSedentaryMinActivityThreshold(int threshold) {
                sedentaryThreshold= threshold;
                return this;
            }

            @Override
            public FilterParameters withCrunchSessionDuration(float duration) {
                crunchSessionDuration= duration;
                return this;
            }

            @Override
            public FilterParameters withHapticSettings(HapticLevel level, int crunchLower, int crunchUpper, float motorStrength, short duration) {
                switch (level) {
                    case L1:
                        l1HapticLower= crunchLower;
                        l1HapticUpper= crunchUpper;
                        l1HapticStrength= motorStrength;
                        l1HapticPeriod= duration;
                        break;
                    case L2:
                        l2HapticLower= crunchLower;
                        l2HapticUpper= crunchUpper;
                        l2HapticStrength= motorStrength;
                        l2HapticPeriod= duration;
                        break;
                    case L3:
                        l3HapticLower= crunchLower;
                        l3HapticUpper= crunchUpper;
                        l3HapticStrength= motorStrength;
                        l3HapticPeriod= duration;
                        break;
                }
                return this;
            }

            @Override
            public FilterParameters withHapticSettings(HapticLevel level, int crunchLower, int crunchUpper) {
                switch (level) {
                    case L1:
                        l1HapticLower= crunchLower;
                        l1HapticUpper= crunchUpper;
                        break;
                    case L2:
                        l2HapticLower= crunchLower;
                        l2HapticUpper= crunchUpper;
                        break;
                    case L3:
                        l3HapticLower= crunchLower;
                        l3HapticUpper= crunchUpper;
                        break;
                }
                return this;
            }

            @Override
            public FilterParameters withHapticCrunchLower(HapticLevel level, int crunchLower) {
                switch (level) {
                    case L1:
                        l1HapticLower= crunchLower;
                        break;
                    case L2:
                        l2HapticLower= crunchLower;
                        break;
                    case L3:
                        l3HapticLower= crunchLower;
                        break;
                }
                return this;
            }

            @Override
            public FilterParameters withHapticCrunchUpper(HapticLevel level, int crunchUpper) {
                switch (level) {
                    case L1:
                        l1HapticUpper= crunchUpper;
                        break;
                    case L2:
                        l2HapticUpper= crunchUpper;
                        break;
                    case L3:
                        l3HapticUpper= crunchUpper;
                        break;
                }
                return this;
            }

            @Override
            public FilterParameters withHapticStrength(HapticLevel level, float motorStrength) {
                switch (level) {
                    case L1:
                        l1HapticStrength= motorStrength;
                        break;
                    case L2:
                        l2HapticStrength= motorStrength;
                        break;
                    case L3:
                        l3HapticStrength= motorStrength;
                        break;
                }
                return this;
            }

            @Override
            public FilterParameters withTapThreshold(float threshold) {
                tapThreshold= threshold;
                return this;
            }

            @Override
            public void commit() {
                final Timer timerController= (Timer) mwCtrllr.getModuleController(Module.TIMER);
                final DataProcessor dpController= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);

                mwCtrllr.addModuleCallback(new Timer.Callbacks() {
                    @Override
                    public void receivedTimerId(byte timerId) {
                        final GPIO gpioController = (GPIO) mwCtrllr.getModuleController(Module.GPIO);
                        final Event eventController = (Event) mwCtrllr.getModuleController(Module.EVENT);

                        sensorTimerId = timerId;
                        gpioController.clearDigitalOutput(sensorPulldownPin);
                        eventController.recordMacro(Timer.Register.TIMER_NOTIFY, timerId);
                        gpioController.readAnalogInput(sensorGPIOPin, GPIO.AnalogMode.SUPPLY_RATIO);
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        LowPassBuilder builder= new LowPassBuilder();
                        builder.withSampleSize(adcSampleSize)
                                .withInputSize(GPIO.ANALOG_DATA_SIZE)
                                .withOutputSize(GPIO.ANALOG_DATA_SIZE);
                        dpController.addReadFilter(TriggerBuilder.buildGPIOAnalogTrigger(true, sensorGPIOPin), builder.build());
                    }
                }).addModuleCallback(new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        sensorDataId= filterId;
                        mwCtrllr.removeModuleCallback(this);

                        setupSedentaryFilter(mwCtrllr);
                    }
                });

                timerController.addTimer(sensorSamplingPeriod, (short) 0, false);
            }

            private byte lowPassId= -1, differentialAvgResetId= -1;

            private void setupSedentaryFilter(final MetaWearController mwCtrllr) {
                final Event eventController= (Event) mwCtrllr.getModuleController(Module.EVENT);
                final DataProcessor dpController= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);

                final DataProcessor.Callbacks comparatorCallback2= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        sedentaryId= filterId;

                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, sedentaryId);
                        dpController.resetFilterState(lowPassId);
                        eventController.stopRecord();

                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, differentialAvgResetId);
                        dpController.resetFilterState(lowPassId);
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);

                        setupCrunchFilter(mwCtrllr);
                    }
                }, lowPassCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        lowPassId= filterId;
                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.GT).withReference(sedentaryThreshold);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(comparatorCallback2);
                        dpController.chainFilters(filterId, RMS_DATA_SIZE, builder.build());
                    }
                }, comparatorCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        differentialAvgResetId= filterId;
                        LowPassBuilder builder= new LowPassBuilder();
                        builder.withSampleSize(sedentaryTime)
                                .withInputSize(RMS_DATA_SIZE)
                                .withOutputSize(RMS_DATA_SIZE);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(lowPassCallback);
                        dpController.chainFilters(differentialId, RMS_DATA_SIZE, builder.build());

                    }
                }, timeCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        differentialId= filterId;
                        Logging logController= (Logging) mwCtrllr.getModuleController(Module.LOGGING);
                        logController.addTrigger(TriggerBuilder.buildDataFilterTrigger(differentialId, RMS_DATA_SIZE));

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.GT)
                                .withReference(sedentaryDeltaAvgReset);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(comparatorCallback);
                        dpController.chainFilters(filterId, RMS_DATA_SIZE, builder.build());
                    }
                }, accumCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        TimeDelayBuilder builder= new TimeDelayBuilder();
                        builder.withDataSize(RMS_DATA_SIZE).withPeriod(sedentaryStepPeriod)
                                .withFilterMode(TimeDelayBuilder.FilterMode.DIFFERENTIAL);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(timeCallback);
                        dpController.chainFilters(filterId, RMS_DATA_SIZE, builder.build());
                    }
                }, rmsCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        AccumulatorBuilder builder= new AccumulatorBuilder();
                        builder.withInputSize(RMS_DATA_SIZE).withOutputSize(RMS_DATA_SIZE);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(accumCallback);
                        dpController.chainFilters(filterId, RMS_DATA_SIZE, builder.build());
                    }
                };

                RMSBuilder builder= new RMSBuilder();
                builder.withInputCount((byte) 3).withSignedInput().withInputSize(GPIO.ANALOG_DATA_SIZE)
                        .withOutputSize(RMS_DATA_SIZE);

                mwCtrllr.addModuleCallback(rmsCallback).addModuleCallback(new Logging.Callbacks() {
                    @Override
                    public void receivedTriggerId(byte triggerId) {
                        activityDiffLoggingId= triggerId;
                        mwCtrllr.removeModuleCallback(this);
                    }
                });
                dpController.addFilter(TriggerBuilder.buildAccelerometerTrigger(), builder.build());
            }

            private DataProcessor.FilterConfig buildEventCounter() {
                AccumulatorBuilder builder= new AccumulatorBuilder();
                final DataProcessor.FilterConfig original= builder.withInputSize(GPIO.ANALOG_DATA_SIZE).withOutputSize(GPIO.ANALOG_DATA_SIZE).build();
                final DataProcessor.FilterConfig eventConfig= new DataProcessor.FilterConfig() {
                    @Override
                    public byte[] bytes() {
                        byte[] configBytes= original.bytes();
                        configBytes[0] |= 0x10;
                        return new byte[0];
                    }

                    @Override
                    public DataProcessor.FilterType type() {
                        return original.type();
                    }
                };

                return eventConfig;
            }

            private byte sensorFilterPass, firstPassthrough, sessionStartId, crunchOffsetId, crunchOffsetCheckId,
                    offsetUpdateGtId, offsetUpdateGtCounterId;
            private void setupCrunchFilter(final MetaWearController mwCtrllr) {
                final Event eventController= (Event) mwCtrllr.getModuleController(Module.EVENT);
                final DataProcessor dpController= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);
                final Logging logController= (Logging) mwCtrllr.getModuleController(Module.LOGGING);

                final DataProcessor.Callbacks offsetUpdateLteEndCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, filterId);
                        dpController.setFilterState(feedbackId, new byte[] { 0x1, 0x0 });
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        setupSessionHaptic(mwCtrllr);
                    }
                }, offsetLteCounterCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, offsetUpdateGtId);
                        dpController.setFilterState(filterId, new byte[] { 0, 0, 0, 0 });
                        eventController.stopRecord();

                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, feedbackId);
                        dpController.setFilterState(filterId, new byte[] { 0, 0, 0, 0 });
                        eventController.stopRecord();

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withReference(CRUNCH_OFFSET_UPDATE_COUNT).withOperation(ComparatorBuilder.Operation.EQ);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(offsetUpdateLteEndCallback);
                        dpController.chainFilters(filterId, GPIO.ANALOG_DATA_SIZE, buildEventCounter());
                    }
                }, offsetUpdateLteCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, filterId);
                        dpController.setFilterState(offsetUpdateGtCounterId, new byte[] { 0, 0, 0, 0 });
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(offsetLteCounterCallback);
                        dpController.chainFilters(filterId, GPIO.ANALOG_DATA_SIZE, buildEventCounter());
                    }
                }, offsetUpdateGtEndCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, filterId);
                        dpController.setFilterState(feedbackId, new byte[] { 0x1, 0x0 });
                        eventController.stopRecord();

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withReference(0).withOperation(ComparatorBuilder.Operation.LTE);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(offsetUpdateLteCallback);
                        dpController.chainFilters(crunchOffsetCheckId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, offsetGteCounterCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        offsetUpdateGtCounterId = filterId;

                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, feedbackId);
                        dpController.setFilterState(offsetUpdateGtCounterId, new byte[] { 0, 0, 0, 0 });
                        eventController.stopRecord();

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withReference(CRUNCH_OFFSET_UPDATE_COUNT).withOperation(ComparatorBuilder.Operation.EQ);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(offsetUpdateGtEndCallback);
                        dpController.chainFilters(offsetUpdateGtCounterId, GPIO.ANALOG_DATA_SIZE, buildEventCounter());
                    }
                }, offsetUpdateGteCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        offsetUpdateGtId = filterId;

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(offsetGteCounterCallback);
                        dpController.chainFilters(offsetUpdateGtId, GPIO.ANALOG_DATA_SIZE, buildEventCounter());
                    }
                }, time3sCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        crunchOffsetId= filterId;

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withReference(0).withOperation(ComparatorBuilder.Operation.GT);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(offsetUpdateGteCallback);
                        dpController.chainFilters(crunchOffsetCheckId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, crunchOffsetCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        crunchOffsetCheckId = filterId;

                        MathBuilder cfgUpdateBuilder= new MathBuilder();
                        cfgUpdateBuilder.withOperation(MathBuilder.Operation.SUBTRACT)
                                .withOperand(0)
                                .withOutputSize(GPIO.ANALOG_DATA_SIZE)
                                .withInputSize(GPIO.ANALOG_DATA_SIZE);

                        eventController.recordCommand(DataProcessor.Register.FILTER_NOTIFICATION, feedbackId, new byte[] {0x2, 0x0, 0x4});
                        dpController.setFilterConfiguration(crunchOffsetCheckId, cfgUpdateBuilder.build());
                        eventController.stopRecord();

                        TimeDelayBuilder builder= new TimeDelayBuilder();
                        builder.withDataSize(GPIO.ANALOG_DATA_SIZE).withPeriod(CRUNCH_THRESHOLD_CHECK_PERIOD);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(time3sCallback);
                        dpController.chainFilters(crunchOffsetCheckId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, crunchOffsetUpdateCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        feedbackId= filterId;
                        logController.addTrigger(TriggerBuilder.buildDataFilterTrigger(feedbackId, GPIO.ANALOG_DATA_SIZE));

                        MathBuilder builder= new MathBuilder();
                        builder.withOperation(MathBuilder.Operation.SUBTRACT)
                                .withOperand(0)
                                .withOutputSize(GPIO.ANALOG_DATA_SIZE)
                                .withInputSize(GPIO.ANALOG_DATA_SIZE);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchOffsetCallback);
                        dpController.chainFilters(sensorFilterPass, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, passthroughCallback2= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        sessionStartId= filterId;

                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, firstPassthrough);
                        dpController.setFilterState(sensorFilterPass, new byte[]{0x0, 0x0});
                        dpController.setFilterState(sessionStartId, new byte[] { 0x0, 0x0 });
                        eventController.stopRecord();

                        eventController.recordMacro(Accelerometer.Register.PULSE_STATUS);
                        dpController.setFilterState(sessionStartId, new byte[]{0x1, 0x0});
                        dpController.setFilterState(firstPassthrough, new byte[] { 0x1, 0x0 });
                        eventController.stopRecord();

                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, sessionStartId);
                        dpController.setFilterState(firstPassthrough, new byte[]{0x0, 0x0});
                        eventController.stopRecord();

                        PassthroughBuilder builder= new PassthroughBuilder();
                        builder.withMode(PassthroughBuilder.Mode.COUNT);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchOffsetUpdateCallback);
                        dpController.chainFilters(sensorFilterPass, GPIO.ANALOG_DATA_SIZE, builder.build());

                    }
                }, passthroughCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(byte filterId) {
                        firstPassthrough= filterId;
                        PassthroughBuilder builder= new PassthroughBuilder();
                        builder.withMode(PassthroughBuilder.Mode.COUNT);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(passthroughCallback2);
                        dpController.chainFilters(sensorDataId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, sensorPassCallback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        sensorPassthroughId = filterId;
                        logController.addTrigger(TriggerBuilder.buildDataFilterTrigger(sensorPassthroughId, GPIO.ANALOG_DATA_SIZE));

                        sensorFilterPass= filterId;
                        PassthroughBuilder builder= new PassthroughBuilder();
                        builder.withMode(PassthroughBuilder.Mode.COUNT);

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(passthroughCallback);
                        dpController.chainFilters(sensorFilterPass, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                };

                mwCtrllr.addModuleCallback(sensorPassCallback).addModuleCallback(new Logging.Callbacks() {
                    @Override
                    public void receivedTriggerId(byte triggerId) {
                        if (sensorDataLoggingId == -1) {
                            sensorDataLoggingId= triggerId;
                        } else {
                            sensorThresholdLoggingId= triggerId;
                            mwCtrllr.removeModuleCallback(this);
                        }
                    }
                });
                PassthroughBuilder builder= new PassthroughBuilder();
                builder.withMode(PassthroughBuilder.Mode.COUNT);

                dpController.chainFilters(sensorDataId, GPIO.ANALOG_DATA_SIZE, builder.build());
            }

            private void setupSessionHaptic(final MetaWearController mwCtrllr) {
                final Haptic hapticController= (Haptic) mwCtrllr.getModuleController(Module.HAPTIC);
                final Event eventController= (Event) mwCtrllr.getModuleController(Module.EVENT);
                final Timer timerController= (Timer) mwCtrllr.getModuleController(Module.TIMER);

                final Timer.Callbacks warningCallback= new Timer.Callbacks() {
                    @Override
                    public void receivedTimerId(byte timerId) {
                        final byte hapticTimerId= timerId;

                        timerController.enableNotification(hapticTimerId);
                        eventController.recordMacro(Timer.Register.TIMER_NOTIFY, timerId);
                        hapticController.startMotor(sessionWarningStrength, sessionWarningDuration);
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(new Timer.Callbacks() {
                            @Override
                            public void receivedTimerId(byte timerId) {
                                byte[] sampleCount= ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                                        .putInt((int) (crunchSessionDuration / (sensorSamplingPeriod / 1000.f))).array();
                                final DataProcessor dpController= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);

                                eventController.recordMacro(Timer.Register.TIMER_NOTIFY, timerId);
                                dpController.setFilterState(sensorFilterPass, sampleCount);
                                eventController.stopRecord();

                                eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, sessionStartId);
                                timerController.startTimer(hapticTimerId);
                                timerController.startTimer(timerId);
                                eventController.stopRecord();

                                eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, sedentaryId);
                                timerController.startTimer(hapticTimerId);
                                timerController.startTimer(timerId);
                                eventController.stopRecord();

                                mwCtrllr.removeModuleCallback(this);
                                setupHapticFeedback(mwCtrllr);

                            }
                        });
                        timerController.addTimer(SESSION_START_DELAY, (short) 1, true);
                    }
                };

                mwCtrllr.addModuleCallback(warningCallback);
                timerController.addTimer(SESSION_START_DELAY / SESSION_WARNING_NUM_PULSES, SESSION_WARNING_NUM_PULSES, false);
            }

            private byte timer90Id, timer70Id, timer40Id;
            private void setupHapticFeedback(final MetaWearController mwCtrllr) {
                final short NUM_HAPTIC_PULSES= 3;
                final int HAPTIC_TIMER_PERIOD= CRUNCH_THRESHOLD_CHECK_PERIOD / NUM_HAPTIC_PULSES;

                final Haptic hapticController= (Haptic) mwCtrllr.getModuleController(Module.HAPTIC);
                final Event eventController= (Event) mwCtrllr.getModuleController(Module.EVENT);
                final Timer timerController= (Timer) mwCtrllr.getModuleController(Module.TIMER);
                final DataProcessor dpController= (DataProcessor) mwCtrllr.getModuleController(Module.DATA_PROCESSOR);

                final DataProcessor.Callbacks crunchRange6Callback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, filterId);
                        timerController.startTimer(timer90Id);
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        listener.ready(new FilterState() {
                            @Override public byte getSessionStartId() { return sessionStartId; }
                            @Override public byte getSensorTimerId() { return sensorTimerId; }
                            @Override public byte getSedentaryLogId() { return activityDiffLoggingId; }
                            @Override public byte getSensorLogId() { return sensorDataLoggingId; }
                            @Override public byte getSensorOffsetLoggingId() { return sensorThresholdLoggingId; }
                            @Override public byte getCrunchOffsetId() { return crunchOffsetId; }

                            @Override public byte getSedentaryId() { return differentialId; }
                            @Override public byte getSensorId() { return sensorPassthroughId; }
                            @Override public byte getOffsetUpdateId() { return feedbackId; }
                            @Override public float getTapThreshold() { return tapThreshold; }

                            @Override
                            public String toString() {
                                return String.format(Locale.US, "{%s: %d, %s: %d, %s: %d, %s: %d, %s: %d, %s: %d, %s: %d, %s: %d, %s: %.3f}",
                                        "sessionStartId", sessionStartId,
                                        "sensorTimerId", sensorTimerId,
                                        "activityLoggingId", activityDiffLoggingId,
                                        "sensorLoggingId", sensorDataLoggingId,
                                        "thresholdLoggingId", sensorThresholdLoggingId,
                                        "activityDifferentialId", differentialId,
                                        "sensorPassthroughId", sensorPassthroughId,
                                        "offsetUpdateId", feedbackId,
                                        "tapThreshold", tapThreshold);
                            }
                        });
                    }
                }, crunchRange5Callback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.LT).withReference(l3HapticUpper)
                                .withSignedCommparison();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchRange6Callback);
                        dpController.chainFilters(filterId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, crunchRange4Callback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, filterId);
                        timerController.startTimer(timer70Id);
                        eventController.stopRecord();

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.GTE).withReference(l3HapticLower)
                                .withSignedCommparison();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchRange5Callback);
                        dpController.chainFilters(crunchOffsetId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, crunchRange3Callback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.LT).withReference(l2HapticUpper)
                                .withSignedCommparison();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchRange4Callback);
                        dpController.chainFilters(filterId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, crunchRange2Callback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        eventController.recordMacro(DataProcessor.Register.FILTER_NOTIFICATION, filterId);
                        timerController.startTimer(timer40Id);
                        eventController.stopRecord();

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.GTE).withReference(l2HapticLower)
                                .withSignedCommparison();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchRange3Callback);
                        dpController.chainFilters(crunchOffsetId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, crunchRange1Callback= new DataProcessor.Callbacks() {
                    @Override
                    public void receivedFilterId(final byte filterId) {
                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.LT).withReference(l1HapticUpper)
                                .withSignedCommparison();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchRange2Callback);
                        dpController.chainFilters(filterId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                };

                final Timer.Callbacks timerCallback90= new Timer.Callbacks() {
                    @Override
                    public void receivedTimerId(byte timerId) {
                        timer90Id= timerId;
                        eventController.recordMacro(Timer.Register.TIMER_NOTIFY, timer90Id);
                        hapticController.startMotor(l3HapticStrength, l3HapticPeriod);
                        eventController.stopRecord();

                        ComparatorBuilder builder= new ComparatorBuilder();
                        builder.withOperation(ComparatorBuilder.Operation.GTE).withReference(l1HapticLower)
                                .withSignedCommparison();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(crunchRange1Callback);
                        dpController.chainFilters(crunchOffsetId, GPIO.ANALOG_DATA_SIZE, builder.build());
                    }
                }, timerCallback70= new Timer.Callbacks() {
                    @Override
                    public void receivedTimerId(byte timerId) {
                        timer70Id= timerId;
                        eventController.recordMacro(Timer.Register.TIMER_NOTIFY, timer70Id);
                        hapticController.startMotor(l2HapticStrength, l2HapticPeriod);
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(timerCallback90);
                        timerController.addTimer(HAPTIC_TIMER_PERIOD, NUM_HAPTIC_PULSES, false);
                    }
                }, timerCallback40= new Timer.Callbacks() {
                    @Override
                    public void receivedTimerId(byte timerId) {
                        timer40Id= timerId;
                        eventController.recordMacro(Timer.Register.TIMER_NOTIFY, timer40Id);
                        hapticController.startMotor(l1HapticStrength, l1HapticPeriod);
                        eventController.stopRecord();

                        mwCtrllr.removeModuleCallback(this);
                        mwCtrllr.addModuleCallback(timerCallback70);
                        timerController.addTimer(HAPTIC_TIMER_PERIOD, NUM_HAPTIC_PULSES, false);
                    }
                };

                mwCtrllr.addModuleCallback(timerCallback40);
                timerController.addTimer(HAPTIC_TIMER_PERIOD, NUM_HAPTIC_PULSES, false);
            }
        };
    }
}
