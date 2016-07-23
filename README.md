# Introduction
A view similar to android seekbar with better implementation, if used as price seekbar. The purpose of modifying this view was to make a rangebar that gives discrete values according to your current selected value.
A sample of discrete values is given below.

```
500,550,600,650,700,750,800,850,900,950,1000,1100,1200,1300,1400,1500,1600,1700,1800,1900,2000,2200
,2400,2600,2800,3000,3200,3400,3600,3800,4000,4500,5000,5500,6000,6500,7000,7500,8000,8500,9000,9500
,10000,11000,12000,13000,14000,15000,16000,17000,18000,19000,20000,22000,24000,26000,28000,30000,32000
,34000,36000,38000,40000,45000,50000
```

#Mode of rangeseekbar

####LINEAR 
A regular rangeseekbar

####DISCRETE
A stteped value rangeseekbar (Supports integer values only)


#How to use in project

###Setup as gradle dependency

```
dependencies {
    compile 'com.guesthouser:guesthouser-rangeseekbar:1.0'
}
```
###Initialization

####Use GHRangeSeekbar in xml layout as a view

```
<com.guesthouser.ghrangeseekbar.GHRangeSeekBar
        android:id="@+id/range1"
        android:layout_width="300dp"
        android:layout_height="40dp"
        android:layout_centerInParent="true"/>
```

####Map this view in java

```
GHRangeSeekBar bar = (GHRangeSeekBar) findViewById(R.id.range1);
```


#Useful methods

####Setting mode of rangeseekbar
```
setmValueType(GHRangeSeekBar.ValueTypes.LINEAR);
setmValueType(GHRangeSeekBar.ValueTypes.DISCRETE);(Supports integer values only)
```
####Setting min and max values
```
setRangeValues(500, 50000);
```
####Setting listener for change in values 
```
setOnRangeSeekBarChangeListener(new GHRangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(GHRangeSeekBar bar, Object minValue, Object maxValue) {
            // minValue : minimum value selected by dragging
            // maxValue : maximum value selected by dragging
                int min1 = (int)minValue;
                int max1 = (int)maxValue;
            }
        });
```
####Notify listener while dragging a thumb
```
setNotifyWhileDragging(true);
```
####Setting background color of rangeseekbar
```
setLineBgColor(Color.RED);
```
####Setting foreground color of rangeseekbar
```
setLineHighlightedColor(Color.GREEN);
```
####Setting bitmap for thumb

![1](https://cloud.githubusercontent.com/assets/7863274/17076346/ddbe81ae-50cd-11e6-9e40-121e0121fd7d.png)

The white circles are thumbs, those can be dragged to change the values

```
setThumbImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
```
####Setting height of rangeseekbar(Horizontal line) in pixels
```
setLineHeight(10);
```

#Run test application

This repository contains an android project which runs a test app, showing the difference between linear rangeseekbar and guesthouser implementation of rangeseekbar. 
