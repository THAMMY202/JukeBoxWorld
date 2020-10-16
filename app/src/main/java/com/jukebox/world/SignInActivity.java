package com.jukebox.world;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSignIn;
    private TextView textViewNewAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initView();
    }

    private void initView() {
        buttonSignIn = findViewById(R.id.btnlogin);
        buttonSignIn.setOnClickListener(this);

        textViewNewAccount = findViewById(R.id.createnewac);
        textViewNewAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.btnlogin:
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                break;

            case R.id.createnewac:
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                break;
        }
    }
}