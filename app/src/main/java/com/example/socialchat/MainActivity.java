package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity
{

    //Decleare the instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //Declare the instance of Toolbar
    private Toolbar mToolbar;

    //Declare ViewPager
    private ViewPager mViewPager;

    //Declare View Pager Adapter
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;

    //Declare Tab Layout
    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();


        //Define the mToolbar
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Social Chat");

        //User in FireBase
        if (mAuth.getCurrentUser() != null)
        {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        //Initialize the Tabs
        mViewPager = (ViewPager) findViewById(R.id.main_TabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //Set adapter for mViewPager
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Set Tab Layout with View Pager
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    //Check to see if the user is currently signed in
    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //Get the current firebase user and store it in "çurrentUser" variable
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //If the user is not signed in
        if(currentUser == null)
        {
            sendToStart();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }

    }

    protected void onStop()
    {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser !=null)
        {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart()
    {
        //User is send back to the home page
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }


    //Make the method to open the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    //Make the method to click on menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);


        //To click on the Log Out button
        if (item.getItemId() == R.id.main_logout_btn)
        {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

            FirebaseAuth.getInstance().signOut();
            sendToStart();

        }
            //To click on the Account Settings button
            if (item.getItemId() == R.id.main_settings_btn)
            {
                //Send us to the Settings Activity
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }


            //To click on the All Users button
            if (item.getItemId() == R.id.main_all_btn)
            {
                //Send us to the Users Activity
                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(usersIntent);
            }


        return true;

    }
}

