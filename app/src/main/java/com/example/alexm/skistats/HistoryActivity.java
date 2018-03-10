package com.example.alexm.skistats;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lv;
    private List<HistoryFile> historyFiles = new ArrayList<>();
    private List<String> gpsFiles = new ArrayList<String>();
    private List<String> gpsFilesNoExtension;
    private List<HistoryFile> HistoryFileDisplayNames;
    //private List<String> fileList = new ArrayList<>();
    private String TAG = "SkiStats.Log";
    private String renameTo = "";

    public void populateHistoryFiles()
    {
        HistoryFile historyFile = new HistoryFile();

        for(int i = 0; i < gpsFiles.size(); i++)
        {
            historyFile.setFileName(gpsFiles.get(i));
            historyFile.setDisplayName(gpsFilesNoExtension.get(i));
            String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
            String full = path + gpsFiles.get(i);
            File file = new File("full");
            long millisec;
            if(file.exists())
            {
                millisec = file.lastModified();
                Date date = new Date(millisec);
                historyFile.setDateLastModified(date);
            }
            else
            {
                //Log.e(TAG,"Error: Cannot find file: " + full);
                historyFile.setDateLastModified(null);
            }
        }
    }

    public void x()
    {
        for(int i = 0; i < historyFiles.size(); i++)
        {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lv = (ListView) findViewById(R.id.HistoryListView);

        getAllGpsFileNames();
        gpsFilesNoExtension = gpsFiles;
        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        sortListsAlphabetically();
        gpsFilesNoExtension = (convertListToSpaces(gpsFilesNoExtension));
        gpsFilesNoExtension = (removeListGpxExtension(gpsFilesNoExtension));
        populateHistoryFiles();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                gpsFilesNoExtension);
        lv.setAdapter(arrayAdapter);
        registerForContextMenu(lv);
        //updateList();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String filename = gpsFiles.get(i);
                String filename = gpsFiles.get(i);
                //Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();
                Intent appInfo = new Intent(HistoryActivity.this, SelectionActivity.class);
                String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
                String full = path + filename;
                appInfo.putExtra("filename", full);
                startActivity(appInfo);
            }
        });
    }

    public String convertStringToUnderscore(String input)
    {
        String converted = input.replaceAll(" ", "_");
        return converted;
    }

    public List<String> convertListToSpaces(List<String> input)
    {
        String converted = "";
        List<String> list = new ArrayList<>();
        for (int i = 0; i < input.size(); i++)
        {
            converted = input.get(i);
            list.add(converted.replaceAll("_", " "));
        }


        return list;
    }

    public List<String> convertListToUnderscore(List<String> input)
    {
        String converted = "";
        List<String> list = new ArrayList<>();
        for (int i = 0; i < input.size(); i++)
        {
            converted = input.get(i);
            list.add(converted.replaceAll(" ", "_"));
        }


        return list;
    }

    public List<String> removeListGpxExtension(List<String> input)
    {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < input.size(); i++)
        {
            String reversed = new StringBuilder(input.get(i)).reverse().toString();
            StringBuilder sb = new StringBuilder(reversed);
            int j = 0;
            while (sb.charAt(j) != '.')
            {
                sb.deleteCharAt(j);
            }
            if(sb.charAt(j) == '.')
            {
                sb.deleteCharAt(j);
            }

            String normal = new StringBuilder(sb.toString()).reverse().toString();
            list.add(normal);
        }

        return list;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.HistoryListView)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(gpsFiles.get(info.position));
            menu.add(Menu.NONE, 0, 0, "Rename");
            menu.add(Menu.NONE, 1, 1, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String menuItemName = "";
        if (menuItemIndex == 0) {
            menuItemName = "Rename";
            renameFile(menuItemName, info.position);
        } else if (menuItemIndex == 1) {
            menuItemName = "Delete";
            deleteFile(menuItemName, info.position);
        }
        //String listItemName = gpsFiles.get(info.position);


        return true;
    }

    public void renameFile(String menuItem, int listPosition)
    {
        final String selectedName = gpsFiles.get(listPosition);

        View view = (LayoutInflater.from(HistoryActivity.this)).inflate(R.layout.popup_rename_file, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(HistoryActivity.this);
        alert.setTitle("Rename Recording");
        //alert.setMessage("Enter the new name for the recording");
        alert.setView(view);
        final EditText userInput = (EditText) view.findViewById(R.id.userInput);

        alert.setCancelable(true)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        renameTo = userInput.getText().toString();
                        Toast.makeText(getApplicationContext(), renameTo,Toast.LENGTH_SHORT).show();

                    }

    });

        Dialog dialog = alert.create();
        dialog.show();


        Toast.makeText(getApplicationContext(), "Editing " + selectedName,Toast.LENGTH_SHORT).show();
        //gpsFiles.get(listPosition);
    }

    public void deleteFile(String menuItem, int listPosition)
    {
        final String selectedName = gpsFiles.get(listPosition);

        AlertDialog.Builder alert = new AlertDialog.Builder(HistoryActivity.this);
        alert.setTitle("Delete Recording");
        alert.setMessage("Do you want to delete this recording?");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        // User selects Yes
                        // do a thing to cancel recording
                        Log.e(TAG, "Deleting recording: " + selectedName);

                        String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
                        File dir = new File(path);

                        String fullFileName = selectedName;// + ".gpx";
                        String fullnamePath = path + fullFileName;
                        File recording = new File(dir, fullFileName);
                        Log.e(TAG,"Full path to remove: " + fullnamePath);

                        recording.delete();
                        if(recording.exists())
                        {
                            getApplicationContext().deleteFile(recording.getName());
                        }
                        // To ensure the file is removed on Windows Operating System also
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fullnamePath}, null, null);

                        for (int j = 0; j < gpsFiles.size(); j++)
                        {
                            if (gpsFiles.get(j).equals(selectedName))
                            {
                                gpsFiles.remove(j);
                            }
                        }
                        updateList();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // User clicked No
                        break;
                }
            }
        };

        alert.setPositiveButton("Delete", dialogClickListener);
        alert.setNegativeButton("Cancel", dialogClickListener);

        AlertDialog dialog = alert.create();
        // Display the alert
        dialog.show();

        Toast.makeText(getApplicationContext(), "Deleting " + selectedName,Toast.LENGTH_SHORT).show();
        //gpsFiles.remove(listPosition);
    }

    public void sortListsAlphabetically()
    {
        Collections.sort(gpsFiles, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(gpsFilesNoExtension, String.CASE_INSENSITIVE_ORDER);
    }

    public void updateList()
    {
        ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
    }

    // temporary method, ghetto version that will do for now....
    public void getAllGpsFileNames()
    {
        // Default Sample GPX Files
        /* for (int i = 1; i < 5; i++)
        {
            File file = new File("gpsData/sauze/Day_" + i + "_2017-2018.gpx");
            gpsFiles.add(file.getName());
            fileList.add(file.getAbsolutePath());
        } */

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
                    //Log.e("SkiStats.Log", "File Name: " + file.getName());
                    gpsFiles.add(file.getName());
                    //fileList.add(file.getAbsolutePath());
                }
            }
        }
    }
}