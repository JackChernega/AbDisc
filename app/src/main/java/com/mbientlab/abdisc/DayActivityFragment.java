package com.mbientlab.abdisc;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mbientlab.abdisc.model.CrunchPosture;
import com.mbientlab.abdisc.model.CrunchPosture$Table;
import com.mbientlab.abdisc.model.StepReading;
import com.mbientlab.abdisc.model.StepReading$Table;
import com.mbientlab.abdisc.utils.AbDiscMarkerView;
import com.mbientlab.abdisc.utils.AbDiscScatterChart;
import com.mbientlab.abdisc.utils.ChartBlankValueFormatter;
import com.mbientlab.abdisc.utils.LayoutUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DayActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayActivityFragment extends Fragment {
    public static final int ACTIVITY_PER_STEP = 6700;
    private LineChart mChart;
    private AbDiscScatterChart mPostureChart;
    private AbDiscScatterChart mCrunchChart;
    private LocalDate dayToView = LocalDate.now();


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DayActivityFragment.
     */
    public static DayActivityFragment newInstance() {
        DayActivityFragment fragment = new DayActivityFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DayActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView currentDay = (TextView) view.findViewById(R.id.activityDay);
        LayoutUtils.setDayInDisplay(dayToView, currentDay);
        view.findViewById(R.id.graph_previous_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToView = dayToView.minusDays(1);
                LayoutUtils.setDayInDisplay(dayToView, currentDay);
                drawStepsGraph();
                drawCrunchPostureGraph();
            }
        });
        view.findViewById(R.id.graph_next_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayToView = dayToView.plusDays(1);
                LayoutUtils.setDayInDisplay(dayToView, currentDay);
                drawStepsGraph();
                drawCrunchPostureGraph();
            }
        });
        drawStepsGraph();
        drawCrunchPostureGraph();
    }

    private void drawCrunchPostureGraph(){
        drawCrunchPostureGraph(mCrunchChart, R.id.crunch_chart, CrunchPosture.MODE_CRUNCH);
        drawCrunchPostureGraph(mPostureChart, R.id.posture_chart, CrunchPosture.MODE_POSTURE);
    }

    private void drawCrunchPostureGraph(AbDiscScatterChart mPostureCrunchChart, int chartId,
                                        String chartType){

        mPostureCrunchChart = (AbDiscScatterChart) getView().findViewById(chartId);
        mPostureCrunchChart.setDescription("");

        mPostureCrunchChart.setDrawGridBackground(false);

        mPostureCrunchChart.setTouchEnabled(true);
        mPostureCrunchChart.setHighlightEnabled(false);

        // enable scaling and dragging
        mPostureCrunchChart.setDragEnabled(false);
        mPostureCrunchChart.setScaleEnabled(false);

        mPostureCrunchChart.setPinchZoom(false);

        mPostureCrunchChart.getLegend().setEnabled(false);

        YAxis yl = mPostureCrunchChart.getAxisLeft();
        yl.setDrawGridLines(false);
        yl.setAxisMaxValue(12);

        mPostureCrunchChart.getAxisRight().setEnabled(false);
        mPostureCrunchChart.getAxisLeft().setEnabled(false);
        mPostureCrunchChart.getXAxis().setEnabled(false);
        int [] gradientColors = {getResources().getColor(R.color.ColorGraphLow),
                getResources().getColor(R.color.ColorGraphHigh)};

        GradientDrawable backgroundGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        mPostureCrunchChart.setBackground(backgroundGradient);

        XAxis xl = mPostureCrunchChart.getXAxis();
        //xl.setTypeface(tf);
        xl.setDrawGridLines(false);
        xl.setDrawAxisLine(false);
        xl.setDrawLimitLinesBehindData(false);


        // some test chart data
        //mPostureCrunchChart.setVisibleYRangeMaximum(20, null);
        int hoursInDay = 24;

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < hoursInDay + 1; i++) {
            //xVals.add((i) + "i");
            xVals.add("");
        }

        // create a dataset and give it a type
        ScatterDataSet set1 = new ScatterDataSet(getCrunchPostureByHourForDay(dayToView, chartType), "DS 1");
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        set1.setDrawHighlightIndicators(false);

        set1.setScatterShapeSize(0f);
        ChartBlankValueFormatter formater = new ChartBlankValueFormatter();
        set1.setValueFormatter(formater);

        ArrayList<ScatterDataSet> dataSets = new ArrayList<ScatterDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        ScatterData data = new ScatterData(xVals, dataSets);

        mPostureCrunchChart.setData(data);
        AbDiscMarkerView mv = new AbDiscMarkerView (getActivity().getApplication().getApplicationContext(),
                R.layout.crunch_marker_view, chartType);


        mPostureCrunchChart.setMarkerView(mv);


        mPostureCrunchChart.drawAllMarkers();
        mPostureCrunchChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //consume all touches
                return true;
            }
        });
        mPostureCrunchChart.setViewPortOffsets(0, 0, 0, -60);
        mPostureCrunchChart.invalidate();
    }

    private List<Entry> getCrunchPostureByHourForDay(LocalDate date, String chartType){
        LocalDateTime startOfDay = date.atStartOfDay();
        List<Entry> crunchPostureByHour = new ArrayList<Entry>();

        float sessionValue = 10;

        if(chartType == CrunchPosture.MODE_POSTURE){
            sessionValue = 10;
        }
        // need to tighten this up
        for (int i = 0; i < 24; i++) {
            List<CrunchPosture> hourCrunchPostures = new Select().from(CrunchPosture.class)
                    .where(Condition.column(CrunchPosture$Table.STARTSTOPDATETIME)
                            .between(startOfDay.plusHours(i).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli())
                            .and(startOfDay.plusHours(i + 1).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()))
                    .queryList();
            int crunchSessions = 0;
            for (CrunchPosture crunchPosture: hourCrunchPostures) {
                if(crunchPosture.getMode().equals(CrunchPosture.MODE_CRUNCH) && crunchPosture.getStatus().equals(CrunchPosture.STATUS_START))
                    crunchSessions++;
            }
            if(crunchSessions > 0)
                crunchPostureByHour.add(new Entry(sessionValue, i));
        }

        return crunchPostureByHour;
    }

    private void drawStepsGraph() {
        List<Integer> stepsByHour = getStepsByHourForDay(dayToView);

        mChart = (LineChart) getView().findViewById(R.id.active_minutes_day_chart);
        int maxValue = (int) (getMaxValue() * 1.05);
        int minValue = maxValue > 100 ? (int) (maxValue * -0.07) : -20;
        mChart.invalidate();

        mChart.setDrawGridBackground(true);
        Legend legend = mChart.getLegend();
        legend.setEnabled(false);
        mChart.setDescription("");
        XAxis x = mChart.getXAxis();
        x.setEnabled(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGridColor(getResources().getColor(R.color.ColorGraphAxis));
        x.setTextColor(getResources().getColor(R.color.ColorGraphText));
        x.setTypeface(Typeface.DEFAULT_BOLD);
        x.setTextSize(13);
        x.setLabelsToSkip(5);
        x.setAvoidFirstLastClipping(false);

        mChart.animateXY(2000, 2000);
        mChart.setHighlightEnabled(false);
        YAxis yLeft = mChart.getAxisLeft();
        yLeft.setEnabled(false);
        yLeft.setAxisMinValue(minValue);
        yLeft.setAxisMaxValue(maxValue);
        yLeft.setStartAtZero(false);
        YAxis yRight = mChart.getAxisRight();
        yRight.setEnabled(false);
        yRight.setAxisMinValue(minValue);
        yRight.setAxisMaxValue(maxValue);
        yRight.setStartAtZero(false);
        x.setDrawAxisLine(false);

        mChart.setDrawGridBackground(false);
        Paint paint = mChart.getRenderer().getPaintRender();
        int heightItemsToConsider[] = {R.id.graph_button_bar, R.id.graph_calories_burned, R.id.graph_day,
                                        R.id.crunch_chart, R.id.posture_chart};
        int height =  LayoutUtils.getComputedGraphHeight(getView(), getActivity(),
                heightItemsToConsider);

        LinearGradient linGrad = new LinearGradient(0, 0, 0, height,
                getResources().getColor(R.color.ColorGraphHigh),
                getResources().getColor(R.color.ColorGraphLow),
                Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
        mChart.fitScreen();
        setData(stepsByHour);
        // dont forget to refresh the drawing
        mChart.invalidate();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private int getMaxValue(){
        LocalDateTime startOfDay = dayToView.atStartOfDay();

        int maxValue = 0;

        for (int i = 0; i < 24; i++) {
            List<StepReading> hourSteps = new Select().from(StepReading.class)
                    .where(Condition.column(StepReading$Table.DATETIME)
                            .between(startOfDay.plusHours(i).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli())
                            .and(startOfDay.plusHours(i + 1).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()))
                    .queryList();
            int steps = 0;
            for (StepReading stepReading: hourSteps) {
                steps += stepReading.getMilliG()/ACTIVITY_PER_STEP;
            }
            if(maxValue < steps){
                maxValue = steps;
            }
        }

        maxValue = maxValue > 100 ? maxValue : 100;
        return maxValue;
    }

    private List<Integer> getStepsByHourForDay(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        List<Integer> stepsByHour = new ArrayList<Integer>();

        for (int i = 0; i < 24; i++) {
            List<StepReading> hourSteps = new Select().from(StepReading.class)
                    .where(Condition.column(StepReading$Table.DATETIME)
                            .between(startOfDay.plusHours(i).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli())
                            .and(startOfDay.plusHours(i + 1).toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()))
                    .queryList();
            int steps = 0;
            for (StepReading stepReading: hourSteps) {
                steps += stepReading.getMilliG()/ACTIVITY_PER_STEP;
            }
            stepsByHour.add(steps);
        }

        return stepsByHour;
    }

    private void setData(List<Integer> stepsForDay) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < (24 + 2); i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> vals1 = new ArrayList<Entry>();

        for (int i = 0; i < stepsForDay.size(); i++) {
            vals1.add(new Entry(stepsForDay.get(i), i));
        }
        vals1.add(new Entry(0, 24));
        vals1.add(new Entry(0, 25));

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals1, "DataSet 1");
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        //set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(6f);
        set1.setCircleSize(5f);
        //set1.setHighLightColor(Color.rgb(244, 117, 117));
        //set1.setColor(Color.rgb(104, 241, 175));
        //set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setDrawHorizontalHighlightIndicator(false);

        // create a data object with the datasets
        LineData data = new LineData(xVals, set1);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        // set data
        mChart.setData(data);
    }


}
