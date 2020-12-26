package com.jukebox.world;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.ViewModel.AlbumDetails;
import com.jukebox.world.ViewModel.Track;
import com.jukebox.world.adapters.AlbumAdapter;
import com.jukebox.world.adapters.AlbumTracksAdapter;

import java.util.ArrayList;

public class AlbumTracksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    private String albumTitle;

    private RecyclerView tracksRecyclerView;
    private ArrayList<Track> trackArrayList = new ArrayList<>();
    private AlbumTracksAdapter albumTracksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_tracks);

        albumTitle = getIntent().getStringExtra("albumTitle");

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("albums").child(albumTitle).child("tracks");

        tracksRecyclerView = findViewById(R.id.tracks_recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        tracksRecyclerView.setLayoutManager(mLayoutManager);
        albumTracksAdapter = new AlbumTracksAdapter(AlbumTracksActivity.this, trackArrayList);
        tracksRecyclerView.setAdapter(albumTracksAdapter);

        getAllTracks();
    }

    Track track;

    private void getAllTracks() {

        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        trackArrayList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            track = new Track();

                            track.setType(snapshot.child("trackType").getValue().toString());
                            track.setFeature(snapshot.child("trackFeature").getValue().toString());
                            track.setTitle(snapshot.child("trackTitle").getValue().toString());
                            track.setUrl(snapshot.child("trackUrl").getValue().toString());
                            trackArrayList.add(track);

                        }


                        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        tracksRecyclerView.setLayoutManager(mLayoutManager);
                        albumTracksAdapter = new AlbumTracksAdapter(AlbumTracksActivity.this, trackArrayList);
                        tracksRecyclerView.setAdapter(albumTracksAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }
}