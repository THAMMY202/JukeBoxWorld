package com.jukebox.world.ui;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.jukebox.world.R;
import com.jukebox.world.SignInActivity;


public class SplashActivity extends FragmentActivity {

    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);


        if (savedInstanceState == null) {
            flyIn();
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                endSplash();
            }
        }, 4000);
    }

    private void flyIn() {
        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
    }

    private void endSplash() {

        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
