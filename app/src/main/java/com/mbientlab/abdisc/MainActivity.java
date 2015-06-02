package com.mbientlab.abdisc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by etsai on 6/1/2015.
 */
public class MainActivity extends ActionBarActivity {
    private ImageButton activityTab, distanceTab;

    private Fragment activityFrag= null, distanceFrag= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityTab= (ImageButton) findViewById(R.id.tab_activity);
        distanceTab= (ImageButton) findViewById(R.id.tab_distance);

        final FragmentManager fragManager= getFragmentManager();
        activityTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragTransaction= fragManager.beginTransaction();

                if (activityFrag == null) {
                    activityFrag= new ActivityFragment();
                }

                fragTransaction.replace(R.id.app_content, activityFrag).commit();
            }
        });
        distanceTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragTransaction= fragManager.beginTransaction();

                if (distanceFrag == null) {
                    distanceFrag= new DistanceFragment();
                }

                fragTransaction.replace(R.id.app_content, distanceFrag).commit();
            }
        });
    }

}
