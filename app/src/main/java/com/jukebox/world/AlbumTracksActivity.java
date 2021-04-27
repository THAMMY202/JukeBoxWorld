package com.jukebox.world;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.jukebox.world.ViewModel.MyPlayListTrack;
import com.jukebox.world.ViewModel.Track;
import com.jukebox.world.adapters.AlbumTracksAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;


public class AlbumTracksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase, mArtistDatabase;
    private String userID;

    private String albumTitle, albumPrice, albumCover, albumArtist, albumArtistUserKey;

    private RecyclerView tracksRecyclerView;
    private ArrayList<Track> trackArrayList = new ArrayList<>();
    private ArrayList<MyPlayListTrack> myPlayListTracks = new ArrayList<>();

    private AlbumTracksAdapter albumTracksAdapter;
    private TextView textViewalbumPrice;
    private Button buttonPay;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayoutButton;
    private ImageView imageViewAlbumCover;
    private Double currentUserWalletValue = 0.0;
    private String userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_album_tracks);

        albumTitle = getIntent().getStringExtra("albumTitle");
        albumPrice = getIntent().getStringExtra("albumPrice");
        albumCover = getIntent().getStringExtra("albumCover");
        albumArtistUserKey = getIntent().getStringExtra("albumArtist");

        imageViewAlbumCover = findViewById(R.id.imgAlbumCoverOut);

        Glide.with(this)
                .asBitmap()
                .load(albumCover)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        imageViewAlbumCover.setImageBitmap(bitmap);
                        createPaletteAsync(bitmap);
                    }
                });


        textViewalbumPrice = findViewById(R.id.tvAlbumPrice);
        relativeLayoutButton = findViewById(R.id.RelativeLayoutBuy);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (albumPrice != null && !albumPrice.isEmpty()) {
            DecimalFormat df = new DecimalFormat("0.00##");
            String result = df.format(Double.parseDouble(albumPrice));
            textViewalbumPrice.setText("R" + result);

        } else {
            textViewalbumPrice.setText("Free");
        }

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("albums").child(albumTitle).child("tracks");
        mArtistDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(albumArtistUserKey);
        getArtistInfo();

        tracksRecyclerView = findViewById(R.id.tracks_recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        tracksRecyclerView.setLayoutManager(mLayoutManager);
        albumTracksAdapter = new AlbumTracksAdapter(AlbumTracksActivity.this, trackArrayList, getCanPayTracks());
        tracksRecyclerView.setAdapter(albumTracksAdapter);

        getAllTracks();

        buttonPay = findViewById(R.id.payButton);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (currentUserWalletValue >= Double.parseDouble(albumPrice)) {

                    Double newWalletValue = currentUserWalletValue - Double.parseDouble(albumPrice);

                    DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                    mUserDatabase.child("wallet").setValue(newWalletValue.toString());

                    startFlutterwave();

                } else {
                    new RaveUiManager(AlbumTracksActivity.this)
                            .setAmount(Double.parseDouble(albumPrice))
                            .setCurrency("ZAR")
                            .setCountry("ZA")
                            .setEmail(userEmail)
                            .setfName(userName)
                            //.setlName("Shabalala")
                            .setNarration("narration")
                            .setPublicKey("FLWPUBK-bb073bc9a16aa65411460250c7aca087-X")
                            .setEncryptionKey("4918493db6585bf187938914")
                            .setTxRef("txRef")
                            .acceptCardPayments(true)
                            .acceptSaBankPayments(true)
                            .onStagingEnv(false)
                            .showStagingLabel(false)
                            .initialize();
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabShareAlbum);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //albumTitle, albumPrice, albumCover, albumArtist
                String shareMessage = "Checkout " + albumTitle + " by " + albumArtist;
                try {
                    ShareCompat.IntentBuilder.from(AlbumTracksActivity.this)
                            .setType("text/plain")
                            .setChooserTitle("Share With")
                            .setText(shareMessage + " http://play.google.com/store/apps/details?id=" + AlbumTracksActivity.this.getPackageName())
                            .startChooser();
                } catch (Exception ex) {
                }
            }
        });

        getCanPayTracks();
    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("wallet") != null) {
                        currentUserWalletValue = Double.parseDouble(map.get("wallet").toString().trim());
                    }

                    if (map.get("email") != null) {
                        userEmail = map.get("email").toString().trim();
                    }

                    if (map.get("firstName") != null) {

                        userName = map.get("firstName").toString().trim();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onGenerated(Palette p) {

                try {
                    Palette.Swatch vibrantSwatch = checkVibrantSwatch(p);

                    Toolbar toolbar = findViewById(R.id.toolbar2);
                    toolbar.setTitle(albumArtist);
                    toolbar.setSubtitle(albumTitle);

                    toolbar.setBackgroundColor(vibrantSwatch.getRgb());
                    toolbar.setTitleTextColor(vibrantSwatch.getTitleTextColor());
                    setSupportActionBar(toolbar);

                } catch (Exception e) {
                    Toolbar toolbar = findViewById(R.id.toolbar2);
                    toolbar.setTitle(albumArtist);
                    toolbar.setSubtitle(albumTitle);
                    setSupportActionBar(toolbar);
                }

            }
        });
    }

    // Return a palette's vibrant swatch after checking that it exists
    private Palette.Swatch checkVibrantSwatch(Palette p) {

        Palette.Swatch vibrant = p.getVibrantSwatch();
        if (vibrant != null) {
            return vibrant;
        }

        return vibrant;
    }


    Track track;
    MyPlayListTrack myPlayListTrack;

    private void getAllTracks() {

        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trackArrayList.clear();
                myPlayListTracks.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    track = new Track();
                    myPlayListTrack = new MyPlayListTrack();

                    track.setType(snapshot.child("trackType").getValue().toString());
                    track.setFeature(snapshot.child("trackFeature").getValue().toString());
                    track.setTitle(snapshot.child("trackTitle").getValue().toString());
                    track.setUrl(snapshot.child("trackUrl").getValue().toString());
                    track.setCover(snapshot.child("trackCover").getValue().toString());
                    track.setDuration(snapshot.child("trackDuration").getValue().toString());
                    trackArrayList.add(track);


                    myPlayListTrack.setArtist(albumArtist);
                    myPlayListTrack.setAlbum(albumTitle);
                    myPlayListTrack.setType(snapshot.child("trackType").getValue().toString());
                    myPlayListTrack.setFeature(snapshot.child("trackFeature").getValue().toString());
                    myPlayListTrack.setTitle(snapshot.child("trackTitle").getValue().toString());
                    myPlayListTrack.setUrl(snapshot.child("trackUrl").getValue().toString());
                    myPlayListTrack.setCover(snapshot.child("trackCover").getValue().toString());
                    myPlayListTrack.setTrackDuration(snapshot.child("trackDuration").getValue().toString());
                    myPlayListTracks.add(myPlayListTrack);

                }

                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                tracksRecyclerView.setLayoutManager(mLayoutManager);
                albumTracksAdapter = new AlbumTracksAdapter(AlbumTracksActivity.this, trackArrayList, getCanPayTracks());
                tracksRecyclerView.setAdapter(albumTracksAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getArtistInfo() {
        mArtistDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("stageName") != null) {
                        albumArtist = map.get("stageName").toString().trim();

                    } else if (map.get("firstName") != null) {
                        albumArtist = map.get("firstName").toString().trim();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                startFlutterwave();
                Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    boolean canPlay = false;

    private Boolean getCanPayTracks() {

        final DatabaseReference mCustomerBroughtDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(albumArtistUserKey).child("albums").child(albumTitle).child("brought");
        mCustomerBroughtDatabase.orderByChild("userID").equalTo(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            canPlay = true;
                            buttonPay.setVisibility(View.GONE);
                        } else {
                            canPlay = false;
                            buttonPay.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        return canPlay;
    }

    private void addToMyAlbums() {
        final DatabaseReference mUserBroughtDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("MyBoughtMusic");
        mUserBroughtDatabase.push().setValue(myPlayListTracks);
    }

    private void startFlutterwave() {

        final DatabaseReference mCustomerBroughtDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(albumArtistUserKey).child("albums").child(albumTitle).child("brought");
        mCustomerBroughtDatabase.orderByChild("userID").equalTo(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(AlbumTracksActivity.this, "You already bought it", Toast.LENGTH_LONG).show();
                        } else {
                            Map userInfo = new HashMap();
                            userInfo.put("userID", userID);
                            buttonPay.setVisibility(View.GONE);
                            mCustomerBroughtDatabase.updateChildren(userInfo);

                            if (!myPlayListTracks.isEmpty()) {
                                addToMyAlbums();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}