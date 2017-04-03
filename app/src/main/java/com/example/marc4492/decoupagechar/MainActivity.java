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
    private int heightOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listChar = new ArrayList<>();

        try {
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.t_4_digits);
            heightOrigin = b.getHeight();

            splitVertical(b);

            ArrayList<Bitmap> listCharTemp = new ArrayList<>(listChar);
            listChar.clear();

            for(int i = 0; i < listCharTemp.size(); i++)
                splitHorizontal(listCharTemp.get(i));

            /*listCharTemp = new ArrayList<>(listChar);
            listChar.clear();

            for(int i = 0; i < listCharTemp.size(); i++)
                splitVertical(listCharTemp.get(i));

            listCharTemp = new ArrayList<>(listChar);
            listChar.clear();

            for(int i = 0; i < listCharTemp.size(); i++)
                splitHorizontal(listCharTemp.get(i));*/

            Toast.makeText(this, String.valueOf(listChar.size()), Toast.LENGTH_SHORT).show();

            for(int i = 0; i < listChar.size(); i++)
            {
                System.out.println(checkDone(listChar.get(i)) + "-------------------------");
                String path = Environment.getExternalStorageDirectory().getPath() + "/NeuralMath/";
                File f = new File(path + String.valueOf(i) + "_" + UUID.randomUUID().toString() + ".png");
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    listChar.get(i).compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                }catch (Exception e){
                    Toast.makeText(this, "Oops! Image could not be saved.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (IOException ex)
        {
            Toast.makeText(this, "erreur :'(", Toast.LENGTH_SHORT).show();
        }
    }












    /**
     * Split les différents caractère de l'image de facon vertical
     *
     * @param btm               L'image à analyser
     * @throws IOException        S'il y a des problèmes
     */
    private void splitVertical(Bitmap btm) throws IOException
    {
        ArrayList<Integer> listBlack = new ArrayList<>();
        int pixel;

        //Check chaque colonne pour voir si elle est blanche : check si + noir que blanc
        for(int i = 0; i < btm.getWidth(); i++) {
            for (int j = 0; j < btm.getHeight(); j++) {
                pixel = btm.getPixel(i, j);
                if (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384) {
                    listBlack.add(i);
                    break;
                }
            }
        }

        //Si l'image n'est pas toute noir
        if(listBlack.size() > 0 && listBlack.size() < btm.getWidth()-1)
        {
            int start = listBlack.get(0);
            int i = 1;
            //Passer au travers de toutes les lignes
            while(i < listBlack.size())
            {
                //Trouver le end du char
                while(i < listBlack.size() && listBlack.get(i) - listBlack.get(i-1) == 1)
                    i++;

                listChar.add(crop(btm, start, 0, listBlack.get(i-1), btm.getHeight()));
                if(i < listBlack.size())
                    start = listBlack.get(i);
                i++;
            }
        }
        else if(listBlack.size() == btm.getWidth()-1)
            return;
    }

    /**
     * Split les différents caractère de l'image de facon horizontal
     *
     * @param btm               L'image à analyser
     * @throws IOException        S'il y a des problèmes
     */
    private void splitHorizontal(Bitmap btm) throws IOException
    {
        ArrayList<Integer> listBlack = new ArrayList<>();
        int pixel;

        //Check chaque colonne pour voir si elle est blanche : check chaque couleurs pour les val hex
        for(int i = 0; i < btm.getHeight() ; i++) {
            for (int j = 0; j < btm.getWidth(); j++) {
                pixel = btm.getPixel(j, i);
                if(Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384)
                {
                    listBlack.add(i);
                    break;
                }
            }
        }

        //Si l'image n'est pas toute noir
        if(listBlack.size() > 0 && listBlack.size() < btm.getHeight()-1)
        {
            int start = listBlack.get(0);
            int i = 1;
            //Passer au travers de toutes les lignes
            while(i < listBlack.size())
            {
                //Trouver le end du char
                while(i < listBlack.size() && listBlack.get(i) - listBlack.get(i-1) == 1)
                    i++;

                listChar.add(crop(btm, 0, start, btm.getWidth(), listBlack.get(i-1)));
                if(i < listBlack.size())
                    start = listBlack.get(i);
                i++;
            }
        }
        else if(listBlack.size() == btm.getHeight()-1)
            return;
    }







    public boolean checkDone(Bitmap btm) {

        boolean top = false, bottom = false, right = false, left = false;
        int pixel;

        for (int i = 0; i < btm.getWidth(); i++) {
            pixel = btm.getPixel(i, 0);
            if (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384) {
                top = true;
                break;
            }
        }

        for (int i = 0; i < btm.getWidth(); i++) {
            pixel = btm.getPixel(i, btm.getHeight() - 1);
            if (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384) {
                bottom = true;
                break;
            }
        }

        for (int i = 0; i < btm.getHeight(); i++) {
            pixel = btm.getPixel(0, i);
            if (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384) {
                left = true;
                break;
            }
        }

        for (int i = 0; i < btm.getHeight(); i++) {
            pixel = btm.getPixel(btm.getWidth() - 1, i);
            if (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) <= 384) {
                right = true;
                break;
            }
        }

        return top && bottom && right && left;
    }









    /**
     * Rogner l'image selon les paramètre
     *
     * @param bitmap        L'image
     * @return              L'image rogner
     * @throws IOException    S'il y a des problèmes
     */
    private Bitmap crop(Bitmap bitmap, int startX, int startY, int endX, int endY) throws IOException
    {
        return Bitmap.createBitmap(bitmap, startX, startY, endX-startX, endY-startY);
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

    public String checkPlace(int up, int down)
    {
        // < et - puisque en haut c'est 0
        if(down < heightOrigin/2 - heightOrigin/16)
            return "exp";
        else if(up > heightOrigin/2 + heightOrigin/16)
            return "ind";
        else
            return "nor";
    }

    public Bitmap fillImage(Bitmap btm)
    {
        int width = btm.getWidth();
        int height = btm.getHeight();

        int borderSize = 5;

        if(width < height)
        {
            Bitmap newImage = Bitmap.createBitmap(height + 2*borderSize, height + 2*borderSize, btm.getConfig());
            Canvas canvas = new Canvas(newImage);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(btm, (newImage.getWidth()-width)/2, borderSize, null);
            return newImage;
        }
        else if (height < width)
        {
            Bitmap newImage = Bitmap.createBitmap(width + 2*borderSize, width + 2*borderSize, btm.getConfig());
            Canvas canvas = new Canvas(newImage);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(btm, borderSize, (newImage.getHeight()-height)/2, null);
            return newImage;
        }
        else
            return btm;
    }
}