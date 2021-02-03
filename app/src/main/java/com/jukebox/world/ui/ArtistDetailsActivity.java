package com.jukebox.world.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jukebox.world.R;

public class ArtistDetailsActivity extends AppCompatActivity {

  private ImageView imageViewProfile;
  private TextView textViewArtistWriteUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);

        findByID();

        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        String artistStageName = getIntent().getStringExtra("artistStageName");

        getSupportActionBar().setSubtitle(setToUpperCase(firstName)+ " " + setToUpperCase(lastName));
        String image = getIntent().getStringExtra("artistImage");



        textViewArtistWriteUp.setText(setToUpperCase(firstName) + " " + setToUpperCase(lastName) + " , "+"known professionally as " + artistStageName);


        if(!image.isEmpty()){
            Glide.with(getApplicationContext()).load(image).into(imageViewProfile);
        }else {
            imageViewProfile.setImageDrawable(getDrawable(R.drawable.artistholder));
        }

    }

    private void findByID(){
        imageViewProfile = findViewById(R.id.artistProfile);
        textViewArtistWriteUp = findViewById(R.id.tvArtistWriteUp);
    }

    private String setToUpperCase(String toUpper){
        String upperString = "";

        if(!toUpper.isEmpty()){
            upperString = toUpper.substring(0, 1).toUpperCase() + toUpper.substring(1).toLowerCase();
        }
        return upperString;
    }
}