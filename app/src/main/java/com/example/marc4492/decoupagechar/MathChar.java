package com.example.marc4492.decoupagechar;

import android.graphics.Bitmap;

public class MathChar {
    private Bitmap image;
    private String place;
    private int xStart = 0;
    private int yStart = 0;

    public MathChar()
    {
        place = "nor";
    }

    public MathChar(Bitmap b, int x, int y)
    {
        image = b;
        place = "nor";
        xStart = x;
        yStart = y;
    }

    public MathChar(Bitmap b, String s)
    {
        image = b;
        place = s;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap b) {
        image = b;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String s) {
        place = s;
    }
}
