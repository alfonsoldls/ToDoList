package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends AppCompatActivity  implements Animation.AnimationListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ConstraintLayout constraintLayout = findViewById(R.id.icongroup);
        Animation animsplash = AnimationUtils.loadAnimation(this, R.anim.splashanimation);
        constraintLayout.startAnimation(animsplash);
        animsplash.setAnimationListener(this);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Intent intent1 = new Intent(Splash.this, Login.class);
        startActivity(intent1);
        finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}