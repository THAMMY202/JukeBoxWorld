package com.jukebox.world;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.jukebox.world.ViewModel.Track;
import com.jukebox.world.adapters.AlbumTracksAdapter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;


//For payment
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ProofOfPayment;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlbumTracksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase, mArtistDatabase;
    private String userID;

    private String albumTitle, albumPrice, albumCover, albumArtist, albumArtistUserKey;

    private RecyclerView tracksRecyclerView;
    private ArrayList<Track> trackArrayList = new ArrayList<>();
    private AlbumTracksAdapter albumTracksAdapter;
    private TextView textViewalbumPrice;
    private Button buttonPay;
    private ProgressBar progressBar;


    private RelativeLayout relativeLayoutButton;
    private CardInputWidget cardInputWidget;

    private ImageView imageViewAlbumCover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        cardInputWidget = findViewById(R.id.cardInputWidget);
        cardInputWidget.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (!albumPrice.isEmpty()) {
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
        albumTracksAdapter = new AlbumTracksAdapter(AlbumTracksActivity.this, trackArrayList);
        tracksRecyclerView.setAdapter(albumTracksAdapter);

        getAllTracks();

        buttonPay = findViewById(R.id.payButton);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payPalPayment();
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
                    track.setCover(snapshot.child("trackCover").getValue().toString());
                    trackArrayList.add(track);

                }

                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                tracksRecyclerView.setLayoutManager(mLayoutManager);
                albumTracksAdapter = new AlbumTracksAdapter(AlbumTracksActivity.this, trackArrayList);
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

    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {
        double dollors_current = 0.059;
        Double randsPrice = Double.parseDouble(albumPrice);
        double dollors = randsPrice / dollors_current;

        PayPalPayment payment = new PayPalPayment(new BigDecimal(dollors), "USD", albumTitle+" cost",PayPalPayment.PAYMENT_INTENT_SALE);
        payment.enablePayPalShippingAddressesRetrieval(true);
        Intent intent = new Intent(AlbumTracksActivity.this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                ProofOfPayment proof = confirm.getProofOfPayment();
                PayPalPayment payment = confirm.getPayment();
                payment.enablePayPalShippingAddressesRetrieval(true);

                if(confirm != null){
                    try{
                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse = jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){
                            Toast.makeText(AlbumTracksActivity.this, "Payment successful", Toast.LENGTH_LONG).show();
                            Toast.makeText(AlbumTracksActivity.this, proof.toString(), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else{

                Toast.makeText(AlbumTracksActivity.this, "Payment unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
    }

}