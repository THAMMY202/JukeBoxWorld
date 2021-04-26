package com.jukebox.world.ui.playList;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jukebox.world.R;
import com.jukebox.world.ui.library.MyLibaryToAddFromActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPlayListsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userID;
    private Button Addbutton;
    private ListView listViewPlayList;
    private ArrayList<String> playListArrayList = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_play_lists, container, false);
        intiView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        playListArrayList.clear();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot item_snapshot: dataSnapshot.getChildren()) {
                    playListArrayList.add(item_snapshot.child("name").getValue().toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_textview,playListArrayList);
                listViewPlayList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void intiView(View view){
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("MyPlayList");

        Addbutton = view.findViewById(R.id.btnAddToPlayList);
        listViewPlayList = view.findViewById(R.id.lstPlayListName);


        listViewPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selected = ((TextView) view.findViewById(R.id.text1)).getText().toString();
                Intent intent = new Intent(getActivity(), MyPlayListSongsActivity.class);
                //Intent intent = new Intent(getActivity(), MyLibaryToAddFromActivity.class);
                intent.putExtra("playListName", selected);
                getActivity().startActivity(intent);
            }
        });

        Addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialogBuilder = new AlertDialog.Builder(getActivity()).create();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_playlist_popup, null);

                final EditText editText = (EditText) dialogView.findViewById(R.id.edt_comment);
                Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
                Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogBuilder.dismiss();
                    }
                });

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!editText.getText().toString().isEmpty()) {
                            Map newImage = new HashMap();
                            newImage.put("name", editText.getText().toString());
                            mUserDatabase.push().setValue(newImage);
                            dialogBuilder.dismiss();
                        }else {
                            Toast.makeText(getActivity(),"Play list name is required",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                dialogBuilder.setView(dialogView);
                dialogBuilder.show();
            }
        });
    }
}