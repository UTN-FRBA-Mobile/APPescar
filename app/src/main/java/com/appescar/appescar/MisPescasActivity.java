package com.appescar.appescar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class MisPescasActivity extends AppCompatActivity {

    ListView listView ;
    private DatabaseReference refDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pescas);


        refDatabase = FirebaseDatabase.getInstance().getReference().child("pescas");

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;


        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.lv_mispescas);

        final ListAdapter adapter = new FirebaseListAdapter<Pesca>(this, Pesca.class, R.layout.una_pesca, refDatabase.orderByChild("uid").equalTo(currentFirebaseUser.getUid())) {
            @Override
            protected void populateView(View view, Pesca pesca, int position) {

                TextView fish = (TextView) view.findViewById(R.id.lv_fish);
                TextView bait = (TextView) view.findViewById(R.id.lv_bait);
                TextView line = (TextView) view.findViewById(R.id.lv_line);
                TextView desc = (TextView) view.findViewById(R.id.lv_description);
                ImageView img = (ImageView) view.findViewById(R.id.lv_list_image);
                TextView tst = (TextView) view.findViewById(R.id.lv_timestamp);


                fish.setText(pesca.getFish());
                bait.setText(pesca.getBait());
                line.setText(pesca.getLine());
                desc.setText(pesca.getDescription());
                tst.setText(pesca.getTst());


                String key = pesca.getImgname();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://apppescar-e204f.appspot.com/");
                StorageReference imgRef = storageRef.child("pescas/"+key+".png");

                Glide.with(getApplicationContext())
                        .using(new FirebaseImageLoader())
                        .load(imgRef)
                        .into(img);

            }

        };



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {
               // String selectedFromList = (listView.getItemAtPosition(position).getString());

                Pesca pesca = (Pesca) adapter.getItem(position);

                Intent intent = new Intent(MisPescasActivity.this, DetallesPesca.class);
                intent.putExtra("fish",pesca.getFish());
                intent.putExtra("line",pesca.getLine());
                intent.putExtra("bait",pesca.getBait());
                intent.putExtra("imgname",pesca.getImgname());
                intent.putExtra("tst",pesca.getTst());
                intent.putExtra("description",pesca.getDescription());
                startActivity(intent);

            }});


        listView.setAdapter(adapter);



    }
}
