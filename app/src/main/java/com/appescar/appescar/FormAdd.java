package com.appescar.appescar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;

public class FormAdd extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    public DatabaseReference refDatabase;

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
                String FormAddTipoPez = ((Spinner) findViewById(R.id.FormAddTipoPez)).getSelectedItem().toString();
                String FormAddTipoLinea = ((Spinner) findViewById(R.id.FormAddTipoLinea)).getSelectedItem().toString();
                String FormAddTipoCarnada = ((Spinner) findViewById(R.id.FormAddTipoCarnada)).getSelectedItem().toString();
                String FormAddDescripcion = ((EditText) findViewById(R.id.FormAddDescripcion)).getText().toString();
                Double lat = new Double("1");
                Double lng = new Double("1");

                if (imageUploadPreview != null && imageUploadPreview.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) imageUploadPreview.getDrawable()).getBitmap();
                    if (bitmap != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                        byte[] image = stream.toByteArray();
                        base64 = Base64.encodeToString(image, 0);
                    }
                }

                refDatabase = FirebaseDatabase.getInstance().getReference().child("pescas");
                Pesca pesca = new Pesca(base64, FormAddTipoPez, FormAddTipoLinea, FormAddTipoCarnada, FormAddDescripcion, lat, lng);
                String key = refDatabase.push().getKey();
                refDatabase.child(key).setValue(pesca);

                finish();
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