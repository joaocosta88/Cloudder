package com.layla.cloudder.utils;

import android.support.v4.app.*;

import com.layla.cloudder.activities.MainFragment;
import com.layla.cloudder.activities.OfflineFragment;
import com.layla.cloudder.activities.QueueFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
	private static int NUM_ITEMS = 3;

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		return NUM_ITEMS;
	}

	@Override
	public Fragment getItem(int position) {
		switch(position) {
		case 0:
			return new MainFragment();
		case 1:
			return new OfflineFragment();
		case 2:
			return new QueueFragment();
		}
		return new MainFragment();
	}

	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			NUM_ITEMS = count;
			notifyDataSetChanged();
		}
	}

	@Override
	public String getPageTitle(int position) {
		switch(position) {
		case 0:
			return "Home";
		case 1:
			return "Downloaded";
		case 2:
			return "Queue";
		}
		return "";
	}
}