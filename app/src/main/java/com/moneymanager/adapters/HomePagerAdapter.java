package com.moneymanager.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.moneymanager.fragments.FHomePage;
import com.moneymanager.utilities.MyCalendar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomePagerAdapter extends FragmentPagerAdapter {

	public HomePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public Fragment getItem(int position) {

		final Bundle args = new Bundle();
		args.putInt("pos", position);

		final SimpleDateFormat sdf = MyCalendar.getSimpleDateFormat();
		args.putString("date", sdf.format(getDateForPosition(position)));

		args.putString("title", getTitleString(position));

		final Fragment frag = new FHomePage();
		frag.setArguments(args);

		return frag;

	}

	@Override
	public CharSequence getPageTitle(int position) {

		return getTitleString(position);

	}

	public int getCount() {
		return 7; // because week
	}

	private Date getDateForPosition(int position) {
		final int noofDays = (6 - position);
		return MyCalendar.dateBeforeDays(noofDays);
	}

	private String getTitleString(int position) {
		if (position == 6) {
			return "Today";
		} else if (position == 5) {
			return "Yesterday";
		} else {

			final int noofDays = (6 - position);

			final Date thatDate = MyCalendar.dateBeforeDays(noofDays);

			return MyCalendar.dateToString(thatDate) + " " + MyCalendar.monthToString(thatDate);

		}
	}

}