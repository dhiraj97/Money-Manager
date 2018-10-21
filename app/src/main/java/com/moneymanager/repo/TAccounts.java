// Created by Dhiraj on 07/01/17.

package com.moneymanager.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.moneymanager.db.DBHelper;
import com.moneymanager.entities.Account;
import com.moneymanager.exceptions.AccountNameExistsException;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.interfaces.IAccount;
import com.moneymanager.utilities.MyCalendar;

import java.text.ParseException;
import java.util.Date;

import static com.moneymanager.Common.DEBT;
import static com.moneymanager.Common.LOAN;

public class TAccounts implements IAccount {

	public static final String TABLE_NAME = "Account";
	public static final String ID = "_ID";
	public static final String NAME = "acc_name";
	public static final String BALANCE = "acc_bal";
	public static final String STARTING_BALANCE = "acc_start_bal";
	public static final String DATE = "acc_date";
	public static final String EXCLUDE = "acc_ex";

	/* Query Strings */
	private DBHelper dbHelper;
	private Context context;

	public TAccounts(Context context) {

		dbHelper = new DBHelper(context);
		this.context = context;

	}

	public static String q_CREATE_TABLE() {
		return
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
						ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						NAME + " TEXT, " +
						BALANCE + " DOUBLE, " +
						STARTING_BALANCE + " DOUBLE, " +
						DATE + " DATETIME, " +
						EXCLUDE + " INTEGER" +
						")";
	}

	private String q_SELECT_ALL_ACCOUNTS(String column, String order) {
		if (column == null) {
			column = NAME;
		}
		if (order == null) {
			order = "ASC";
		}
		return "SELECT * FROM " + TABLE_NAME + " ORDER BY " + column + " " + order;
	}

	private String q_SELECT_ACCOUNT(int id) {
		return "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = " + id;
	}

	// TODO shift in TTransaction
	private String q_COUNT_TRANSACTION(int id) {
		return "SELECT COUNT(" + TTransactions.TABLE_NAME + "." + TTransactions.ID + ") AS cT FROM " + TTransactions.TABLE_NAME + " WHERE " + TTransactions.ACCOUNT + " = " + id;
	}

	// TODO shift in TDebt
	private String q_COUNT_DEBT(int id) {
		return "SELECT COUNT(" + TDebt.TABLE_NAME + "." + TDebt.ID + ") AS cD FROM " + TDebt.TABLE_NAME +
				" WHERE " + TDebt.ACCOUNT + " = " + id +
				" AND " + TDebt.TYPE + " = " + DEBT;
	}

	// TODO shift in TDebt
	private String q_COUNT_LOAN(int id) {
		return "SELECT COUNT(" + TDebt.TABLE_NAME + "." + TDebt.ID + ") AS cL FROM " + TDebt.TABLE_NAME +
				" WHERE " + TDebt.ACCOUNT + " = " + id +
				" AND " + TDebt.TYPE + " = " + LOAN;
	}

	private String q_SUM_BALANCE_FROM_ALL_ACCOUNTS() {
		return "SELECT SUM( " + BALANCE + ") AS " + BALANCE + " FROM " + TABLE_NAME;
	}

	private String q_SUM_BALANCE_OF_ACCOUNT(int acc_id) {
		return "SELECT SUM( " + BALANCE + ") AS " + BALANCE + " FROM " + TABLE_NAME + " WHERE " + ID + " = " + acc_id;
	}


	private String q_CHECK_ACCOUNT_NAME(String newAccountName) {

		return "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME + " = '" + newAccountName + "'";

	}



	@Override
	public long insertNewAccount(Account account) {

		final ContentValues cv = new ContentValues();
		cv.put(NAME, account.getName());
		cv.put(BALANCE, account.getBalance());
		cv.put(STARTING_BALANCE, account.getStartingBalance());
		cv.put(DATE, account.formatedDateTime());
		cv.put(EXCLUDE, account.isExclude());

		return dbHelper.insert(TABLE_NAME, cv);

	}

	@Override
	public Account[] getAllAccounts(String column, String order) throws NoAccountsException {

		final Cursor c = dbHelper.select(q_SELECT_ALL_ACCOUNTS(column, order), null);

		if (c.getCount() == 0) {
			throw new NoAccountsException();
		}

		final Account[] accounts = new Account[c.getCount()];

		while (c.moveToNext()) {

			accounts[c.getPosition()] = extractAccountFromCursor(c);

		}

		return accounts;

	}

