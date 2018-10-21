// Created by Dhiraj on 01/02/17.

package com.moneymanager.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.moneymanager.db.DBHelper;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Debt;
import com.moneymanager.entities.User;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.repo.interfaces.IDebt;
import com.moneymanager.utilities.MyCalendar;

import java.text.ParseException;
import java.util.Date;

import static com.moneymanager.Common.*;

public class TDebt implements IDebt {

	public static final String TABLE_NAME = "Debt";
	public static final String ID = "_ID";
	public static final String TYPE = "debt_type";
	public static final String USER = "debt_user";
	public static final String AMOUNT = "debt_amt";
	public static final String ACCOUNT = "debt_acc";
	public static final String INFO = "debt_info";
	public static final String DATETIME = "debt_datetime";

	private final String DID_alias = "dID";
	private final String SELECT_DEBT_JOIN_USER_AND_ACCOUNT =
			"SELECT " + TABLE_NAME + "." + ID + " AS " + DID_alias + "," +
					TUser.TABLE_NAME + "." + ID + " AS uid, * FROM " + TABLE_NAME +
					" JOIN " + TUser.TABLE_NAME + " ON " + USER + " = " + TUser.TABLE_NAME + "." + TUser.ID +
					" JOIN " + TAccounts.TABLE_NAME + " ON " + ACCOUNT + " = " + TAccounts.TABLE_NAME + "." + TAccounts.ID;
	private DBHelper dbHelper;
	private Context context;

	public TDebt(Context context) {
		this.context = context;
		dbHelper = new DBHelper(context);
	}

	public static String q_CREATE_TABLE() {
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				TYPE + " INTEGER," +
				USER + " INTEGER," +
				AMOUNT + " DOUBLE," +
				ACCOUNT + " INTEGER," +
				INFO + " TEXT," +
				DATETIME + " DATETIME," +
				"FOREIGN KEY(" + USER + ") REFERENCES " + TUser.TABLE_NAME + "(" + TUser.ID + ")," +
				"FOREIGN KEY(" + ACCOUNT + ") REFERENCES " + TAccounts.TABLE_NAME + "(" + TAccounts.ID + ") ON DELETE CASCADE " +
				");";
	}

	private String q_SELECT_DEBT(int id) {
		return SELECT_DEBT_JOIN_USER_AND_ACCOUNT + " WHERE " + DID_alias + " = " + id;
	}

	private String q_SELECT_DEBTS(int type) {

		return SELECT_DEBT_JOIN_USER_AND_ACCOUNT + " WHERE " + TYPE + " = " + type + " ORDER BY " + DATETIME + " DESC";

	}

	private String q_SELECT_ALL_DEBTS_OF_USER(int user_id) {

		return SELECT_DEBT_JOIN_USER_AND_ACCOUNT + " WHERE " + USER + " = " + user_id + " ORDER BY " + DATETIME + " DESC";

	}

	private String q_SELECT_ACCOUNT_DEBTS(int accId) {

		return SELECT_DEBT_JOIN_USER_AND_ACCOUNT + " WHERE " + ACCOUNT + " = " + accId + " ORDER BY " + DATETIME + " DESC";

	}

	private String q_SELECT_VERY_SPECIFIC_DEBT(int useId, int accID, int type, String dateString) {
		return SELECT_DEBT_JOIN_USER_AND_ACCOUNT +
				" WHERE " + USER + " = " + useId +
				" AND " + ACCOUNT + " = " + accID +
				" AND " + TYPE + " = " + type +
				" AND " + DATETIME + " = '" + dateString + "'";
	}

	@Override
	public void insertDebt(Debt debt) throws InsufficientBalanceException {

//		Debt existingdebt = getVerySpecificDebt(debt.getUser().getId(), debt.getAccount().getId(), debt.getType(), debt.getDate());

		// if debt does not exists, insert new else update existing one
//		if (existingdebt == null) {

		final ContentValues cv = new ContentValues();
		cv.put(TYPE, debt.getType());
		cv.put(USER, debt.getUser().getId());
		cv.put(AMOUNT, debt.getAmount());
		cv.put(ACCOUNT, debt.getAccount().getId());
		cv.put(INFO, debt.getInfo());
		cv.put(DATETIME, MyCalendar.getSimpleDateFormat().format(debt.getDate()));
		dbHelper.insert(TABLE_NAME, cv);

		// update account balance if necessary
		TAccounts tAccounts = new TAccounts(context);
		final boolean add = debt.getType() == LOAN;
		tAccounts.updateAccountBalance(debt.getAccount().getId(), debt.getAmount(), add);

//		} else {
//
//			final double existingAmount = existingdebt.getAmount();
//
//			existingdebt.setAmount(existingAmount + debt.getAmount());
//
//			updateDebtAmount(existingdebt);
//
//		}

	}

