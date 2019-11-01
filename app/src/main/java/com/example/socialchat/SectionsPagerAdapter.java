package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class SectionsPagerAdapter extends FragmentPagerAdapter
{

    //CONSTRUCTOR
    public SectionsPagerAdapter(@NonNull FragmentManager fm)
    {
        super(fm);
    }

    //getItem method --> positie van de tabs
    @Override
    public Fragment getItem(int position)
    {
        //Switch statement for each tab
        switch(position)
        {
            case 0:
                RequestsFragment requestsFragment =  new RequestsFragment();
                return requestsFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

                default:
                    return null;
        }


    }

    //getCount method --> aantal tabs = 3
    @Override
    public int getCount()
    {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "REQUESTS";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

                default:
                    return null;

        }

    }
}
