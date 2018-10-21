package com.moneymanager.activities.debts;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.moneymanager.R;
import com.moneymanager.activities.AUser;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Debt;
import com.moneymanager.entities.User;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.repo.TDebt;
import com.moneymanager.repo.TUser;
import com.moneymanager.utilities.MyCalendar;

import java.util.ArrayList;
import java.util.Date;

import static com.moneymanager.Common.*;

public class AEditDebt extends AppCompatActivity {

	private final String[] debtTypeNames = {"Debt", "Debt repay", "Loan", "Loan repay"};
	Debt debt;
	TDebt tDebt;
	private ArrayList<String> user_name_list;
	private ArrayList<Integer> user_id_list;
	private ArrayList<String> acc_name_list;
	private ArrayList<Integer> acc_id_list;
	private int selectedUserID = -1;
	private int selectedAccountID = -1;
	private int selectedDebtType = DEBT;
	private double selectedAccountBalance = -1;
	private double debtAmt = -1;
	private int selectedDebtId = -1;

	// Views
	private TextView type_text;
	private TextView acc_text;
	private TextView user_text;
	private TextView date_text;
	private EditText amt_edittext;
	private EditText info_edittext;
	private Date selectedDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_edit_debt);

		setupToolbar(this, R.id.edit_debt_toolbar, "Edit Debt");

		user_name_list = new ArrayList<>();
		user_id_list = new ArrayList<>();
		acc_name_list = new ArrayList<>();
		acc_id_list = new ArrayList<>();
		tDebt = new TDebt(this);

		selectedDebtId = getIntent().getIntExtra("debt", -1);
		debt = tDebt.getDebt(selectedDebtId);
		selectedAccountID = debt.getAccount().getId();
		selectedDebtType = debt.getType();
		selectedUserID = debt.getUser().getId();
		selectedDate = debt.getDate();

		updateAccountsList();
		updateUserList();

		//Setting up Views

		type_text = (TextView) findViewById(R.id.edit_debt_type);
		type_text.setText("Type: " + getTypeText(selectedDebtType));
		type_text.setEnabled(false);

		amt_edittext = (EditText) findViewById(R.id.edit_debt_amt);
		amt_edittext.setText(String.valueOf(debt.getAmount()));


		acc_text = (TextView) findViewById(R.id.edit_debt_acc);
		acc_text.setText("account: " + debt.getAccount().getName());
		acc_text.setEnabled(false);
		setAccount(debt.getAccount().getId());

		user_text = (TextView) findViewById(R.id.edit_debt_user);
		user_text.setText(getUserType(selectedDebtType) + ": " + debt.getUser().getName());
		user_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final String[] names = new String[user_name_list.size()];
				for (int i = 0; i < names.length; i++) {
					names[i] = user_name_list.get(i);
				}

				final AlertDialog.Builder builder = new AlertDialog.Builder(AEditDebt.this);
				builder.setCancelable(true);
				builder.setTitle("Select a User");
				builder.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						selectedUserID = user_id_list.get(i);
						user_text.setText(getUserType(selectedDebtType) + ": " + names[i]);
						dialogInterface.dismiss();
					}
				});
				builder.setPositiveButton("manage Users", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(AEditDebt.this, AUser.class));
					}
				});
				builder.create().show();


			}
		});


		date_text = (TextView) findViewById(R.id.edit_debt_date);
		date_text.setText(MyCalendar.getNiceFormatedCompleteDateString(debt.getDate()));
		date_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AEditDebt.DatePickerFragment datePickerFragment = new AEditDebt.DatePickerFragment();
				datePickerFragment.show(getSupportFragmentManager(), "Pick a new Date");
			}
		});
		selectedDate = debt.getDate();

		info_edittext = (EditText) findViewById(R.id.edit_debt_info);
		info_edittext.setText(debt.getInfo());

	}

	@Override
	public void onResume() {
		super.onResume();
		updateUserList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.edit_debt_menu, menu);

		return true;
	}

	private String getTypeText(int type) {
		switch (type) {
			case DEBT:
				return debtTypeNames[0];
			case DEBT_REPAY:
				return debtTypeNames[1];
			case LOAN:
				return debtTypeNames[2];
			case LOAN_REPAY:
				return debtTypeNames[3];
			default:
				return "Select Debt Type";
		}
	}

	private String getUserType(int type) {

		switch (type) {
			case DEBT:
			case DEBT_REPAY:
				return "Borrower";

			case LOAN:
			case LOAN_REPAY:
				return "Lender";

			default:
				return "User";
		}

	}

	private void updateUserList() {

		final TUser tUser = new TUser(this);

		User[] user_array;
		switch (selectedDebtType) {
			case DEBT:
				user_array = tUser.getAllUsers();
				break;
			case DEBT_REPAY:
				// load only those users that have taken debt from me
				if (selectedAccountID == ALL_ACCOUNT_ID) {
					user_array = tUser.getSpecificUsersFromAllAccounts(DEBT);
				} else {
					user_array = tUser.getSpecificUsers(selectedAccountID, DEBT);
				}
				break;
			case LOAN:
				user_array = tUser.getAllUsers();
				break;
			case LOAN_REPAY:
				// load only those users that i have taken money from
				if (selectedAccountID == ALL_ACCOUNT_ID) {
					user_array = tUser.getSpecificUsersFromAllAccounts(LOAN);
				} else {
					user_array = tUser.getSpecificUsers(selectedAccountID, LOAN);
				}
				break;
			default:
				user_array = tUser.getAllUsers();
				break;
		}

		user_name_list.clear();
		user_id_list.clear();

		for (User user : user_array) {
			user_name_list.add(user.getName());
			user_id_list.add(user.getId());
		}
	}

	// Account update is only needed once and is done in onActivityCreated
	private void updateAccountsList() {

		final TAccounts acc = new TAccounts(this);

		try {
			final Account[] acc_array = acc.getAllAccounts(null, null);
			acc_name_list.clear();
			acc_id_list.clear();

			for (Account a : acc_array) {
				acc_name_list.add(a.getName());
				acc_id_list.add(a.getId());
			}
		} catch (NoAccountsException e) {
			e.printStackTrace();
		}


	}

	public boolean onOptionsItemSelected(MenuItem item) {

		final Debt updatedDebt = getUpdatedDebt();

		switch (item.getItemId()) {
			case R.id.edit_debt_menu_save:
				// update transaction


				if (updatedDebt != null) {

					try {
						tDebt.updateDebt(updatedDebt);
						String x = debt.getType() == DEBT ? "Debt" : "Loan";
						Toast.makeText(this, x + " Updated", Toast.LENGTH_SHORT).show();
						finish();
					} catch (InsufficientBalanceException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}

				}

				break;

		}


		return true;
	}

	private Debt getUpdatedDebt() {
		final TUser user_table = new TUser(this);
		final TAccounts acc_table = new TAccounts(this);

		String errorMessage;

		User user;
		if (selectedUserID <= 0) {
			errorMessage = "Select a User";
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			user = user_table.getUser(selectedUserID);
		}

		// amount
		final String amt = amt_edittext.getText().toString();
		if (amt.equals("")) {
			amt_edittext.setError("Amount cannot be empty");
			return null;
		} else if (Double.valueOf(amt) > selectedAccountBalance && debt.getType() == DEBT && selectedAccountID != debt.getAccount().getId()) {
			amt_edittext.setError("Expense should not exceed Account Balance: (" + formatAmt(selectedAccountBalance) + ")");
			return null;
		}

		// Account
		Account acc;
		if (selectedAccountID <= 0) {
			errorMessage = "Select an Account first";// this should not happen since the account is set to current by default, but still...
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			acc = acc_table.getAccount(selectedAccountID);
		}

		// info
		final String info = ((TextView) findViewById(R.id.edit_debt_info)).getText().toString();

		// date
		if (selectedDate == null) {
			selectedDate = MyCalendar.dateToday();
		}

		return new Debt(selectedDebtId, selectedDebtType, user, Double.valueOf(amt), acc, info, selectedDate);
	}

	// set account and along with that, update the selected account balance
	private void setAccount(int selectedAccount) {

		this.selectedAccountID = selectedAccount;

		TAccounts tAccounts = new TAccounts(this);
		selectedAccountBalance = tAccounts.getSumOfBalanceOfAccount(selectedAccount);

	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		private Debt debt;

		public Dialog onCreateDialog(Bundle savedInstanceState) {

			debt = ((AEditDebt) getActivity()).debt;

			Date ddate = debt.getDate();

			DatePickerDialog dp = new DatePickerDialog(getActivity(), this, (1900 + ddate.getYear()), ddate.getMonth(), ddate.getDate());
			return dp;
		}

		public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

			year -= 1900;//
			Date newDate = new Date(year, month, dayOfMonth);

			if (newDate.after(MyCalendar.dateToday())) {
				Toast.makeText(getActivity(), "Date cannot be greater than today's date", Toast.LENGTH_LONG).show();
				return;
			}

			((AEditDebt) getActivity()).selectedDate = newDate;

			final TextView text = (TextView) getActivity().findViewById(R.id.edit_debt_date);
			text.setText(MyCalendar.getNiceFormatedCompleteDateString(newDate));
		}

	}
}
