package com.jukebox.world.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jukebox.world.R;
import com.jukebox.world.ViewModel.SocialModel;

import java.util.List;

public class SocialAdapter extends ArrayAdapter<SocialModel> {

    private Activity context;
    List<SocialModel> list;

    public SocialAdapter(Activity context, List<SocialModel> departmentList) {
        super(context, R.layout.layout_socail, departmentList);
        this.context = context;
        this.list = departmentList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_socail, null, true);

        TextView textViewName =  listViewItem.findViewById(R.id.tvSocailTitle);
        TextView textviewemail =  listViewItem.findViewById(R.id.tvSocailUrl);
        ImageView imageViewSocailIcon =  listViewItem.findViewById(R.id.imgSocailIcon);

        SocialModel socialModel = list.get(position);
        textViewName.setText(socialModel.getName());

        if(!socialModel.getUrl().isEmpty()){
            //textviewemail.setText(socialModel.getUrl());
            textviewemail.setText("Added");
        }else {
            textviewemail.setText("Not added");
        }

        //textviewnumber.setText(department.getPhone());
        return listViewItem;
    }
}
