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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CustomerLogRegActivity extends AppCompatActivity {
    private Button CustomerLogBtn;
    private Button CustomerRegBtn;
    private TextView CustomerLinkText;
    private TextView CustomerStatus;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private DatabaseReference customersDatabaseRef;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;


    private FirebaseUser currentUser;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_customer_log_reg );
        mAuth=FirebaseAuth.getInstance ();

                CustomerLogBtn =(Button) findViewById ( R.id. loginCustomerBtn);
                CustomerRegBtn=(Button) findViewById ( R.id.registerCustomerBtn );
                CustomerLinkText=(TextView) findViewById ( R.id. textCustomerRegister);
                CustomerStatus=(TextView) findViewById ( R.id. textCustomerLogin);
                EmailCustomer=(EditText)findViewById ( R.id.mailCustomer );
                PasswordCustomer=(EditText)findViewById ( R.id.passCustomer );
                loadingbar= new ProgressDialog ( this );


        CustomerLogBtn.setVisibility ( View.VISIBLE );
        CustomerRegBtn.setEnabled ( false);
                CustomerLinkText.setOnClickListener ( new View.OnClickListener ( ) {
                    @Override
                    public void onClick(View v) {
                        CustomerLogBtn.setVisibility ( View.INVISIBLE );
                        CustomerLinkText.setVisibility ( View.INVISIBLE );
                        CustomerStatus.setText ( "register_customer" );

                        CustomerRegBtn.setVisibility ( View.VISIBLE );
                        CustomerRegBtn.setEnabled ( true );
                    }
                } );
                CustomerRegBtn.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                String email  = EmailCustomer.getText ().toString ();
                String password  = PasswordCustomer.getText().toString ();
                RegisterCustomer(email,password);
            }
        } );
                CustomerLogBtn.setOnClickListener ( new View.OnClickListener ( ) {
                    @Override
                    public void onClick(View v) {
                        String email  = EmailCustomer.getText ().toString ();
                        String password  = PasswordCustomer.getText ().toString ();
                        SignInCustomer(email,password);
                    }
                } );


    }
/////////////////login button///////////////////
    private void SignInCustomer(String email , String password) {
        if (TextUtils.isEmpty ( email )){
            Toast.makeText (CustomerLogRegActivity.this,"enter your Email",Toast.LENGTH_SHORT).show (); }
        if (TextUtils.isEmpty ( password)){
            Toast.makeText (CustomerLogRegActivity.this,"enter your password",Toast.LENGTH_SHORT).show (); }
        else {
            loadingbar.setTitle ( "Customer logging" );
            loadingbar.setMessage ( "please wait..." );
            loadingbar.show ();
            mAuth.signInWithEmailAndPassword ( email, password ).addOnCompleteListener ( new OnCompleteListener <AuthResult> ( ) {
                @Override
                public void onComplete(@NonNull Task <AuthResult> task) {
                    if (task.isSuccessful ()){
                        Intent CustomerIntent= new Intent (CustomerLogRegActivity.this,CustomerMapActivity.class);
                        startActivity ( CustomerIntent );
                        Toast.makeText ( CustomerLogRegActivity.this,"logged in",Toast.LENGTH_SHORT ).show ();
                        loadingbar.dismiss ();
                    }
                    else {
                        Toast.makeText ( CustomerLogRegActivity.this,"logging ERROR!",Toast.LENGTH_SHORT ).show ();
                        loadingbar.dismiss (); } }} ); } }


///////////////////////register button/////////////////////////////

    private void RegisterCustomer(String email , String password) {
        if (TextUtils.isEmpty ( email )){
            Toast.makeText (CustomerLogRegActivity.this,"enter your Email",Toast.LENGTH_SHORT).show (); }
        if (TextUtils.isEmpty ( password)){
            Toast.makeText (CustomerLogRegActivity.this,"enter your password",Toast.LENGTH_SHORT).show (); }
        else {
            loadingbar.setTitle ( "customer Registration" );
            loadingbar.setMessage ( "please wait..." );
            loadingbar.show ();
            mAuth.createUserWithEmailAndPassword ( email, password ).addOnCompleteListener ( new OnCompleteListener <AuthResult> ( ) {
                @Override
                public void onComplete(@NonNull Task <AuthResult> task) {
                    if (task.isSuccessful ()){
                        Toast.makeText ( CustomerLogRegActivity.this,"Registered",Toast.LENGTH_SHORT ).show ();
                        currentUserId =  mAuth.getCurrentUser ( ).getUid();
                        customersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(currentUserId);
                        customersDatabaseRef.setValue(true);
                        Intent DriverIntent= new Intent (CustomerLogRegActivity.this,DriverMapActivity.class);
                        startActivity (DriverIntent  );
                        loadingbar.dismiss (); }
                    else {
                        Toast.makeText ( CustomerLogRegActivity.this,"ERROR!",Toast.LENGTH_SHORT ).show ();
                        loadingbar.dismiss (); }
                }
            } );
        }
    }


}

