package com.mbientlab.abdisc;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DayActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayActivityFragment extends Fragment {
    private LineChart mChart;


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
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        drawGraph();
    }

    private int getComputedGraphHeight(){
        int elementIds[] = {R.id.graph_button_bar, R.id.graph_calories_burned};

        int totalHeight = 0;

        for(int i = 0; i < elementIds.length; i++){
            View viewToMeasure = getView().findViewById(elementIds[i]);
            viewToMeasure.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            totalHeight += viewToMeasure.getMeasuredHeight();
        }
        getView().measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int containerViewHeight = getView().getMeasuredHeight();
        return((int) Math.round((containerViewHeight - totalHeight) * 0.8));
    }

    private void drawGraph(){
        mChart = (LineChart) getView().findViewById(R.id.active_minutes_day_chart);

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
        setData(24, 60);
        mChart.animateXY(2000, 2000);
        mChart.setHighlightEnabled(false);
        YAxis yLeft = mChart.getAxisLeft();
        yLeft.setEnabled(false);
        yLeft.setAxisMinValue(-5);
        yLeft.setAxisMaxValue(65);
        yLeft.setStartAtZero(false);
        YAxis yRight = mChart.getAxisRight();
        yRight.setEnabled(false);
        yRight.setAxisMinValue(-7);
        yRight.setAxisMaxValue(65);
        yRight.setStartAtZero(false);
        x.setDrawAxisLine(false);


        mChart.setDrawGridBackground(false);
        Paint paint = mChart.getRenderer().getPaintRender();
        int height = getComputedGraphHeight();
        mChart.getLayoutParams().height = height;

        LinearGradient linGrad = new LinearGradient(0, 0, 0, height,
                getResources().getColor(R.color.ColorGraphHigh),
                getResources().getColor(R.color.ColorGraphLow),
                Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
        mChart.fitScreen();

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
    private void setData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < (count + 2); i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> vals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);// + (float)
            // ((mult *
            // 0.1) / 10);
            vals1.add(new Entry(val, i));
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
