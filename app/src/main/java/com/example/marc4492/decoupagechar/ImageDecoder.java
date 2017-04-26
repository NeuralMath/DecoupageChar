package com.example.marc4492.decoupagechar;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class qui contient le réseau de neurones et qui obtient l'équation en string
 *
 * @author Marc4492
 * 10 février 2017
 */

public class ImageDecoder {
    private ArrayList<MathChar> listChar;
    private String[] charList;

    private int totalWidth;
    private int totalHeight;

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
    public ImageDecoder(final int input, final int hidden, final int output, final double training, final SQLiteDatabase database, String[] charListing) throws IOException {
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
        int index = 0;
        //Add le char dans l'eq
        for (int i = 0; i < listChar.size(); i++) {
            MathChar firstVSplitChar = listChar.get(i);

            //mC : chaque horizontal dans valTemp
            for (int j = 0; j < firstVSplitChar.getListInner().size(); j++) {
                MathChar hSplitFromFirstV = firstVSplitChar.getListInner().get(j);
                //Juste un à l'horizontal
                if(hSplitFromFirstV.getListInner().size() == 0)
                {
                    hSplitFromFirstV.setValue(String.valueOf(index));
                    index++;

                    if(i > 0) {
                        //get 0 cause probleme
                        //height pas bonne idée
                        if (hSplitFromFirstV.getHeight() <= listChar.get(i-1).getListInner().get(0).getHeight()) {
                            if(line.charAt(line.length()-1) == ')')
                                line = line.substring(0, line.length()-1) + hSplitFromFirstV.getValue() + ")";
                            else
                                line += "^(" + hSplitFromFirstV.getValue() + ")";
                        }
                    }
                    else
                        line += hSplitFromFirstV.getValue();
                }
                else {
                    //Toute les verticals de l'horizontal
                    for (int k = 0; k < hSplitFromFirstV.getListInner().size(); k++) {
                        MathChar vSplitFromHFromV = hSplitFromFirstV.getListInner().get(k);

                        if (vSplitFromHFromV.getListInner().size() == 0) {
                            vSplitFromHFromV.setValue(String.valueOf(index));
                            index++;
                            //EXP et non fraction (voir cas speciaux exp multiple)
                            if (firstVSplitChar.getListInner().size() < 3) {
                                if (j + 1 < firstVSplitChar.getListInner().size()) {
                                    firstVSplitChar.getListInner().get(j + 1).setValue(String.valueOf(index));
                                    index++;
                                    j++;

                                    if(vSplitFromHFromV.getXStart() > firstVSplitChar.getListInner().get(j).getXStart())
                                        line += firstVSplitChar.getListInner().get(j).getValue() + "^(" + vSplitFromHFromV.getValue() + ")";
                                    else
                                        line += vSplitFromHFromV.getValue() + "_(" + firstVSplitChar.getListInner().get(j).getValue() + ")";
                                }
                            }
                            else
                            {
                                //TO DO check si fration ou x_()^()
                            }
                        }
                    }
                }
            }
        }
        //line += replaceChar();

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

    public String replaceChar()
    {

        //TO DO
        //Checker avec Godefroy pour les x_1, x_2
        //Check si exposant contient plus qu'un char


        //Tolerance de 5%
        final int toleranceHeight = (int) (totalHeight *0.05);
        final int toleranceWidth = (int) (totalWidth*0.05);
        String buildedEq = listChar.get(0).getValue();

        //get le premier char
        for(int i = 1; i < listChar.size()-1; i++) {

            //Donc un apres lautre
            /*if (listChar.get(i).getXEnd() < listChar.get(i + 1).getXStart()) {
                //S'il sont assez proche pour etre des exp/ind
                if (listChar.get(i + 1).getXStart() - listChar.get(i).getXEnd() < toleranceWidth) {
                    //TO DO
                    //Insert tolerance
                    if (listChar.get(i + 1).getYStart() < listChar.get(i).getYStart()) {
                        buildedEq += listChar.get(i).getValue() + "^(" + listChar.get(i + 1).getValue() + ")";
                    } else if (listChar.get(i + 1).getYStart() > listChar.get(i).getYStart()) {
                        buildedEq += listChar.get(i).getValue() + "_(" + listChar.get(i + 1).getValue() + ")";
                    }
                } else {
                    //Si c'est l'avant dernier, ajouter les deux
                    if(i+2 == listChar.size())
                        buildedEq += listChar.get(i).getValue() + "," + listChar.get(i+1).getValue();
                    else
                        buildedEq += listChar.get(i).getValue();
                }
            }
            //Donc un par dessus l'autre
            else {
                if (listChar.get(i + 1).getXStart() < listChar.get(i).getXStart()) {
                    if (listChar.get(i + 1).getYStart() > listChar.get(i).getYStart()) {
                        buildedEq += listChar.get(i + 1).getValue() + "^(" + listChar.get(i).getValue() + ")";
                    }
                } else if (listChar.get(i + 1).getXStart() > listChar.get(i).getXStart()) {
                    if (listChar.get(i + 1).getYStart() > listChar.get(i).getYStart()) {
                        buildedEq += listChar.get(i).getValue() + "_(" + listChar.get(i + 1).getValue() + ")";
                    }
                }*/


            if (listChar.get(i).getTop() != null) {
                String temp = listChar.get(i).getValue() + "^(" + listChar.get(i).getTop().getValue();
                while (listChar.get(i).getTop().getRight() != null) {
                    temp += listChar.get(i).getTop().getRight().getValue();
                    i++;
                }
                temp += ")";
                buildedEq += temp;
            } else if (listChar.get(i).getRight() != null)
                buildedEq += listChar.get(i).getRight().getValue();


        }


        /*{
            //Litteralement char2 dessus char
            if(listChar.get(i-1).getXStart() - toleranceWidth < listChar.get(i).getXStart() && listChar.get(i-1).getXStart() + toleranceWidth > listChar.get(i).getXStart())
            {
                buildedEq += "(" + listChar.get(i).getValue() + "," + listChar.get(i-1).getValue() + ")";
            }
            //Si char2 est par dessus char mais pas au complet
            else if(listChar.get(i-1).getXStart() + listChar.get(i-1).getWidth() + toleranceWidth > listChar.get(i).getXStart() - toleranceWidth)
            {
                //TO DO
                //Fraction

                if(listChar.get(i-1).getYStart() < listChar.get(i).getYStart())
                    buildedEq += "_(" + listChar.get(i).getValue() + ")";
                else
                    buildedEq += "^(" + listChar.get(i).getValue() + ")";
            }
            //Un a cote de lautre
            if(listChar.get(i-1).getXStart() + listChar.get(i-1).getWidth() + toleranceWidth < listChar.get(i).getXStart() - toleranceWidth)
            {
                buildedEq += listChar.get(i).getValue();
            }
        }*/

        //TO DO
        //Call post treatment
        return buildedEq;
    }
}