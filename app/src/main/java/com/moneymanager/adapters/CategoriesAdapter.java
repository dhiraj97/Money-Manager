// Created by Dhiraj on 13/01/17.

package com.moneymanager.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.moneymanager.fragments.FCategoryList;

public class CategoriesAdapter extends FragmentPagerAdapter {

	public CategoriesAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {

		final Fragment frag = new FCategoryList();
		final Bundle bundle = new Bundle();
		bundle.putInt("pos", position);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public int getCount() {
		return 2;
	}

	public CharSequence getPageTitle(int position) {

		if (position == 0) {
			return "Expense";
		} else {
			return "Income";
		}

	}

}
