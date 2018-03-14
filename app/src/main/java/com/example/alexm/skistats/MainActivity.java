package com.example.alexm.skistats;

import android.content.Intent;
import android.graphics.Typeface;
import android.icu.text.AlphabeticIndex;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private ImageButton recordButton;
    private ImageButton historyButton;
    private ImageButton mapsButton;
    private ImageButton importButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main_tile);
        initButtons();

        recordButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RecordActivity.class));
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        importButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ImportActivity.class));
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void initButtons()
    {
        recordButton = (ImageButton)findViewById(R.id.img_btn_record);
        historyButton = (ImageButton)findViewById(R.id.img_btn_history);
        mapsButton = (ImageButton)findViewById(R.id.img_btn_maps);
        importButton = (ImageButton)findViewById(R.id.img_btn_import);
        settingsButton = (ImageButton)findViewById(R.id.img_btn_settings);
        exitButton = (ImageButton)findViewById(R.id.img_btn_exit);
    }


}


/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "--Menu to start recording--", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_record) {
            startActivity(new Intent(MainActivity.this, RecordActivity.class));

        } else if (id == R.id.nav_history){

            startActivity(new Intent(MainActivity.this, HistoryActivity.class));

        } else if (id == R.id.nav_map) {

            startActivity(new Intent (MainActivity.this, MapsActivity.class));
        } else if (id == R.id.nav_settings) {

            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_import) {

            startActivity(new Intent(MainActivity.this, ImportActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
        }*/
