package com.jukebox.world.ui.library;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import com.jukebox.world.R;
import com.jukebox.world.ViewModel.MyPlayListTrack;
import com.jukebox.world.adapters.MyLibaryAdapter;
import com.jukebox.world.ui.playList.MyPlayListSongsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MyLibaryFragment extends Fragment {

    private ArrayList<MyPlayListTrack> dataModels;
    private ArrayList<MyPlayListTrack> myPlayListTrackArrayList = new ArrayList<>();
    private MyPlayListTrack dataModel = new MyPlayListTrack();
    private ListView listView;
    private static MyLibaryAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

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

    private String playListName;
    private Boolean shouldShowAppToPlayList = false;
    private Button buttonSaveToPlayList;
    private TextView textViewAudioStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_libary, container, false);

        dataModels = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.lstMyMusic);

        findViewById(view);
        clickListeners();
        return view;
    }

    private void clickListeners() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dataModel = dataModels.get(position);
                selectedPosition = position;

                textViewArtistName.setText(dataModel.getArtist());
                if (dataModel.getFeature().isEmpty()) {
                    textViewSongTitle.setText(dataModel.getTitle());
                } else {
                    textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                }

                textViewSongDuration.setText(". " + getDuration(dataModel.getUrl()));
                Glide.with(getActivity()).load(dataModel.getCover()).into(imageViewSongCover);

                Glide.with(getActivity()).load(dataModel.getCover()).into(imageViewSongCoverForFullPage);
                textViewSongTitleForFullPage.setText(dataModel.getTitle());
                if (dataModel.getFeature().isEmpty()) {
                    textViewSongArtistForFullPage.setText(dataModel.getArtist());
                } else {
                    textViewSongArtistForFullPage.setText(dataModel.getArtist() + " & " + dataModel.getFeature() + " ");
                }

                relativeLayoutPlayingMedia.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getActivity(), R.anim.left_in);
                relativeLayoutPlayingMedia.startAnimation(animation);

                if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                    if (mediaPlayer != null) {

                        clearMediaPlayer();
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                        }

                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                        try {
                            String url = dataModel.getUrl();
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
                if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

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

                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));

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

                        textViewSongCurrentTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(eTime),
                                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
                        textViewSongStartTimeForFullPage.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                                TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                        seekBarSongForFullPage.setProgress(sTime);

                        hdlr.postDelayed(UpdateSongTime, 100);


                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
                    }
                }
            }
        });

        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition += 1;
                animation = AnimationUtils.loadAnimation(getActivity(), R.anim.left_in);
                relativeLayoutPlayingMedia.startAnimation(animation);

                if (dataModels.size() > selectedPosition ) {

                    MyPlayListTrack dataModel = dataModels.get(selectedPosition);

                    textViewArtistName.setText(dataModel.getArtist());
                    if (dataModel.getFeature().isEmpty()) {
                        textViewSongTitle.setText(dataModel.getTitle());
                    } else {
                        textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                    }

                    textViewSongDuration.setText(". " + getDuration(dataModel.getUrl()));
                    Glide.with(getActivity()).load(dataModel.getCover()).into(imageViewSongCover);

                    if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                        if (mediaPlayer != null) {

                            if (mediaPlayer.isPlaying()) {

                                clearMediaPlayer();

                                if (mediaPlayer == null) {
                                    mediaPlayer = new MediaPlayer();
                                }

                            }

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                            } catch (Exception e) {

                            }
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "No more songs", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });


        //End of current page steor buttons


        imageViewNextSongForFullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataModels.size() > selectedPosition + 1) {

                    MyPlayListTrack dataModel = dataModels.get(selectedPosition + 1);

                    textViewArtistName.setText(dataModel.getArtist());
                    if (dataModel.getFeature().isEmpty()) {
                        textViewSongTitle.setText(dataModel.getTitle());
                    } else {
                        textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                    }

                    textViewSongDuration.setText(". " + getDuration(dataModel.getUrl()));
                    Glide.with(getActivity()).load(dataModel.getCover()).into(imageViewSongCover);

                    if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                            } catch (Exception e) {

                            }

                        } else {
                            clearMediaPlayer();
                            if (mediaPlayer == null) {
                                mediaPlayer = new MediaPlayer();
                            }

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                            } catch (Exception e) {

                            }
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "No more song", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        imageViewPreviousSongForFullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int a = dataModels.size();
                int b = selectedPosition - 1;
                if (b >= a) {

                    MyPlayListTrack dataModel = dataModels.get(selectedPosition - 1);

                    textViewArtistName.setText(dataModel.getArtist());
                    if (dataModel.getFeature().isEmpty()) {
                        textViewSongTitle.setText(dataModel.getTitle());
                    } else {
                        textViewSongTitle.setText(dataModel.getTitle() + " (feat. " + dataModel.getFeature() + " )");
                    }

                    textViewSongDuration.setText(". " + getDuration(dataModel.getUrl()));
                    Glide.with(getActivity()).load(dataModel.getCover()).into(imageViewSongCover);

                    if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                            } catch (Exception e) {

                            }

                        } else {
                            clearMediaPlayer();
                            if (mediaPlayer == null) {
                                mediaPlayer = new MediaPlayer();
                            }

                            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                            try {
                                String url = dataModel.getUrl();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                            } catch (Exception e) {

                            }
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "No more song", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        imageViewCurrentPlaySongForFullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dataModel = dataModels.get(selectedPosition);

                if (dataModel.getUrl() != null && dataModel.getUrl().length() > 0) {

                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {

                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));

                        try {
                            String url = dataModel.getUrl();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(url);
                            mediaPlayer.prepare();
                            mediaPlayer.start();

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

                        } catch (Exception e) {

                        }

                    } else {
                        /*clearMediaPlayer();
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                        }

                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));*/
                    }
                }

            }
        });

        imageViewSongCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayoutSongs.setVisibility(View.GONE);
                relativeLayoutSongMoreDetailsPage.setVisibility(View.VISIBLE);
            }
        });

    }

    private void findViewById(View view) {

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("MyBoughtMusic");

        try {
            playListName = this.getArguments().getString("playListName", "");
            shouldShowAppToPlayList = this.getArguments().getBoolean("shouldShowAppToPlayList", false);

        } catch (Exception e) {

        }

        mediaPlayer = new MediaPlayer();
        textViewAudioStatus = view.findViewById(R.id.textViewAudioStatus);
        buttonSaveToPlayList = view.findViewById(R.id.btnAddToPlayList);
        seekBarSongForFullPage = view.findViewById(R.id.song_seekbar);
        textViewSongTitleForFullPage = view.findViewById(R.id.imgMoreDetailsSongTitle);
        textViewSongArtistForFullPage = view.findViewById(R.id.imgMoreDetailsSongArtist);
        textViewSongStartTimeForFullPage = view.findViewById(R.id.txtStartTime);
        textViewSongCurrentTimeForFullPage = view.findViewById(R.id.txtSongTime);

        imageViewSongCoverForFullPage = view.findViewById(R.id.imgMoreDetailsSongCover);
        imageViewPreviousSongForFullPage = view.findViewById(R.id.imgMoreDetailsSongPrevious);
        imageViewCurrentPlaySongForFullPage = view.findViewById(R.id.imgMoreDetailsSongPlay);
        imageViewNextSongForFullPage = view.findViewById(R.id.imgMoreDetailsSongNext);

        linearLayoutSongs = view.findViewById(R.id.SongLinearLayout);
        relativeLayoutSongMoreDetailsPage = view.findViewById(R.id.rlPlayingMediaFullPage);
        relativeLayoutPlayingMedia = view.findViewById(R.id.rlPlayingMedia);
        imageViewNext = view.findViewById(R.id.imgOutputNext);
        imageViewPlay = view.findViewById(R.id.imgOutputPlay);
        imageViewSongCover = view.findViewById(R.id.imgOutputSongCover);
        textViewArtistName = view.findViewById(R.id.tvOutputArtistName);
        textViewSongTitle = view.findViewById(R.id.tvOutputSongTitle);
        textViewSongDuration = view.findViewById(R.id.tvOutputSongDuration);

        dataModels = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.lstMyMusic);

    }

    @Override
    public void onStart() {
        super.onStart();
        dataModels.clear();
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    if (childDataSnapshot.getValue() != null) {

                        for (DataSnapshot ing : childDataSnapshot.getChildren()) {
                            dataModels.add(ing.getValue(MyPlayListTrack.class));
                        }
                    }
                }

                adapter = new MyLibaryAdapter(dataModels, getActivity());
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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

    private void downloadDataTask() {
        class Downloadtask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataModels.clear();

                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                            if (childDataSnapshot.getValue() != null) {

                                for (DataSnapshot ing : childDataSnapshot.getChildren()) {
                                    dataModels.add(ing.getValue(MyPlayListTrack.class));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (dataModels != null && !dataModels.isEmpty()) {
                    adapter = new MyLibaryAdapter(dataModels, getActivity());
                    listView.setAdapter(adapter);

                }

                if (shouldShowAppToPlayList) {
                    buttonSaveToPlayList.setVisibility(View.VISIBLE);
                }

                // progressBar.dismiss();
            }
        }

        Downloadtask dt = new Downloadtask();
        dt.execute();
    }
}