package com.moneymanager.fragments;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.moneymanager.R;
import com.moneymanager.activities.AUser;
import com.moneymanager.activities.transaction.AAddTransaction;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Debt;
import com.moneymanager.entities.User;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.repo.TDebt;
import com.moneymanager.repo.TUser;
import com.moneymanager.utilities.MyCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.moneymanager.Common.*;

public class FAddDebt extends Fragment {

	private static Date selectedDebtDate;
	private final String[] debtTypeNames = {"Debt", "Debt repay", "Loan", "Loan repay"};
	// Views
	EditText info_text;
	TextInputLayout tip;
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

	public FAddDebt() {
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateUserList();

	}


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		if (getArguments() != null) {
			selectedDebtId = getArguments().getInt("debt_id");
		}


		final View rootView = inflater.inflate(R.layout.f_add_debt, container, false);

		final TextView type_text = (TextView) rootView.findViewById(R.id.add_debt_type);
		final TextView acc_text = (TextView) rootView.findViewById(R.id.add_debt_acc);
		final TextView user_text = (TextView) rootView.findViewById(R.id.add_debt_user);
		final TextView date_text = (TextView) rootView.findViewById(R.id.add_debt_date);
		tip = (TextInputLayout) rootView.findViewById(R.id.f_add_debt_amt_textinput);
		final EditText amt_text = (EditText) rootView.findViewById(R.id.add_debt_amt);
		info_text = (EditText) rootView.findViewById(R.id.add_debt_info);


