package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity
{
    //Instance Toolbar
    private Toolbar mToolbar;

    //Instance RecyclerView
    private RecyclerView mUsersList;

    //Reference Firebase DB
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        //Declare instance Toolbar
        mToolbar = (Toolbar) findViewById(R.id.users_appBar);


        //Include the toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Set database to "Users" object --> retrieve everything inside the object
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);


        //Declare instance UsersList
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));


    }

    //Method to retrieve data realtime
    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =

                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDatabase, Users.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options)
        {

            @Override
            public void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int position, @NonNull Users users)
            {

                usersViewHolder.setDisplayName(users.getName());
                usersViewHolder.setUserStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getImage());

                //Get the key of the user
                final String user_id = getRef(position).getKey();


                //User clicks on image+name --> send to Profile Activity
                usersViewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                       //Use user key to send the key to the user profile
                       Intent profile_Intent = new Intent(UsersActivity.this, ProfileActivity.class);
                       profile_Intent.putExtra("user_id", user_id);
                       startActivity(profile_Intent);

                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_single_layout, viewGroup, false);
                UsersViewHolder viewHolder = new UsersViewHolder(view);
                return viewHolder;

            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //Create view holder
    public class UsersViewHolder extends RecyclerView.ViewHolder
    {
        //Declare the view
        View mView;

        //Constructor
        public UsersViewHolder(@NonNull View itemView)
        {
            super(itemView);

            //Initialize the view
            mView = itemView;
        }

        //Method to set the name
        public void setDisplayName(String name)
        {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        //Method to set the status
        public void setUserStatus(String status)
        {

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }


        //Method to set the image
        public void setUserImage(String image)
        {
            CircleImageView userImage = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.defaultimage).into(userImage);
        }

    }

}
