package com.jukebox.world.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jukebox.world.MusicCategoryRecyclerViewAdapter;
import com.jukebox.world.R;

import java.util.ArrayList;
import java.util.Hashtable;

public class HomeFragment extends Fragment implements MusicCategoryRecyclerViewAdapter.ItemClickListener {

    private MusicCategoryRecyclerViewAdapter adapter;
    private Hashtable items = new Hashtable();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.rvCategory);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        adapter = new MusicCategoryRecyclerViewAdapter(getActivity(),populateAdapter());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        return root;
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private ArrayList<String> populateAdapter(){
        ArrayList<String> categoriesArrayList = new ArrayList<>();

        categoriesArrayList.add("All");
        categoriesArrayList.add("Maskandi");
        categoriesArrayList.add("Gospel");
        categoriesArrayList.add("House");

        return categoriesArrayList;
    }
}