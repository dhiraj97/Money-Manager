// Created by Dhiraj on 05/02/17.

package com.moneymanager.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.moneymanager.db.DBHelper;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Budget;
import com.moneymanager.entities.Category;
import com.moneymanager.repo.interfaces.IBudget;
import com.moneymanager.utilities.MyCalendar;

import java.text.ParseException;
import java.util.Date;

public class TBudget implements IBudget {

	public static final String TABLE_NAME = "Budgets";
	public static final String ID = "_ID";
	public static final String AMOUNT = "bud_amt";
	public static final String CATEGORY = "bud_cat";
	public static final String ACCOUNT = "bud_acc";
	public static final String DATETIME = "bud_datetime";
	public static final String PERIOD = "bud_period";
	public static final String NOTIFY = "bud_notify";
	private DBHelper dbHelper;
	private Context context;
	private String BID_alias = "bID";
	private String SELECT_BUDGET_JOIN_CAT_AND_ACC =
			"SELECT " +
					TABLE_NAME + "." + ID + " AS " + BID_alias + "," +
					TCategories.TABLE_NAME + "." + TCategories.ID + " AS cID," +
					TAccounts.TABLE_NAME + "." + TAccounts.ID + " AS aID," +
					"*" +
					" FROM " + TABLE_NAME +
					" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
					" JOIN " + TAccounts.TABLE_NAME + " ON " + ACCOUNT + " = " + TAccounts.TABLE_NAME + "." + TAccounts.ID;

	public TBudget(Context context) {

		dbHelper = new DBHelper(context);
		this.context = context;

	}

	/* Query Strings */
	public static String q_CREATE_TABLE() {
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				AMOUNT + " DOUBLE," +
				CATEGORY + " INTEGER," +
				ACCOUNT + " INTEGER," +
				DATETIME + " DATETIME," +
				PERIOD + " INTEGER," +
				NOTIFY + " INTEGER," +
				"FOREIGN KEY(" + CATEGORY + ") REFERENCES " + TCategories.TABLE_NAME + "(" + TCategories.ID + ")," +
				"FOREIGN KEY(" + ACCOUNT + ") REFERENCES " + TAccounts.TABLE_NAME + "(" + TAccounts.ID + ") ON DELETE CASCADE " +
				");";
	}

	private String q_SELECT_ALL_BUDGETS() {

		return SELECT_BUDGET_JOIN_CAT_AND_ACC + " ORDER BY " + DATETIME + " DESC";

	}


	@Override
	public long insertBudget(Budget budget) {

		final ContentValues cv = new ContentValues();
		cv.put(AMOUNT, budget.getAmount());
		cv.put(CATEGORY, budget.getCategory().getId());
		cv.put(ACCOUNT, budget.getAccount().getId());
		cv.put(DATETIME, MyCalendar.getSimpleDateFormat().format(budget.getStartDate()));
		cv.put(PERIOD, budget.getPeriod());
		cv.put(NOTIFY, 1);
		return dbHelper.insert(TABLE_NAME, cv);

	}

	@Override
	public Budget[] getAllBudgets() {

		Cursor c = dbHelper.select(q_SELECT_ALL_BUDGETS(), null);

		Budget[] budgets = new Budget[c.getCount()];

		while (c.moveToNext()) {
			budgets[c.getPosition()] = extractBudgetFromCursor(c);
		}

		return budgets;

	}

	@Override
	public void removeBudget(int id) {
		dbHelper.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(id)});
	}

	@Override
	public void removeBudgetsForAccount(int id) {
		dbHelper.delete(TABLE_NAME, ACCOUNT + " = ?", new String[]{String.valueOf(id)});
	}

	private Budget extractBudgetFromCursor(Cursor c) {
		final int id = c.getInt(c.getColumnIndex(BID_alias));
		final double amount = c.getDouble(c.getColumnIndex(AMOUNT));
		Date dateTime = null;
		try {
			dateTime = MyCalendar.getSimpleDateFormat().parse(c.getString(c.getColumnIndex(DATETIME)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final int period = c.getInt(c.getColumnIndex(PERIOD));

		// create categroy object
		final int cat_id = c.getInt(c.getColumnIndex(CATEGORY));
		final String cat_name = c.getString(c.getColumnIndex(TCategories.NAME));
		final int cat_type = c.getInt(c.getColumnIndex(TCategories.TYPE));
		final boolean cat_ex = c.getInt(c.getColumnIndex(TCategories.EXCLUDE)) == 1;
		final Category category = new Category(cat_id, cat_name, cat_type, cat_ex);

		// create account object
		final int acc_id = c.getInt(c.getColumnIndex(ACCOUNT));
		final String acc_name = c.getString(c.getColumnIndex(TAccounts.NAME));
		final double acc_balance = c.getDouble(c.getColumnIndex(TAccounts.BALANCE));
		final boolean acc_exclude = c.getInt(c.getColumnIndex(TAccounts.EXCLUDE)) == 1;
		final double acc_start_balance = c.getDouble(c.getColumnIndex(TAccounts.STARTING_BALANCE));
		Date acc_date = null;
		try {
			acc_date = MyCalendar.getSimpleDateFormat().parse(c.getString(c.getColumnIndex(TAccounts.STARTING_BALANCE)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final Account account = new Account(acc_id, acc_name, acc_balance, acc_start_balance, acc_date, acc_exclude);

		return new Budget(id, category, account, amount, dateTime, period);
	}

}
