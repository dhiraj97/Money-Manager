package com.moneymanager.activities.transaction;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.moneymanager.R;
import com.moneymanager.activities.category.ACategories;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Category;
import com.moneymanager.entities.Transaction;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.repo.TCategories;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;

import java.util.ArrayList;
import java.util.Date;

import static com.moneymanager.Common.*;

public class AEditTransaction extends AppCompatActivity {

	private int selectedTransactionID;
	private Transaction transaction;
	private int selectedCategory = -1;
	private int selectedAccount = -1;

	private TTransactions tTransactions;

	private Category[] income_cat_arr;
	private Category[] expense_cat_arr;
	private ArrayList<String> cat_name_list;
	private ArrayList<Integer> cat_id_list;
	private ArrayList<String> acc_name_list;
	private ArrayList<Integer> acc_id_list;

	private double selectedAccountBalance = -1;

	// Views
	private EditText amt_edittext;
	private ToggleButton type_toggle;
	private TextView cat_text;
	private TextView acc_text;
	private EditText info_edittext;
	private TextView date_text;
	private Date selectedDate;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_edit_transaction);

		setupToolbar(this, R.id.edit_transaction_toolbar, "Edit Transaction");


		cat_name_list = new ArrayList<>();
		cat_id_list = new ArrayList<>();
		acc_name_list = new ArrayList<>();
		acc_id_list = new ArrayList<>();

		selectedTransactionID = getIntent().getIntExtra("trans_id", -1);

		tTransactions = new TTransactions(this);
		transaction = tTransactions.getTransaction(selectedTransactionID);

		// setting up views
		amt_edittext = (EditText) findViewById(R.id.edit_trans_amt);
		amt_edittext.setText(String.valueOf(transaction.getAmount()));

		type_toggle = (ToggleButton) findViewById(R.id.edit_trans_type);
		type_toggle.setChecked(transaction.getCategory().getType() == EXPENSE);
		type_toggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateCategoryList();
				type_toggle.setTextColor(getMyColor(AEditTransaction.this, type_toggle.isChecked() ? R.color.colorWhite : R.color.colorPrimaryDark));
			}
		});

		updateCategoryList();
		cat_text = (TextView) findViewById(R.id.edit_trans_cat);
		cat_text.setText("category: " + transaction.getCategory().getName());
		cat_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final String[] names = new String[cat_name_list.size()];
				for (int i = 0; i < names.length; i++) {
					names[i] = cat_name_list.get(i);
				}

				final AlertDialog.Builder builder = new AlertDialog.Builder(AEditTransaction.this);
				builder.setCancelable(true);
				builder.setTitle("Select a Category");
				builder.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						selectedCategory = cat_id_list.get(i);
						cat_text.setText("category: " + names[i]);
						Log.i(mylog, "selected category id: " + selectedCategory);
						dialogInterface.dismiss();
					}
				});
				builder.setPositiveButton("manage categories", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						Intent intent = new Intent(AEditTransaction.this, ACategories.class);
						intent.putExtra("type", type_toggle.isChecked() ? EXPENSE : INCOME);
						startActivity(intent);
					}
				});
				builder.create().show();


			}
		});
		selectedCategory = transaction.getCategory().getId();

		acc_text = (TextView) findViewById(R.id.edit_trans_acc);
		acc_text.setText("account: " + transaction.getAccount().getName());
		acc_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final String[] names = new String[acc_name_list.size()];
				for (int i = 0; i < names.length; i++) {
					names[i] = acc_name_list.get(i);
				}

				final AlertDialog.Builder builder = new AlertDialog.Builder(AEditTransaction.this);
				builder.setCancelable(true);
				builder.setTitle("Select an Account");
				builder.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						setAccount(acc_id_list.get(i));
						acc_text.setText("account: " + names[i]);
						Log.i(mylog, "selected account id: " + selectedAccount);
						dialogInterface.dismiss();
					}
				});
				builder.create().show();


			}
		});
		selectedAccount = transaction.getAccount().getId();
		updateAccountsList();
		setAccount(selectedAccount);

		info_edittext = (EditText) findViewById(R.id.edit_trans_info);
		info_edittext.setText(transaction.getInfo());

		date_text = (TextView) findViewById(R.id.edit_trans_date);
		date_text.setText(MyCalendar.getNiceFormatedCompleteDateString(transaction.getDateTime()));
		date_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final DatePickerFragment datePickerFragment = new DatePickerFragment();
				datePickerFragment.show(getSupportFragmentManager(), "Pick a new Date");
			}
		});
		selectedDate = transaction.getDateTime();


	}

	@Override
	protected void onResume() {
		super.onResume();
		updateCategoryList();
		updateAccountsList();
	}

	// update Category list according to 'income' 'expense' selection
	private void updateCategoryList() {

		final boolean income = !type_toggle.isChecked();

		final TCategories cats = new TCategories(this);

		Category[] cat_array;

		if (income) {
			cat_array = cats.getTypeSpecificCategories(TCategories.INCOME);
		} else {
			cat_array = cats.getTypeSpecificCategories(TCategories.EXPENSE);
		}

		cat_name_list.clear();
		cat_id_list.clear();

		for (Category cat : cat_array) {
			cat_name_list.add(cat.getName());
			cat_id_list.add(cat.getId());
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

	// set account and along with that, update the selected account balance
	private void setAccount(int selectedAccount) {

		this.selectedAccount = selectedAccount;

		TAccounts tAccounts = new TAccounts(this);
		selectedAccountBalance = tAccounts.getSumOfBalanceOfAccount(selectedAccount);

	}


	/**
	 * Toolbar Menu Stuff
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_trans_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		final Transaction updatedTransaction = getUpdatedTransaction();

		switch (item.getItemId()) {
			case R.id.edit_trans_menu_save:
				// update transaction


				if (updatedTransaction != null) {

					try {
						tTransactions.updateTransaction(updatedTransaction);
						Toast.makeText(this, "Transaction Updated", Toast.LENGTH_SHORT).show();
					} catch (InsufficientBalanceException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}

				}

				break;
			case R.id.edit_trans_menu_delete:
				// delete transaction

				final Snackbar sb = Snackbar.make(findViewById(R.id.a_edit_transaction_coordinate_layout),
						"Delete transaction?",
						Snackbar.LENGTH_SHORT)
						.setAction("Sure", new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								tTransactions.removeTransaction(updatedTransaction);
								finish();

							}
						});
				View sbv = sb.getView();
				sbv.setBackgroundColor(getMyColor(this, R.color.colorRed));
				sb.setActionTextColor(getMyColor(this, R.color.colorPrimaryDark));
				sb.show();


				break;
		}


		return true;
	}

	private Transaction getUpdatedTransaction() {

		final TCategories cat_table = new TCategories(this);
		final TAccounts acc_table = new TAccounts(this);

		String errorMessage;

		// category
		Category cat;
		if (selectedCategory <= 0) {
			errorMessage = "Select a Category";
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			cat = cat_table.getCategory(selectedCategory);
		}

		// amount
		final String amt = amt_edittext.getText().toString();
		if (amt.equals("")) {
			amt_edittext.setError("Amount cannot be empty");
			return null;
		} else if (Double.valueOf(amt) > selectedAccountBalance && cat.getType() == EXPENSE && selectedAccount != transaction.getAccount().getId()) {
			amt_edittext.setError("Expense should not exceed Account Balance: (" + formatAmt(selectedAccountBalance) + ")");
			return null;
		}

		// Account
		Account acc;
		if (selectedAccount <= 0) {
			errorMessage = "Select an Account first";// this should not happen since the account is set to current by default, but still...
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
			return null;
		} else {
			acc = acc_table.getAccount(selectedAccount);
		}

		// info
		final String info = ((TextView) findViewById(R.id.edit_trans_info)).getText().toString();

		// date
		if (selectedDate == null) {
			selectedDate = MyCalendar.dateToday();
		}

		// exclude
		final boolean ex = false;

		return new Transaction(selectedTransactionID, Double.valueOf(amt), cat, acc, info, selectedDate, ex);

	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		private Transaction transaction;

		public Dialog onCreateDialog(Bundle savedInstanceState) {

			transaction = ((AEditTransaction) getActivity()).transaction;

			Date tdate = transaction.getDateTime();

			DatePickerDialog dp = new DatePickerDialog(getActivity(), this, (1900 + tdate.getYear()), tdate.getMonth(), tdate.getDate());
			return dp;
		}

		public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

			year -= 1900;//
			Date newDate = new Date(year, month, dayOfMonth);

			if (newDate.after(MyCalendar.dateToday())) {
				Toast.makeText(getActivity(), "Date cannot be greater than today's date", Toast.LENGTH_LONG).show();
				return;
			}

			((AEditTransaction) getActivity()).selectedDate = newDate;

			final TextView text = (TextView) getActivity().findViewById(R.id.edit_trans_date);
			text.setText(MyCalendar.getNiceFormatedCompleteDateString(newDate));
		}

	}

}