package com.hellodemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellodemo.fragment.ArtistsFragment;
import com.hellodemo.fragment.GroupsFragment;
import com.hellodemo.fragment.LabelsFragment;

/**
 * Created by new user on 2/24/2018.
 */

public class SendPopupPagerAdapter extends FragmentStatePagerAdapter {
    private String[] titles = {"Groups", "Artists", "Labels"};

    private final int count = 3;
    long uploadID, groupID;

    /**
     * this makes sure that this class is not initialized using this constructor
     */
    private SendPopupPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    /**
     * @param fm       fragment manager<br>
     * @param uploadID uploadID of the song we are sharing.. (-1 in case of adding contacts to groups)<br>
     * @param groupID  id of the group we are adding the contacts in.. (-1 in case of sending songs)<br>
     */
    public SendPopupPagerAdapter(FragmentManager fm, long uploadID, long groupID) {
        super(fm);
        this.uploadID = uploadID;
        this.groupID = groupID;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return GroupsFragment.newInstance(uploadID);
            case 1:

                return ArtistsFragment.newInstance(uploadID);
            case 2:
                return LabelsFragment.newInstance(uploadID);
        }
        return null;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
