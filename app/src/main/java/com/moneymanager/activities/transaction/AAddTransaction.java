package com.moneymanager.activities.transaction;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.moneymanager.R;
import com.moneymanager.adapters.AddTransactionAdapter;
import com.moneymanager.entities.*;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.fragments.FAddDebt;
import com.moneymanager.fragments.FAddTransaction;
import com.moneymanager.repo.*;
import com.moneymanager.utilities.MyCalendar;

import java.util.Date;

import static com.moneymanager.Common.*;

public class AAddTransaction extends AppCompatActivity implements
		FAddTransaction.OnCategorySelectListener,
		FAddTransaction.OnAccountSelectListener,
		FAddTransaction.OnDateSelectListener {

	private ViewPager viewPager;
	private int selectedCategoryId = -1;
	private int selectedDebtAccountId = -1;
	private int selectedTransactionAccountId = -1;
	private int selectedDebtType = DEBT;
	private double selectedDebtAmount = -1;
	private int selectedDebtId = -1;
	private int selectedUserId = -1;
	private Date selectedTransactionDate = null;
	private double selectedTransactionAccountBalance;
	private double selectedDebtAccountBalance;
	private Date selectedDebtDate;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_add_transaction);

		setupToolbar(this, R.id.transaction_toolbar, "Add a Transaction");

		// setting up ViewPager Stuff
		final int debtID = getIntent().getIntExtra("debt", -1);
		final AddTransactionAdapter ta = new AddTransactionAdapter(getSupportFragmentManager(), debtID);

		viewPager = (ViewPager) findViewById(R.id.transaction_viewpager);
		viewPager.setAdapter(ta);
		if (debtID < 0) {
			viewPager.setCurrentItem(0);
		} else {
			viewPager.setCurrentItem(1);

		}

	}

	public void OnSetDateClick(View view) {

		switch (viewPager.getCurrentItem()) {
			case 0:
				final FAddTransaction.TimePickerFragment timePickerFragment = new FAddTransaction.TimePickerFragment();
				timePickerFragment.show(getSupportFragmentManager(), "Pick a Date");
				break;
			case 1:
				final FAddDebt.DatePickerFragment datePickerFragment = new FAddDebt.DatePickerFragment();
				datePickerFragment.show(getSupportFragmentManager(), "Pick a Date");
				break;

		}


	}

	private Transaction getNewTransaction() {

		final TCategories cat_table = new TCategories(this);
		final TAccounts acc_table = new TAccounts(this);

		String errorMessage;

		// category
		Category cat;
		if (selectedCategoryId <= 0) {
			errorMessage = "Select a Category";
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			cat = cat_table.getCategory(selectedCategoryId);
		}

		// Account
		Account acc;
		if (selectedTransactionAccountId <= 0) {
			errorMessage = "Select an Account first";// this should not happen since the account is set to current by default, but still...
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			acc = acc_table.getAccount(selectedTransactionAccountId);
		}

		// amount
		final EditText add_trans_amt = (EditText) findViewById(R.id.add_trans_amt);
		final String amt = add_trans_amt.getText().toString();
		if (amt.equals("")) {
			errorMessage = "Amount cannot be empty";
			add_trans_amt.setError(errorMessage);
			return null;
		} else if (Double.valueOf(amt) > selectedTransactionAccountBalance && cat.getType() == EXPENSE) {
			add_trans_amt.setError("Expense should not exceed Account Balance: (" + formatAmt(selectedTransactionAccountBalance) + ")");
			return null;
		}

		// info
		final String info = ((TextView) findViewById(R.id.add_trans_info)).getText().toString();

		// date
		if (selectedTransactionDate == null) {
			selectedTransactionDate = MyCalendar.dateToday();
		}

		// exclude
//		final boolean ex = ((Switch) findViewById(R.id.add_trans_ex)).isChecked();

		return new Transaction(-1, Double.valueOf(amt), cat, acc, info, selectedTransactionDate, false);

	}

	private Debt getNewDebt() {

		final TUser user_table = new TUser(this);
		final TAccounts acc_table = new TAccounts(this);
		TDebt tDebt = new TDebt(this);

		String errorMessage;

		// user
		User user;
		if (selectedUserId <= 0) {
			errorMessage = "Select a User";
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			user = user_table.getUser(selectedUserId);
		}

		// Account
		Account acc;
		if (selectedDebtAccountId <= 0) {
			errorMessage = "Select an Account first";// this should not happen since the account is set to current by default, but still...
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			acc = acc_table.getAccount(selectedDebtAccountId);
		}

		// info
		final String info = ((TextView) findViewById(R.id.add_debt_info)).getText().toString();

		// date
		if (selectedTransactionDate == null) {
			selectedTransactionDate = MyCalendar.dateToday();
		}

		// amount
		final EditText add_debt_amt = (EditText) findViewById(R.id.add_debt_amt);
		double amt;

		if (add_debt_amt.getText().toString().equals("")) {
			amt = -1;
		} else {
			amt = Double.valueOf(add_debt_amt.getText().toString());
		}

		if (amt == -1) {
			errorMessage = "Amount cannot be empty";
			add_debt_amt.setError(errorMessage);
			return null;
		} else {

			switch (selectedDebtType) {

				case DEBT:
					if (selectedDebtAccountBalance < amt) {
						errorMessage = "amount exceeds Account Balance: " + selectedDebtAccountBalance;
						add_debt_amt.setError(errorMessage);
						return null;
					}
					break;
				case DEBT_REPAY:
					if (selectedDebtAmount < 0) {
						errorMessage = "debt does not exists";
						add_debt_amt.setError(errorMessage);
						return null;
					} else if (amt > selectedDebtAmount) {
						errorMessage = "amount exceeds debt amount: " + selectedDebtAmount;
						add_debt_amt.setError(errorMessage);
						return null;
					} else {
						amt = selectedDebtAmount - amt;
					}
					break;
				case LOAN:
					break;
				case LOAN_REPAY:

					if (selectedDebtAmount < 0) {
						errorMessage = "loan does not exists";
						add_debt_amt.setError(errorMessage);
						return null;
					} else if (amt > selectedDebtAmount) {
						errorMessage = "amount exceeds loan amount: " + selectedDebtAmount;
						add_debt_amt.setError(errorMessage);
						return null;
					} else {
						amt = selectedDebtAmount - amt;
					}
					break;

			}

		}

		return new Debt(selectedDebtId, selectedDebtType, user, amt, acc, info, selectedDebtDate);

	}

	@Override
	public void onBackPressed() {


		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle("Sure you want to leave?")
				.setMessage("your changes won't be saved")
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setPositiveButton("Yes, leave", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.create();
		alertDialog.show();

	}

	/**
	 * Toolbar Menu Stuff
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_trans_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		// whether to insert transaction or debt
		switch (viewPager.getCurrentItem()) {
			case 0: {
				// insert new transaction data into database
				final TTransactions trans_table = new TTransactions(this);
				final Transaction newTransaction = getNewTransaction();
				if (newTransaction != null) {
					try {
						trans_table.insertNewTransaction(newTransaction);
					} catch (InsufficientBalanceException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
					Log.i(mylog, newTransaction.toString());
					finish();
					Toast.makeText(this, "New Transaction added successfully in " + newTransaction.getAccount().getName(), Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 1: {
				// insert new transaction data into database
				final TDebt debt_table = new TDebt(this);
				final Debt new_debt = getNewDebt();
				if (new_debt != null) {

					try {

						if (new_debt.getType() == DEBT || new_debt.getType() == LOAN) {
							debt_table.insertDebt(new_debt);
							Toast.makeText(this, "new debt added", Toast.LENGTH_SHORT).show();
						} else {
							debt_table.updateDebtAmount(new_debt);
							Toast.makeText(this, "debt updated", Toast.LENGTH_SHORT).show();
						}


						finish();
					} catch (InsufficientBalanceException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
				break;
			}

		}


		return true;
	}

	@Override
	public void updateCategoryId(int categoryID) {
		this.selectedCategoryId = categoryID;
	}

	@Override
	public void updateTransactionAccountId(int accountId) {
		this.selectedTransactionAccountId = accountId;
	}

	public void updateDebtAccountId(int accountId) {
		this.selectedDebtAccountId = accountId;
	}

	@Override
	public void updateTransactionAccountBalance(double amount) {
		this.selectedTransactionAccountBalance = amount;
	}

	public void updateDebtAccountBalance(double amount) {
		this.selectedDebtAccountBalance = amount;
	}

	public void updateUserId(int selectedUser) {
		this.selectedUserId = selectedUser;
	}

	public void updateDebtType(int selectedDebtType) {
		this.selectedDebtType = selectedDebtType;
	}

	public void updateDebtAmount(double selectedDebtAmount) {
		this.selectedDebtAmount = selectedDebtAmount;
	}

	public void setUpdateDebtId(int id) {
		this.selectedDebtId = id;
	}

	@Override
	public void updateTransactionDate(Date date) {
		selectedTransactionDate = date;
	}


	public void updateDebtDate(Date date) {
		selectedDebtDate = date;
	}

}
