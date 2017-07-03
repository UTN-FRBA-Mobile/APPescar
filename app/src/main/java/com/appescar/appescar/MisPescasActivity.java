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

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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


                fish.setText(pesca.getFish());
                bait.setText(pesca.getBait());
                line.setText(pesca.getLine());
                desc.setText(pesca.getDescription());


                byte[] decoded = Base64.decode(pesca.getImg(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                img.setImageBitmap(decodedByte);

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

                byte[] decoded = Base64.decode(pesca.getImg(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                String fileName = "myImage";//no .png or .jpg needed
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
                    fo.write(bytes.toByteArray());
                    // remember close file output
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                intent.putExtra("img",fileName);
                intent.putExtra("description",pesca.getDescription());
                startActivity(intent);

            }});


        listView.setAdapter(adapter);



    }
}
