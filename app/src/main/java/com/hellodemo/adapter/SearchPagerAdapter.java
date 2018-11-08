package com.hellodemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellodemo.fragment.searchActivity.ArtistsFragment;
import com.hellodemo.fragment.searchActivity.LabelsFragment;


public class SearchPagerAdapter extends FragmentStatePagerAdapter {
    private String[] titles = {"Artists", "Labels"};

    private final int count = 2;

    /**
     * @param fm fragment manager<br>
     */
    public SearchPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return ArtistsFragment.newInstance();

            case 1:
                return LabelsFragment.newInstance();

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
