package com.example.marc4492.decoupagechar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class qui contient le réseau de neurones et qui obtient l'équation en string
 *
 * @author Marc4492
 * 10 février 2017
 */

public class ImageDecoder {
    private ArrayList<MathChar> listChar;
    private String[] charList;
    private int index = 0;

    private int totalWidth;
    private int totalHeight;

    private Context context;

    /**
     * Contructeur qui initialise le réseau
     *
     * @param input       Nombre de neurones d'input dans le réseau
     * @param hidden      Nombre de neurones de hidden dans le réseau
     * @param output      Nombre de neurones d'output dans le réseau
     * @param training    Training rate du reseau
     * @param charListing List des char avec leur index dans le réseau
     *
     * @throws IOException S'il y a des problèmes de fichier, ...
     */
    public ImageDecoder(Context c, final int input, final int hidden, final int output, final double training, final SQLiteDatabase database, String[] charListing) throws IOException {
        context = c;

        listChar = new ArrayList<>();
        charList = charListing;
    }

    //IL NE FAUT PAS QUE LES CARACTERES TOUCHE AU BORD DE LA PHOTO PLZ CA VA ETRE PLUS SIMPLE
    /**
     * Obtient l'équation en string
     *
     * @param btm L'image à décoder
     * @return l'équation en String
     * @throws IOException S'il y a des problème avec l'image
     */
    public String findSting(Bitmap btm) throws IOException {
        totalHeight = btm.getHeight();
        totalWidth = btm.getWidth();

        listChar.clear();
        String line = "";

        //Split tous les chars
        splitChar(btm);

        Collections.sort(listChar, new Comparator<MathChar>() {
            @Override
            public int compare(MathChar mathChar, MathChar t1) {
                return mathChar.getXStart() - t1.getXStart();
            }
        });

        final int toleranceHeight = (int) (totalHeight *0.1);
        final int toleranceWidth = (int) (totalWidth*0.1);
        boolean notCheckingLast = false;
        int indexToLook = 0;

        for(int i = 0; i < listChar.size(); i++)
            listChar.get(i).setValue(String.valueOf(i));

        line += listChar.get(0).getValue();

        for(index = 1; index < listChar.size(); index++)
        {
            if(!notCheckingLast)
                indexToLook = index - 1;

            //Si un à côté de l'autre
            if(Math.abs(listChar.get(indexToLook).getYEnd() - listChar.get(index).getYEnd()) <= toleranceHeight ) {
                notCheckingLast = false;
                line += listChar.get(index).getValue();
            }

            //Si un exposant
            else if(listChar.get(index).getYEnd() <= listChar.get(indexToLook).getYMiddle()) {
                notCheckingLast = true;
                line = findExposant(line, toleranceHeight);
            }

            //Si un indice
            else if(listChar.get(index).getYStart() > listChar.get(indexToLook).getYMiddle()) {
                notCheckingLast = true;
                line = findIndice(line, toleranceHeight);
            }
            else
                Toast.makeText(context, "Le découpage de caractère ne s'est pas déroulé normalement", Toast.LENGTH_LONG).show();
        }

        return line;
    }

    /**
     * Split les différents caractère de l'image
     *
     * @param btm               L'image à analyser
     * @throws IOException        S'il y a des problèmes
     */
    private void splitChar(Bitmap btm) throws IOException
    {
        MathChar mC = new MathChar(btm, 0, 0, btm.getWidth(), btm.getHeight());
        mC.splitChar(true);
        listChar.addAll(mC.getStaticList());
    }


    public String findExposant(String line,  int toleranceHeight)
    {
        line += "^(" + listChar.get(index).getValue();
        if(index < listChar.size()-1) {
            index++;
            while (index < listChar.size()) {
                if(Math.abs(listChar.get(index - 1).getYEnd() - listChar.get(index).getYEnd()) <= toleranceHeight)
                    line += listChar.get(index).getValue();
                else if(listChar.get(index).getYEnd() <= listChar.get(index-1).getYMiddle())
                    line = findExposant(line, toleranceHeight);
                else {
                    index--;
                    break;
                }
                index++;
            }
        }
        line += ")";

        return line;
    }

    public String findIndice(String line,  int toleranceHeight)
    {
        line += "_(" + listChar.get(index).getValue();
        if(index < listChar.size()-1) {
            index++;
            while (index < listChar.size()) {
                if(Math.abs(listChar.get(index - 1).getYEnd() - listChar.get(index).getYEnd()) <= toleranceHeight)
                    line += listChar.get(index).getValue();
                else if(listChar.get(index).getYStart() > listChar.get(index -1).getYMiddle())
                    line = findIndice(line, toleranceHeight);
                else {
                    index--;
                    break;
                }
                index++;
            }
        }
        line += ")";

        return line;
    }

    /**
     * Get un array une dimension de la valeur binaire des pixels d'une iimage
     *
     * @param bitmap        L'image
     * @return              Un tableau de int selon les pixels(1 ou 0)
     * @throws IOException    S'il y a des problèmes
     */
    private int[]getIOPixels(Bitmap bitmap) throws IOException
    {
        ArrayList<Integer> pixels = new ArrayList<>();

        //Selon la valeur du pixel, 1 ou 0
        int pixel;
        for(int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                pixel = bitmap.getPixel(i, j);
                if(Color.red(pixel) < 0x0C && Color.green(pixel) < 0x0C && Color.blue(pixel) < 0x0C)
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

    private Bitmap fillImage(Bitmap btm)
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