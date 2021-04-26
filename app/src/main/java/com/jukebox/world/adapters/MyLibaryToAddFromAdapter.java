package com.jukebox.world.adapters;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jukebox.world.R;
import com.jukebox.world.ViewModel.MyPlayListTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MyLibaryToAddFromAdapter extends ArrayAdapter<MyPlayListTrack>{

private ArrayList<MyPlayListTrack> dataSet;
        Context mContext;

private static class ViewHolder {
    CheckBox chkIsAdded;
    TextView txtArtistName;
    TextView txtSongTitle;
    TextView txtSongDuration;
    ImageView imgSongCover;
}

    public MyLibaryToAddFromAdapter(ArrayList<MyPlayListTrack> data, Context context) {
        super(context, R.layout.custome_mylibary_info, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyPlayListTrack dataModel = getItem(position);
        ViewHolder viewHolder;

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custome_mylibary_info, parent, false);
            viewHolder.txtArtistName = (TextView) convertView.findViewById(R.id.tvListArtistName);
            viewHolder.txtSongTitle = (TextView) convertView.findViewById(R.id.tvListSongTitle);
            viewHolder.txtSongDuration = (TextView) convertView.findViewById(R.id.tvListSongDuration);
            viewHolder.imgSongCover = (ImageView) convertView.findViewById(R.id.imgListSongCover);
            viewHolder.chkIsAdded = convertView.findViewById(R.id.checkBox1);

            convertView.setTag(viewHolder);

        viewHolder.txtArtistName.setText(dataModel.getArtist());

        if(dataModel.getFeature().isEmpty()){
            viewHolder.txtSongTitle.setText(dataModel.getTitle());
        }else {
            viewHolder.txtSongTitle.setText(dataModel.getTitle() + "(feat. " + dataModel.getFeature()+")");
        }

        viewHolder.txtSongDuration.setText( ". "+ getDuration(dataModel.getUrl()));
        Picasso.get().load(dataModel.getCover()).into(viewHolder.imgSongCover);

        viewHolder.chkIsAdded.setChecked(dataModel.isSelected());

        viewHolder.chkIsAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                MyPlayListTrack myPlayListTrack  = (MyPlayListTrack) cb.getTag();
                Toast.makeText(mContext,"Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),Toast.LENGTH_LONG).show();
                myPlayListTrack.setSelected(cb.isChecked());
            }
        });

        return convertView;
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
}
