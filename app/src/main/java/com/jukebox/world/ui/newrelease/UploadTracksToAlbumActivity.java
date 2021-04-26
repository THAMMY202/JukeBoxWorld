package com.jukebox.world.ui.newrelease;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.Track;

import java.util.HashMap;
import java.util.Map;

public class UploadTracksToAlbumActivity extends AppCompatActivity {
    private LinearLayout linearLayoutTrackCover;
    private String TrackName, FeaturedArtist, TrackType;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    private Uri resultTrackCoverUri;

    private RadioGroup radioTrackTypeGroup;
    private RadioButton radioTrackTypeButton;

    private EditText mProposedReleaseDate, editTextReleaseTitle, editTextTrackTitle, editTextFeatureArtist, editTextReleasePrice;
    private String albumTitle, albumPrice, albumCover, albumArtist, albumArtistUserKey;

    private Button buttonPublish, buttonUploadTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_tracks_to_album);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("albums");

        albumTitle = getIntent().getStringExtra("albumTitle");
        albumPrice = getIntent().getStringExtra("albumPrice");
        albumCover = getIntent().getStringExtra("albumCover");
        albumArtistUserKey = getIntent().getStringExtra("albumArtist");

        findViewById();

        buttonUploadTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextTrackTitle.getText().toString() != null && editTextTrackTitle.getText().toString() != "") {
                    TrackName = editTextTrackTitle.getText().toString();

                    if (editTextFeatureArtist.getText().toString() != null && editTextFeatureArtist.getText().toString() != "") {
                        FeaturedArtist = editTextFeatureArtist.getText().toString();
                    } else {
                        FeaturedArtist = "None";
                    }

                    int selectedId = radioTrackTypeGroup.getCheckedRadioButtonId();
                    radioTrackTypeButton = findViewById(selectedId);
                    TrackType = radioTrackTypeButton.getText().toString();
                    chooseTrackFile();
                }
            }
        });

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(UploadTracksToAlbumActivity.this);
                builder.setTitle(albumTitle);
                builder.setMessage("Are you sure that you want to publish ?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                if (mCustomerDatabase != null && albumTitle != null && !albumTitle.isEmpty()) {
                                    mCustomerDatabase.child(albumTitle).child("isPublished").setValue("true");
                                    Toast.makeText(UploadTracksToAlbumActivity.this, albumTitle + " published successful", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                builder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder.create();
                alert11.show();
            }
        });

        linearLayoutTrackCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });
    }


    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
    }

    private void chooseTrackFile() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Track"), 1);
    }

    private void findViewById() {
        radioTrackTypeGroup = findViewById(R.id.radioGroup);
        linearLayoutTrackCover = findViewById(R.id.linLayout);
        editTextReleaseTitle = findViewById(R.id.etReleaseTitle);
        buttonUploadTrack = findViewById(R.id.btnUploadTrack);
        buttonPublish = findViewById(R.id.buttonPublish);
        editTextTrackTitle = findViewById(R.id.etTrackTitle);
        editTextFeatureArtist = findViewById(R.id.etFeatureArtist);
    }

    private void uploadTrackCover(final Uri trackUri, final Uri trackCoverUri) {

        final ProgressDialog progressDialog = new ProgressDialog(UploadTracksToAlbumActivity.this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("albums").child(userID).child(albumTitle).child(TrackName);

        filePath.putFile(trackCoverUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Uri downUri = task.getResult();
                    uploadTrack(trackUri, downUri.toString());

                }
            }
        });

    }

    private void uploadTrack(Uri trackUri, final String trackCover) {

        final ProgressDialog progressDialog = new ProgressDialog(UploadTracksToAlbumActivity.this);
        progressDialog.setTitle("Uploading Track Details");
        progressDialog.show();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("albums").child(userID).child(albumTitle).child(TrackName + ".mp3");

        filePath.putFile(trackUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Uri downUri = task.getResult();

                    Track track = new Track();
                    track.setDuration(getDuration(downUri.toString()));
                    track.setUrl(downUri.toString());
                    track.setTitle(TrackName);
                    track.setFeature(FeaturedArtist);
                    track.setType(TrackType);

                    if (!trackCover.isEmpty()) {
                        track.setCover(trackCover);
                    } else {
                        track.setCover(downUri.toString());
                    }

                    Map newImage = new HashMap();
                    newImage.put("trackUrl", track.getUrl());
                    newImage.put("trackCover", track.getCover());
                    newImage.put("trackTitle", track.getTitle());
                    newImage.put("trackFeature", track.getFeature());
                    newImage.put("trackType", track.getType());
                    newImage.put("trackDuration", track.getDuration());

                    mCustomerDatabase.child(albumTitle).child("tracks").push().setValue(newImage);
                }
            }
        });

    }

    private static String getDuration(String pathStr) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(pathStr, new HashMap<String, String>());
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        String duration = convertMillieToHMmSs(timeInMillisec);

        return duration;
    }

    public static String convertMillieToHMmSs(long millie) {
        long seconds = (millie / 1000);
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        String result = "";
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format("%02d:%02d", minute, second);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageTrackUri = data.getData();
            uploadTrackCover(imageTrackUri, resultTrackCoverUri);

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultTrackCoverUri = imageUri;
        }
    }

}