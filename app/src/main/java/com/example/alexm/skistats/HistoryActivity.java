package com.example.alexm.skistats;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class HistoryActivity extends AppCompatActivity {

    private ListView recordingLv;
    private ListView importLv;
    private List<HistoryFile> historyRecordingFiles = new ArrayList<>();
    private List<HistoryFile> historyImportFiles = new ArrayList<>();
    private File sdDir = Environment.getExternalStorageDirectory();
    private String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
    private String TAG = "SkiStats.Log";
    private String renameTo = "";

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1001;

    public void gpxReadFailed()
    {
        Log.e(TAG, "Error parsing gpx file");
        Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (!checkPermissionForReadExtertalStorage())
        {
            requestPermissionForReadExtertalStorage();
        }
        if (!checkPermissionForReadExtertalStorage())
        {
            finish();
        }
        recordingLv = (ListView) findViewById(R.id.HistoryRecordingListView);
        importLv = (ListView) findViewById(R.id.HistoryImportListView);

        getAllRecordingNames();
        getAllImportNames();
        sortListsAlphabetically();

        HistoryAdapter recordingAdapter = new HistoryAdapter(this, R.layout.history_list_row, historyRecordingFiles);
        HistoryAdapter importAdapter = new HistoryAdapter(this, R.layout.history_list_row, historyImportFiles);
        recordingLv.setAdapter(recordingAdapter);
        importLv.setAdapter(importAdapter);

        //setListViewHeightBasedOnChildren(recordingLv);
        //setListViewHeightBasedOnChildren(importLv);

        ListUtils.setDynamicHeight(recordingLv);
        ListUtils.setDynamicHeight(importLv);

        registerForContextMenu(recordingLv);
        updateList();
        recordingLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String filename = gpsFiles.get(i);
                String filename = historyRecordingFiles.get(i).getFileName();
                //Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();
                Intent appInfo = new Intent(HistoryActivity.this, SelectionActivity.class);
                String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
                String full = path + filename;
                appInfo.putExtra("filename", full);
                startActivity(appInfo);
            }
        });

        recordingLv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        importLv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        importLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String filename = gpsFiles.get(i);
                String filename = historyImportFiles.get(i).getFileName();
                //Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();
                Intent appInfo = new Intent(HistoryActivity.this, SelectionActivity.class);
                String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Imports/";
                String full = path + filename;
                appInfo.putExtra("filename", full);
                startActivity(appInfo);
            }
        });
    }

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions((Activity) HistoryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            HistoryAdapter mListAdapter = (HistoryAdapter) mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(0, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + 100 + (mListView.getDividerHeight() * (mListAdapter.getCount()-1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

    /*
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        HistoryAdapter listAdapter = (HistoryAdapter) listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
        listView.setLayoutParams(params);
    } */


    public String convertStringToUnderscore(String input)
    {
        String converted = input.replaceAll(" ", "_");
        return converted;
    }

    public String convertStringToSpaces(String input)
    {
        String converted = input;
        converted = converted.replaceAll("_", " ");

        return converted;
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

    public String removeGpxExtension(String input)
    {
            String reversed = new StringBuilder(input).reverse().toString();
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
        return normal;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.HistoryRecordingListView)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(historyRecordingFiles.get(info.position).displayName);
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

    public void renameFile(String menuItem, final int listPosition)
    {
        final String selectedName = historyRecordingFiles.get(listPosition).filename;
        final String displayName = historyRecordingFiles.get(listPosition).displayName;

        View view = (LayoutInflater.from(HistoryActivity.this)).inflate(R.layout.popup_rename_file, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(HistoryActivity.this);
        alert.setTitle("Rename File");
        alert.setView(view);
        final EditText userInput = (EditText) view.findViewById(R.id.userInput);
        userInput.setText(displayName);
        alert.setCancelable(true)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        renameTo = userInput.getText().toString();
                        String fullRenameTo = ensureGpxExtension(renameTo);
                        File file = new File(path + selectedName);
                        File renamed = new File(path + fullRenameTo);
                        Boolean flag = file.renameTo(renamed);
                        if (flag)
                        {
                            Log.e(TAG, selectedName + " has been renamed");
                            Toast.makeText(getApplicationContext(), selectedName + " renamed to: " + renameTo,Toast.LENGTH_SHORT).show();
                            HistoryFile historyFile = new HistoryFile(fullRenameTo, renameTo, historyRecordingFiles.get(listPosition).dateLastModified);
                            historyRecordingFiles.set(listPosition, historyFile);
                            updateList();
                        }
                        else
                        {
                            Log.e(TAG,"Error: " + selectedName + " rename to " + renameTo + " failed");
                        }


                    }

    });

        Dialog dialog = alert.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();


        Toast.makeText(getApplicationContext(), "Editing " + selectedName,Toast.LENGTH_SHORT).show();
    }

    public void deleteFile(String menuItem, int listPosition)
    {
        final String selectedName = historyRecordingFiles.get(listPosition).filename;

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

                        for (int j = 0; j < historyRecordingFiles.size(); j++)
                        {
                            if (historyRecordingFiles.get(j).filename.equals(selectedName))
                            {
                                historyRecordingFiles.remove(j);
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
        Collections.sort(historyRecordingFiles, new Comparator<HistoryFile>() {
            @Override
            public int compare(final HistoryFile hf1, final HistoryFile hf2) {
                return hf1.getDisplayName().compareTo(hf2.getDisplayName());
            }
        });
        Collections.sort(historyImportFiles, new Comparator<HistoryFile>() {
            @Override
            public int compare(final HistoryFile hf1, final HistoryFile hf2) {
                return hf1.getDisplayName().compareTo(hf2.getDisplayName());
            }
        });
    }

    public void updateList()
    {
        ((HistoryAdapter) recordingLv.getAdapter()).notifyDataSetChanged();
        ((HistoryAdapter) importLv.getAdapter()).notifyDataSetChanged();


        //setListViewHeightBasedOnChildren(recordingLv);
        //setListViewHeightBasedOnChildren(importLv);

        ListUtils.setDynamicHeight(recordingLv);
        ListUtils.setDynamicHeight(importLv);
    }

    public void getAllRecordingNames()
    {
        // User Recording GPX Files
        String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
        File[] files = new File(path).listFiles();

        showFiles(files, "Recordings/");
    }

    public void getAllImportNames()
    {
        // Imported GPX Files
        String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Imports/";
        File[] files = new File(path).listFiles();

        showFiles(files, "Imports/");
    }
    public void showFiles(File[] files, String location)
    {
        for (File file : files)
        {
            if(file.isDirectory())
            {
                showFiles(file.listFiles(), location);
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
                    //gpsFiles.add(file.getName());
                    HistoryFile hs;
                    HistoryFile historyFile;
                    String filename = file.getName();
                    String displayname = convertStringToSpaces(filename);
                    displayname = removeGpxExtension(displayname);

                    //Log.e(TAG,"displayname: " + displayname);
                    Date date = new Date();
                    String path = "/SkiStats/GPS/" + location;
                    String full = path + filename;
                    /*
                        Now I just need to loop through each file, get the date of the first track point and set to the date of the file.
                        takes too long
                     */
                    File file2 = new File(sdDir + full);
                    //long millisec;
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    if(file2.exists())
                    {
                        // too slow
                        //date = getDate(file.getAbsolutePath());
                    }
                    else
                    {
                        Log.e(TAG,"Error: Cannot find file: " + full);
                    }
                    Date fileDate = date;
                    String fileDateString = df.format(date).toString();
                    historyFile = new HistoryFile(filename, displayname, fileDate);
                    if (location.equals("Recordings/"))
                    {
                        historyRecordingFiles.add(historyFile);
                    }
                    else if (location.equals("Imports/"))
                    {
                        historyImportFiles.add(historyFile);
                    }
                }
            }
        }
    }

    public Date getDate(String filename) {
        Gpx parsedGpx = null;
        GPXParser mParser = new GPXParser();
        Date date = new Date();
        LatLng latlng;
        try {
            if (filename.charAt(0) == '/')
            {
                StringBuilder sb = new StringBuilder(filename);
                sb.deleteCharAt(0);
                filename = sb.toString();
            }
            //Log.e(TAG,"FilePath: " + filename);
            File file = new File(filename);
            InputStream in = new FileInputStream(file);
            parsedGpx = mParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        if (parsedGpx != null)
        {
            // Get first track date
            List<Track> tracks = parsedGpx.getTracks();
            Track track = tracks.get(0);
            List<TrackSegment> segments = track.getTrackSegments();
            TrackSegment segment = segments.get(0);
            date = segment.getTrackPoints().get(0).getTime().toDate();
            //Log.e(TAG,"Date: " + date.toString());
            return date;
        }
        return date;
    }

    public String ensureGpxExtension(String name)
    {
        String store = name;
        String gpx = ".gpx";
        // Reverse string to make file extension appear first
        String reverse = new StringBuilder(name).reverse().toString();
        String extension = "";
        for (int i = 0; i < 4; i++)
        {
            extension += reverse.charAt(i);
        }
        // Reverse file extension back
        extension = new StringBuilder(extension).reverse().toString();
        if (!extension.equals(".gpx"))
        {
            name += gpx;
        }
        return name;
    }
}