package com.example.alexm.skistats;

/**
 * Created by AlexM on 04/03/2018.
 */

public class SkiVector {

    private double _distance;
    private double _height;

    public SkiVector()
    {

    }

    public void setDistance(double distance)
    {
        _distance = distance;
    }

    public void setHeight(double height)
    {
        _height = height;
    }

    public double getDistance()
    {
        return _distance;
    }

    public double getHeight()
    {
        return _height;
    }

    public String toString()
        {
            return "Distance: " + _distance + " | Height: " + _height;
        }
}
