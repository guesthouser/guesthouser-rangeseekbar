package com.guesthouser.rangeseekbartestapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.guesthouser.ghrangeseekbar.GHRangeSeekBar;

public class MainActivity extends AppCompatActivity {

    GHRangeSeekBar bar;
    TextView min, max;
    ToggleButton toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar = (GHRangeSeekBar) findViewById(R.id.range1);
        min = (TextView) findViewById(R.id.txt1);
        max = (TextView) findViewById(R.id.txt2);
        toggle = (ToggleButton)findViewById(R.id.toggle1);

        bar.setLineBgColor(Color.RED);
        bar.setLineHighlightedColor(Color.GREEN);
        // set seekbar thumb image here
//        bar.setThumbImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        bar.setRangeValues(500, 50000);
        min.setText(500 + "");
        max.setText(50000 + "");

        bar.setLineHeight(10);

        bar.setNotifyWhileDragging(true);

        bar.setmValueType(GHRangeSeekBar.ValueTypes.LINEAR);

        bar.setOnRangeSeekBarChangeListener(new GHRangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(GHRangeSeekBar bar, Object minValue, Object maxValue) {
                min.setText(minValue + "");
                max.setText(maxValue + "");
            }
        });



        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    bar.setmValueType(GHRangeSeekBar.ValueTypes.DISCRETE);
                else
                    bar.setmValueType(GHRangeSeekBar.ValueTypes.LINEAR);

            }
        });


    }
}
