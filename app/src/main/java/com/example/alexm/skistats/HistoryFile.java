package com.example.alexm.skistats;

import java.util.Date;

/**
 * Created by AlexM on 09/03/2018.
 */

public class HistoryFile {

    public String filename;
    public String displayName;
    public Date dateLastModified;

    public HistoryFile()
    {
        super();
    }

    public HistoryFile(String filename, String displayName, Date dateLastModified)
    {
        super();
        this.filename = filename;
        this.displayName = displayName;
        this.dateLastModified = dateLastModified;
    }

    public void setFileName(String input)
    {
        filename = input;
    }

    public void setDisplayName(String input)
    {
        displayName = input;
    }

    public void setDateLastModified(Date input)
    {
        dateLastModified = input;
    }

    public String getFileName()
    {
        return filename;
    }


    public String getDisplayName()
    {
        return displayName;
    }

    public Date getDateLastModified()
    {
        return dateLastModified;
    }
}

/*
private String _filename;
    private String _displayName;
    private Date _dateLastModified;

    public HistoryFile()
    {

    }

    public void setFileName(String input)
    {
        _filename = input;
    }

    public void setDisplayName(String input)
    {
        _displayName = input;
    }

    public void setDateLastModified(Date input)
    {
        _dateLastModified = input;
    }

    public String getFileName()
    {
        return _filename;
    }


    public String getDisplayName()
    {
        return _displayName;
    }

    public Date getDateLastModified()
    {
        return _dateLastModified;
    }
 */
