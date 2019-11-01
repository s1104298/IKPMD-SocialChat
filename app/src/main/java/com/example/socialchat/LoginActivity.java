package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity
{

    //Instance toolbar
    private Toolbar mToolbar;

    //Instance Firebase Auth
    private FirebaseAuth mAuth;

    //Make new instance
    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLogin_btn;

    //Instance Progress Dialog
    private ProgressDialog mLoginProgress;

    //Instance DB Ref
    private DatabaseReference mUserDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Instance Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initiate Progress Dialog
        mLoginProgress = new ProgressDialog(this);

        //Database Ref
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        //Assign the instances --> Login Fields
        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);
        mLogin_btn = (Button) findViewById(R.id.login_btn);


        //If user clicks on this button --> login to firebase db
        mLogin_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();


                //If textfields are not blank --> start login proces
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email, password);
                }
            }
        });


    }

    //Make new method for user to login
    private void loginUser(String email, String password)
    {
        //Sign in user with email + password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                //Check if user is signed in
                if(task.isSuccessful())
                {
                    mLoginProgress.dismiss();

                    //Get user_id
                    String current_user_id = mAuth.getCurrentUser().getUid();

                    //Get the tokenId
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    //Store the tokenId
                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken)
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            //Send user to main activity
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                }
                else
                {
                    //If the registration failed
                    mLoginProgress.hide();

                    //Make toast error
                    Toast.makeText(LoginActivity.this, "Cannot sign in. Please try again!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
