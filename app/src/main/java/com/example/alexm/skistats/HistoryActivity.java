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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView recordingLv;
    private ListView importLv;
    private List<HistoryFile> historyRecordingFiles = new ArrayList<>();
    private List<HistoryFile> historyImportFiles = new ArrayList<>();
    private File sdDir = Environment.getExternalStorageDirectory();
    private String pathRecord = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
    private String pathImport = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Imports/";
    private String TAG = "SkiStats.Log";
    private String renameTo = "";

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1001;
    private final int REC_RENAME = 0;
    private final int REC_DELETE = 1;
    private final int IMP_RENAME = 2;
    private final int IMP_DELETE = 3;

    public void gpxReadFailed()
    {
        Log.e(TAG, "Error parsing gpx file");
        Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Make sure correct permissions are granted
        if (!checkPermissionForReadExtertalStorage())
        {
            requestPermissionForReadExternalStorage();
        }
        if (!checkPermissionForReadExtertalStorage())
        {
            finish();
        }
        recordingLv = (ListView) findViewById(R.id.HistoryRecordingListView);
        importLv = (ListView) findViewById(R.id.HistoryImportListView);

        // Check permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);
        } else {

            // If permission granted


            getAllRecordingNames();
            getAllImportNames();
            sortListsAlphabetically();

        }
        // Custom History Adapter for displaying custom list view row and the displayname from the arraylist
        HistoryAdapter recordingAdapter = new HistoryAdapter(this, R.layout.history_list_row, historyRecordingFiles);
        HistoryAdapter importAdapter = new HistoryAdapter(this, R.layout.history_list_row, historyImportFiles);
        recordingLv.setAdapter(recordingAdapter);
        importLv.setAdapter(importAdapter);

        // Set the height of the listview relative to the amount of list items
        ListUtils.setDynamicHeight(recordingLv);
        ListUtils.setDynamicHeight(importLv);

        // Add on hold for recording history list
        registerForContextMenu(recordingLv);

        updateList();
        recordingLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String filename = historyRecordingFiles.get(i).getFileName();
                Intent appInfo = new Intent(HistoryActivity.this, SelectionActivity.class);
                String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
                String full = path + filename;
                // Add the selected filename to the intent and start the activity
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
        // Add on hold for import history list
        registerForContextMenu(importLv);
        importLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String filename = historyImportFiles.get(i).getFileName();
                Intent appInfo = new Intent(HistoryActivity.this, SelectionActivity.class);
                String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Imports/";
                String full = path + filename;
                // Add the selected filename to the intent and start the activity
                appInfo.putExtra("filename", full);
                startActivity(appInfo);
            }
        });
        getCorrectRecordingFileDates();
        getCorrectImportFileDates();
        updateList();
    }

    // Get the date using the <time> value stored in each .gpx file
    private void getCorrectRecordingFileDates()
    {
        Date date;
        for (int i = 0; i < historyRecordingFiles.size(); i++)
        {
            String name = historyRecordingFiles.get(i).getFileName();
            String directory = sdDir + "/" +  "SkiStats/GPS/Recordings/";
            String absPath = directory + name;
            // Once the <time> value is found stop reading the file (that's why it's quick)
            date = getDateQuick(absPath);
            historyRecordingFiles.get(i).setDateCreated(date);
        }
    }

    // Get the date using the <time> value stored in each .gpx file
    private void getCorrectImportFileDates()
    {
        Date date;
        for (int i = 0; i < historyImportFiles.size(); i++)
        {
            String name = historyImportFiles.get(i).getFileName();
            String directory = sdDir + "/" +  "SkiStats/GPS/Imports/";
            String absPath = directory + name;
            // Once the <time> value is found stop reading the file (that's why it's quick)
            date = getDateQuick(absPath);
            historyImportFiles.get(i).setDateCreated(date);
        }
    }

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExternalStorage() {
        try {
            ActivityCompat.requestPermissions((Activity) HistoryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Calculate what the height should be for the list view depending on how many items there are and the custom row line height
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
            // Adding extra 100 onto height to ensure that list is always visible, regardless if empty.
            // Also fits the screen better when populated
            params.height = height + 100 + (mListView.getDividerHeight() * (mListAdapter.getCount()-1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

    // Convert any underscores in a string to a space. Used for display name so lists look nicer
    public String convertStringToSpaces(String input)
    {
        String converted = input;
        converted = converted.replaceAll("_", " ");
        return converted;
    }

    // Remove the .gpx extension from the end of a filename (ex. test.gpx = test)
    public String removeGpxExtension(String input)
    {
        // Reverse string it is definitely at the end of the string
        String reversed = new StringBuilder(input).reverse().toString();
            StringBuilder sb = new StringBuilder(reversed);
            int j = 0;
            // Until it reaches the . (start of extension) remove the character
            while (sb.charAt(j) != '.')
            {
                sb.deleteCharAt(j);
            }
            // When it gets to the . then also delete the .
            if(sb.charAt(j) == '.')
            {
                sb.deleteCharAt(j);
            }
            // Reverse the string again back to its normal form
            String normal = new StringBuilder(sb.toString()).reverse().toString();
        return normal;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        switch(v.getId())
        {
            // On hold context menus for each history list
            case R.id.HistoryRecordingListView:
                menu.setHeaderTitle(historyRecordingFiles.get(info.position).getDisplayName());
                menu.add(Menu.NONE, REC_RENAME, 0, "Rename");
                menu.add(Menu.NONE, REC_DELETE, 1, "Delete");
                break;

            case R.id.HistoryImportListView:
                menu.setHeaderTitle(historyImportFiles.get(info.position).getDisplayName());
                menu.add(Menu.NONE, IMP_RENAME, 0, "Rename");
                menu.add(Menu.NONE, IMP_DELETE, 1, "Delete");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String menuItemName = "";
        switch (menuItemIndex)
        {
            // Call either the rename or delete method, passing in the Recording or Import flag so it knows what logic to use
            case REC_RENAME:
                menuItemName = "Rename";
                renameAFile(menuItemName, info.position, "Recordings");
                break;
            case REC_DELETE:
                menuItemName = "Delete";
                deleteAFile(menuItemName, info.position, "Recordings");
                break;
            case IMP_RENAME:
                menuItemName = "Rename";
                renameAFile(menuItemName, info.position, "Imports");
                break;
            case IMP_DELETE:
                menuItemName = "Delete";
                deleteAFile(menuItemName, info.position, "Imports");
                break;
        }
        return true;
    }

    public void renameAFile(String menuItem, final int listPosition, final String list)
    {
        final String selectedName;
        final String displayName;
        if (list.equals("Recordings"))
        {
            selectedName = historyRecordingFiles.get(listPosition).getFileName();
            displayName = historyRecordingFiles.get(listPosition).getDisplayName();
        }
        else
        {
            selectedName = historyImportFiles.get(listPosition).getFileName();
            displayName = historyImportFiles.get(listPosition).getDisplayName();
        }
        // Create a view to show the popup window for renaming a file
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
                        // Ensure it remains a .gpx file by adding the .gpx extension if not present
                        String fullRenameTo = ensureGpxExtension(renameTo);
                        File file;
                        File renamed;
                        if (list.equals("Recordings"))
                        {
                            file = new File(pathRecord + selectedName);
                            renamed = new File(pathRecord + fullRenameTo);
                        }
                        else
                        {
                            file = new File(pathImport + selectedName);
                            renamed = new File(pathImport + fullRenameTo);
                        }
                        if(renamed.exists())
                        {
                            Toast.makeText(getApplicationContext(),"A file with that name already exists",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Boolean flag = file.renameTo(renamed);
                            if (flag)
                            {
                                Log.e(TAG, selectedName + " has been renamed");
                                Toast.makeText(getApplicationContext(), selectedName + " renamed to: " + renameTo,Toast.LENGTH_SHORT).show();
                                if (list.equals("Recordings"))
                                {
                                    HistoryFile historyFile = new HistoryFile(fullRenameTo, renameTo, historyRecordingFiles.get(listPosition).getDateCreated());
                                    historyRecordingFiles.set(listPosition, historyFile);
                                }
                                else
                                {
                                    HistoryFile historyFile = new HistoryFile(fullRenameTo, renameTo, historyImportFiles.get(listPosition).getDateCreated());
                                    historyImportFiles.set(listPosition, historyFile);
                                }
                            }
                            else
                            {
                                Log.e(TAG,"Error: " + selectedName + " rename to " + renameTo + " failed");
                            }
                            sortListsAlphabetically();
                            updateList();
                        }
                    }
                });
        // Set the keyboard to open automatically, show the dialog
        Dialog dialog = alert.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        Toast.makeText(getApplicationContext(), "Editing " + selectedName,Toast.LENGTH_SHORT).show();
    }

    public void deleteAFile(String menuItem, int listPosition, final String list)
    {
        final String selectedName;
        if (list.equals("Recordings"))
        {
            selectedName = historyRecordingFiles.get(listPosition).getFileName();
        }
        else
        {
            selectedName = historyImportFiles.get(listPosition).getFileName();
        }


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
                        Log.e(TAG, "Deleting recording: " + selectedName);
                        File dir;
                        String fullFileName = selectedName;
                        String fullnamePath;
                        if (list.equals("Recordings"))
                        {
                            dir = new File(pathRecord);
                            fullnamePath = pathRecord + fullFileName;
                        }
                        else
                        {
                            dir = new File(pathImport);
                            fullnamePath = pathImport + fullFileName;
                        }



                        File recording = new File(dir, fullFileName);
                        Log.e(TAG,"Full path to remove: " + fullnamePath);

                        recording.delete();
                        if(recording.exists())
                        {
                            getApplicationContext().deleteFile(recording.getName());
                        }
                        // To ensure the file is removed on Windows Operating System also
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fullnamePath}, null, null);

                        if (list.equals("Recordings"))
                        {
                            for (int j = 0; j < historyRecordingFiles.size(); j++)
                            {
                                if (historyRecordingFiles.get(j).getFileName().equals(selectedName))
                                {
                                    historyRecordingFiles.remove(j);
                                }
                            }
                        }
                        else
                        {
                            for (int j = 0; j < historyImportFiles.size(); j++)
                            {
                                if (historyImportFiles.get(j).getFileName().equals(selectedName))
                                {
                                    historyImportFiles.remove(j);
                                }
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
    }

    // Sort each history list alphabetically using their display names (what people see on the list views)
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

    // Tell the HistoryAdapter that the data set has changed - refreshes the listviews
    public void updateList()
    {
        ((HistoryAdapter) recordingLv.getAdapter()).notifyDataSetChanged();
        ((HistoryAdapter) importLv.getAdapter()).notifyDataSetChanged();

        // Set the height again to fit with current data set (possibly less/more than before)
        ListUtils.setDynamicHeight(recordingLv);
        ListUtils.setDynamicHeight(importLv);
    }

    public void getAllRecordingNames()
    {
        // User Recording GPX Files
        String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
        File[] files = new File(path).listFiles();

        addGpxFiles(files, "Recordings/");
    }

    public void getAllImportNames()
    {
        // Imported GPX Files
        String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Imports/";
        File[] files = new File(path).listFiles();

        addGpxFiles(files, "Imports/");
    }

    // Add each of the GPX files returned in the recordings folder and import folder
    public void addGpxFiles(File[] files, String location)
    {
        for (File file : files)
        {
            // Recursion :) Get all files in the sub-directory using the same method
            if(file.isDirectory())
            {
                addGpxFiles(file.listFiles(), location);
            }
            else
            {
                // Reverse string to make file extension appear first
                String reverse = new StringBuilder(file.getName()).reverse().toString();
                String extension = "";
                // Only 4 characters because it should be .gpx which is (length 4)
                for (int i = 0; i < 4; i++)
                {
                    extension += reverse.charAt(i);
                }
                // Reverse file extension back
                extension = new StringBuilder(extension).reverse().toString();
                if (extension.equals(".gpx"));
                {
                    HistoryFile historyFile;
                    String filename = file.getName();
                    String displayname = convertStringToSpaces(filename);
                    displayname = removeGpxExtension(displayname);

                    Date date = new Date();
                    Date fileDate = date;
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
    // It's called quick because the alternative method read the entire gpx file - not just until it found the 2nd <time> flag
    // and its v quick
    public Date getDateQuick(String filename) {
        DateFormat longdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        boolean found = false;
        String time = "";
        try {
            // If a / was carried over accidently in the filepath, remove it
            if (filename.charAt(0) == '/')
            {
                StringBuilder sb = new StringBuilder(filename);
                sb.deleteCharAt(0);
                filename = sb.toString();
            }
            // Read the file line by line
            BufferedReader br=new BufferedReader(
                    new FileReader(new File(filename)));
            String line;
            String text = "";
            int count = 0;
            try
            {
                while ((line=br.readLine())!=null && !found)
                {
                    if(line.contains("<time>"))
                    {
                        // If it is the 2nd occurrence of <time> being found
                        if (count == 1)
                        {
                            String cropped = line;
                            StringBuilder croppedSb = new StringBuilder(line);
                            // <time> is on the line somewhere - delete the characters until starts with <time>
                            while(!cropped.startsWith("<time>"))
                            {
                                croppedSb.deleteCharAt(0);
                                cropped = croppedSb.toString();
                            }
                            // Split the string each time there is a >
                            String[] splits = cropped.split(">");
                            // Get the 2nd value from the split string (ex. <time>FROM HERE ONWARDS</time>
                            String[] cut = splits[1].split("<");
                            // Split the string each time there is a <
                            // Set time equal to the first split anything before the closing tag <time> starts
                            time = cut[0];
                            // Convert
                            Date temp = longdf.parse(time);
                            String tempString = df.format(temp);
                            Date created = df.parse(tempString);
                            found = true;
                            return created;
                        }
                        count++;
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(TAG,"Error: Parsing date failed for: " + filename);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return date;
    }

    // If doesn't already have gpx extension, add one
    public String ensureGpxExtension(String name)
    {
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