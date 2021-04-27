package com.jukebox.world.ui.newrelease;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.AlbumDetails;
import com.jukebox.world.ViewModel.Track;
import com.jukebox.world.adapters.AlbumAdapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewReleaseFragment extends Fragment {

    private Spinner mGenreSpinner;
    private FloatingActionButton mFabChoosePic;
    private EditText mProposedReleaseDate, editTextReleaseTitle, editTextTrackTitle, editTextFeatureArtist,editTextReleasePrice;
    private TextView textViewReleaseTitle, textViewArtistRelease;
    private Calendar myCalendar = Calendar.getInstance();
    private RelativeLayout relativeLayoutAlbum, relativeLayoutTracks;
    private Button buttonAddTrack, buttonUploadTrack;
    private com.google.android.material.textfield.TextInputLayout ReleasePriceTextInputLayout;


    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase, mUserDatabase;
    private String userID;
    private String AlbumName, AlbumReleaseDate, AlbumGenre, TrackName, FeaturedArtist, TrackType;

    private RadioGroup radioTrackTypeGroup;
    private RadioButton radioTrackTypeButton;
    private ImageView mAlbumCover;
    private Uri resultAlbumCoverUri,resultTrackCoverUri;
    private View v;

    private GridLayoutManager layoutManager;
    private RecyclerView albumsRecyclerView;
    private ArrayList<AlbumDetails> albumDetailsArrayList = new ArrayList<>();
    private AlbumAdapter albumAdapter;

    private Switch SwitchReleasePaidOrFree;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_new_release, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("albums");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        initViews(v);
        getUserInfo();

        mProposedReleaseDate.setFocusableInTouchMode(false);
        mProposedReleaseDate.setFocusable(false);
        mProposedReleaseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        editTextReleaseTitle.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                textViewReleaseTitle.setText(s);
            }
        });

        buttonAddTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (buttonAddTrack.getText().toString().equals(" UPLOAD ALBUM DETAILS ")) {

                    if (TextUtils.isEmpty(textViewReleaseTitle.getText().toString())) {
                        textViewReleaseTitle.setError("Release title is required");
                        return;
                    }

                    if (TextUtils.isEmpty(mProposedReleaseDate.getText().toString())) {
                        mProposedReleaseDate.setError("Release date is required");
                        return;
                    }

                    if ( resultAlbumCoverUri != null && TextUtils.isEmpty(resultAlbumCoverUri.toString())) {
                        Toast.makeText(getActivity(), "Album cover is required", Toast.LENGTH_LONG).show();
                        return;
                    }

                    AlbumName = textViewReleaseTitle.getText().toString();
                    AlbumReleaseDate = mProposedReleaseDate.getText().toString();

                    buttonAddTrack.setText("+ 1 TRACK");
                    uploadAlbumDetails(resultAlbumCoverUri);

                } else {
                    relativeLayoutTracks.setVisibility(View.VISIBLE);
                    relativeLayoutAlbum.setVisibility(View.GONE);
                }
            }
        });

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
                    radioTrackTypeButton = v.findViewById(selectedId);
                    TrackType = radioTrackTypeButton.getText().toString();
                    chooseTrackFile();
                }
            }
        });

        mFabChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        SwitchReleasePaidOrFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ReleasePriceTextInputLayout.setVisibility(View.VISIBLE);
                } else {
                    ReleasePriceTextInputLayout.setVisibility(View.GONE);
                }
            }
        });



        return v;
    }

    private void getAllAlbums() {

        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            AlbumDetails albumDetails;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    albumDetailsArrayList.clear();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.hasChild("albumCoverUrl") && ds.hasChild("albumGenre") && ds.hasChild("albumRealiseDate") && ds.hasChild("albumTitle")) {

                            albumDetails  = new AlbumDetails();

                            albumDetails.setGenre(ds.child("albumGenre").getValue().toString());
                            albumDetails.setTitle(ds.child("albumTitle").getValue().toString());
                            albumDetails.setCoverImageUrl(ds.child("albumCoverUrl").getValue().toString());
                            albumDetails.setRealiseDate(ds.child("albumRealiseDate").getValue().toString());
                            albumDetails.setArtist(ds.child("albumArtist").getValue().toString());
                            albumDetails.setPrice(ds.child("albumPrice").getValue().toString());
                            albumDetailsArrayList.add(albumDetails);
                        }
                    }

                    LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                    albumsRecyclerView.setLayoutManager(horizontalLayoutManager);
                    albumAdapter = new AlbumAdapter(getActivity(),albumDetailsArrayList,mAuth.getCurrentUser().getUid());
                    albumsRecyclerView.setAdapter(albumAdapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initViews(View view) {

        layoutManager = new GridLayoutManager(getActivity(), 2);
        albumsRecyclerView = view.findViewById(R.id.albums_recyclerView);
        albumAdapter = new AlbumAdapter(getActivity(),albumDetailsArrayList,mAuth.getCurrentUser().getUid());
        albumsRecyclerView.setAdapter(albumAdapter);


        albumsRecyclerView.setHasFixedSize(true);
        if (checkIsTablet()) {
            albumsRecyclerView.setLayoutManager(layoutManager);
        } else {
            albumsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        mGenreSpinner = view.findViewById(R.id.spGenre);
        mFabChoosePic = view.findViewById(R.id.fabChoosePic);
        mAlbumCover = view.findViewById(R.id.imgAlbumCover);
        mProposedReleaseDate = view.findViewById(R.id.proposedReleaseDate);
        textViewReleaseTitle = view.findViewById(R.id.tvReleaseTitle);
        textViewArtistRelease = view.findViewById(R.id.tvArtistRelease);
        editTextReleaseTitle = view.findViewById(R.id.etReleaseTitle);

        radioTrackTypeGroup = view.findViewById(R.id.radioGroup);

        relativeLayoutTracks = view.findViewById(R.id.TracksRelative);
        relativeLayoutAlbum = view.findViewById(R.id.AlbumRelative);
        buttonAddTrack = view.findViewById(R.id.btnAddTrack);
        buttonUploadTrack = view.findViewById(R.id.btnUploadTrack);
        editTextTrackTitle = view.findViewById(R.id.etTrackTitle);
        editTextFeatureArtist = view.findViewById(R.id.etFeatureArtist);
        SwitchReleasePaidOrFree = view.findViewById(R.id.ReleasePaidOrFree);
        editTextReleasePrice = view.findViewById(R.id.ReleasePrice);
        ReleasePriceTextInputLayout = view.findViewById(R.id.ReleasePriceHolder);

        getAllAlbums();
        setupSpinner();
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setBirth();
        }
    };

    private void getUserInfo(){
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("stageName")!=null){
                        textViewArtistRelease.setText(map.get("stageName").toString());
                    }

                    if(map.get("firstName")!=null){
                        textViewArtistRelease.setText(map.get("firstName").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
    }

    private void setBirth() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mProposedReleaseDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_gender_options, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGenreSpinner.setAdapter(genderSpinnerAdapter);

        mGenreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    AlbumGenre = selection.toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                AlbumGenre = "";
            }
        });
    }

    private void chooseTrackFile() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Track"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageTrackUri = data.getData();

            uploadTrackCover(imageTrackUri,resultTrackCoverUri);

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();

            resultAlbumCoverUri = imageUri;

            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                mAlbumCover.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();

            resultTrackCoverUri = imageUri;
        }
    }


    private void uploadTrackCover(final Uri trackUri,final Uri trackCoverUri) {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("albums").child(userID).child(AlbumName).child(TrackName);

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
                    uploadTrack(trackUri,downUri.toString());

                }
            }
        });

    }

    private void uploadTrack(Uri trackUri, final String trackCover) {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("albums").child(userID).child(AlbumName).child(TrackName + ".mp3");

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
                    track.setUrl(downUri.toString());
                    track.setTitle(TrackName);
                    track.setFeature(FeaturedArtist);
                    track.setDuration(getDuration(downUri.toString()));
                    track.setType(TrackType);

                    if(!trackCover.isEmpty()){
                        track.setCover(trackCover);
                    }else {
                        track.setCover(downUri.toString());
                    }

                    Map newImage = new HashMap();
                    newImage.put("trackUrl", track.getUrl());
                    newImage.put("trackCover", track.getCover());
                    newImage.put("trackTitle", track.getTitle());
                    newImage.put("trackFeature", track.getFeature());
                    newImage.put("trackType", track.getType());
                    newImage.put("trackDuration", track.getDuration());
                    mCustomerDatabase.child(AlbumName).child("tracks").push().setValue(newImage);

                }
            }
        });

    }

    private void uploadAlbumDetails(Uri albumkUri) {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading Album Details");
        progressDialog.show();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("albums").child(userID).child(AlbumName);

        filePath.putFile(albumkUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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

                    AlbumDetails albumDetails = new AlbumDetails();
                    albumDetails.setRealiseDate(AlbumReleaseDate);
                    albumDetails.setGenre(AlbumGenre);
                    albumDetails.setTitle(AlbumName);
                    albumDetails.setCoverImageUrl(downUri.toString());

                    Map newImage = new HashMap();
                    newImage.put("albumCoverUrl", albumDetails.getCoverImageUrl());
                    newImage.put("albumTitle", albumDetails.getTitle());
                    newImage.put("albumRealiseDate", albumDetails.getRealiseDate());
                    newImage.put("albumGenre", albumDetails.getGenre());
                    newImage.put("albumArtist", userID.trim());

                    if(!editTextReleasePrice.getText().toString().isEmpty()){
                        newImage.put("albumPrice", editTextReleasePrice.getText().toString());
                    }else {
                        newImage.put("albumPrice", 0.00);
                    }

                    //mCustomerDatabase.updateChildren(newImage);
                    //mCustomerDatabase.child(AlbumName).push().setValue(newImage);
                    mCustomerDatabase.child(AlbumName).updateChildren(newImage);
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