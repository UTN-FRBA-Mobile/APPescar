package com.appescar.appescar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;

public class FormAdd extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button uploadImageButton = (Button)findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "@string/pick_photo"), PICK_IMAGE);
            }
        });
        Button FormAddSaveButton = (Button)findViewById(R.id.FormAddSaveButton);
        FormAddSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ImageView imageUploadPreview = (ImageView) findViewById(R.id.imageUploadPreview);
                String base64 = "";
                if (imageUploadPreview != null && imageUploadPreview.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) imageUploadPreview.getDrawable()).getBitmap();
                    if (bitmap != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                        byte[] image = stream.toByteArray();
                        base64 = Base64.encodeToString(image, 0);
                    }
                }
                Log.d(base64, base64);
            }

        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FormAdd.super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Picasso.with(this).load(data.getData()).noPlaceholder().centerCrop().fit().into((ImageView) findViewById(R.id.imageUploadPreview));
            } else {
                // Mostrar error - No se pudo obtener la imagen
            }
        }
    }
}
