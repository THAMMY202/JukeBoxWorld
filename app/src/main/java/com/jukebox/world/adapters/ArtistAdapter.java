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
import com.jukebox.world.ViewModel.Artist;
import com.jukebox.world.ui.ArtistDetailsActivity;

import java.util.ArrayList;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder>  {

    private Context context;
    public ArrayList<Artist> albumDetails;

    public ArtistAdapter(Context context, ArrayList<Artist> albumDetails) {
        this.albumDetails = albumDetails;
        this.context = context;
    }

    @NonNull
    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_cardview, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        final Artist album = albumDetails.get(position);
        holder.textViewAlbumTitle.setText(album.getStageName());

        if(album.getProfileImageUrl() != null && !album.getProfileImageUrl().equals("") && !album.getProfileImageUrl().isEmpty()){
            Glide.with(context).load(album.getProfileImageUrl()).into( holder.imageViewCover);
        }else {
            holder.imageViewCover.setImageDrawable(context.getDrawable(R.drawable.artistholder));
        }

        holder.imageViewCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ArtistDetailsActivity.class);
                intent.putExtra("artistImage",album.getProfileImageUrl());
                intent.putExtra("firstName",album.getFirstName());
                intent.putExtra("lastName",album.getLastName());
                intent.putExtra("artistKey",album.getKey());
                context.startActivity(intent);
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

            textViewAlbumTitle = (TextView) itemView.findViewById(R.id.tvArtistName);
            imageViewCover = (ImageView) itemView.findViewById(R.id.imgArtistCover);
        }
    }

    public void updateList(ArrayList<Artist> newList){
        albumDetails = new ArrayList<>();
        albumDetails.addAll(newList);
        notifyDataSetChanged();
    }
}
