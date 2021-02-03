package com.jukebox.world.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AlbumTracksAdapter extends RecyclerView.Adapter<AlbumTracksAdapter.ViewHolder> {

    private static Context context2;
    public Context context;
    public ArrayList<Track> trackArrayList;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public AlbumTracksAdapter(Context context, ArrayList<Track> trackArrayList) {
        this.trackArrayList = trackArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public AlbumTracksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        context2 = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracks_cardview, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Track track = trackArrayList.get(position);
        holder.textViewTrackTitle.setText(track.getTitle());
        String milliseconds =  getDuration(track.getUrl());
        holder.textViewTrackTime.setText(milliseconds);
        Glide.with(context).load(track.getUrl()).into(holder.imageViewCover);

        holder.imageViewPlayTrack.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_media_play));
        holder.imageViewPlayTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (track.getUrl() != null && track.getUrl().length() > 0) {

                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {

                        holder.imageViewPlayTrack.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_media_pause));

                        Runnable mUpdateSeekbar = new Runnable() {
                            @Override
                            public void run() {
                                holder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                mSeekbarUpdateHandler.postDelayed(this, 50);
                            }
                        };

                        try {
                            String url = track.getUrl();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(url);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                            mSeekbarUpdateHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    int duration = mediaPlayer.getCurrentPosition();
                                    String time = String.format("%02d:%02d ", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                                    holder.textViewTrackTime.setText(time);
                                    mSeekbarUpdateHandler.postDelayed(this,1000);
                                }
                            });

                        } catch (Exception e) {

                        }

                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            holder.seekBar.setMax(mediaPlayer.getDuration());
                        }


                    } else {

                        clearMediaPlayer();

                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                        }

                        //mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                        holder.imageViewPlayTrack.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_media_play));
                    }

                }
            }
        });

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

   private Handler mSeekbarUpdateHandler = new Handler();

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
        }
        else {
            return String.format("%02d:%02d" , minute, second);
        }

    }

    @Override
    public int getItemCount() {
        return trackArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewCover;
        public ImageView imageViewPlayTrack;
        public TextView textViewTrackTitle;
        public TextView textViewTrackTime;
        public SeekBar seekBar;


        public ViewHolder(View itemView) {
            super(itemView);

            textViewTrackTitle = (TextView) itemView.findViewById(R.id.tvTrackTitle);
            imageViewCover = (ImageView) itemView.findViewById(R.id.imgTrackAlbum);
            imageViewPlayTrack = (ImageView) itemView.findViewById(R.id.imgPlayTrack);
            textViewTrackTime = (TextView) itemView.findViewById(R.id.tvTrackTime);
            seekBar = (SeekBar) itemView.findViewById(R.id.song_seekbar);
        }
    }

    public void updateList(ArrayList<Track> newList) {
        trackArrayList = new ArrayList<>();
        trackArrayList.addAll(newList);
        notifyDataSetChanged();
    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

}
