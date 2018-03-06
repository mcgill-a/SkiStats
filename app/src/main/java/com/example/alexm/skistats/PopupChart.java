package com.example.alexm.skistats;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.math.BigDecimal;
import java.sql.Array;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.*;

public class PopupChart extends AppCompatActivity {


    private XYPlot plot;
    private ArrayList<String> altitudeStrings;
    private int minAltitude = Integer.MAX_VALUE;
    private int maxAltitude = Integer.MIN_VALUE;
    private int counter = 0;

      public Number[] getAltitudes()
    {
        Number altitudes[] = new Number[altitudeStrings.size()];
        int current = 0;
        String filter = "";
        BigDecimal stripedVal;

        for(int i = 0; i < altitudeStrings.size(); i++)
        {
            filter = altitudeStrings.get(i);
            stripedVal = new BigDecimal(filter).stripTrailingZeros();
            current = stripedVal.intValue();
            altitudes[i] = current;
            /*
            only needed if using pinch zoom
            counter++;

            if (current > maxAltitude)
            {
                maxAltitude = current;
            }
            if (current < minAltitude) {
                minAltitude = current;
            }
            */
        }
        return altitudes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_chart);

        Bundle bundleObject = getIntent().getExtras();
        altitudeStrings = (ArrayList<String>) bundleObject.getSerializable("alts");

        //convertAltitudesToNumbers();
        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int)(height * 0.45));


        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // create an array of y-values to plot:
        Number[] series1Numbers = {1500, 1550, 1620, 1660, 1700, 1925, 2100, 2150, 2000, 1900, 1500, 1550, 1620, 1660, 1700, 1925, 2100, 2150, 2000, 1900};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(getAltitudes()), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_formatter_1);

        series1Format.setPointLabelFormatter(null);


        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(15, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        // need to use multithreading before implementing this. too resource intensive.
        //PanZoom.attach(plot, PanZoom.Pan.HORIZONTAL, PanZoom.Zoom.STRETCH_HORIZONTAL);
        //plot.getOuterLimits().set(0, counter, minAltitude, maxAltitude);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#"));
    }
}
