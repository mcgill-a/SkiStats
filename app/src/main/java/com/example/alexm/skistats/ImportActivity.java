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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;


public class ImportActivity extends AppCompatActivity {

    private static final String TAG = "SkiStats.Log";
    private ImageButton importButton;
    //public GPXParser mParser = new GPXParser();

    private static final int FILE_CODE = 1;
    private String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Imports/";

    private List<File> imports = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        importButton = (ImageButton)findViewById(R.id.importImage);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startFilePicker();
            }
        });
    }

    public void filterFiles()
    {
        int failCount = 0;
        for (int i = 0; i < imports.size(); i++)
        {
            if (checkGpxExtension(imports.get(i).getName()))
            {
                Log.e(TAG,"Importing file: " + imports.get(i).getName());

                if(importFile(imports.get(i).getAbsolutePath(), path))
                {
                    Log.e(TAG,"Successfully imported file: " + imports.get(i).getName());
                }
                else
                {
                    Log.e(TAG,"Error: Failed to import file: " + imports.get(i).getName());
                }
            }
            else
            {
                failCount++;
            }
        }
        if(failCount == 1)
        {
            Toast.makeText(getApplicationContext(), failCount + " file was not imported as it was not a GPX file",Toast.LENGTH_SHORT).show();
        }
        else if (failCount > 1)
        {
            Toast.makeText(getApplicationContext(), failCount + " files were not imported as they were not GPX files",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "All files were successfuly imported",Toast.LENGTH_SHORT).show();
        }
        imports.clear();
    }

    public boolean importFile(String original, String target)
    {
        try {
            File directory = Environment.getExternalStorageDirectory();
            if (directory.canWrite())
            {
                int end = original.toString().lastIndexOf("/");
                String str1 = original.toString().substring(0, end);
                String str2 = original.toString().substring(end+1, original.length());
                Log.e(TAG,"filename: " + original + " | str1 = " + str1 + "| str2 = " + str2);
                File source = new File(str1, str2);
                File destination= new File(target, str2);
                destination.getParentFile().mkdirs();
                if (source.exists())
                {
                    int index = 1;
                    while (destination.exists())
                    {
                        index++;
                        String incrementingName = removeGpxExtension(str2) + "(" + index + ").gpx";
                        destination = new File(target, incrementingName);
                    }
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            return true;
        } catch (Exception e)
        {
            return false;
        }
    }

    public String addGpxExtension(String input)
    {
        return (input += ".gpx");
    }

    public String removeGpxExtension(String input)
    {
        String store = input;
        String gpx = ".gpx";
        // Reverse string to make file extension appear first
        StringBuilder reverse = new StringBuilder(input).reverse();
        String output = "";
        String extension = "";
        int length = input.length();
        for (int i = 0; i < 4; i++)
        {
            extension += reverse.charAt(i);
        }
        // Reverse file extension back
        extension = new StringBuilder(extension).reverse().toString();
        if (extension.equals(".gpx"))
        {
            for (int i = 0; (i < 4); i++)
            {
                reverse.deleteCharAt(0);
            }
            output = new StringBuilder(reverse).reverse().toString();
            return output;
        }
        else
        {
            return null;
        }
    }

    public boolean checkGpxExtension (String filename)
    {
        String store = filename;
        String gpx = ".gpx";
        // Reverse string to make file extension appear first
        String reverse = new StringBuilder(filename).reverse().toString();
        String extension = "";
        for (int i = 0; i < 4; i++)
        {
            extension += reverse.charAt(i);
        }
        // Reverse file extension back
        extension = new StringBuilder(extension).reverse().toString();
        if (extension.equals(".gpx"))
        {
            return true;
        }
        else
        {
            return false;
        }

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
        filterFiles();
    }

}
