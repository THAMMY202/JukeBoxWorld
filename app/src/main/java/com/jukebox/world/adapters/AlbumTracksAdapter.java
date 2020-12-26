package com.jukebox.world.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
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

import java.util.ArrayList;

public class AlbumTracksAdapter extends RecyclerView.Adapter<AlbumTracksAdapter.ViewHolder> {

    private Context context;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracks_cardview, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Track track = trackArrayList.get(position);
        holder.textViewTrackTitle.setText(track.getTitle());
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



    @Override
    public int getItemCount() {
        return trackArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewCover;
        public ImageView imageViewPlayTrack;
        public TextView textViewTrackTitle;
        public SeekBar seekBar;


        public ViewHolder(View itemView) {
            super(itemView);

            textViewTrackTitle = (TextView) itemView.findViewById(R.id.tvTrackTitle);
            imageViewCover = (ImageView) itemView.findViewById(R.id.imgTrackAlbum);
            imageViewPlayTrack = (ImageView) itemView.findViewById(R.id.imgPlayTrack);
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
