package com.example.socialchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity
{
    //Make new instance
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    //Instance toolbar
    private Toolbar mToolbar;

    //Progress Dialog
    private ProgressDialog mRegProgress;

    //Instance Firebase Auth
    private FirebaseAuth mAuth;

    //Instance Firebase Database --> to read or write data from the database
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initieer Progress Dialog
        mRegProgress = new ProgressDialog(this);

        //Instance Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Assign the instances --> Registration Fields
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        //If user clicks on this button --> registrer to firebase db
        mCreateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Get the values from the textboxes
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                //If textfields are not blank --> start registration proces
                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account!");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name, email, password);
                }


            }
        });
    }

    //Make new method to register a user
    private void register_user(final String display_name, String email, String password)
    {
        //Create user with email + password
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                //Check if user is registrered
               if(task.isSuccessful())
               {
                   //Get current user
                   FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                   //Get user id
                   String uid = current_user.getUid();

                   //Store the root with child object and that child object:
                   //mDatabase = root, "users" = child and "uid" is child of "users".
                   mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                   //Get the tokenId
                   String deviceToken = FirebaseInstanceId.getInstance().getToken();

                   //Store the three values: image-name-status
                   HashMap<String, String> userMap = new HashMap<>();

                   //Put the values in the userMap
                   userMap.put("name", display_name);
                   userMap.put("status", "Hello, I'm using Social Chat");
                   userMap.put("image", "default");
                   userMap.put("thumb_image", "default");
                   userMap.put("device_token", deviceToken);

                   //Add values to database
                   mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
                   {
                       @Override
                       public void onComplete(@NonNull Task<Void> task)
                       {
                           //If task is successful --> close registration dialog
                           if(task.isSuccessful())
                           {
                               mRegProgress.dismiss();

                               //Send user to main activity
                               Intent main_Intent = new Intent(RegisterActivity.this, MainActivity.class);
                               main_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               startActivity(main_Intent);
                               finish();

                           }
                       }
                   });

               }
               else
               {
                   //If the registration failed
                  mRegProgress.hide();

                   //Make toast error
                   Toast.makeText(RegisterActivity.this, "Cannot create a new account. Please try again!", Toast.LENGTH_LONG).show();
               }
            }
        });

    }
}
