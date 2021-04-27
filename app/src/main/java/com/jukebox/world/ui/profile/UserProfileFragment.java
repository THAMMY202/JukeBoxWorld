package com.jukebox.world.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jukebox.world.MainActivity;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.SocialModel;
import com.jukebox.world.adapters.SocialAdapter;
import com.jukebox.world.ui.SocailActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment {
    private EditText editTextName,editTextSurname,editTextEmail,editTextPhone,editTextStageName;
    private CircleImageView mPicture;
    private FloatingActionButton mFabChoosePic,actionButtonAddSocail;
    private Button btnSave;

    private ListView socialListView;
    private List<SocialModel> SocialModellist;
    private DatabaseReference mSocialMediaDatabase;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    private String mProfileImageUrl;
    private Uri resultUri;
    public static Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_user_profile, container, false);

        context = getContext();

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        mSocialMediaDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID).child("social");

        initView(view);
        initListner();

        getUserInfo();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        SocialModellist = new ArrayList<>();
        mSocialMediaDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SocialModellist.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    SocialModel socialModel = postSnapshot.getValue(SocialModel.class);
                    SocialModellist.add(socialModel);
                }

                SocialAdapter arrayAdapter = new SocialAdapter(getActivity(), SocialModellist);
                socialListView.setAdapter(arrayAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("firstName")!=null){
                        editTextName.setText(map.get("firstName").toString());
                    }
                    if(map.get("lastName")!=null){
                        editTextSurname.setText(map.get("lastName").toString());
                    }
                    if(map.get("email")!=null){
                        editTextEmail.setText(map.get("email").toString());
                    }
                    if(map.get("phone")!=null){
                        editTextPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("stageName")!=null){
                        editTextStageName.setText(map.get("stageName").toString());
                    }

                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        //Glide.with(getActivity()).load(mProfileImageUrl).into(mPicture);
                        Glide.with(context).load(mProfileImageUrl).into(mPicture);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveUserInformation() {
        String mName = editTextName.getText().toString();
        String  mPhone = editTextPhone.getText().toString();
        String  mSurname = editTextSurname.getText().toString();
        String mEmail = editTextEmail.getText().toString();
        String mStageName = editTextStageName.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("firstName", mName);
        userInfo.put("lastName", mSurname);
        userInfo.put("email", mEmail);
        userInfo.put("phone", mPhone);
        userInfo.put("stageName", mStageName);
        mCustomerDatabase.updateChildren(userInfo);

        if(resultUri != null) {

            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", firebaseUri.toString());
                  //mCustomerDatabase.updateChildren(newImage);
                    return;
                }
            });


            //add file on Firebase and got Download Link
            filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downUri = task.getResult();
                        Map newImage = new HashMap();
                        newImage.put("profileImageUrl", downUri.toString());
                        mCustomerDatabase.updateChildren(newImage);
                    }
                }
            });



        }else{
            return;
        }

    }

    private void initView(View view){


        editTextName = view.findViewById(R.id.name);
        editTextSurname = view.findViewById(R.id.etLastname);
        editTextEmail = view.findViewById(R.id.etEmail);
        editTextPhone = view.findViewById(R.id.etPhone);
        editTextStageName = view.findViewById(R.id.etArtistname);
        mFabChoosePic = view.findViewById(R.id.fabChoosePic);
        actionButtonAddSocail = view.findViewById(R.id.floatingActionButton);
        mPicture = view.findViewById(R.id.picture);
        socialListView = view.findViewById(R.id.lstSocial);
        btnSave = view.findViewById(R.id.btn_save);

        mFabChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    private void initListner() {

        socialListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Department User = departmentList.get(i);
                //CallUpdateAndDeleteDialog(User.getUserid(), User.getTitle(),User.getEmail(),User.getPhone());
            }
        });

        actionButtonAddSocail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SocailActivity.class));
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;

            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                mPicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}