package com.jukebox.world.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jukebox.world.R;

public class ArtistDetailsActivity extends AppCompatActivity {

  private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);

        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");

        getSupportActionBar().setSubtitle(firstName+ " " + lastName);

        String image = getIntent().getStringExtra("artistImage");

        imageViewProfile = findViewById(R.id.artistProfile);

        if(!image.isEmpty()){
            Glide.with(getApplicationContext()).load(image).into(imageViewProfile);
        }else {
            imageViewProfile.setImageDrawable(getDrawable(R.drawable.artistholder));
        }

    }
}