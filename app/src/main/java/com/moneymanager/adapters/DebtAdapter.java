// Created by Dhiraj on 03/02/17.

package com.moneymanager.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.moneymanager.fragments.FDebts;

public class DebtAdapter extends FragmentPagerAdapter {

	public DebtAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {

		final Fragment frag = new FDebts();
		final Bundle bundle = new Bundle();
		bundle.putBoolean("debt", position == 0);
		frag.setArguments(bundle);
		return frag;

	}

	@Override
	public int getCount() {
		return 2;
	}

	public CharSequence getPageTitle(int position) {

		if (position == 0) {
			return "Debts";
		} else {
			return "Loans";
		}

	}

}
