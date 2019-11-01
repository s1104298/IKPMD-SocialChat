package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity
{
    //Declare the layout of the profile
    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mDeclineBtn;

    //Create DatabaseReferences
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    //Create ProgressDialog
    private ProgressDialog mProgressDialog;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        //Initiate the root ref
        mRootRef = FirebaseDatabase.getInstance().getReference();

        //Initiate the databases
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        //Initiate the layout of the profile
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        //Set the friend state
        mCurrent_state = "not_friends";

        //Set the decline button
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        //Initialize the Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



        mUsersDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.defaultimage).into(mProfileImage);

                //Buttons should not be shown at your own profile
                if(mCurrent_user.getUid().equals(user_id))
                {

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);

                }


                //FRIENDS LIST & REQUEST FEATURE

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        //Check if user-object has the id of the friend
                        if(dataSnapshot.hasChild(user_id))
                        {
                            //Get request type
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            //If he/she received the request
                            if(req_type.equals("received"))
                            {

                                //Change current state
                                mCurrent_state = "req_received";

                                //Change text of this button
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                //Make the decline button appear
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);


                            }
                            //If we have sent him/her the request
                            else if(req_type.equals("sent"))
                            {

                                //Change current state
                                mCurrent_state = "req_sent";

                                //Change text of this button
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                //Make the decline button disappear
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();


                        } else {


                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {

                                    //Check if you're friends already
                                    if(dataSnapshot.hasChild(user_id)){

                                        //Change current state
                                        mCurrent_state = "friends";

                                        //Change text of this button
                                        mProfileSendReqBtn.setText("Unfriend this Person");

                                        //Make the decline button disappear
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        //Create listener for send request btn
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                mProfileSendReqBtn.setEnabled(false);

                //NOT FRIENDS STATE

                //If not friends --> send request
                if(mCurrent_state.equals("not_friends"))
                {


                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    //To store data in "Notification" Firebase --> create HashMap
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    //Set values when the other user is recieving the request
                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {

                            if(databaseError != null)
                            {
                                //Send toast
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            }
                            else
                                {
                                    //Change current state
                                    mCurrent_state = "req_sent";

                                    //Change text of this button
                                    mProfileSendReqBtn.setText("Cancel Friend Request");

                            }

                            mProfileSendReqBtn.setEnabled(true);


                        }
                    });

                }


                //CANCEL REQUEST STATE

                if(mCurrent_state.equals("req_sent"))
                {
                    //Delete the person you've sent the request
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    //If request is canceled --> enable the button
                                    mProfileSendReqBtn.setEnabled(true);

                                    //Change current state
                                    mCurrent_state = "not_friends";

                                    //Change text of this button
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    //Make the decline button disappear
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });

                        }
                    });

                }


                //REQUEST RECEIVED STATE

                if(mCurrent_state.equals("req_received"))
                {

                    //Store the date
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    //Create hashmap
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {


                            if(databaseError == null)
                            {

                                mProfileSendReqBtn.setEnabled(true);

                                //Change current state
                                mCurrent_state = "friends";

                                //Change text of this button
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                //Make the decline button disappear
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            else
                                {
                                    String error = databaseError.getMessage();

                                    //Create toast
                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                                }

                        }
                    });

                }


                //UNFRIEND

                if(mCurrent_state.equals("friends"))
                {
                    //Create hashmap
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {


                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            else
                                {

                                    String error = databaseError.getMessage();

                                    //Create toast
                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                                }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }


            }
        });


    }

}


