package com.jukebox.world;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.animations.DescriptionAnimation;
import com.glide.slider.library.slidertypes.BaseSliderView;
import com.glide.slider.library.slidertypes.TextSliderView;
import com.glide.slider.library.tricks.ViewPagerEx;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.ViewModel.AdsSlideViewModel;


import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    private TextView mprofileName;
    private TextView mprofileBalance;
    private ImageView mProfileImage;

    private DatabaseReference databaseReference;
    private List<AdsSlideViewModel> slideLists;
    private ImageView imageViewShareApp;

    private SliderLayout mDemoSlider;

    private String userName, userEmail;
    private String saveWalletMoney;

    private MenuItem menuItemAds, menuItemAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);


        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_profile, R.id.nav_new_release).setDrawerLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        View header = navigationView.getHeaderView(0);

        mProfileImage = header.findViewById(R.id.profileImage);
        mprofileName = header.findViewById(R.id.profileName);
        mprofileBalance = header.findViewById(R.id.tvUserBalance);

        imageViewShareApp = findViewById(R.id.imgAppShare);
        imageViewShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareCompat.IntentBuilder.from(MainActivity.this)
                        .setType("text/plain")
                        .setChooserTitle("Share With")
                        .setText("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())
                        .startChooser();
            }
        });

        mprofileBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawer.closeDrawer(GravityCompat.START);
                alertDialogWallet();
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference();
        slideLists = new ArrayList<>();
        getAllAds();

    }

    void alertDialogWallet() {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.custom_wallet_dialog, null);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.etAmount);

        alertDialogBuilder
                .setTitle("UPDATE MY WALLET")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (userInput.getText() != null && !userInput.getText().toString().isEmpty()) {

                            saveWalletMoney = userInput.getText().toString();

                            new RaveUiManager(MainActivity.this)
                                    .setAmount(Double.parseDouble(userInput.getText().toString()))
                                    .setCurrency("ZAR")
                                    .setCountry("ZA")
                                    .setEmail(userEmail)
                                    .setfName(userName)
                                    //.setlName("Shabalala")
                                    .setNarration("narration")
                                    .setPublicKey("FLWPUBK-bb073bc9a16aa65411460250c7aca087-X")
                                    .setEncryptionKey("4918493db6585bf187938914")
                                    .setTxRef(userID)
                                    .acceptCardPayments(true)
                                    .acceptSaBankPayments(true)
                                    .onStagingEnv(false)
                                    .showStagingLabel(false)
                                    .initialize();
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("role") != null && !map.get("role").toString().equalsIgnoreCase("Admin")) {
                        hideAdminItems();
                    }

                    if (map.get("email") != null) {
                        userEmail = map.get("email").toString().trim();
                    }

                    if (map.get("firstName") != null) {
                        mprofileName.setText(map.get("firstName").toString().trim());
                        userName = map.get("firstName").toString().trim();
                    }

                    if (map.get("firstName") != null && map.get("lastName") != null) {
                        mprofileName.setText(map.get("firstName").toString().trim() + " " + map.get("lastName"));
                    }

                    if (map.get("wallet") != null) {

                        DecimalFormat df = new DecimalFormat("0.00##");

                        String result = df.format(Double.parseDouble(map.get("wallet").toString().trim()));
                        mprofileBalance.setText("Wallet Balance: R " + result);

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


    private void hideAdminItems() {
        menuItemAdmin.setVisible(false);
        menuItemAds.setVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menuItemAds = menu.findItem(R.id.action_ads);
        menuItemAdmin = menu.findItem(R.id.action_add_admin);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ads:
                startActivity(new Intent(MainActivity.this, AdsActivity.class));
                return true;

            case R.id.action_add_admin:
                startActivity(new Intent(MainActivity.this, AdminRegistrationActivity.class));
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
        mDemoSlider = findViewById(R.id.slider);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        //.placeholder(R.drawable.placeholder)
        //.error(R.drawable.placeholder);

        for (int i = 0; i < slideLists.size(); i++) {
            TextSliderView sliderView = new TextSliderView(this);
            sliderView
                    .image(slideLists.get(i).getImageUrl())
                    .description(slideLists.get(i).getName())
                    .setRequestOption(requestOptions)
                    .setProgressBarVisible(true)
                    .setOnSliderClickListener(MainActivity.this);

            //add your extra information
            sliderView.bundle(new Bundle());
            sliderView.getBundle().putString("extra", slideLists.get(i).getName());
            mDemoSlider.addSlider(sliderView);
        }

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);

        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);
        mDemoSlider.stopCyclingWhenTouch(false);

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
                            //Toast.makeText(MainActivity.this, "All banners fetched", Toast.LENGTH_SHORT).show();
                            usingFirebaseImages(slideLists);
                        } else {
                            Toast.makeText(MainActivity.this, "No banners in firebase", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(MainActivity.this, "NO banners found \n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                final DatabaseReference updateWalletBalanceDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("wallet");
                updateWalletBalanceDatabase.setValue(saveWalletMoney);
                Toast.makeText(this, "Payment was successfully ", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "Payment error", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "Payment was cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}