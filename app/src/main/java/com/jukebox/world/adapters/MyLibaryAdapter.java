package com.jukebox.world.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.MyPlayListTrack;
import java.util.ArrayList;


public class MyLibaryAdapter extends ArrayAdapter<MyPlayListTrack> {

   private Context mContext;

    private static class ViewHolder {
        TextView txtArtistName;
        TextView txtSongTitle;
        TextView txtSongDuration;
        ImageView imgSongCover;
    }

    public MyLibaryAdapter(ArrayList<MyPlayListTrack> data, Context context) {
        super(context, R.layout.my_play_list_row, data);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyPlayListTrack dataModel = getItem(position);
        ViewHolder viewHolder;

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.my_play_list_row, parent, false);
        viewHolder.txtArtistName = convertView.findViewById(R.id.tvListArtistName);
        viewHolder.txtSongTitle = convertView.findViewById(R.id.tvListSongTitle);
        viewHolder.txtSongDuration = convertView.findViewById(R.id.tvListSongDuration);
        viewHolder.imgSongCover = convertView.findViewById(R.id.imgListSongCover);

        convertView.setTag(viewHolder);

        viewHolder.txtArtistName.setText(dataModel.getArtist());

        if (dataModel.getFeature().isEmpty()) {
            viewHolder.txtSongTitle.setText(dataModel.getTitle());
        } else {
            viewHolder.txtSongTitle.setText(dataModel.getTitle() + "(feat. " + dataModel.getFeature() + ")");
        }

        viewHolder.txtSongDuration.setText(String.format("." + dataModel.getTrackDuration()));
        //viewHolder.txtSongDuration.setText( ". "+ getDuration(dataModel.getUrl()));
        Glide.with(mContext).load(dataModel.getCover()).into(viewHolder.imgSongCover);
        return convertView;
    }
}
