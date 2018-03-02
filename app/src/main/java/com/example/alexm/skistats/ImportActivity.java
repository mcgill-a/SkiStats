package com.example.alexm.skistats;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;


public class ImportActivity extends AppCompatActivity {

    private static final String TAG = "SkiStats.Log";
    public EditText editText;
    public TextView textView;
    public Button save, load;
    GPXParser mParser = new GPXParser();

    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "aa/test1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);


        editText = (EditText) findViewById(R.id.txtInput);
        textView = (TextView) findViewById(R.id.txtView);
        save = (Button) findViewById(R.id.btnExport);
        load = (Button) findViewById(R.id.btnImport);

        File dir = new File(path);
        dir.mkdirs();
    }




    public void btnTest(View view)
    {
        Gpx parsedGpx = null;

        try
        {
            InputStream in = getAssets().open("testData.gpx");
            parsedGpx = mParser.parse(in);
        }
        catch(IOException | XmlPullParserException e)
        {
            e.printStackTrace();
        }

        if(parsedGpx != null)
        {
            Integer count = 0;
            List<Track> tracks = parsedGpx.getTracks();
            for(int i = 0; i < tracks.size(); i++)
            {
                Track track = tracks.get(i);
                Log.e(TAG, "track " + i + ":");
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++)
                {
                    TrackSegment segment = segments.get(i);
                    Log.e(TAG," segment " + j + ":");
                    for (TrackPoint trackPoint : segment.getTrackPoints())
                    {
                        Log.e(TAG, "   point: lat" + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude() + ", time " + trackPoint.getTime());
                        count++;
                    }
                }
            }
            Toast.makeText(getApplicationContext(), "Entry Count: " + count, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.e(TAG, "Error parsing gpx file");
            //Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
        }
    }


    public void btnExportClick(View view)
    {
        Log.e("alexm.org", "Step 1");
        File file = new File(path + "/savedFile.txt");
        Log.e("alexm.org", "Step 2");

        String [] saveText = new String[100];
        saveText[0] = "test";
        // String [] saveText = String.valueOf(editText.getText()).split(System.getProperty("line.seperator"));
        Log.e("alexm.org", "Step 3");
        editText.setText("");
        Log.e("alexm.org", "Step 4");
        Toast.makeText(getApplicationContext(), "Saved, not rly but lets pretend anyways", Toast.LENGTH_LONG).show();
        Log.e("alexm.org", "Step 5");
        //Save(file, saveText);
        Log.e("alexm.org", "Step 6");
    }

    public void btnImportClick(View view) {
        File file = new File(path + "/savedFile.txt");

        String[] loadText = new String[6];
        loadText[0] = "String 1";
        loadText[1] = "String 2";
        loadText[2] = "String 3";
        loadText[3] = "String 4";
        loadText[4] = "String 5";
        loadText[5] = "String 6";


        String finalString = "";

        for (int i = 0; i < loadText.length; i++) {
            finalString += loadText[i] + "\n";
        }

        textView.setText(finalString);
    }
}
