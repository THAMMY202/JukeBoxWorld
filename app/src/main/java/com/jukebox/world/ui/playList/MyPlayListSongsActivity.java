package com.jukebox.world.ui.playList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.MyPlayListTrack;
import com.jukebox.world.adapters.MyLibaryAdapter;
import com.jukebox.world.ui.library.MyLibaryFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class MyPlayListSongsActivity extends AppCompatActivity {
    private TextView textViewAudioStatus;
    private String playListName, playListPrimaryKey;
    private String userID;
    private TextView textViewNoSongs;

    private ListView listView;
    private static MyLibaryAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserPlayListDatabase;
    private ArrayList<MyPlayListTrack> myPlayListTrackArrayList = new ArrayList<>();
    private MyPlayListTrack myPlayListTrack = new MyPlayListTrack();

    private ImageView imageViewSongCover, imageViewPlay, imageViewNext;
    private TextView textViewArtistName, textViewSongTitle, textViewSongDuration;
    private RelativeLayout relativeLayoutPlayingMedia;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int selectedPosition = -1;
    private Animation animation;

    private LinearLayout linearLayoutSongs;
    private RelativeLayout relativeLayoutSongMoreDetailsPage;

    private ImageView imageViewSongCoverForFullPage, imageViewNextSongForFullPage, imageViewPreviousSongForFullPage, imageViewCurrentPlaySongForFullPage;
    private TextView textViewSongTitleForFullPage, textViewSongArtistForFullPage, textViewSongStartTimeForFullPage, textViewSongCurrentTimeForFullPage;
    private SeekBar seekBarSongForFullPage;
    private static int oTime = 0, sTime = 0, eTime = 0, fTime = 5000, bTime = 5000;
    private Handler hdlr = new Handler();
    private ProgressDialog progressBar;
    private BarVisualizer mVisualizer;
    private MenuItem menuItemAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playListName = getIntent().getStringExtra("playListName");
        //playListPrimaryKey = getIntent().getStringExtra("playListPrimaryKey");
        getSupportActionBar().setTitle(playListName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_my_play_list_songs);

        initView();
        clickListeners();
    }

    private void initView() {

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("syncing songs...");
        progressBar.show();

        mVisualizer = findViewById(R.id.BarVisualizerFullPage);
        textViewNoSongs = findViewById(R.id.tvNosongs);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mUserPlayListDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("MyPlayLists").child(playListName);

        mediaPlayer = new MediaPlayer();
        textViewAudioStatus = findViewById(R.id.textViewAudioStatus);

        seekBarSongForFullPage = findViewById(R.id.song_seekbar);
        textViewSongTitleForFullPage = findViewById(R.id.imgMoreDetailsSongTitle);
        textViewSongArtistForFullPage = findViewById(R.id.imgMoreDetailsSongArtist);
        textViewSongStartTimeForFullPage = findViewById(R.id.txtStartTime);
        textViewSongCurrentTimeForFullPage = findViewById(R.id.txtSongTime);

        imageViewSongCoverForFullPage = findViewById(R.id.imgMoreDetailsSongCover);
        imageViewPreviousSongForFullPage = findViewById(R.id.imgMoreDetailsSongPrevious);
        imageViewCurrentPlaySongForFullPage = findViewById(R.id.imgMoreDetailsSongPlay);
        imageViewNextSongForFullPage = findViewById(R.id.imgMoreDetailsSongNext);

        linearLayoutSongs = findViewById(R.id.SongLinearLayout);
        relativeLayoutSongMoreDetailsPage = findViewById(R.id.rlPlayingMediaFullPage);
        relativeLayoutPlayingMedia = findViewById(R.id.rlPlayingMedia);
        imageViewNext = findViewById(R.id.imgOutputNext);
        imageViewPlay = findViewById(R.id.imgOutputPlay);
        imageViewSongCover = findViewById(R.id.imgOutputSongCover);
        textViewArtistName = findViewById(R.id.tvOutputArtistName);
        textViewSongTitle = findViewById(R.id.tvOutputSongTitle);
        textViewSongDuration = findViewById(R.id.tvOutputSongDuration);
        listView = findViewById(R.id.lstMyMusic);
    }

    private void clickListeners() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myPlayListTrack = myPlayListTrackArrayList.get(position);
                selectedPosition = position;

                textViewArtistName.setText(myPlayListTrack.getArtist());
                if (myPlayListTrack.getFeature().isEmpty()) {
                    textViewSongTitle.setText(myPlayListTrack.getTitle());
                } else {
                    textViewSongTitle.setText(myPlayListTrack.getTitle() + " (feat. " + myPlayListTrack.getFeature() + " )");
                }

                textViewSongDuration.setText(". " + myPlayListTrack.getDuration());
                // textViewSongDuration.setText(". " + getDuration(myPlayListTrack.getUrl()));
                Glide.with(MyPlayListSongsActivity.this).load(myPlayListTrack.getCover()).into(imageViewSongCover);

                Glide.with(MyPlayListSongsActivity.this).load(myPlayListTrack.getCover()).into(imageViewSongCoverForFullPage);
                textViewSongTitleForFullPage.setText(myPlayListTrack.getTitle());
                if (myPlayListTrack.getFeature().isEmpty()) {
                    textViewSongArtistForFullPage.setText(myPlayListTrack.getArtist());
                } else {
                    textViewSongArtistForFullPage.setText(myPlayListTrack.getArtist() + " & " + myPlayListTrack.getFeature() + " ");
                }

                relativeLayoutPlayingMedia.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(MyPlayListSongsActivity.this, R.anim.left_in);
                relativeLayoutPlayingMedia.startAnimation(animation);

                if (myPlayListTrack.getUrl() != null && myPlayListTrack.getUrl().length() > 0) {

                    if (mediaPlayer != null) {

                        clearMediaPlayer();
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                        }

                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));

                        try {
                            String url = myPlayListTrack.getUrl();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(url);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            textViewAudioStatus.setText("Playing");

                            eTime = mediaPlayer.getDuration();
                            sTime = mediaPlayer.getCurrentPosition();

                            if (oTime == 0) {
                                seekBarSongForFullPage.setMax(eTime);
                                oTime = 1;
                            }

                            //get the AudioSessionId from your MediaPlayer and pass it to the visualizer
                            int audioSessionId = mediaPlayer.getAudioSessionId();
                            if (audioSessionId != -1)
                                mVisualizer.setAudioSessionId(audioSessionId);

                            textViewSongCurrentTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                                    TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
                            textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                                    TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                            seekBarSongForFullPage.setProgress(sTime);

                            hdlr.postDelayed(UpdateSongTime, 100);

                        } catch (Exception e) {

                        }
                    }
                }
            }
        });

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myPlayListTrack.getUrl() != null && myPlayListTrack.getUrl().length() > 0) {

                    if (mediaPlayer.isPlaying() && textViewAudioStatus.getText().toString().equals("Playing")) {
                        mediaPlayer.pause();
                        textViewAudioStatus.setText("Paused");

                        eTime = mediaPlayer.getDuration();
                        sTime = mediaPlayer.getCurrentPosition();

                        if (oTime == 0) {
                            seekBarSongForFullPage.setMax(eTime);
                            oTime = 1;
                        }

                        textViewSongCurrentTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
                        textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                                TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                        seekBarSongForFullPage.setProgress(sTime);

                        hdlr.postDelayed(UpdateSongTime, 100);

                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_play));
                        imageViewCurrentPlaySongForFullPage.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_play));

                    } else if (!mediaPlayer.isPlaying() && textViewAudioStatus.getText().toString().equals("Paused")) {

                        textViewAudioStatus.setText("Playing");
                        mediaPlayer.seekTo(sTime);
                        mediaPlayer.start();

                        eTime = mediaPlayer.getDuration();
                        sTime = mediaPlayer.getCurrentPosition();

                        if (oTime == 0) {
                            seekBarSongForFullPage.setMax(eTime);
                            oTime = 1;
                        }

                        //get the AudioSessionId from your MediaPlayer and pass it to the visualizer
                        int audioSessionId = mediaPlayer.getAudioSessionId();
                        if (audioSessionId != -1)
                            mVisualizer.setAudioSessionId(audioSessionId);

                        textViewSongCurrentTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
                        textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                                TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                        seekBarSongForFullPage.setProgress(sTime);

                        hdlr.postDelayed(UpdateSongTime, 100);


                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));
                        imageViewCurrentPlaySongForFullPage.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));
                    }
                }
            }
        });

        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition += 1;
                animation = AnimationUtils.loadAnimation(MyPlayListSongsActivity.this, R.anim.left_in);
                relativeLayoutPlayingMedia.startAnimation(animation);

                if (myPlayListTrackArrayList.size() > selectedPosition) {

                    MyPlayListTrack dataModel = myPlayListTrackArrayList.get(selectedPosition);

                    textViewArtistName.setText(dataModel.getArtist());
                    if (dataModel.getFeature().isEmpty()) {
                        textViewSongTitle.setText(dataModel.getTitle());
                    } else {
                        textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                    }

                    textViewSongDuration.setText(". " + dataModel.getUrl());
                    Glide.with(MyPlayListSongsActivity.this).load(dataModel.getCover()).into(imageViewSongCover);

                    if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                        if (mediaPlayer != null) {

                            if (mediaPlayer.isPlaying()) {

                                clearMediaPlayer();

                                if (mediaPlayer == null) {
                                    mediaPlayer = new MediaPlayer();
                                }

                            }

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                //get the AudioSessionId from your MediaPlayer and pass it to the visualizer
                                int audioSessionId = mediaPlayer.getAudioSessionId();
                                if (audioSessionId != -1)
                                    mVisualizer.setAudioSessionId(audioSessionId);

                            } catch (Exception e) {

                            }
                        }
                    }

                } else {
                    Toast.makeText(MyPlayListSongsActivity.this, "No more songs", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });


        //End of current page steor buttons

        imageViewSongCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayoutSongs.setVisibility(View.GONE);
                relativeLayoutSongMoreDetailsPage.setVisibility(View.VISIBLE);
                menuItemAdd.setVisible(false);

            }
        });

        imageViewNextSongForFullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition += 1;
                animation = AnimationUtils.loadAnimation(MyPlayListSongsActivity.this, R.anim.left_in);
                relativeLayoutPlayingMedia.startAnimation(animation);

                if (myPlayListTrackArrayList.size() > selectedPosition) {

                    MyPlayListTrack dataModel = myPlayListTrackArrayList.get(selectedPosition);

                    textViewArtistName.setText(dataModel.getArtist());
                    textViewSongArtistForFullPage.setText(dataModel.getArtist());

                    if (dataModel.getFeature().isEmpty()) {
                        textViewSongTitle.setText(dataModel.getTitle());
                        textViewSongTitleForFullPage.setText(dataModel.getTitle());
                    } else {
                        textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                        textViewSongTitleForFullPage.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                    }

                    textViewSongDuration.setText(". " + dataModel.getUrl());


                    Glide.with(MyPlayListSongsActivity.this).load(dataModel.getCover()).into(imageViewSongCover);
                    Glide.with(MyPlayListSongsActivity.this).load(dataModel.getCover()).into(imageViewSongCoverForFullPage);

                    if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                        if (mediaPlayer != null) {

                            if (mediaPlayer.isPlaying()) {

                                clearMediaPlayer();

                                if (mediaPlayer == null) {
                                    mediaPlayer = new MediaPlayer();
                                }

                            }

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));
                            imageViewCurrentPlaySongForFullPage.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                int audioSessionId = mediaPlayer.getAudioSessionId();
                                if (audioSessionId != -1)
                                    mVisualizer.setAudioSessionId(audioSessionId);

                            } catch (Exception e) {

                            }
                        }
                    }

                } else {
                    Toast.makeText(MyPlayListSongsActivity.this, "No more songs", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        imageViewPreviousSongForFullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition -= 1;
                animation = AnimationUtils.loadAnimation(MyPlayListSongsActivity.this, R.anim.left_in);
                relativeLayoutPlayingMedia.startAnimation(animation);

                if (selectedPosition >= 0 && myPlayListTrackArrayList.size() > selectedPosition) {

                    MyPlayListTrack dataModel = myPlayListTrackArrayList.get(selectedPosition);

                    textViewArtistName.setText(dataModel.getArtist());
                    textViewSongArtistForFullPage.setText(dataModel.getArtist());

                    if (dataModel.getFeature().isEmpty()) {
                        textViewSongTitle.setText(dataModel.getTitle());
                        textViewSongTitleForFullPage.setText(dataModel.getTitle());
                    } else {
                        textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                        textViewSongTitleForFullPage.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                    }

                    textViewSongDuration.setText(". " + dataModel.getUrl());


                    Glide.with(MyPlayListSongsActivity.this).load(dataModel.getCover()).into(imageViewSongCover);
                    Glide.with(MyPlayListSongsActivity.this).load(dataModel.getCover()).into(imageViewSongCoverForFullPage);

                    if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                        if (mediaPlayer != null) {

                            if (mediaPlayer.isPlaying()) {

                                clearMediaPlayer();

                                if (mediaPlayer == null) {
                                    mediaPlayer = new MediaPlayer();
                                }

                            }

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));
                            imageViewCurrentPlaySongForFullPage.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                int audioSessionId = mediaPlayer.getAudioSessionId();
                                if (audioSessionId != -1)
                                    mVisualizer.setAudioSessionId(audioSessionId);

                            } catch (Exception e) {

                            }
                        }
                    }

                } else {
                    Toast.makeText(MyPlayListSongsActivity.this, "No more songs", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });


        imageViewCurrentPlaySongForFullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myPlayListTrack.getUrl() != null && myPlayListTrack.getUrl().length() > 0) {

                    if (mediaPlayer.isPlaying() && textViewAudioStatus.getText().toString().equals("Playing")) {
                        mediaPlayer.pause();
                        textViewAudioStatus.setText("Paused");

                        eTime = mediaPlayer.getDuration();
                        sTime = mediaPlayer.getCurrentPosition();

                        if (oTime == 0) {
                            seekBarSongForFullPage.setMax(eTime);
                            oTime = 1;
                        }

                        textViewSongCurrentTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
                        textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                                TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                        seekBarSongForFullPage.setProgress(sTime);

                        hdlr.postDelayed(UpdateSongTime, 100);

                        imageViewCurrentPlaySongForFullPage.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_play));
                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_play));

                    } else if (!mediaPlayer.isPlaying() && textViewAudioStatus.getText().toString().equals("Paused")) {

                        textViewAudioStatus.setText("Playing");
                        mediaPlayer.seekTo(sTime);
                        mediaPlayer.start();

                        eTime = mediaPlayer.getDuration();
                        sTime = mediaPlayer.getCurrentPosition();

                        if (oTime == 0) {
                            seekBarSongForFullPage.setMax(eTime);
                            oTime = 1;
                        }

                        int audioSessionId = mediaPlayer.getAudioSessionId();
                        if (audioSessionId != -1)
                            mVisualizer.setAudioSessionId(audioSessionId);

                        textViewSongCurrentTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
                        textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                                TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                        seekBarSongForFullPage.setProgress(sTime);

                        hdlr.postDelayed(UpdateSongTime, 100);

                        imageViewCurrentPlaySongForFullPage.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));
                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(MyPlayListSongsActivity.this, android.R.drawable.ic_media_pause));
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        myPlayListTrackArrayList.clear();
        mUserPlayListDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myPlayListTrackArrayList.clear();

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    if (childDataSnapshot.getValue() != null) {

                        for (DataSnapshot ing : childDataSnapshot.getChildren()) {
                            myPlayListTrackArrayList.add(ing.getValue(MyPlayListTrack.class));
                        }
                    }
                }

                if (myPlayListTrackArrayList.isEmpty()) {
                    progressBar.dismiss();
                    textViewNoSongs.setVisibility(View.VISIBLE);
                    return;
                } else {
                    adapter = new MyLibaryAdapter(myPlayListTrackArrayList, MyPlayListSongsActivity.this);
                    listView.setAdapter(adapter);
                    textViewNoSongs.setVisibility(View.GONE);
                    progressBar.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVisualizer != null)
            mVisualizer.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_song_main, menu);
        menuItemAdd = menu.findItem(R.id.action_add);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add:
                MyLibaryFragment myLibaryFragment = new MyLibaryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("playListName", playListName);
                bundle.putBoolean("shouldShowAppToPlayList", true);
                myLibaryFragment.setArguments(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flHolder, myLibaryFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static String getDuration(String pathStr) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(pathStr, new HashMap<String, String>());
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        String duration = convertMillieToHMmSs(timeInMillisec); //use this duration

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

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            sTime = mediaPlayer.getCurrentPosition();
            textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime), TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
            seekBarSongForFullPage.setProgress(sTime);
            hdlr.postDelayed(this, 100);
        }
    };

    @Override
    public void onBackPressed() {

        if (relativeLayoutSongMoreDetailsPage.getVisibility() == View.VISIBLE) {
            relativeLayoutSongMoreDetailsPage.setVisibility(View.GONE);
            linearLayoutSongs.setVisibility(View.VISIBLE);
            menuItemAdd.setVisible(true);
        } else {
            super.onBackPressed();
        }
    }
}