package com.appescar.appescar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class FullPicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_pic);
        ImageView image = (ImageView) findViewById(R.id.FULL_img);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(this.openFileInput("myImage"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        image.setImageBitmap(bitmap);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
