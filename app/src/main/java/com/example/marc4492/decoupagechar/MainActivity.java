package com.example.marc4492.decoupagechar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView t = (TextView) findViewById(R.id.bob);

        try {
            ImageDecoder imageDecoder = new ImageDecoder(0, 0, 0, 0, null, null);
            t.setText(imageDecoder.findSting(resize(BitmapFactory.decodeResource(getResources(), R.drawable.exposant), 200, 100)));
        }
        catch (IOException ex)
        {
            Toast.makeText(this, "erreur :'(", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resize(Bitmap bitmap, int width, int height) throws IOException
    {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }
}