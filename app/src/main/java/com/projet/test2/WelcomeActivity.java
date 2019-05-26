package com.projet.test2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WelcomeActivity extends AppCompatActivity {
private Button btnWelcomeCustomer;
private Button btnWelcomeDriver;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_welcom );
        btnWelcomeDriver = (Button) findViewById(R.id.buttonDriver);
        btnWelcomeCustomer   = (Button)findViewById (R.id.buttonCustomer);

       // mAuth = FirebaseAuth.getInstance();

   //firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
        //@Override
        //public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
      // {
            // currentUser = FirebaseAuth.getInstance().getCurrentUser();

             // if(currentUser != null)
            // {
              //   Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                 //  startActivity(intent);
           // }
   //  }
    //  };





        //link to the next screen///////////////////////
        btnWelcomeCustomer.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                Intent loginRegCustomerIntent= new Intent (WelcomeActivity.this,CustomerLogRegActivity.class);
                startActivity ( loginRegCustomerIntent );
            }
        } );
        btnWelcomeDriver.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                Intent loginRegDriverIntent= new Intent (WelcomeActivity.this,DriverLogRegiActivity.class);
                startActivity ( loginRegDriverIntent );
            }
        } );



    }
   // @Override
  // protected void onStart()
  // {
       //super.onStart();
//firebaseAuthListner);
   //}


  // @Override
 //  protected void onStop()
  //{
      //  super.onStop();

     //   mAuth.removeAuthStateListener(firebaseAuthListner);
    //}
}
