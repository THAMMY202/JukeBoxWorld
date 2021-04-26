package com.jukebox.world.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.AlbumDetails;
import com.jukebox.world.ViewModel.Artist;
import com.jukebox.world.ViewModel.SocialModel;
import com.jukebox.world.adapters.AlbumAdapter;

import java.util.ArrayList;

public class ArtistDetailsActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private TextView textViewArtistWriteUp;
    private RecyclerView albumsRecyclerView;
    private AlbumAdapter albumAdapter;
    private ArrayList<AlbumDetails> albumDetailsArrayList = new ArrayList<>();
    private String ArtistUserID;

    private ImageView imageViewFacebook, imageViewTwitter, imageViewYouTube, imageViewInstagram;
    private String facebookUrl, twitterUrl, youTubeUrl, instagramUrl;

    private FirebaseAuth mAuth;
    private  String artistStageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);

        mAuth = FirebaseAuth.getInstance();

        findByID();

        ArtistUserID = getIntent().getStringExtra("artistKey");
        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        artistStageName = getIntent().getStringExtra("artistStageName");

        getSupportActionBar().setSubtitle(setToUpperCase(firstName) + " " + setToUpperCase(lastName));
        String image = getIntent().getStringExtra("artistImage");

        textViewArtistWriteUp.setText(setToUpperCase(firstName) + " " + setToUpperCase(lastName) + " , " + "known professionally as " + artistStageName);

        if (!image.isEmpty()) {
            Glide.with(getApplicationContext()).load(image).into(imageViewProfile);
        } else {
            imageViewProfile.setImageDrawable(getDrawable(R.drawable.artistholder));
        }

        getAllAlbums();
        getAllSocailNetworks();

        imageViewFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (facebookUrl != null && !facebookUrl.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(facebookUrl));
                    startActivity(i);
                } else {

                    Toast.makeText(ArtistDetailsActivity.this, artistStageName + " is not available on facebook as yet ", Toast.LENGTH_LONG).show();
                }
            }
        });

        imageViewTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (twitterUrl != null && !twitterUrl.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(twitterUrl));
                    startActivity(i);
                } else {
                    Toast.makeText(ArtistDetailsActivity.this, artistStageName + " is not available on twitte as yet ", Toast.LENGTH_LONG).show();
                }
            }
        });

        imageViewYouTube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (youTubeUrl != null && !youTubeUrl.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(youTubeUrl));
                    startActivity(i);
                } else {
                    Toast.makeText(ArtistDetailsActivity.this, artistStageName + " is not available on youTube as yet ", Toast.LENGTH_LONG).show();
                }
            }
        });

        imageViewInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instagramUrl != null && !instagramUrl.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(instagramUrl));
                    startActivity(i);
                } else {
                    Toast.makeText(ArtistDetailsActivity.this, artistStageName + " is not available on instagram as yet ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void findByID() {
        imageViewProfile = findViewById(R.id.artistProfile);
        imageViewFacebook = findViewById(R.id.imageViewFacebook);
        imageViewTwitter = findViewById(R.id.imageViewTwitter);
        imageViewYouTube = findViewById(R.id.imageViewYouTube);
        imageViewInstagram = findViewById(R.id.imageViewInstagram);

        textViewArtistWriteUp = findViewById(R.id.tvArtistWriteUp);
        albumsRecyclerView = findViewById(R.id.recyclerViewArtist);
        albumAdapter = new AlbumAdapter(ArtistDetailsActivity.this, albumDetailsArrayList, mAuth.getCurrentUser().getUid());
        albumsRecyclerView.setAdapter(albumAdapter);
    }

    private void getAllAlbums() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        Query albumsQuery = ref.orderByKey().equalTo(ArtistUserID);
        albumsQuery.addListenerForSingleValueEvent(new ValueEventListener() {

            AlbumDetails albumDetails;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot productsSnapshot = requestSnapshot.child("albums");
                    for (DataSnapshot ds : productsSnapshot.getChildren()) {

                        if (ds.hasChild("albumCoverUrl") && ds.hasChild("albumGenre") && ds.hasChild("albumRealiseDate") && ds.hasChild("albumTitle")) {

                            albumDetails = new AlbumDetails();
                            albumDetails.setGenre(ds.child("albumGenre").getValue().toString());
                            albumDetails.setTitle(ds.child("albumTitle").getValue().toString());
                            albumDetails.setCoverImageUrl(ds.child("albumCoverUrl").getValue().toString());
                            albumDetails.setRealiseDate("" + ds.child("albumRealiseDate").getValue());
                            albumDetails.setPrice(ds.child("albumPrice").getValue().toString());
                            albumDetails.setArtist(ds.child("albumArtist").getValue().toString());
                            albumDetailsArrayList.add(albumDetails);
                        }
                    }
                }


                if (checkIsTablet()) {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(ArtistDetailsActivity.this, 4);
                    gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    albumsRecyclerView.setLayoutManager(gridLayoutManager);
                    albumAdapter = new AlbumAdapter(ArtistDetailsActivity.this, albumDetailsArrayList, mAuth.getCurrentUser().getUid());
                    albumsRecyclerView.setAdapter(albumAdapter);
                } else {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(ArtistDetailsActivity.this, 2);
                    gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    albumsRecyclerView.setLayoutManager(gridLayoutManager);
                    albumAdapter = new AlbumAdapter(ArtistDetailsActivity.this, albumDetailsArrayList, mAuth.getCurrentUser().getUid());
                    albumsRecyclerView.setAdapter(albumAdapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException(); // don't ignore errors
            }
        });
    }

    private String setToUpperCase(String toUpper) {
        String upperString = "";

        if (!toUpper.isEmpty()) {
            upperString = toUpper.substring(0, 1).toUpperCase() + toUpper.substring(1).toLowerCase();
        }
        return upperString;
    }

    private boolean checkIsTablet() {
        boolean isTablet = false;
        Display display = ArtistDetailsActivity.this.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));

        if (diagonalInches >= 7.0) {
            isTablet = true;
        }

        return isTablet;
    }

    private void getAllSocailNetworks() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        Query albumsQuery = ref.orderByKey().equalTo(ArtistUserID);
        albumsQuery.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot productsSnapshot = requestSnapshot.child("social");
                    for (DataSnapshot ds : productsSnapshot.getChildren()) {

                        SocialModel socialMedia = ds.getValue(SocialModel.class);

                        if (socialMedia.getUrl() != null && socialMedia.getUrl().contains("facebook")) {
                            facebookUrl = socialMedia.getUrl().toString();
                            imageViewFacebook.setEnabled(true);

                        } else if (socialMedia.getUrl() != null && socialMedia.getUrl().contains("twitter")) {
                            twitterUrl = socialMedia.getUrl().toString();
                            imageViewTwitter.setEnabled(true);

                        } else if (socialMedia.getUrl() != null && socialMedia.getUrl().contains("youtube")) {
                            youTubeUrl = socialMedia.getUrl().toString();
                            imageViewYouTube.setEnabled(true);

                        } else if (socialMedia.getUrl() != null && socialMedia.getUrl().contains("instagram")) {
                            instagramUrl = socialMedia.getUrl().toString();
                            imageViewInstagram.setEnabled(true);
                        }
                    }
                }


                if (checkIsTablet()) {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(ArtistDetailsActivity.this, 4);
                    gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    albumsRecyclerView.setLayoutManager(gridLayoutManager);
                    albumAdapter = new AlbumAdapter(ArtistDetailsActivity.this, albumDetailsArrayList, mAuth.getCurrentUser().getUid());
                    albumsRecyclerView.setAdapter(albumAdapter);
                } else {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(ArtistDetailsActivity.this, 2);
                    gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    albumsRecyclerView.setLayoutManager(gridLayoutManager);
                    albumAdapter = new AlbumAdapter(ArtistDetailsActivity.this, albumDetailsArrayList, mAuth.getCurrentUser().getUid());
                    albumsRecyclerView.setAdapter(albumAdapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException(); // don't ignore errors
            }
        });
    }
}