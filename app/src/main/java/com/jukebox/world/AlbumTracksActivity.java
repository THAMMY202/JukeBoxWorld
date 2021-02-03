package com.jukebox.world;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

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
    private DatabaseReference mCustomerDatabase,mArtistDatabase;
    private String userID;

    private String albumTitle,albumPrice,albumCover,albumArtist,albumArtistUserKey;

    private RecyclerView tracksRecyclerView;
    private ArrayList<Track> trackArrayList = new ArrayList<>();
    private AlbumTracksAdapter albumTracksAdapter;
    private TextView textViewalbumPrice;
    private Button buttonPay;
    private ProgressBar progressBar;

    private static final String BACKEND_URL = "http://10.0.2.2:4242/";

    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;

    private RelativeLayout relativeLayoutButton;
    private  CardInputWidget cardInputWidget;

    private ImageView imageViewAlbumCover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        configPayment();
        startCheckout();

        if(!albumPrice.isEmpty()){
            textViewalbumPrice.setText("R"+albumPrice);
        }else {
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
    }

    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onGenerated(Palette p) {

                try{
                    Palette.Swatch vibrantSwatch = checkVibrantSwatch(p);

                    Toolbar toolbar =  findViewById(R.id.toolbar2);
                    toolbar.setTitle(albumArtist);
                    toolbar.setSubtitle(albumTitle);

                    toolbar.setBackgroundColor(vibrantSwatch.getRgb());
                    toolbar.setTitleTextColor(vibrantSwatch.getTitleTextColor());
                    setSupportActionBar(toolbar);

                }catch (Exception e){
                    Toolbar toolbar =  findViewById(R.id.toolbar2);
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
                    }

                    else if (map.get("firstName") != null) {
                        albumArtist = map.get("firstName").toString().trim();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void configPayment() {
        // Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_EWyVlmphMKYGEsIo8QAneKCW")
        );
    }

    private void startCheckout() {
        // Create a PaymentIntent by calling the server's endpoint.
        /*MediaType mediaType = MediaType.get("application/json; charset=utf-8");
       String json = "{"
                + ""currency":"usd","
                + ""items":["
                + "{"id":"photo_subscription"}"
                + "]"
                + "}";

                RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new PayCallback(AlbumTracksActivity.this));

        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
                PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
                if (params != null) {
                    ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                    stripe.confirmPayment(AlbumTracksActivity.this, confirmParams);
                }
            }
        });*/

        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracksRecyclerView.setVisibility(View.GONE);
                relativeLayoutButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                cardInputWidget.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(AlbumTracksActivity.this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );

        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<AlbumTracksActivity> activityRef;

        PayCallback(@NonNull AlbumTracksActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final AlbumTracksActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            Toast.makeText(activity, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final AlbumTracksActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                Toast.makeText(activity, "Error: " + response.toString(), Toast.LENGTH_LONG).show();
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<AlbumTracksActivity> activityRef;

        PaymentResultCallback(@NonNull AlbumTracksActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final AlbumTracksActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert("Payment completed",gson.toJson(paymentIntent)
                );
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert("Payment failed", Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final AlbumTracksActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString());
        }
    }

}