		type_text.setText("Type: " + getTypeText());
		type_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setCancelable(true);
				builder.setTitle("Select the Debt Type");
				builder.setItems(debtTypeNames, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						selectedDebtType = i + DEBT;
						((AAddTransaction) getActivity()).updateDebtType(selectedDebtType);
						type_text.setText("Type: " + getTypeText());
						Log.i(mylog, "selected debt type: " + selectedDebtType);
						dialogInterface.dismiss();
						updateUserList();
						updateDebtAmount();
					}
				});
				builder.create().show();
			}
		});

		acc_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String[] names = new String[acc_name_list.size()];
				for (int i = 0; i < names.length; i++) {
					names[i] = acc_name_list.get(i);
				}

				final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setCancelable(true);
				builder.setTitle("Select an Account");
				builder.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						setAccount(acc_id_list.get(i));
						acc_text.setText("account: " + names[i]);
						dialogInterface.dismiss();
						date_text.setEnabled(true);

						updateUserList();
						user_text.setEnabled(true);
						updateDebtAmount();
					}
				});
				builder.create().show();
			}
		});

		user_text.setText("Enter " + getUserType());
		user_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final String[] names = new String[user_name_list.size()];
				for (int i = 0; i < names.length; i++) {
					names[i] = user_name_list.get(i);
				}

				final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setCancelable(true);
				builder.setTitle("Select a User");
				builder.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						selectedUserID = user_id_list.get(i);
						((AAddTransaction) getActivity()).updateUserId(selectedUserID);
						user_text.setText(getUserType() + ": " + names[i]);
						dialogInterface.dismiss();
						amt_text.setEnabled(true);
						info_text.setEnabled(true);
						updateDebtAmount();
					}
				});
				builder.setPositiveButton("manage Users", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(getContext(), AUser.class));
					}
				});
				builder.create().show();


			}
		});

		selectedDebtDate = MyCalendar.dateToday();

		Debt debt = null;
		if (selectedDebtId > 0) {
			debt = new TDebt(getContext()).getDebt(selectedDebtId);
			selectedDebtType = debt.getType() + 1;
			((AAddTransaction) getActivity()).updateDebtType(selectedDebtType);
			selectedAccountID = debt.getAccount().getId();
			setAccount(selectedAccountID);
			selectedUserID = debt.getUser().getId();
			((AAddTransaction) getActivity()).updateUserId(selectedUserID);
			selectedDebtDate = debt.getDate();
			updateDebtAmount();
		}
		if (debt != null) {
			type_text.setText("Type: " + getTypeText());
			type_text.setEnabled(true);
			acc_text.setText("account: " + debt.getAccount().getName());
			user_text.setText(getUserType() + ": " + debt.getUser().getName());
			user_text.setEnabled(true);
			info_text.setText(debt.getInfo());
			info_text.setEnabled(true);
			date_text.setEnabled(true);
			final double amt = debt.getAmount();
			tip.setHint("amount: " + amt);
			amt_text.setEnabled(true);

		}
		date_text.setText(MyCalendar.getNiceFormatedCompleteDateString(selectedDebtDate));
		((AAddTransaction) getActivity()).updateDebtDate(selectedDebtDate);

		return rootView;
	}

	private String getTypeText() {
		switch (selectedDebtType) {
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

	/**
	 * Initialization stuff goes here.
	 * NOTE: No android.view.View init stuff should happen here
	 */
	private void init() {
		user_name_list = new ArrayList<>();
		user_id_list = new ArrayList<>();
		acc_name_list = new ArrayList<>();
		acc_id_list = new ArrayList<>();

		updateUserList();
		updateAccountsList();

		//setAccount(CURRENT_ACCOUNT_ID);

	}

	private void updateUserList() {

		final TUser tUser = new TUser(getContext());

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

	void updateDebtAmount() {

		TDebt tDebt = new TDebt(getContext());
		int type;
		switch (selectedDebtType) {
			case DEBT:
				type = DEBT;
				break;
			case LOAN:
				type = LOAN;
				break;
			default:
				type = selectedDebtType - 1;
		}
		Debt debt = tDebt.getVerySpecificDebt(selectedUserID, selectedAccountID, type, selectedDebtDate);

		if (debt != null) {
			debtAmt = debt.getAmount();
			((AAddTransaction) getActivity()).setUpdateDebtId(debt.getId());
			tip.setHint("amount: " + debtAmt);
		} else {
			debtAmt = -1;
			tip.setHint("amount");
		}

		Log.i(mylog, "amount updated " + debtAmt);

		((AAddTransaction) getActivity()).updateDebtAmount(debtAmt);

		setDebtInfo(debt);
	}

	void setDebtInfo(Debt debt) {

		if (info_text != null) {

			info_text.setText(debt != null ? debt.getInfo() : "");

		}

	}

	// Account update is only needed once and is done in onActivityCreated
	private void updateAccountsList() {

		final TAccounts acc = new TAccounts(getContext());

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

		selectedAccountID = selectedAccount;
		((AAddTransaction) getActivity()).updateDebtAccountId(selectedAccountID);

		TAccounts tAccounts = new TAccounts(getContext());
		selectedAccountBalance = tAccounts.getSumOfBalanceOfAccount(selectedAccount);
		((AAddTransaction) getActivity()).updateDebtAccountBalance(selectedAccountBalance);

	}

	private String getUserType() {

		switch (selectedDebtType) {
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

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		public Dialog onCreateDialog(Bundle savedInstanceState) {

			final Calendar cal = Calendar.getInstance();
			int y = cal.get(Calendar.YEAR);
			int m = cal.get(Calendar.MONTH);
			int d = cal.get(Calendar.DAY_OF_MONTH);
			DatePickerDialog dp = new DatePickerDialog(getActivity(), this, y, m, d);
			return new DatePickerDialog(getContext(), this, selectedDebtDate.getYear() + 1900, selectedDebtDate.getMonth(), selectedDebtDate.getDate());
		}

		public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

			year -= 1900;//
			Date newDate = new Date(year, month, dayOfMonth);

			if (newDate.after(MyCalendar.dateToday())) {
				Toast.makeText(getActivity(), "Date cannot be greater than today's date", Toast.LENGTH_LONG).show();
				return;
			}

			selectedDebtDate = newDate;

			((AAddTransaction) getActivity()).updateDebtDate(selectedDebtDate);

			final TextView text = (TextView) getActivity().findViewById(R.id.add_debt_date);
			text.setText(MyCalendar.getNiceFormatedCompleteDateString(selectedDebtDate));

			for (Fragment f : getFragmentManager().getFragments()) {

				if (f instanceof FAddDebt) {
					((FAddDebt) f).updateDebtAmount();
				}

			}

		}

	}

}
