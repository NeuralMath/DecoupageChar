package com.example.marc4492.decoupagechar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Bitmap> listChar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listChar = new ArrayList<>();

        try {
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.complete);

            splitChar(b);

            Toast.makeText(this, String.valueOf(listChar.size()), Toast.LENGTH_SHORT).show();

            for(int i = 0; i < listChar.size(); i++)
            {
                String path = Environment.getExternalStorageDirectory().getPath() + "/NeuralMath/";
                File f = new File(path + String.valueOf(i) + "_" + UUID.randomUUID().toString() + ".png");
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    listChar.get(i).compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                }catch (Exception e) {
                    Toast.makeText(this, "Oops! Image could not be saved.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (IOException ex)
        {
            Toast.makeText(this, "erreur :'(", Toast.LENGTH_SHORT).show();
        }
    }

    public void splitChar(Bitmap btm) throws IOException
    {
        MathChar mC = new MathChar(btm, 0, 0, btm.getWidth(), btm.getHeight());
        mC.getListChar().clear();
        mC.splitChar(true);
        for(MathChar mCInnerFirst : mC.getListChar())
            listChar.add(resize(fillImage(mCInnerFirst.getImage()), 45, 45));
    }

    /**
     * Changer la grandeur de l'image en gardant le ratio
     *
     * @param bitmap        L'image
     * @return              L'image resized
     * @throws IOException    S'il y a des problèmes
     */
    private Bitmap resize(Bitmap bitmap, int width, int height) throws IOException
    {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    /**
     * Get un array une dimension de la valeur binaire des pixels d'une iimage
     *
     * @param bitmap        L'image
     * @return              Un tableau de int selon les pixels(1 ou 0)
     * @throws Exception    S'il y a des problèmes
     */
    private int[]getIOPixels(Bitmap bitmap) throws Exception
    {
        ArrayList<Integer> pixels = new ArrayList<>();

        //Selon la valeur du pixel, 1 ou 0
        int pixel;
        for(int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                pixel = bitmap.getPixel(i, j);
                if(Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384)
                    pixels.add(1);
                else
                    pixels.add(0);
            }
        }

        //Transformation en array de int
        int[] inputValues = new int[bitmap.getWidth()*bitmap.getHeight()];
        for(int i = 0; i < pixels.size(); i++)
            inputValues[i] = pixels.get(i);

        return inputValues;
    }

    public Bitmap fillImage(Bitmap btm)
    {
        int width = btm.getWidth();
        int height = btm.getHeight();

        int borderSize = 5;

        Bitmap newImage;
        Canvas canvas;

        if(width < height)
        {
            newImage = Bitmap.createBitmap(height + 2*borderSize, height + 2*borderSize, btm.getConfig());
            canvas = new Canvas(newImage);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(btm, (newImage.getWidth()-width)/2, borderSize, null);
            return newImage;
        }
        else if (height < width)
        {
            newImage = Bitmap.createBitmap(width + 2*borderSize, width + 2*borderSize, btm.getConfig());
            canvas = new Canvas(newImage);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(btm, borderSize, (newImage.getHeight()-height)/2, null);
            return newImage;
        }
        else
        {
            newImage = Bitmap.createBitmap(width + 2*borderSize, height + 2*borderSize, btm.getConfig());
            canvas = new Canvas(newImage);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(btm, borderSize, borderSize, null);
            return newImage;
        }
    }
}