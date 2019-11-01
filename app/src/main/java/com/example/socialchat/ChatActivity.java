package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity
{
    //Define user
    private String mChatUser;

    //Define toolbar
    private Toolbar mChatToolbar;

    //Define DBRef
    private DatabaseReference mRootRef;

    //Define custom toolbar
    private TextView mNameView;
    private TextView mLastSeenView;
    private CircleImageView mImage;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;


    //Define the buttons + message field
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    //Define to retrieve and store messages
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    //Variabele to load x items
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    //After refreshing --> set messages in reverse from bottom to top
    private int itemPosition = 0;

    //Variabele to get the last key of each page
    private String mLastKey = "";

    private String mPreviousKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //Initialize toolbar
        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        //Set custom bar to main toolbar
        ActionBar customActionBar = getSupportActionBar();

        customActionBar.setDisplayHomeAsUpEnabled(true);
        customActionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        //Receive the user id + name
        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        //Set title to username
        //getSupportActionBar().setTitle(userName);

        //Set layout inflater
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Set a view
        View customActionBarView = inflater.inflate(R.layout.chat_custom_bar, null);
        customActionBar.setCustomView(customActionBarView);


        //Initialize custom action bar items
        mNameView = (TextView) findViewById(R.id.custom_bar_name);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mImage = (CircleImageView) findViewById(R.id.custom_bar_image);


        //Define the buttons and message field
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);


        //Method to load messages
        loadMessages();


        //Retrieve name, last seen and image
        mNameView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                Picasso.get().load(image).placeholder(R.drawable.defaultimage).into(mImage);


                //Add the data
                if( online.equals("true"))
                {
                    //User is online
                    mLastSeenView.setText("Online");

                }
                else
                {
                    //Show last seen time
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    //User is offline
                    mLastSeenView.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //Check if it does not contains the user id --> create one
                if(!dataSnapshot.hasChild(mChatUser))
                {
                    //Create two maps
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference)
                        {
                            if(databaseError != null)
                            {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        //Create ClickOnListener for chat send button
        mChatSendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Go to this method
                sendMessage();

            }
        });



        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                //Load more items/chats
                mCurrentPage++;

                //Set itemposition to 0
                itemPosition = 0;


                //Load the messages
                loadMoreMessages();
            }
        });


    }

    private void loadMoreMessages()
    {
        DatabaseReference msgRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);

        Query msgQuery = msgRef.orderByKey().endAt(mLastKey).limitToLast(10);

        msgQuery.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                Messages message = dataSnapshot.getValue(Messages.class);
                //Get key of first item when messages are loaded
                String messageKey = dataSnapshot.getKey();


                if(!mPreviousKey.equals(messageKey))
                {
                    //After refreshing --> data goes from bottom to top
                    messagesList.add(itemPosition++ ,message);

                }
                else
                {
                    mPreviousKey = mLastKey;
                }

                if(itemPosition == 1)
                {
                    mLastKey = messageKey;

                }


                Log.d("TOTAL_KEYS", "Last Key : " + mLastKey + " | Previous Key : " + mPreviousKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                //After loading the data --> stop refreshing
                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    //Method to load messages
    private void loadMessages()
    {
        DatabaseReference msgRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);

        Query msgQuery = msgRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        //Retrieve the data and load it
        msgQuery.addChildEventListener(new ChildEventListener()
        {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                Messages message = dataSnapshot.getValue(Messages.class);

                //Get the key of position 10
                itemPosition ++;

                if(itemPosition == 1)
                {
                    //Get key of first item when messages are loaded
                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPreviousKey = messageKey;


                }


                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() -1);

                //After loading the data --> stop refreshing
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    //Method to send messages
    private void sendMessage()
    {
        //Retrieve the message
        String message = mChatMessageView.getText().toString();

        //Check if message is empty or not
        if(!TextUtils.isEmpty(message))
        {
            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;


            //Push ID for the message
            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            //Get the push ID
            String pushID = user_message_push.getKey();

            //Add message to database
            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            //Map stores values of the users
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + pushID, messageMap);
            messageUserMap.put(chat_user_ref + "/" + pushID, messageMap);

            //Clear chatbalk after sending a message
            mChatMessageView.setText("");


            //Store the data in rootref
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener()
            {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference)
                {
                    if(databaseError != null)
                    {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });


        }
    }
}
