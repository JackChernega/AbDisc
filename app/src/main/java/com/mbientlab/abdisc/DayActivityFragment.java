package com.mbientlab.abdisc;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
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
        mChart = (LineChart) getView().findViewById(R.id.active_minutes_day_chart);

        mChart.setDrawGridBackground(true);
        XAxis x = mChart.getXAxis();
        x.setEnabled(true);
        setData(45, 100);
        mChart.animateXY(2000, 2000);
        YAxis yLeft = mChart.getAxisLeft();
        yLeft.setEnabled(false);
        YAxis yRight = mChart.getAxisRight();
        yRight.setEnabled(false);

        //mChart.setBackgroundColor(getResources().getColor(R.color.ColorTransparent));
        mChart.setDrawGridBackground(false);
        Paint paint = mChart.getRenderer().getPaintRender();

        LinearGradient linGrad = new LinearGradient(0, 0, 0, 500,
                getResources().getColor(R.color.ColorGraphHigh),
                getResources().getColor(R.color.ColorGraphLow),
                Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
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
        for (int i = 0; i < count; i++) {
            xVals.add((1990 +i) + "");
        }

        ArrayList<Entry> vals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 20;// + (float)
            // ((mult *
            // 0.1) / 10);
            vals1.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals1, "DataSet 1");
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        //set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(2f);
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
