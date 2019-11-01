package com.example.socialchat;


import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment
{
    //Declerations
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mView;

    public FriendsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mView;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        //Firebase
        FirebaseRecyclerOptions<Friends> options =

                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery( mFriendsDatabase, Friends.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull final Friends friends) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String status = dataSnapshot.child("status").getValue().toString();
                        final String image = dataSnapshot.child("image").getValue().toString();



                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);
                        }


                        friendsViewHolder.setFriendName(userName);
                        friendsViewHolder.setFriendsStatus(status);
                        friendsViewHolder.setFriendsImage(image);


                        // When Click to Friends View
                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if(i == 0)
                                        {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }

                                        if(i == 1)
                                        {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });

                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }


        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
        friendsRecyclerViewAdapter.startListening();

    }

    //Create viewholder
    public class FriendsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public FriendsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;

        }


        //Method to set the name
        public void setFriendName(String name)
        {
            TextView friendNameView = (TextView) mView.findViewById(R.id.user_single_name);
            friendNameView.setText(name);
        }

        //Method to set the status
        public void setFriendsStatus(String status)
        {

            TextView friendsStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            friendsStatusView.setText(status);
        }

        //Method to set the image
        public void setFriendsImage(String image)
        {
            CircleImageView friendImage = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.defaultimage).into(friendImage);
        }


        public void setUserOnline(String onlineStatus)
        {
            ImageView userOnlineView =  (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(onlineStatus.equals("true"))
            {
                userOnlineView.setVisibility(View.VISIBLE);
            }

            else
            {
                userOnlineView.setVisibility(View.INVISIBLE);

            }
        }





    }


}
