// Created by Dhiraj on 24/02/17.

package com.moneymanager.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.moneymanager.db.DBHelper;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Transfer;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.repo.interfaces.ITransfer;
import com.moneymanager.utilities.MyCalendar;

import java.text.ParseException;
import java.util.Date;

public class TTransfers implements ITransfer {

	public static final String TABLE_NAME = "Transfer";
	public static final String ID = "_ID";
	public static final String TO_ACCOUNT = "transfer_to_account";
	public static final String FROM_ACCOUNT = "transfer_from_account";
	public static final String AMOUNT = "transfer_amount";
	public static final String DATE = "transfer_date";

	private DBHelper dbHelper;
	private Context context;

	public TTransfers(Context context) {
		this.dbHelper = new DBHelper(context);
		this.context = context;
	}

	/* Quey Strings */
	public static String q_CREATE_TABLE() {

		return
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
						ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						TO_ACCOUNT + " INTEGER," +
						FROM_ACCOUNT + " INTEGER," +
						AMOUNT + " DOUBLE," +
						DATE + " DATETIME," +
						"FOREIGN KEY(" + TO_ACCOUNT + ") REFERENCES " + TAccounts.TABLE_NAME + "(" + TAccounts.ID + ") ON DELETE CASCADE, " +
						"FOREIGN KEY(" + FROM_ACCOUNT + ") REFERENCES " + TAccounts.TABLE_NAME + "(" + TAccounts.ID + ") ON DELETE CASCADE " +
						")";

	}

	private String q_SELECT_ALL_TRANSFERS() {
		return "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DATE + " DESC";
	}

	private String q_SELECT_ACCOUNT_TRANSFERS(int accountID) {
		return "SELECT * FROM " + TABLE_NAME +
				" WHERE " + TO_ACCOUNT + " = " + accountID +
				" OR " + FROM_ACCOUNT + " = " + accountID +
				" ORDER BY " + DATE + " DESC";
	}

	@Override
	public long addTransfer(Transfer transfer) throws InsufficientBalanceException {

		final TAccounts tAccounts = new TAccounts(context);
		final Account fAccount = transfer.getFromAccount();
		final Account tAccount = transfer.getToAccount();
		final double amt = transfer.getAmount();

		if (amt > fAccount.getBalance()) {
			throw new InsufficientBalanceException();
		} else {

			tAccounts.updateAccountBalance(fAccount.getId(), amt, false);
			tAccounts.updateAccountBalance(tAccount.getId(), amt, true);

			final ContentValues cv = new ContentValues();
			cv.put(TO_ACCOUNT, tAccount.getId());
			cv.put(FROM_ACCOUNT, fAccount.getId());
			cv.put(AMOUNT, amt);
			cv.put(DATE, transfer.formatedDateTime());

			return dbHelper.insert(TABLE_NAME, cv);

		}

	}

	@Override
	public Transfer[] getAllTransfers() {

		Cursor c = dbHelper.select(q_SELECT_ALL_TRANSFERS(), null);
		final Transfer[] transfers = new Transfer[c.getCount()];
		while (c.moveToNext()) {
			transfers[c.getPosition()] = extractTransferFromCursor(c);
		}

		return transfers;

	}

	@Override
	public Transfer[] getAccountTransfers(int accountID) {
		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSFERS(accountID), null);
		final Transfer[] transfers = new Transfer[c.getCount()];
		while (c.moveToNext()) {
			transfers[c.getPosition()] = extractTransferFromCursor(c);
		}

		return transfers;
	}

	@Override
	public void removeTransfersForAccount(int id) {

		Cursor c = dbHelper.select(q_SELECT_ACCOUNT_TRANSFERS(id), null);

		String delTransferIds = "";

		while (c.moveToNext()) {

			Transfer transfer = extractTransferFromCursor(c);
			if (transfer.getFromAccount().getId() == -1 || transfer.getToAccount().getId() == -1) {
				delTransferIds += transfer.getId();
			}

			if (!c.isLast()) {
				delTransferIds += ",";
			}

		}

		if (!delTransferIds.equals("")) {
			dbHelper.delete(TABLE_NAME, ID + " IN (?)", new String[]{delTransferIds});
		}

	}

	private Transfer extractTransferFromCursor(Cursor c) {

		final int id = c.getInt(c.getColumnIndex(ID));
		final double amount = c.getDouble(c.getColumnIndex(AMOUNT));
		final int to_account_id = c.getInt(c.getColumnIndex(TO_ACCOUNT));
		final int from_account_id = c.getInt(c.getColumnIndex(FROM_ACCOUNT));
		Date date = null;
		try {
			date = MyCalendar.getSimpleDateFormat().parse(c.getString(c.getColumnIndex(DATE)));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		final TAccounts tAccounts = new TAccounts(context);

		// create to account
		Account to_account = tAccounts.getAccount(to_account_id);
		if (to_account == null) {
			to_account = new Account(-1, "deleted account", -1, -1, null, false);
		}

		Account from_account = tAccounts.getAccount(from_account_id);
		if (from_account == null) {
			from_account = new Account(-1, "deleted account", -1, -1, null, false);
		}

		return new Transfer(id, to_account, from_account, amount, date);

	}

}
