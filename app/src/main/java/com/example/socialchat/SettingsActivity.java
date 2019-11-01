package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class SettingsActivity extends AppCompatActivity
{

    //Instance Firebase Database --> to read or write data from the database
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //Declare the instances for Settings LayOut
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;

    private static final int GALLERY_PICK = 1;

    //Create storage reference for Firebase
    private StorageReference mProfileImageStorage;

    //Progres Dialog when user starts uploading profile image
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize the instances for Settings Lay Out
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);

        mStatusBtn = (Button) findViewById(R.id.settings_status_btn);
        mImageBtn = (Button) findViewById(R.id.settings_image_btn);

        //Initialize the instance for image storage in Firebase
        mProfileImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Reference to "User" object
        String current_uid = mCurrentUser.getUid();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        //Offline capability firebase
        mUserDatabase.keepSynced(true);


        //Retrieve the child objects from User object
        mUserDatabase.addValueEventListener(new ValueEventListener()
        {
            //When you retrieve data or data is changed --> this method will start
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String name = (String) dataSnapshot.child("name").getValue();
                final String image = (String) dataSnapshot.child("image").getValue();
                String status = (String) dataSnapshot.child("status").getValue();
                String thumb_image = (String) dataSnapshot.child("thumb_image").getValue();


                //Change the values
                mName.setText(name);
                mStatus.setText(status);

                //Check if image is NOT default --> then load the image
                if(!image.equals("default"))
                {

                    //Load the profile image offline
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.defaultimage).into(mDisplayImage, new Callback()
                    {

                        @Override
                        public void onSuccess()
                        {
                            //Do nothing

                        }
                        //What if the image is not stored offline
                        //Load the image online
                        @Override
                        public void onError(Exception e)
                        {
                            Picasso.get().load(image).placeholder(R.drawable.defaultimage).into(mDisplayImage);
                        }
                    });

                }



            }

            //Method for handling errors
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        //Method to make mStatusBtn work
        mStatusBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Make a string of the status textfield
                String status_value = mStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                //Send the input of the status to the status settings
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });

         //Method to make mImageBtn work
        mImageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //Make an intent to get images from your phone
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");       //We only wanna get images
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT); //We get the content from there
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });


    }

    //Method to get the crop results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //if we haven't an error
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            //Getting the Data of the Image and saving it in a Uri.
            Uri imageUri = data.getData();

            //Start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

        }

        //Check if the image is cropped or not
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            //The result is stored in the "result" variable
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //Check if the result is Ok or not. OK --> store the image in a uri.
            if (resultCode == RESULT_OK)
            {
                //Show progress dialog if user is uploading a profile image
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while we upload en process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                //To make a thumbnail
                File thumb_filePath = new File(resultUri.getPath());


                //Getting the Current UID of the User and storing it in a String.
                final String current_user_id = mCurrentUser.getUid();

                //Compress to make a thumbnail
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(80)
                        .compressToBitmap(thumb_filePath);

                //Store the bitmap in firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                //Saving the image in the Firebase Storage and naming the child with the UID.
                final StorageReference filepath = mProfileImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mProfileImageStorage.child("profile_images").child("thumb_image").child(current_user_id + ".jpg");

                //If the resultUri is nor Empty or NULL.
                if (resultUri != null)
                {
                    //Setup an OnCompleteListener to store the image in the desired location in the storage
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            //If task is successfull when we display a toast
                            if (task.isSuccessful())
                            {
                                mProfileImageStorage.child("profile_images").child(current_user_id + ".jpg")
                                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                {
                                    @Override
                                    public void onSuccess(Uri image_uri)
                                    {
                                        //Download URL for the image
                                        final String downloadUrl = image_uri.toString();

                                        //Create upload task to upload the thumbnail in Firebase
                                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                            {
                                                //Download URL for the thumbnail
                                                String thumb_downloadUrl = thumb_task.toString();

                                                if(thumb_task.isSuccessful())
                                                {
                                                    //Create Map
                                                    Map update_hashMap = new HashMap();
                                                    update_hashMap.put("image", downloadUrl);
                                                    update_hashMap.put("thumb_image", thumb_downloadUrl);

                                                    mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            //If profile image is uploaded
                                                            if(task.isSuccessful())
                                                            {
                                                                mProgressDialog.dismiss();
                                                                Toast.makeText(SettingsActivity.this, "The profile image is uploaded!", Toast.LENGTH_LONG).show();
                                                            }

                                                        }
                                                    });

                                                }
                                                else
                                                {
                                                    //If thumbnail is not uploaded
                                                    Toast.makeText(SettingsActivity.this, "The thumbnail is not uploaded. Please try again!", Toast.LENGTH_LONG).show();
                                                    mProgressDialog.dismiss();

                                                }


                                            }
                                        });

                                    }
                                });
                            }
                            else
                                {
                                    //If profile image is not uploaded
                                    Toast.makeText(SettingsActivity.this, "The profile image is not uploaded. Please try again!", Toast.LENGTH_LONG).show();
                                    mProgressDialog.dismiss();
                                }
                        }
                    });
                }

            }
            //Check if the result is Ok or not. not OK --> error.
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

}
