package com.example.alexm.skistats;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    //public GPXParser mParser = new GPXParser();

    private static final int FILE_CODE = 1;


    private List<File> imports = new ArrayList<>();
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "aa/test1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        startFilePicker();
    }

    public void startFilePicker()
    {
        Intent i = new Intent(getApplicationContext(), FilePickerActivity.class);

        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        String location = Environment.getExternalStorageDirectory().getPath();
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, location);

        startActivityForResult(i, FILE_CODE);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if((requestCode == FILE_CODE) && (resultCode == Activity.RESULT_OK))
        {
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            for (Uri uri : files)
            {
                File file = Utils.getFileForUri(uri);
                imports.add(file);
                Log.e(TAG, file.getAbsolutePath());
            }
        }
    }

}
