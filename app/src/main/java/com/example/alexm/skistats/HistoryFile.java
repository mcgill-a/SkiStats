package com.example.alexm.skistats;

import java.util.Date;

/**
 * Created by AlexM on 09/03/2018.
 */

public class HistoryFile {

    public String _filename;
    public String _displayName;
    public Date _dateCreated;

    public HistoryFile()
    {
        super();
    }

    public HistoryFile(String filename, String displayName, Date dateCreated)
    {
        super();
        this._filename = filename;
        this._displayName = displayName;
        this._dateCreated = dateCreated;
    }

    public void setDateCreated(Date input)
    {
        _dateCreated = input;
    }

    public String getFileName()
    {
        return _filename;
    }


    public String getDisplayName()
    {
        return _displayName;
    }

    public Date getDateCreated()
    {
        return _dateCreated;
    }
}