package com.example.alexm.skistats;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class PopupChart extends AppCompatActivity {


    private XYPlot plot;
    private ArrayList<String> altitudeStrings;

      public Number[] getAltitudes()
    {
        // Define the size of the list
        Number altitudes[] = new Number[altitudeStrings.size()];
        int current = 0;
        String filter = "";
        BigDecimal stripedVal;
        // Add each item in the arraylist of altitudes to a list - required format for the chart
        for(int i = 0; i < altitudeStrings.size(); i++)
        {
            filter = altitudeStrings.get(i);
            stripedVal = new BigDecimal(filter).stripTrailingZeros();
            current = stripedVal.intValue();
            altitudes[i] = current;
        }
        return altitudes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_chart);

        Bundle bundleObject = getIntent().getExtras();
        altitudeStrings = (ArrayList<String>) bundleObject.getSerializable("alts");

        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int)(height * 0.46));


        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        if (altitudeStrings.size() == 0)
        {
            Toast.makeText(getApplicationContext(),"No GPS Data Loaded",Toast.LENGTH_LONG).show();
        }
        else if(altitudeStrings.size() > 0 && altitudeStrings.size() < 10)
        {
            Toast.makeText(getApplicationContext(),"Not enough GPS Data Loaded",Toast.LENGTH_LONG).show();
        }
        else
        {
            // turn the array into XYSeries:
            // (Y_VALS_ONLY means use the element index as the x value)
            XYSeries series1 = new SimpleXYSeries(
                    Arrays.asList(getAltitudes()), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

            // create formatters to use for drawing a series using LineAndPointRenderer
            // and configure them from xml:
            LineAndPointFormatter series1Format =
                    new LineAndPointFormatter(this, R.xml.line_formatter_1);

            series1Format.setPointLabelFormatter(null);


            // Smooth the line slightly - somewhat reduces sudden spikes by smoothing over
            series1Format.setInterpolationParams(
                    new CatmullRomInterpolator.Params(15, CatmullRomInterpolator.Type.Centripetal));

            // add a new series' to the xyplot:
            plot.addSeries(series1, series1Format);
            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#"));
        }
    }
}
