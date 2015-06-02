package com.mbientlab.abdisc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Created by etsai on 6/1/2015.
 */
public class MainActivity extends ActionBarActivity {
    private Fragment activityFrag= null, distanceFrag= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FragmentManager fragManager= getFragmentManager();
        final FragmentTransaction fragTransaction= fragManager.beginTransaction();
        findViewById(R.id.tab_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activityFrag == null) {
                    activityFrag= new ActivityFragment();
                }

                fragTransaction.replace(R.id.app_content, activityFrag).commit();
            }
        });
        findViewById(R.id.tab_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (distanceFrag == null) {
                    distanceFrag= new DistanceFragment();
                }

                fragTransaction.replace(R.id.app_content, distanceFrag).commit();
            }
        });
    }

}
