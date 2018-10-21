// Created by Dhiraj on 07/01/17.

package com.moneymanager.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.moneymanager.Common;
import com.moneymanager.activities.stats.AStats;
import com.moneymanager.db.DBHelper;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Budget;
import com.moneymanager.entities.Category;
import com.moneymanager.entities.Transaction;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.interfaces.ITransaction;
import com.moneymanager.utilities.MyCalendar;

import java.text.ParseException;
import java.util.Date;

import static com.moneymanager.Common.*;

public class TTransactions implements ITransaction {

	public static final String TABLE_NAME = "Transactions";
	public static final String ID = "_ID";
	public static final String AMOUNT = "trans_amt";
	public static final String CATEGORY = "trans_cat";
	public static final String ACCOUNT = "trans_acc";
	public static final String INFO = "trans_info";
	public static final String DATETIME = "trans_datetime";
	public static final String EXCLUDE = "trans_ex";
	private DBHelper dbHelper;

	private Context context;
	private String TID_alias = "tID";
	private String SELECT_TRANS_JOIN_CAT_AND_ACC =
			"SELECT " +
					TABLE_NAME + "." + ID + " AS " + TID_alias + "," +
					TCategories.TABLE_NAME + "." + TCategories.ID + " AS cID," +
					TAccounts.TABLE_NAME + "." + TAccounts.ID + " AS aID," +
					"*" +
					" FROM " + TABLE_NAME +
					" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
					" JOIN " + TAccounts.TABLE_NAME + " ON " + ACCOUNT + " = " + TAccounts.TABLE_NAME + "." + TAccounts.ID;
	private String DEFAULT_ORDER_BY = " ORDER BY " + TABLE_NAME + "." + ID + " ASC";
	private String DEFAULT_ORDER_BY_ID = " ORDER BY " + ACCOUNT + " ASC, " + ID + " DESC";
	private String DEFAULT_ORDER_BY_TID = " ORDER BY " + DATETIME + " DESC, " + TID_alias + " DESC";

	public TTransactions(Context context) {
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
				INFO + " TEXT," +
				DATETIME + " DATETIME," +
				EXCLUDE + " INTEGER," +
				"FOREIGN KEY(" + CATEGORY + ") REFERENCES " + TCategories.TABLE_NAME + "(" + TCategories.ID + ") ," +
				"FOREIGN KEY(" + ACCOUNT + ") REFERENCES " + TAccounts.TABLE_NAME + "(" + TAccounts.ID + ") ON DELETE CASCADE " +
				")";
	}

