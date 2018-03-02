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

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;


public class ImportActivity extends AppCompatActivity {

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


    public void btnTest()
    {
        Gpx parsedGpx = null;

        try
        {
            InputStream in = getAssets().open("test.gpx");
            parsedGpx = mParser.parse(in);
        }
        catch(IOException | XmlPullParserException e)
        {
            e.printStackTrace();
        }

        if(parsedGpx == null)
        {
            Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // do something
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
