// Created by Dhiraj on 13-12-2016.

package com.moneymanager;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class Common {

	public static final String mylog = "mylog";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String[] currenySymbols = {
			"\u0024 USD",
			"\u20B9 INR",
			"\u00A3 GBP",
			"\u20AC EUR",
			"\u00A5 YEN"
	};
	public static final int INCOME = 0;
	public static final int EXPENSE = 1;
	/**
	 * i have given
	 */
	public static final int DEBT = 5;
	/**
	 * i am repayed
	 */
	public static final int DEBT_REPAY = 6;
	/**
	 * i have taken
	 */
	public static final int LOAN = 7;
	/**
	 * i am repaying
	 */
	public static final int LOAN_REPAY = 8;
	// SharedPreferences Constants
	public static final String spFILE_NAME = "mm_sp_file";
	public static final String spCURRENT_ACCOUNT_ID = "account_id";
	public static final String spBUDGET_LIMIT = "budget_limit";
	public static final String spCURRENCY_SYMBOL = "currency_symbol";
	// pie chart contants
	public static final int HOLE_RADIUS = 50;
	public static final int SLICE_SPACE = 0;
	public static String CURRENCY_FORMAT = "$";// default is $
	public static int ALL_ACCOUNT_ID = -9837; // Used to get info of all accounts
	public static int CURRENT_ACCOUNT_ID = ALL_ACCOUNT_ID;
	public static String CURRENT_ACCOUNT_NAME;

	public static void setupToolbar(AppCompatActivity activity, int id, String title) {

		final Toolbar toolbar = (Toolbar) activity.findViewById(id);
		toolbar.setTitle(title);
		activity.setSupportActionBar(toolbar);

	}

	public static int getMyColor(Context context, int color) {
		return ContextCompat.getColor(context, color);
	}

	public static String formatAmt(double amt) {
		if (amt % 1 == 0) {
			return String.format("%.0f " + CURRENCY_FORMAT, amt);
		} else {
			return String.format("%.2f " + CURRENCY_FORMAT, amt);
		}
	}

}
