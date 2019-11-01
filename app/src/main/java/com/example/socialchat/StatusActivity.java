package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity
{
    //Declare the instance of Toolbar
    private Toolbar mToolbar;

    //Declare instance of Status Activity
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Instance Firebase Database --> to read or write data from the database
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;


    //Instance Progress Dialog
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Instance Current User UID
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();




        //Instance Firebase
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        //Define the mToolbar
        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Retrieve the string from the status value
        String status_value = getIntent().getStringExtra("status_value");



        //Define Status Activity
        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveBtn = (Button) findViewById(R.id.status_save_btn);

        //Set the status like the status value
        mStatus.getEditText().setText(status_value);

        //Make onClickListener for Button
        mSaveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //Define Progress Dialog
                mProgress = new ProgressDialog(StatusActivity.this);
                //If user clicks on button --> progress dialog
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes.");
                mProgress.show();


                //Get value from text input layout
                String status = mStatus.getEditText().getText().toString();

                //Set the status to the status that is typed in
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        //Check if status is changed/updated
                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();

                        }
                        else
                        {
                            //Show this toast message
                            Toast.makeText(getApplicationContext(), "There was some error in saving changes.", Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        });
    }
}

