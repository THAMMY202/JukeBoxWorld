package com.jukebox.world.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jukebox.world.R;
import com.jukebox.world.ViewModel.AdsSlideViewModel;
import com.jukebox.world.ViewModel.SocialModel;

import java.util.HashMap;
import java.util.Map;

public class SocailActivity extends AppCompatActivity {
    private EditText editTextTitle,editTextUrl;
    private Button buttonAdd;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socail);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        editTextTitle = findViewById(R.id.edtName);
        editTextUrl = findViewById(R.id.edtUrl);
        buttonAdd = findViewById(R.id.btnAdd);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editTextTitle.getText().toString().isEmpty()){
                    editTextTitle.setError("required");
                    return;
                }

                String ImageUploadId = mCustomerDatabase.push().getKey();
                SocialModel socialModel = new SocialModel(ImageUploadId , editTextTitle.getText().toString(),editTextUrl.getText().toString());
                mCustomerDatabase.child("social").child(ImageUploadId).setValue(socialModel);
            }
        });
    }
}