package com.mbientlab.abdisc.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.utils.Highlight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lgleason on 9/6/15.
 */
public class AbDiscScatterChart extends ScatterChart{
    public AbDiscScatterChart(Context context) {
        super(context);
    }

    public AbDiscScatterChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbDiscScatterChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void drawAllMarkers() {
        List<Highlight> all = new ArrayList<Highlight>();

        for (int i = 0; i < mData.getXValCount(); i++) {
            for (int j = 0; j < mData.getDataSetCount(); j++) {
                all.add(new Highlight(i, j));
            }
        }
        highlightValues(all.toArray(new Highlight[all.size()]));

        //drawMarkers();
    }
}
