package com.projet.test2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        super.onCreate ( savedInstanceState );

        setContentView ( R.layout.activity_main );
        Thread thread=new Thread (){

            @Override
            public void run(){
                try{sleep ( 7000 );

                }
                catch (Exception e)
                {e.printStackTrace ();

                }
                finally {
                    Intent welcomeIntent=new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity ( welcomeIntent );

                }


            }

        };
        thread.start ();
    }
@Override
protected  void onPause(){
        super.onPause ();
    finish ();
    }
}

