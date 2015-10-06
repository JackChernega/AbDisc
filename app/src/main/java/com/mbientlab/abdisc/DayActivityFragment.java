package com.mbientlab.abdisc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
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
import com.mbientlab.abdisc.model.CrunchPosture;
import com.mbientlab.abdisc.model.StepReading;
import com.mbientlab.abdisc.model.StepReading$Table;
import com.mbientlab.abdisc.utils.AbDiscMarkerView;
import com.mbientlab.abdisc.utils.AbDiscScatterChart;
import com.mbientlab.abdisc.utils.ChartBlankValueFormatter;
import com.mbientlab.abdisc.utils.CrunchPostureDataUtils;
import com.mbientlab.abdisc.utils.LayoutUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.util.ArrayList;
import java.util.HashMap;
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
    private AppState appState;

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
    }

    @Override
    public void onStart(){
        super.onStart();
        drawStepsGraph();
        appState.setCurrentFragment(this);
        drawCrunchPostureGraph();
    }

    private void drawCrunchPostureGraph() {
        SharedPreferences sharedPreferences = appState.getSharedPreferences();
        String abDiscMode = sharedPreferences.getString(ProfileFragment.PROFILE_AB_DISK_MODE, CrunchPosture.MODE_CRUNCH);

        Switch testDataSwitch = (Switch) getView().getRootView().findViewById(R.id.testData);
        boolean getTestData = testDataSwitch.isChecked();

        HashMap sessionsData = CrunchPostureDataUtils.getCrunchPostureByHourForDay(dayToView, abDiscMode, getTestData);

        int totalSessions = (int) sessionsData.get("totalSessions");

        String modeString = getString(R.string.label_graph_crunch);

        if (abDiscMode.equals(CrunchPosture.MODE_POSTURE)) {
            modeString = getString(R.string.label_graph_posture);
        }

        TextView totalCrunchSessionsTodayTextField = (TextView) getView().findViewById(R.id.crunch_sessions_today_text_field);
        totalCrunchSessionsTodayTextField.setText(
                String.valueOf(totalSessions) + "   " +
                        modeString + "   " +
                        getText(R.string.label_graph_sessions) + "   " +
                        getText(R.string.label_graph_today)
        );

        drawCrunchPostureGraph(mCrunchChart, R.id.crunch_chart, abDiscMode, sessionsData);
        //drawCrunchPostureGraph(mPostureChart, R.id.posture_chart, CrunchPosture.MODE_POSTURE);
    }

    private void drawCrunchPostureGraph(AbDiscScatterChart mPostureCrunchChart, int chartId,
                                        String chartType, HashMap sessionsData) {

        mPostureCrunchChart = (AbDiscScatterChart) getView().findViewById(chartId);
        mPostureCrunchChart.setDescription("");

        mPostureCrunchChart.setDrawGridBackground(false);
        mPostureCrunchChart.setNoDataText("");

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



        int[] gradientColors = getColorGradient((int[]) sessionsData.get("sessionsByHour"));

        GradientDrawable backgroundGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        mPostureCrunchChart.setBackground(backgroundGradient);

        XAxis xl = mPostureCrunchChart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setDrawAxisLine(false);
        xl.setDrawLimitLinesBehindData(false);


        // some test chart data
        int hoursInDay = 24;

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < hoursInDay + 1; i++) {
            xVals.add("");
        }

        // create a dataset and give it a type
        ScatterDataSet set1 = new ScatterDataSet((List<Entry>)sessionsData.get("sessionEntries"), "DS 1");
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        set1.setDrawHighlightIndicators(false);

        set1.setScatterShapeSize(0f);
        ChartBlankValueFormatter formater = new ChartBlankValueFormatter();
        set1.setValueFormatter(formater);

        ArrayList<ScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        ScatterData data = new ScatterData(xVals, dataSets);

        mPostureCrunchChart.setData(data);
        AbDiscMarkerView mv = new AbDiscMarkerView(getActivity().getApplication().getApplicationContext(),
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
        int xyOffset = getView().findViewById(R.id.chart_spacer1).getWidth();
        mPostureCrunchChart.setViewPortOffsets(xyOffset, 0, xyOffset, -60);
        mPostureCrunchChart.invalidate();
    }

    private int[] getColorGradient(int[] sessionsByHour){
        int graphLow = getResources().getColor(R.color.ColorGraphLow);
        int lowRed = Color.red(graphLow);
        int lowGreen = Color.green(graphLow);
        int lowBlue = Color.blue(graphLow);
        int graphHigh = getResources().getColor(R.color.ColorGraphHigh);
        int highRed = Color.red(graphHigh);
        int highGreen = Color.green(graphHigh);
        int highBlue = Color.blue(graphHigh);

        int sessionGoal = appState.getSharedPreferences().getInt(ProfileFragment.PROFILE_SESSIONS,
                ProfileFragment.DEFAULT_SESSIONS_GOAL);

        int[] colors = new int[24];
        int currentColor = graphLow;
        int currentStepTotal = 0;

        for (int i = 0 ; i < sessionsByHour.length - 1; i++) {
            currentStepTotal += sessionsByHour[i+1];
            if(sessionsByHour[i+1] == 0){
                colors[i] = currentColor;
            } else {
                int red = interpolate(lowRed, highRed, currentStepTotal, sessionGoal);
                int green = interpolate(lowGreen, highGreen, currentStepTotal, sessionGoal);
                int blue = interpolate(lowBlue, highBlue, currentStepTotal, sessionGoal);
                currentColor = Color.rgb(red, green, blue);
                colors[i] = currentColor;
            }
        }
        return colors;
    }

    private int interpolate(float begin, float end, float step, float max){
        if (begin < end) {
            return (int) (((end - begin) * (step / max)) + begin);
        } else {
            return (int) (((begin - end) * (1 - (step / max))) + end);
        }
    }

    private void drawStepsGraph() {
        HashMap stepData = getStepsByHourForDay(dayToView);
        List<Integer> stepsByHour = (List<Integer>) stepData.get("stepsByHour");

        mChart = (LineChart) getView().findViewById(R.id.active_minutes_day_chart);

        int activeMinutes = (int) stepData.get("activeMinutes");
        int rawMaxValue = (int) stepData.get("maxValue");
        int maxValue = (int) (rawMaxValue * 1.05);
        int minValue = maxValue > 100 ? (int) (maxValue * -0.07) : -20;

        TextView textView = (TextView) getView().findViewById(R.id.total_active_minutes);
        textView.setText(String.valueOf(activeMinutes) + " ");

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
                R.id.crunch_chart};
        int height = LayoutUtils.getComputedGraphHeight(getView(), getActivity(),
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
            appState = (AppState) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private HashMap getStepsByHourForDay(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        List<Integer> stepsByHour = new ArrayList<>();
        Switch testDataSwitch = (Switch) getView().getRootView().findViewById(R.id.testData);
        boolean getTestData = testDataSwitch.isChecked();
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
                long stepsThisMinute = stepReading.getMilliG() / ACTIVITY_PER_STEP;

                if(stepsThisMinute > 5){
                    steps += stepsThisMinute;
                    activeMinutes++;
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

    private void setData(List<Integer> stepsForDay) {
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < (24 + 2); i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> vals1 = new ArrayList<>();

        for (int i = 0; i < stepsForDay.size(); i++) {
            vals1.add(new Entry(stepsForDay.get(i), i));
        }
        vals1.add(new Entry(0, 24));
        vals1.add(new Entry(0, 25));

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals1, "DataSet 1");
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setDrawCircles(false);
        set1.setLineWidth(6f);
        set1.setCircleSize(5f);
        set1.setDrawHorizontalHighlightIndicator(false);

        // create a data object with the datasets
        LineData data = new LineData(xVals, set1);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        // set data
        mChart.setData(data);
    }

}
