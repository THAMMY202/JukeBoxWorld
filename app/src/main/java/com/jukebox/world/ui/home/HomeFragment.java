package com.jukebox.world.ui.home;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.MusicCategoryRecyclerViewAdapter;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.AlbumDetails;
import com.jukebox.world.ViewModel.Artist;
import com.jukebox.world.ViewModel.Track;
import com.jukebox.world.adapters.AlbumAdapter;
import com.jukebox.world.adapters.ArtistAdapter;

import java.util.ArrayList;
import java.util.Hashtable;

public class HomeFragment extends Fragment implements MusicCategoryRecyclerViewAdapter.ItemClickListener {

    private MusicCategoryRecyclerViewAdapter adapter;
    private Hashtable items = new Hashtable();

    private GridLayoutManager layoutManager;
    private RecyclerView albumsRecyclerView,artistRecyclerView;
    private ArrayList<AlbumDetails> albumDetailsArrayList = new ArrayList<>();
    private ArrayList<Artist> artistArrayList = new ArrayList<>();
    private ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mAlbumsDatabase;
    private String userID;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mAlbumsDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");

        albumsRecyclerView = root.findViewById(R.id.home_albums_recyclerView);
        artistRecyclerView = root.findViewById(R.id.home_artists_recyclerView);

        layoutManager = new GridLayoutManager(getActivity(), 2);
        albumAdapter = new AlbumAdapter(getActivity(),albumDetailsArrayList);
        albumsRecyclerView.setAdapter(albumAdapter);


        layoutManager = new GridLayoutManager(getActivity(), 2);
        artistAdapter = new ArtistAdapter(getActivity(),artistArrayList);
        artistRecyclerView.setAdapter(artistAdapter);


        albumsRecyclerView.setHasFixedSize(true);
        if (checkIsTablet()) {
            albumsRecyclerView.setLayoutManager(layoutManager);
            artistRecyclerView.setLayoutManager(layoutManager);
        } else {
            albumsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            artistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        getAllAlbums();
        getAllArtist();

        return root;
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private void getAllAlbums() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            AlbumDetails albumDetails;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot: dataSnapshot.getChildren()) {
                    DataSnapshot productsSnapshot = requestSnapshot.child("albums");
                    for (DataSnapshot ds: productsSnapshot.getChildren()) {

                        if (ds.hasChild("albumCoverUrl") && ds.hasChild("albumGenre") && ds.hasChild("albumRealiseDate") && ds.hasChild("albumTitle")) {

                            albumDetails  = new AlbumDetails();
                            albumDetails.setGenre(ds.child("albumGenre").getValue().toString());
                            albumDetails.setTitle(ds.child("albumTitle").getValue().toString());
                            albumDetails.setCoverImageUrl(ds.child("albumCoverUrl").getValue().toString());
                            albumDetails.setRealiseDate(ds.child("albumRealiseDate").getValue().toString());
                            albumDetailsArrayList.add(albumDetails);
                        }
                    }
                }

                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                albumsRecyclerView.setLayoutManager(horizontalLayoutManager);
                albumAdapter = new AlbumAdapter(getActivity(),albumDetailsArrayList);
                albumsRecyclerView.setAdapter(albumAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException(); // don't ignore errors
            }
        });
    }

    private void getAllArtist() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            Artist artist;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    artist  = new Artist();
                    artist.setKey(ds.getKey());
                    artist.setEmail(ds.child("email").getValue().toString());
                    artist.setFirstName(ds.child("firstName").getValue().toString());
                    artist.setLastName(ds.child("lastName").getValue().toString());
                    artist.setEmail(ds.child("email").getValue().toString());
                    artist.setPhone(ds.child("phone").getValue().toString());

                    if(ds.child("stageName").getValue().toString()!=null){
                        artist.setStageName(ds.child("stageName").getValue().toString());
                    }else {
                        artist.setStageName("unspecified");
                    }


                    if(ds.child("profileImageUrl").getValue().toString()!=null){
                        artist.setProfileImageUrl(ds.child("profileImageUrl").getValue().toString());
                    }

                    artistArrayList.add(artist);

                }

                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                artistRecyclerView.setLayoutManager(horizontalLayoutManager);
                artistAdapter = new ArtistAdapter(getActivity(),artistArrayList);
                artistRecyclerView.setAdapter(artistAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    private boolean checkIsTablet() {
        boolean isTablet = false;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
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
}