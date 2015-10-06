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

package com.mbientlab.abdisc;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.mbientlab.abdisc.model.StepReading;
import com.mbientlab.abdisc.model.StepReading$Table;
import com.mbientlab.abdisc.utils.GoalDataUtils;
import com.mbientlab.abdisc.utils.LayoutUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Copyright 2014 MbientLab Inc. All rights reserved.
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
 *
 *
 * Created by Lance Gleason of Polyglot Programming LLC. on 6/1/2015.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 *
 */
public class StepCountFragment extends Fragment {
    private LocalDate dayToView = LocalDate.now();
    private SharedPreferences sharedPreferences;
    private AppState appState;
    private View createdView;
    private int stepsForDay;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof  AppState)) {
            throw new ClassCastException(String.format(Locale.US, "%s %s", activity.toString(),
                    activity.getString(R.string.error_app_state)));
        }

        appState = (AppState) activity;
        sharedPreferences = appState.getSharedPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_count, container, false);
    }

    private TextView stepCountValue;

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        createdView = view;
    }

    @Override
    public void onStart(){
        super.onStart();
        appState.setCurrentFragment(this);
        final TextView currentDay = (TextView) createdView.findViewById(R.id.activityDay);
        LayoutUtils.setDayInDisplay(dayToView, currentDay);
        createdView.findViewById(R.id.graph_previous_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToView = dayToView.minusDays(1);
                LayoutUtils.setDayInDisplay(dayToView, currentDay);
                drawGraphAndSetText(createdView);
            }
        });
        createdView.findViewById(R.id.graph_next_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToView = dayToView.plusDays(1);
                LayoutUtils.setDayInDisplay(dayToView, currentDay);
                drawGraphAndSetText(createdView);
            }
        });
        drawGraphAndSetText(createdView);
    }

    private void drawGraphAndSetText(View view){
        stepsForDay = getStepsForDay(dayToView);
        int stepGoal = GoalDataUtils.getStepGoal(sharedPreferences);
        int stepsToGoal = stepGoal - stepsForDay;
        if(stepsToGoal < 0)
            stepsToGoal = 0;

        TextView stepsToGoalValue = (TextView) view.findViewById(R.id.textStepsToGoalValue);
        stepsToGoalValue.setText(String.valueOf(stepsToGoal));

        TextView stepsToday = (TextView) view.findViewById(R.id.textStepsTodayValue);
        stepsToday.setText(NumberFormat.getInstance().format(stepsForDay));

        int goalPercent = (int) (((float) stepsForDay/ (float)stepGoal) * 100.00f);
        TextView percentOfGoal = (TextView) view.findViewById(R.id.textStepsPercentGoalValue);
        percentOfGoal.setText(String.valueOf(goalPercent) + "%");

        double distanceValue = (double) (stepsForDay * GoalDataUtils.getStride(sharedPreferences))/63360;
        DecimalFormat distanceFormatter = new DecimalFormat("#.##");
        TextView distance = (TextView) view.findViewById(R.id.textStepsDistanceValue);
        distance.setText(distanceFormatter.format(distanceValue) + "mi");

        drawGraph(view);
    }

    private void drawGraph(View view){
        DecoView decoView = (DecoView) view.findViewById(R.id.stepsArc);

        decoView.deleteAll();
        decoView.configureAngles(330, 0);
        int stepGoal = GoalDataUtils.getStepGoal(sharedPreferences);
        decoView.addSeries(new SeriesItem.Builder(getResources().getColor(R.color.ColorButtonBarSeparator),
                getResources().getColor(R.color.ColorButtonBarSeparator))
                .setRange(0, stepGoal, stepGoal)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(20f))
                .build());

        int seriesIndex = decoView.addSeries(new SeriesItem.Builder(getResources().getColor(R.color.ColorGraphLow),
                getResources().getColor(R.color.ColorGraphHigh))
                .setRange(0, stepGoal, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(18f))
                .build());

        int seriesIndex2 = decoView.addSeries(new SeriesItem.Builder(getResources().getColor(R.color.ColorGraphLow),
                getResources().getColor(R.color.ColorGraphHigh))
                .setRange(0, stepGoal, 0)
                .setInitialVisibility(false)
                .setLineWidth(getDimension(12f))
                .build());

        decoView.executeReset();


        int graphSteps = stepsForDay;
        if (graphSteps > stepGoal)
            graphSteps = stepGoal;

        decoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(100)
                .setDuration(100)
                .build());

        decoView.addEvent(new DecoEvent.Builder(graphSteps)
                .setIndex(seriesIndex)
                .setDelay(200)
                .setDuration(1000)
                .build());

        decoView.addEvent(new DecoEvent.Builder(graphSteps)
                .setIndex(seriesIndex2)
                .setDelay(800)
                .setDuration(1000)
                .build());

        decoView.invalidate();

    }

    private int getStepsForDay(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        Switch testDataSwitch = (Switch) getView().getRootView().findViewById(R.id.testData);
        boolean getTestData = testDataSwitch.isChecked();
        int steps = 0;

        for (int i = 0; i < 24; i++) {
            List<StepReading> hourSteps = new Select().from(StepReading.class)
                    .where(Condition.column(StepReading$Table.DATETIME)
                                    .between(startOfDay.plusHours(i).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli())
                                    .and(startOfDay.plusHours(i + 1).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()),
                            Condition.column(StepReading$Table.ISTESTDATA).eq(getTestData))
                    .queryList();
            for (StepReading stepReading : hourSteps) {
                long stepsThisMinute = stepReading.getMilliG() / DayActivityFragment.ACTIVITY_PER_STEP;

                if(stepsThisMinute > 5){
                    steps += stepsThisMinute;
                }
            }
        }

        return steps;
    }

    public void stepCountUpdated(int newStepCount) {
        if (isVisible()) {
            stepCountValue.setText(String.format(Locale.US, "%d", newStepCount));
        }
    }

    protected float getDimension(float base) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, base, getResources().getDisplayMetrics());
    }
}