	private String q_SELECT_TRANSACTION(int id) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC + " WHERE " + TID_alias + " = " + id;
	}

	private String q_SELECT_ALL_TRANSACTIONS(String column, String order) {
		if (column == null) {
			column = DATETIME;
		}
		if (order == null) {
			order = "DESC";
		}
		return SELECT_TRANS_JOIN_CAT_AND_ACC + " ORDER BY " + column + " " + order;
	}

	private String q_SELECT_ALL_TRANSACTIONS_FOR_DAY(Date date) {
		// get Date
		final String date_format = MyCalendar.getSimpleDateFormat().format(date);
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE " + DATETIME + " = '" + date_format + "'" +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ACCOUNT_TRANSACTIONS(int accId) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE " + ACCOUNT + " = " + accId +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ACCOUNT_TRANSACTIONS_FOR_DAY(int accId, Date date) {
		// get Date
		final String date_format = MyCalendar.getSimpleDateFormat().format(date);
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE " + DATETIME + " = '" + date_format + "' AND " + ACCOUNT + " = " + accId +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_SUM_TRANSACTION_FOR_ACCOUNT_FOR_TYPE_ON_DATE(int acc, int type, Date date) {

		final String date_format = MyCalendar.getSimpleDateFormat().format(date);
		return "SELECT SUM(" + AMOUNT + ") AS " + AMOUNT + " FROM " + TABLE_NAME +
				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
				" WHERE " + DATETIME + " = '" + date_format + "' AND " +
				TCategories.TYPE + " = " + type + " AND " +
				ACCOUNT + " = " + acc +
				DEFAULT_ORDER_BY;


	}

	private String q_SELECT_SUM_TRANSACTION_FOR_TYPE_ON_DATE(int type, Date date) {

		final String date_format = MyCalendar.getSimpleDateFormat().format(date);
		return "SELECT SUM(" + AMOUNT + ") AS " + AMOUNT + " FROM " + TABLE_NAME +
				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
				" WHERE " + DATETIME + " = '" + date_format + "' AND " + TCategories.TYPE + " = " + type +
				DEFAULT_ORDER_BY;


	}

//	private String q_SELECT_TRANSACTIONS_GROUP_BY_CATEGORY_ON_DATE(int type, Date date) {
//		final String date_format = MyCalendar.getSimpleDateFormat().format(date);
//		return "SELECT *, SUM(" + AMOUNT + ") AS SUM FROM " + TABLE_NAME +
//				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
//				" WHERE " + DATETIME + " = '" + date_format + "' AND " + TCategories.TYPE + " = " + type + "" +
//				" GROUP BY " + TCategories.NAME;
//	}

	private String q_SELECT_ALL_TRANSACTIONS_FOR_PERIOD(String start_date, String end_date) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE " + DATETIME + " BETWEEN '" + start_date + "' AND '" + end_date + "'" +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ACCOUNT_TRANSACTIONS_FOR_PERIOD(int accID, String start_date, String end_date) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE " + DATETIME + " BETWEEN '" + start_date + "' AND '" + end_date + "'" +
				" AND " + ACCOUNT + " = " + accID +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ALL_TRANSACTIONS_FOR_MONTH(String monthDigits, String yearDigits) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE strftime('%m'," + DATETIME + ") = '" + monthDigits + "'" +
				" AND strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ACCOUNT_TRANSACTIONS_FOR_MONTH(int accID, String monthDigits, String yearDigits) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE strftime('%m'," + DATETIME + ") = '" + monthDigits + "' " +
				" AND strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				" AND " + ACCOUNT + " = " + accID +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ALL_TRANSACTIONS_FOR_YEAR(String yearDigits) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_ACCOUNT_TRANSACTIONS_FOR_YEAR(int accID, String yearDigits) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				" AND " + ACCOUNT + " = " + accID +
				DEFAULT_ORDER_BY_TID;
	}

	private String q_SELECT_DAILY_SUM(int type, String monthDigits, String yearDigits) {

		return "SELECT SUM(" + AMOUNT + ") AS sum," +
				" 0 AS period" +
				" FROM " + TABLE_NAME +
				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
				" WHERE strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				" AND  strftime('%m'," + DATETIME + ") = '" + monthDigits + "' " +
				" AND " + TCategories.TYPE + " = " + type +
				" GROUP BY period ORDER BY period ASC";

	}

	private String q_SELECT_DAILY_SUM_FOR_ACCOUNT(int type, String monthDigits, String yearDigits, int accId) {

		return "SELECT SUM(" + AMOUNT + ") AS sum," +
				" 0 AS period" +
				" FROM " + TABLE_NAME +
				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
				" WHERE strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				" AND  strftime('%m'," + DATETIME + ") = '" + monthDigits + "' " +
				" AND " + TCategories.TYPE + " = " + type +
				" AND " + ACCOUNT + " = " + accId +
				" GROUP BY period ORDER BY period ASC";

	}

	// TODO to be worked on later
	private String q_SELECT_WEEKLY_SUM(int type, String weekStart, String weekEnd) {
		return "SELECT SUM(" + AMOUNT + ") AS SUM, " +
				"";
	}

	private String q_SELECT_MONTHLY_SUM(int type, String yearDigits) {
		return "SELECT SUM(" + AMOUNT + ") AS sum, strftime('%m', " + DATETIME + ") AS period" +
				" FROM " + TABLE_NAME +
				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
				" WHERE strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				" AND " + TCategories.TYPE + " = " + type +
				" GROUP BY period ORDER BY period ASC";

	}

	private String q_SELECT_MONTHLY_SUM_FOR_ACCOUNT(int type, String yearDigits, int accId) {
		return "SELECT SUM(" + AMOUNT + ") AS sum, strftime('%m', " + DATETIME + ") AS period" +
				" FROM " + TABLE_NAME +
				" JOIN " + TCategories.TABLE_NAME + " ON " + CATEGORY + " = " + TCategories.TABLE_NAME + "." + TCategories.ID +
				" WHERE strftime('%Y'," + DATETIME + ") = '" + yearDigits + "' " +
				" AND " + TCategories.TYPE + " = " + type +
				" AND " + ACCOUNT + " = " + accId +
				" GROUP BY period ORDER BY period ASC";

	}

	private String q_SELECT_TRANSACTION_FOR_BUDGET(int acc, int cat, String startDate, String endDate) {
		return SELECT_TRANS_JOIN_CAT_AND_ACC +
				" WHERE " + CATEGORY + " = " + cat +
				" AND " + ACCOUNT + " = " + acc +
				" AND " + TCategories.TYPE + " = " + EXPENSE +
				" AND " + DATETIME + " BETWEEN '" + startDate + "' AND '" + endDate + "'" +
				" ORDER BY " + DATETIME + " DESC";
	}

	@Override
	public Transaction getTransaction(int selectedTransactionID) {
		Cursor c = dbHelper.select(q_SELECT_TRANSACTION(selectedTransactionID), null);

		if (c.moveToFirst()) {
			return extractTransactionFromCursor(c);
		} else {
			return null;
		}

	}

	@Override
	public Transaction[] getAllTransactions(String column, String order) {

		final Cursor c = dbHelper.select(q_SELECT_ALL_TRANSACTIONS(column, order), null);

		if (c.getCount() == 0) {
			try {
				throw new NoAccountsException();
			} catch (NoAccountsException e) {
				e.printStackTrace();
			}
		}

		final Transaction[] trans = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			trans[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return trans;

	}

	@Override
	public double getSumOfTransactionTypeForDay(int type, Date date) {
		final Cursor c = dbHelper.select(q_SELECT_SUM_TRANSACTION_FOR_TYPE_ON_DATE(type, date), null);
		c.moveToFirst();
		return c.getDouble(c.getColumnIndex(AMOUNT));
	}

	@Override
	public double getAccountSpecificSumOfTransactionTypeForDay(int acc, int type, Date date) {

		final Cursor c = dbHelper.select(q_SELECT_SUM_TRANSACTION_FOR_ACCOUNT_FOR_TYPE_ON_DATE(acc, type, date), null);
		c.moveToFirst();
		return c.getDouble(c.getColumnIndex(AMOUNT));

	}

	/* Day transactions */
	public Transaction[] getTransactionsForDay(Date date) {

		Cursor c = dbHelper.select(q_SELECT_ALL_TRANSACTIONS_FOR_DAY(date), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;
	}

	@Override
	public Transaction[] getAccountSpecificTransactions(int accID) {
		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSACTIONS(accID), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;
	}

	@Override
	public Transaction[] getAccountSpecificTransactionsForDay(int accId, Date date) {
		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSACTIONS_FOR_DAY(accId, date), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;
	}

	/* Week Transactions */
	@Override
	public Transaction[] getTransactionsForWeek(Date date) {

		Date[] dates = MyCalendar.weekEndandStartDatesforDate(date);
		String startDate = MyCalendar.stringFormatOfDate(dates[0]);
		String endDate = MyCalendar.stringFormatOfDate(dates[1]);

		final Cursor c = dbHelper.select(q_SELECT_ALL_TRANSACTIONS_FOR_PERIOD(startDate, endDate), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;
	}

	@Override
	public Transaction[] getBudgetSpecificTransactions(Budget budget) {

		final String startDate = MyCalendar.stringFormatOfDate(budget.getStartDate());
		final String endDate = MyCalendar.stringFormatOfDate(MyCalendar.dateAfterDays(budget.getStartDate(), budget.getPeriod()));
		final int cat = budget.getCategory().getId();
		final int acc = budget.getAccount().getId();

		final Cursor c = dbHelper.select(q_SELECT_TRANSACTION_FOR_BUDGET(acc, cat, startDate, endDate), null);

		Transaction[] t = new Transaction[c.getCount()];
		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;

	}

	@Override
	public Transaction[] getAccountSpecificTransactionsForWeek(int accId, Date date) {

		Date[] dates = MyCalendar.weekEndandStartDatesforDate(date);
		String startDate = MyCalendar.stringFormatOfDate(dates[0]);
		String endDate = MyCalendar.stringFormatOfDate(dates[1]);

		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSACTIONS_FOR_PERIOD(accId, startDate, endDate), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;
	}

	/* Month Transactions */

	@Override
	public Transaction[] getTransactionsForMonth(Date date) {

		String monthDigi = MyCalendar.monthToStringDigits(date);
		String yearDigi = MyCalendar.yearToString(date);

		final Cursor c = dbHelper.select(q_SELECT_ALL_TRANSACTIONS_FOR_MONTH(monthDigi, yearDigi), null);

		final Transaction[] transactions = new Transaction[c.getCount()];

		while (c.moveToNext()) {

			final int pos = c.getPosition();
			transactions[pos] = extractTransactionFromCursor(c);

		}

		return transactions;

	}

	@Override
	public Transaction[] getAccountSpecificTransactionsForMonth(int aacID, Date date) {

		String monthDigi = MyCalendar.monthToStringDigits(date);
		String yearDigi = MyCalendar.yearToString(date);

		final Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSACTIONS_FOR_MONTH(aacID, monthDigi, yearDigi), null);

		final Transaction[] transactions = new Transaction[c.getCount()];

		while (c.moveToNext()) {

			final int pos = c.getPosition();
			transactions[pos] = extractTransactionFromCursor(c);

		}

		return transactions;

	}


	/* Year Transactions */
	@Override
	public Transaction[] getTransactionsForYear(Date date) {

		String yearDigi = MyCalendar.yearToString(date);

		final Cursor c = dbHelper.select(q_SELECT_ALL_TRANSACTIONS_FOR_YEAR(yearDigi), null);

		final Transaction[] transactions = new Transaction[c.getCount()];

		while (c.moveToNext()) {

			final int pos = c.getPosition();
			transactions[pos] = extractTransactionFromCursor(c);

		}

		return transactions;

	}

	@Override
	public Transaction[] getAccountSpecificTransactionsForYear(int selectedAccountID, Date date) {

		String yearDigi = MyCalendar.yearToString(date);

		final Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSACTIONS_FOR_YEAR(selectedAccountID, yearDigi), null);

		final Transaction[] transactions = new Transaction[c.getCount()];

		while (c.moveToNext()) {

			final int pos = c.getPosition();
			transactions[pos] = extractTransactionFromCursor(c);

		}

		return transactions;

	}

	/* Custom Period Transactions */
	@Override
	public Transaction[] getTransactionsForCustomPeriod(Date startingDate, Date endingDate) {

		String startDate = MyCalendar.stringFormatOfDate(startingDate);
		String endDate = MyCalendar.stringFormatOfDate(endingDate);
		Cursor c = dbHelper.select(q_SELECT_ALL_TRANSACTIONS_FOR_PERIOD(startDate, endDate), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;

	}

	@Override
	public Transaction[] getAccountSpecificTransactionsForCustomPeriod(int accId, Date startingDate, Date endingDate) {

		String startDate = MyCalendar.stringFormatOfDate(startingDate);
		String endDate = MyCalendar.stringFormatOfDate(endingDate);
		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSACTIONS_FOR_PERIOD(accId, startDate, endDate), null);

		final Transaction[] t = new Transaction[c.getCount()];

		while (c.moveToNext()) {
			t[c.getPosition()] = extractTransactionFromCursor(c);
		}

		return t;

	}

	@Override
	public double[] getPeriodSums(int period, int type, Date date) {

		String yearDigi = MyCalendar.yearToString(date);
		String monthDigi = MyCalendar.monthToStringDigits(date);
		String query = "";
		int arr_size = 0;
		switch (period) {
			case AStats.DAY:
				arr_size = 1;
				query = q_SELECT_DAILY_SUM(type, monthDigi, yearDigi);
				break;
			case AStats.WEEK:
				arr_size = 7;
				Date[] dates = MyCalendar.weekEndandStartDatesforDate(date);
				String weekStart = MyCalendar.stringFormatOfDate(dates[0]);
				String weekEnd = MyCalendar.stringFormatOfDate(dates[1]);
				query = q_SELECT_WEEKLY_SUM(type, weekStart, weekEnd);
				break;
			case AStats.MONTH:
				break;
			case AStats.YEAR:
				arr_size = 14;
				query = q_SELECT_MONTHLY_SUM(type, yearDigi);
				break;
			default:
				query = "soon";
				break;

		}
		Cursor c = dbHelper.select(query, null);

		final double[] amts = new double[arr_size];

		if (!c.moveToFirst()) {
			return amts;
		}
		for (int i = 0; i < arr_size; i++) {

			if (c.getInt(c.getColumnIndex("period")) == i) {
				amts[i] = c.getDouble(c.getColumnIndex("sum"));
				if (!c.isLast()) {
					c.moveToNext();
				}
			} else {
				amts[i] = 0;
			}
		}

		return amts;
	}

	@Override
	public double[] getPeriodSumsForAccount(int period, int type, Date date, int accId) {

		String yearDigi = MyCalendar.yearToString(date);
		String monthDigi = MyCalendar.monthToStringDigits(date);
		String query = "";
		switch (period) {
			case AStats.DAY:
				query = q_SELECT_DAILY_SUM_FOR_ACCOUNT(type, monthDigi, yearDigi, accId);
				break;
			case AStats.WEEK:
				break;
			case AStats.MONTH:
				break;
			case AStats.YEAR:
				query = q_SELECT_MONTHLY_SUM_FOR_ACCOUNT(type, yearDigi, accId);
				break;
			default:
				query = "soon";
				break;

		}
		Cursor c = dbHelper.select(query, null);
		final double[] amts = new double[14];

		if (!c.moveToFirst()) {
			return amts;
		}
		for (int i = 0; i < amts.length; i++) {

			if (c.getInt(c.getColumnIndex("period")) == i) {
				amts[i] = c.getDouble(c.getColumnIndex("sum"));
				if (!c.isLast()) {
					c.moveToNext();
				}

			} else {
				amts[i] = 0;
			}
		}

		return amts;
	}


	@Override
	public void insertNewTransaction(Transaction transaction) throws InsufficientBalanceException {

		final ContentValues cv = new ContentValues();
		cv.put(AMOUNT, transaction.getAmount());
		cv.put(CATEGORY, transaction.getCategory().getId());
		cv.put(ACCOUNT, transaction.getAccount().getId());
		cv.put(INFO, transaction.getInfo());
		cv.put(DATETIME, MyCalendar.getSimpleDateFormat().format(transaction.getDateTime()));
		cv.put(EXCLUDE, transaction.isExclude());
		dbHelper.insert(TABLE_NAME, cv);

		// update account balance
		TAccounts tAccounts = new TAccounts(context);
		tAccounts.updateAccountBalance(transaction.getAccount().getId(), transaction.getAmount(), transaction.getCategory().getType() == INCOME);

	}

	public void updateTransaction(Transaction t) throws InsufficientBalanceException {

		Cursor c = dbHelper.select(q_SELECT_TRANSACTION(t.getId()), null);
		c.moveToFirst();
		final double oldAmt = extractTransactionFromCursor(c).getAmount();
		final double amtDiff = t.getAmount() - oldAmt;

		final ContentValues cv = new ContentValues();
		cv.put(AMOUNT, t.getAmount());
		cv.put(CATEGORY, t.getCategory().getId());
		cv.put(ACCOUNT, t.getAccount().getId());
		cv.put(INFO, t.getInfo());
		cv.put(DATETIME, t.formatedDateTime());
		cv.put(EXCLUDE, t.isExclude());

		dbHelper.update(TABLE_NAME, cv, ID + " = ?", new String[]{String.valueOf(t.getId())});

		TAccounts tAccounts = new TAccounts(context);

		// income
		if (t.getCategory().getType() == INCOME) {

			tAccounts.updateAccountBalance(t.getAccount().getId(), amtDiff, true);

		} else {

			tAccounts.updateAccountBalance(t.getAccount().getId(), amtDiff, false);

		}

	}


	@Override
	public void removeTransaction(Transaction t) {

		// Correct the account balance before removing the transaction
		// if expense transaction is removed, add to account balance
		// if income transaction is removed, dedut from account balance

		TAccounts tAccounts = new TAccounts(context);

		// this reversal of category is hence necessary, because updateTransactionAccountBalance(...) method,
		// adds to balance if income, deduts if expense
		try {
			tAccounts.updateAccountBalance(t.getAccount().getId(), t.getAmount(), t.getCategory().getType() == EXPENSE);
		} catch (InsufficientBalanceException e) { // Here the balance should never be insufficient
			e.printStackTrace();
		}

		dbHelper.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(t.getId())});

	}

	@Override
	public void removeTransactionsForAccount(int id) {
		dbHelper.delete(TABLE_NAME, ACCOUNT + " = ?", new String[]{String.valueOf(id)});
	}

	@Override
	public Transaction[] getsSearchedTransactions(String[] queries) {

		if (queries.length == 0) {
			return null;
		} else {

			String query = SELECT_TRANS_JOIN_CAT_AND_ACC + " WHERE ";
			for (String q : queries) {

				if (q.equals("")) {
					continue;
				}

				query += " " + q + " AND ";

			}
			query += " '1'='1' ";
			query += DEFAULT_ORDER_BY_TID;

			Cursor c = dbHelper.select(query, null);

			if (c.getCount() <= 0) {
				return null;
			}

			Log.i(mylog, query);

			Transaction[] transactions = new Transaction[c.getCount()];
			while (c.moveToNext()) {
				transactions[c.getPosition()] = extractTransactionFromCursor(c);
			}

			return transactions;

		}

	}

	@Override
	public void shiftDeletedTransactions(Category cat) {

		// 1 - Other expense, 2 - Other income
		int newCatID = cat.getType() == Common.EXPENSE ? 1 : 2;

		ContentValues cv = new ContentValues();
		cv.put(CATEGORY, newCatID);

		dbHelper.update(TABLE_NAME, cv, CATEGORY + " = ?", new String[]{String.valueOf(cat.getId())});

	}

	private Transaction extractTransactionFromCursor(Cursor c) {

		final int id = c.getInt(c.getColumnIndex(TID_alias));
		final double amount = c.getDouble(c.getColumnIndex(AMOUNT));
		final String info = c.getString(c.getColumnIndex(INFO));
		Date dateTime = null;
		try {
			dateTime = MyCalendar.getSimpleDateFormat().parse(c.getString(c.getColumnIndex(DATETIME)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final boolean ex = c.getInt(c.getColumnIndex(EXCLUDE)) == 1;

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
		final double acc_start_balance = c.getDouble(c.getColumnIndex(TAccounts.STARTING_BALANCE));
		Date acc_date = null;
		try {
			acc_date = MyCalendar.getSimpleDateFormat().parse(c.getString(c.getColumnIndex(TAccounts.STARTING_BALANCE)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final boolean acc_exclude = c.getInt(c.getColumnIndex(TAccounts.EXCLUDE)) == 1;
		final Account account = new Account(acc_id, acc_name, acc_balance, acc_start_balance, acc_date, acc_exclude);

		return new Transaction(id, amount, category, account, info, dateTime, ex);

	}

}