	@Override
	public int countTransactions(int id) {

		final Cursor c = dbHelper.select(q_COUNT_TRANSACTION(id), null);
		if (c.moveToFirst()) {
			return c.getInt(c.getColumnIndex("cT"));
		} else {
			return -1;
		}

	}

	@Override
	public int countDebt(int id) {

		final Cursor c = dbHelper.select(q_COUNT_DEBT(id), null);
		if (c.moveToFirst()) {
			return c.getInt(c.getColumnIndex("cD"));
		} else {
			return -1;
		}

	}

	@Override
	public int countLoan(int id) {

		final Cursor c = dbHelper.select(q_COUNT_LOAN(id), null);
		if (c.moveToFirst()) {
			return c.getInt(c.getColumnIndex("cL"));
		} else {
			return -1;
		}

	}

	@Override
	public Account getAccount(int id) {
		final Cursor c = dbHelper.select(q_SELECT_ACCOUNT(id), null);

		if (c.moveToFirst()) {

			return extractAccountFromCursor(c);

		} else {
			try {
				throw new Exception("No Account found");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void removeAccount(int id) {

		TTransactions tTransaction = new TTransactions(context);
		tTransaction.removeTransactionsForAccount(id);

		// remove budgets
		TBudget tBudget = new TBudget(context);
		tBudget.removeBudgetsForAccount(id);

		// remove debts
		TDebt tDebt = new TDebt(context);
		tDebt.removeDebtsForAccount(id);

		// remove transfers
		TTransfers tTransfers = new TTransfers(context);
		tTransfers.removeTransfersForAccount(id);

		dbHelper.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(id)});

	}

	@Override
	public double getSumOfBalanceOfAllAccounts() {
		final Cursor c = dbHelper.select(q_SUM_BALANCE_FROM_ALL_ACCOUNTS(), null);
		if (c.moveToNext()) {
			return c.getDouble(c.getColumnIndex(BALANCE));
		} else {
			return -1;
		}
	}

	@Override
	public double getSumOfBalanceOfAccount(int selectedAccount) {
		final Cursor c = dbHelper.select(q_SUM_BALANCE_OF_ACCOUNT(selectedAccount), null);
		if (c.moveToNext()) {
			return c.getDouble(c.getColumnIndex(BALANCE));
		} else {
			return -1;
		}
	}

	@Override
	public void updateAccount(Account account) throws AccountNameExistsException {

		Cursor c = dbHelper.select(q_CHECK_ACCOUNT_NAME(account.getName().replace("'", "''")), null);

		if (c.getCount() > 0) {
			throw new AccountNameExistsException();
		} else {

			final ContentValues cv = new ContentValues();
			cv.put(NAME, account.getName().toLowerCase());// to be stored in lower case in database
			dbHelper.update(TABLE_NAME, cv, ID + " = ?", new String[]{String.valueOf(account.getId())});
		}

	}

	@Override
	public void updateAccountBalance(int id, double amount, boolean add) throws InsufficientBalanceException {

		final Cursor c = dbHelper.select(q_SELECT_ACCOUNT(id), null);
		c.moveToFirst();
		final double bal = c.getDouble(c.getColumnIndex(BALANCE));
		double new_bal = bal;// set this to bal instead of 0 so, even if something goes wrong bal won't reset to 0
		if (add) {
			new_bal = bal + amount;
		} else {
			if (amount > bal) {

				throw new InsufficientBalanceException();

			} else {
				new_bal = bal - amount;
			}
		}
		c.close();

		final ContentValues cv = new ContentValues();
		cv.put(BALANCE, new_bal);
		dbHelper.update(TABLE_NAME, cv, ID + " = ?", new String[]{String.valueOf(id)});

	}

	private Account extractAccountFromCursor(Cursor c) {
		final int id = c.getInt(c.getColumnIndex(ID));
		final String name = c.getString(c.getColumnIndex(NAME));
		final double balance = c.getDouble(c.getColumnIndex(BALANCE));
		final double start_bal = c.getDouble(c.getColumnIndex(STARTING_BALANCE));
		String dateString = c.getString(c.getColumnIndex(DATE));
		dateString = dateString == null || dateString.equals("") ? MyCalendar.stringFormatOfDate(MyCalendar.dateToday()) : dateString;
		Date dateTime = null;
		try {
			dateTime = MyCalendar.getSimpleDateFormat().parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final boolean exclude = c.getInt(c.getColumnIndex(EXCLUDE)) == 1;

		return new Account(id, name, balance, start_bal, dateTime, exclude);
	}

}
