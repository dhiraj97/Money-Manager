package com.moneymanager.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.moneymanager.fragments.FAddDebt;
import com.moneymanager.fragments.FAddTransaction;

public class AddTransactionAdapter extends FragmentPagerAdapter {

	int debtID;

	public AddTransactionAdapter(FragmentManager fm, int debtId) {
		super(fm);
		this.debtID = debtId;
	}

	public Fragment getItem(int position) {

		if (position == 0) {
			return new FAddTransaction();
		} else {
			Bundle b = new Bundle();
			b.putInt("debt_id", debtID);
			FAddDebt f = new FAddDebt();
			f.setArguments(b);
			return f;
		}


	}

	public int getCount() {
		return 2;
	}

	public CharSequence getPageTitle(int position) {

		if (position == 0) {
			return "Transaction";
		} else {
			return "Debt";
		}

	}
}