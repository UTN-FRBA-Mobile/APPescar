package com.appescar.appescar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;

public class DetallesPesca extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_pesca);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();

        TextView name = (TextView) findViewById(R.id.DETAILS_name);
        String fish = intent.getStringExtra("fish");
        name.setText(fish);

        TextView bait = (TextView) findViewById(R.id.DETAILS_bait);
        bait.setText(intent.getStringExtra("bait"));

        TextView line = (TextView) findViewById(R.id.DETAILS_line);
        line.setText(intent.getStringExtra("line"));

        TextView description = (TextView) findViewById(R.id.DETAILS_description);
        description.setText(intent.getStringExtra("description"));

        TextView timestamp = (TextView) findViewById(R.id.DETAILS_timestamp);
        timestamp.setText(intent.getStringExtra("tst"));

        ImageView image = (ImageView) findViewById(R.id.DETAILS_img);


        String imgname = intent.getStringExtra("imgname");

        if (imgname!="") {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://apppescar-e204f.appspot.com/");
            StorageReference imgRef = storageRef.child("pescas/"+imgname+".png");


            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(imgRef)
                    .into(image);

        }


        image.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intentimg = new Intent(DetallesPesca.this, FullPicActivity.class);
                intentimg.putExtra("imgname",intent.getStringExtra("imgname"));
                startActivity(intentimg);
            }
        });

    }

}
