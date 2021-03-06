package com.jukebox.world.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jukebox.world.AlbumTracksActivity;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.AlbumDetails;
import com.jukebox.world.ui.newrelease.UploadTracksToAlbumActivity;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>  {

    private Context context;
    public ArrayList<AlbumDetails> albumDetails;
    private String currentUserId;

    public AlbumAdapter(Context context, ArrayList<AlbumDetails> albumDetails,String currentUserId) {
        this.albumDetails = albumDetails;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.albums_cardview, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        final AlbumDetails album = albumDetails.get(position);
        holder.textViewAlbumTitle.setText(album.getTitle());
        Glide.with(context).load(album.getCoverImageUrl()).into( holder.imageViewCover);

        holder.imageViewCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumTracksActivity.class);
                intent.putExtra("albumTitle",album.getTitle());
                intent.putExtra("albumPrice",album.getPrice());
                intent.putExtra("albumCover",album.getCoverImageUrl());
                intent.putExtra("albumArtist",album.getArtist());
                context.startActivity(intent);
            }
        });

        holder.imageViewCover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(currentUserId.equalsIgnoreCase(album.getArtist())){
                    Intent intent = new Intent(context, UploadTracksToAlbumActivity.class);
                    intent.putExtra("albumTitle",album.getTitle());
                    intent.putExtra("albumPrice",album.getPrice());
                    intent.putExtra("albumCover",album.getCoverImageUrl());
                    intent.putExtra("albumArtist",album.getArtist());
                    context.startActivity(intent);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumDetails.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewCover;
        public TextView textViewAlbumTitle;


        public ViewHolder(View itemView) {
            super(itemView);

            textViewAlbumTitle = (TextView) itemView.findViewById(R.id.tvAlbumTitleCd);
            imageViewCover = (ImageView) itemView.findViewById(R.id.imgAlbumCoverCd);
        }
    }

    public void updateList(ArrayList<AlbumDetails> newList){
        albumDetails = new ArrayList<>();
        albumDetails.addAll(newList);
        notifyDataSetChanged();
    }
}
