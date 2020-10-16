package com.jukebox.world;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicCategoryRecyclerViewAdapter extends RecyclerView.Adapter<MusicCategoryRecyclerViewAdapter.ViewHolder> {

    private List<String> mCategories;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

  public MusicCategoryRecyclerViewAdapter(Context context,List<String> categories) {
        this.mInflater = LayoutInflater.from(context);
        this.mCategories = categories;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String animal = mCategories.get(position);
        holder.CategoryTextView.setText(animal);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView CategoryTextView;

        ViewHolder(View itemView) {
            super(itemView);
            CategoryTextView = itemView.findViewById(R.id.tvCategory);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public String getItem(int id) {
        return mCategories.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}