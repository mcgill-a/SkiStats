package com.example.alexm.skistats;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.alexm.skistats.HistoryFile;
import com.example.alexm.skistats.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by AlexM on 10/03/2018.
 */

public class HistoryAdapter extends ArrayAdapter<HistoryFile> {

    Context context;
    int layoutResourceId;
    List<HistoryFile> historyFiles= null;

    public HistoryAdapter(Context context, int layoutResourceId, List<HistoryFile> historyFiles)
    {
        super(context, layoutResourceId, historyFiles);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.historyFiles = historyFiles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        HistoryFileHolder holder = null;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new HistoryFileHolder();
            holder.displayname = (TextView) row.findViewById(R.id.name);
            holder.date = (TextView) row.findViewById(R.id.date);

            row.setTag(holder);
        }
        else
        {
            holder = (HistoryFileHolder)row.getTag();
        }

        HistoryFile historyFile = historyFiles.get(position);
        holder.displayname.setText(historyFile.getDisplayName());
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        Date date = historyFile.getDateCreated();
        String dateString = df.format(date).toString();
        holder.date.setText(dateString);

        return row;
    }


    static class HistoryFileHolder
    {
        TextView displayname;
        TextView date;
    }

}
