package com.projet.test2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLogRegiActivity extends AppCompatActivity {
    private Button DriverLogBtn;
    private Button DriverRegBtn;
    private TextView DriverLinkText;
    private TextView DriverStatus;
    private EditText EmailDriver;
    private EditText PasswordDriver;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_driver_log_regi );
        mAuth=FirebaseAuth.getInstance ();
///////////linking UI///////////////////////////////////////////////////
        DriverLogBtn = findViewById ( R.id. loginDriverBtn);
        DriverRegBtn= findViewById ( R.id.registerDriverBtn );
        DriverLinkText= findViewById ( R.id. textDriverRegister);
        DriverStatus= findViewById ( R.id. textDriverLogin);
        EmailDriver= findViewById ( R.id.mailDriver );
        PasswordDriver= findViewById ( R.id.passDriver );
        loadingBar = new ProgressDialog ( this );
////////////set buttons visible /invisible///////////////////////////////
        DriverLogBtn.setVisibility ( View.VISIBLE );
        DriverRegBtn.setEnabled ( false);
                DriverLinkText.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                DriverLogBtn.setVisibility ( View.INVISIBLE );
                DriverLinkText.setVisibility ( View.INVISIBLE );
                DriverStatus.setText ( "register_driver" );

                DriverRegBtn.setVisibility ( View.VISIBLE );
                DriverRegBtn.setEnabled (true );
            }
        } );


        DriverRegBtn.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                String email  =EmailDriver.getText ().toString ();
                String password  =PasswordDriver.getText ().toString ();
                RegeisterDriver(email,password);
            }
        } );
        DriverLogBtn.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                String email  = EmailDriver.getText ().toString ();
                String password  = PasswordDriver.getText ().toString ();
                SignInDriver(password,email);
            }
        } );
    }
//////////login method////////
    private void SignInDriver(String password , String email) {
        if (TextUtils.isEmpty ( email )){
            Toast.makeText (DriverLogRegiActivity.this,"enter your Email",Toast.LENGTH_SHORT).show ();
        }
        if (TextUtils.isEmpty ( password)){
            Toast.makeText (DriverLogRegiActivity.this,"enter your password",Toast.LENGTH_SHORT).show ();
        }
        else {
            loadingBar.setTitle ( "Driver logging" );
            loadingBar.setMessage ( "please wait..." );
            loadingBar.show ();
            mAuth.signInWithEmailAndPassword ( email, password ).addOnCompleteListener ( new OnCompleteListener <AuthResult> ( ) {
                @Override
                public void onComplete(@NonNull Task <AuthResult> task) {
                    if (task.isSuccessful ()){
                        Toast.makeText ( DriverLogRegiActivity.this,"logged in",Toast.LENGTH_SHORT ).show ();
                        loadingBar.dismiss ();
                        Intent DriverIntent= new Intent (DriverLogRegiActivity.this,DriverMapActivity.class);
                        startActivity (DriverIntent  );
                    }
                    else {
                        Toast.makeText ( DriverLogRegiActivity.this,"logging ERROR!",Toast.LENGTH_SHORT ).show ();
                        loadingBar.dismiss ();
                    }
                }
            } );
        }
    }


//register methodd//////////////////
    private void RegeisterDriver(String email , String password) {
        if (TextUtils.isEmpty ( email )){
            Toast.makeText (DriverLogRegiActivity.this,"enter your Email",Toast.LENGTH_SHORT).show ();
        }
        if (TextUtils.isEmpty ( password)){
            Toast.makeText (DriverLogRegiActivity.this,"enter your password",Toast.LENGTH_SHORT).show ();
        }
        else {
            loadingBar.setTitle ( "Driver Registration" );
            loadingBar.setMessage ( "please wait..." );
            loadingBar.show ();
            mAuth.createUserWithEmailAndPassword ( email, password ).addOnCompleteListener ( new OnCompleteListener <AuthResult> ( ) {
                @Override
                public void onComplete(@NonNull Task <AuthResult> task) {
                    if (task.isSuccessful ()){
                        Toast.makeText ( DriverLogRegiActivity.this,"Registered",Toast.LENGTH_SHORT ).show ();
                        String userID =mAuth.getCurrentUser ().getUid ();
                        //DatabaseReference current_user_db= FirebaseDatabase.getInstance ().getReference ().child (  )
                        loadingBar.dismiss ();
                        Intent DriverIntent= new Intent (DriverLogRegiActivity.this,DriverMapActivity.class);
                        startActivity (DriverIntent  );
                    }
                    else {
                        Toast.makeText ( DriverLogRegiActivity.this,"ERROR!",Toast.LENGTH_SHORT ).show ();
                        loadingBar.dismiss ();
                    }
                }
            } );
        }
    }
}
