package com.jukebox.world;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextEmail, editTextPassword, editTextStagName;
    private Button buttonSignUp;
    private CheckBox checkBoxUserRole;

    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    FirebaseUser user = null;

    private DatabaseReference mCustomerDatabase;
    private String userID, userRole = "Artist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initView();
    }

    private void initView() {

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        editTextName = findViewById(R.id.fullName);
        editTextPhone = findViewById(R.id.phone);
        editTextEmail = findViewById(R.id.Email);
        checkBoxUserRole = findViewById(R.id.chkUserRole);
        editTextStagName = findViewById(R.id.etStageName);
        editTextPassword = findViewById(R.id.password);
        buttonSignUp = findViewById(R.id.registerBtn);

        TextView textViewSignInLink = findViewById(R.id.createText);
        textViewSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });


        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

        checkBoxUserRole.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    userRole = "Artist";
                    editTextStagName.setVisibility(View.VISIBLE);
                } else {
                    userRole = "Customer";
                    editTextStagName.setVisibility(View.GONE);
                }
            }
        });

    }

    private void createUser() {

        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String stagName = editTextStagName.getText().toString().trim();

        if (userRole.equals("Artist")) {

            if (TextUtils.isEmpty(stagName)) {
                Toast.makeText(getApplicationContext(), "Enter stagName!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Enter your name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                        } else {

                            userID = auth.getCurrentUser().getUid();
                            mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

                            Map userInfo = new HashMap();
                            userInfo.put("email", email);
                            userInfo.put("password", password);
                            userInfo.put("phone", phone);
                            userInfo.put("firstName", name);
                            userInfo.put("role", userRole);
                            if (userRole.equals("Artist")) {
                                userInfo.put("stageName", stagName);
                            }

                            mCustomerDatabase.updateChildren(userInfo);

                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}