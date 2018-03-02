package com.example.alexm.skistats;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        // Instanciating an array list (you don't need to do this,
        // you already have yours).

        /*List<String> gpsHistoryArray = new ArrayList<String>();
        gpsHistoryArray.add("04/02/2017 - Ski");
        gpsHistoryArray.add("05/02/2017 - Ski");
        gpsHistoryArray.add("06/02/2017 - Ski");
        gpsHistoryArray.add("07/02/2017 - Ski");*/


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
        for (int i = 1; i < 5; i++)
        {
            File file = new File("gpsData/sauze/Day_" + i + "_2017-2018.gpx");
            gpsFiles.add(file.getName());
        }
    }
}