	@Override
	public Debt getDebt(int id) {
		Cursor c = dbHelper.select(q_SELECT_DEBT(id), null);

		if (c.moveToFirst()) {
			return extractDebtFromCursor(c);
		} else {
			return null;
		}

	}

	@Override
	public void updateDebtAmount(Debt new_debt) throws InsufficientBalanceException {

		// first update the account balance
		final double oldDebtAmt = getDebt(new_debt.getId()).getAmount();
		final double diff = oldDebtAmt - new_debt.getAmount();
		TAccounts tAccounts = new TAccounts(context);
		final boolean add = new_debt.getType() == DEBT || new_debt.getType() == DEBT_REPAY;
		tAccounts.updateAccountBalance(new_debt.getAccount().getId(), diff, add);

		if (new_debt.getAmount() == 0) {
			// remove the debt
			removeDebt(new_debt);

		} else {

			// then update the debt
			ContentValues cv = new ContentValues();
			cv.put(AMOUNT, new_debt.getAmount());

			dbHelper.update(TABLE_NAME, cv, ID + " = ?", new String[]{String.valueOf(new_debt.getId())});
		}
	}

	@Override
	public Debt[] getDebts(int type) {

		Cursor c = dbHelper.select(q_SELECT_DEBTS(type), null);

		Debt[] debts = new Debt[c.getCount()];

		while (c.moveToNext()) {
			debts[c.getPosition()] = extractDebtFromCursor(c);
		}

		return debts;
	}

	@Override
	public Debt[] getAllDebtsForUser(int id) {

		Cursor c = dbHelper.select(q_SELECT_ALL_DEBTS_OF_USER(id), null);

		Debt[] debts = new Debt[c.getCount()];

		while (c.moveToNext()) {
			debts[c.getPosition()] = extractDebtFromCursor(c);
		}

		return debts;
	}

	@Override
	public Debt getVerySpecificDebt(int useId, int accID, int type, Date date) {

		String dateString = MyCalendar.getSimpleDateFormat().format(date);
		Cursor c = dbHelper.select(q_SELECT_VERY_SPECIFIC_DEBT(useId, accID, type, dateString), null);

		if (c.moveToFirst()) {
			return extractDebtFromCursor(c);
		} else {
			return null;
		}

	}

	@Override
	public Debt[] getAccountSpecificDebts(int selectedAccountId) {
		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_DEBTS(selectedAccountId), null);

		Debt[] debts = new Debt[c.getCount()];

		while (c.moveToNext()) {
			debts[c.getPosition()] = extractDebtFromCursor(c);
		}

		return debts;
	}

	public void updateDebt(Debt d) throws InsufficientBalanceException {

		Cursor c = dbHelper.select(q_SELECT_DEBT(d.getId()), null);
		c.moveToFirst();
		final Debt exitsingDebt = extractDebtFromCursor(c);
		final double oldAmt = exitsingDebt.getAmount();
		final double amtDiff = d.getAmount() - oldAmt;

		if (d.getAmount() == 0) {
			removeDebt(d);
		} else {
			final ContentValues cv = new ContentValues();
			cv.put(TYPE, d.getType());
			cv.put(ACCOUNT, d.getAccount().getId());
			cv.put(USER, d.getUser().getId());
			cv.put(DATETIME, d.formatedDateTime());
			cv.put(AMOUNT, d.getAmount());
			cv.put(INFO, d.getInfo());

			dbHelper.update(TABLE_NAME, cv, ID + " = ?", new String[]{String.valueOf(d.getId())});
		}
		TAccounts tAccounts = new TAccounts(context);

		// income
		if (d.getType() == DEBT) {

			tAccounts.updateAccountBalance(d.getAccount().getId(), amtDiff, false);

		} else {

			tAccounts.updateAccountBalance(d.getAccount().getId(), amtDiff, true);

		}

	}

	@Override
	public void removeDebt(Debt debt) {

		dbHelper.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(debt.getId())});

	}

	@Override
	public void removeDebtsForAccount(int id) {
		dbHelper.delete(TABLE_NAME, ACCOUNT + " = ?", new String[]{String.valueOf(id)});
	}

	private Debt extractDebtFromCursor(Cursor c) {

		final int id = c.getInt(c.getColumnIndex(DID_alias));
		final int type = c.getInt(c.getColumnIndex(TYPE));

		// create a User
		final int user_id = c.getInt(c.getColumnIndex(USER));
		final String user_name = c.getString(c.getColumnIndex(TUser.NAME));
		final User user = new User(user_id, user_name);

		final double amount = c.getDouble(c.getColumnIndex(AMOUNT));

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

		final String info = c.getString(c.getColumnIndex(INFO));
		Date date = null;
		try {
			date = MyCalendar.getSimpleDateFormat().parse(c.getString(c.getColumnIndex(DATETIME)));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return new Debt(id, type, user, amount, account, info, date);

	}

}
