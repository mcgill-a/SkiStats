package com.example.alexm.skistats;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lv = (ListView) findViewById(R.id.HistoryListView);

        // Instanciating an array list (you don't need to do this,
        // you already have yours).
        List<String> gpsHistoryArray = new ArrayList<String>();
        gpsHistoryArray.add("04/02/2017 - Ski");
        gpsHistoryArray.add("05/02/2017 - Ski");
        gpsHistoryArray.add("06/02/2017 - Ski");
        gpsHistoryArray.add("07/02/2017 - Ski");


        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                gpsHistoryArray );

        lv.setAdapter(arrayAdapter);
    }
}
