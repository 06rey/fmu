package com.example.findmeuv.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.findmeuv.R;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class BookingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        final ViewPager bookingPager = view.findViewById(R.id.bookingPager);
        final TabLayout bookingTabLayout = view.findViewById(R.id.bookingTabLayout);

        BookingFragmentPagerAdapter adapter = new BookingFragmentPagerAdapter(getChildFragmentManager(), bookingTabLayout.getTabCount());
        bookingPager.setAdapter(adapter);
        bookingPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(bookingTabLayout));

        bookingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                bookingPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class BookingFragmentPagerAdapter extends FragmentPagerAdapter {

        private int tabNum;

        private BookingFragmentPagerAdapter(FragmentManager fm, int tabNum) {
            super(fm);
            this.tabNum = tabNum;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new BookingPendingFragment();
                case 1:
                    return new BookingTravelingFragment();
                case 2:
                    return new BookingHistoryFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return this.tabNum;
        }
    }
}
