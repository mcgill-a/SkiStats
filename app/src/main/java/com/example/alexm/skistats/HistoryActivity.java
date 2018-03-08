package com.example.alexm.skistats;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lv;
    private List<String> gpsFiles = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lv = (ListView) findViewById(R.id.HistoryListView);

        getAllGpsFileNames();

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                gpsFiles);

        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String filename = gpsFiles.get(i);
                //Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();
                Intent appInfo = new Intent(HistoryActivity.this, SelectionActivity.class);
                appInfo.putExtra("filename", filename);
                startActivity(appInfo);
            }
        });
    }

    // temporary method, ghetto version that will do for now....
    public void getAllGpsFileNames()
    {
        // Default Sample GPX Files
        for (int i = 1; i < 5; i++)
        {
            File file = new File("gpsData/sauze/Day_" + i + "_2017-2018.gpx");
            gpsFiles.add(file.getName());
        }

        // User Recording GPX Files
        String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
        File[] files = new File(path).listFiles();

        showFiles(files);
    }
    public void showFiles(File[] files)
    {
        for (File file : files)
        {
            if(file.isDirectory())
            {
                showFiles(file.listFiles());
            }
            else
            {
                // Reverse string to make file extension appear first
                String reverse = new StringBuilder(file.getName()).reverse().toString();
                String extension = "";
                for (int i = 0; i < 4; i++)
                {
                    extension += reverse.charAt(i);
                }
                // Reverse file extension back
                extension = new StringBuilder(extension).reverse().toString();
                if (extension.equals(".gpx"));
                {
                    Log.e("SkiStats.Log", "File Name: " + file.getName());
                }
            }
        }
    }

}
