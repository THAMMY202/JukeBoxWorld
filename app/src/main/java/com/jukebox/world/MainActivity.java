package com.jukebox.world;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.ViewModel.AdsSlideViewModel;
import com.jukebox.world.ui.SocailActivity;
import com.squareup.picasso.Picasso;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    private TextView mprofileName;
    private ImageView mProfileImage;

    private ViewFlipper viewFlipper;
    private DatabaseReference databaseReference;
    private List<AdsSlideViewModel> slideLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home,R.id.nav_profile,R.id.nav_new_release).setDrawerLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        View header = navigationView.getHeaderView(0);

        mProfileImage = header.findViewById(R.id.profileImage);
        mprofileName = header.findViewById(R.id.profileName);


        databaseReference = FirebaseDatabase.getInstance().getReference();
        slideLists = new ArrayList<>();

        getUserInfo();
        getAllAds();

        /*BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications).build();
        NavController navControllerBottom = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navControllerBottom, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navControllerBottom);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
       // getAllAds();
    }

    private void getUserInfo() {
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        /*if (map.get("firstName") != null &&  map.get("lastName") != null) {
                            mprofileName.setText(map.get("name").toString().trim() + " " + map.get("lastName"));
                        }*/

                        if (map.get("firstName") != null) {
                            mprofileName.setText(map.get("firstName").toString().trim());
                        }

                        if (map.get("profileImageUrl") != null) {
                            Glide.with(getApplication()).load(map.get("profileImageUrl").toString().trim()).into(mProfileImage);
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_ads:
                startActivity(new Intent(MainActivity.this,AdsActivity.class));
                return true;

            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                Intent setupIntent = new Intent(MainActivity.this, SignUpActivity.class);
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(setupIntent);
                finish();
                return true;

                default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    private void usingFirebaseImages(List<AdsSlideViewModel> slideLists) {

        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper_ads_show);

        for (int i = 0; i < slideLists.size(); i++) {
            String downloadImageUrl = slideLists.get(i).getImageUrl();
            flipImages(downloadImageUrl);
        }
    }

    private void getAllAds() {

        databaseReference.child("All_Image_Uploads_Database")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            slideLists.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                AdsSlideViewModel model = snapshot.getValue(AdsSlideViewModel.class);
                                slideLists.add(model);
                            }
                            Toast.makeText(MainActivity.this, "All banners fetched", Toast.LENGTH_SHORT).show();
                            usingFirebaseImages(slideLists);
                        } else {
                            Toast.makeText(MainActivity.this, "No banners in firebase", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "NO banners found \n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void flipImages(String imageUrl) {

        //ViewFlipper.LayoutParams params = new ViewFlipper.LayoutParams(ViewFlipper.LayoutParams.MATCH_PARENT, ViewFlipper.LayoutParams.WRAP_CONTENT);
        ImageView imageView = new ImageView(MainActivity.this);
       // imageView.setLayoutParams(params);
        Picasso.with(MainActivity.this)
                .load(imageUrl)
                //.fit().centerCrop()
                .fit()
                .into(imageView);

        viewFlipper.addView(imageView);
        viewFlipper.setFlipInterval(2500);
        viewFlipper.setAutoStart(true);

        viewFlipper.startFlipping();
        viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
    }

}