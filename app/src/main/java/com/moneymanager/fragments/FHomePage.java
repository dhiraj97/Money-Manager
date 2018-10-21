package com.moneymanager.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.stats.AStats;
import com.moneymanager.activities.transaction.AEditTransaction;
import com.moneymanager.entities.Transaction;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.moneymanager.Common.*;

public class FHomePage extends Fragment {

	private int position;
	private Date myDate; // date for this page
	private SimpleDateFormat sdf;
	private String title;
	private String overviewCardTitle;
	private TTransactions tTransactions;
	private Transaction[] myTransactions; // transactions for this page

	// Views
	private ListView transListView;
	private TextView cardIncomeTextView;
	private TextView cardExpenseTextView;
	private TextView cardTotalTextView;
	private TextView noTransText;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sdf = MyCalendar.getSimpleDateFormat();
		position = getArguments().getInt("pos");
		try {
			myDate = sdf.parse(getArguments().getString("date"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		title = getArguments().getString("title");
		overviewCardTitle = MyCalendar.dateToString(myDate) + " " + MyCalendar.monthToFullString(myDate) + "'s Overview";

		tTransactions = new TTransactions(getContext());
		if (CURRENT_ACCOUNT_ID == ALL_ACCOUNT_ID) {
			myTransactions = tTransactions.getTransactionsForDay(myDate);
		} else {
			myTransactions = tTransactions.getAccountSpecificTransactionsForDay(CURRENT_ACCOUNT_ID, myDate);
		}

	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle bundle) {


		Log.i(mylog, MyCalendar.getNiceFormatedCompleteDateString(myDate));

		final View root = inflater.inflate(R.layout.f_home_page, container, false);

		// Overcard
		final CardView card = (CardView) root.findViewById(R.id.f_home_overview_card);
		card.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(getContext(), AStats.class);
				intent.putExtra("date", sdf.format(myDate));
				startActivity(intent);
			}
		});

		// set overview card's title
		final TextView text = (TextView) root.findViewById(R.id.f_home_overview_card_title);
		text.setText(overviewCardTitle);

		// set income text
		cardIncomeTextView = (TextView) root.findViewById(R.id.f_home_overview_card_income_amt);

		// set expense text
		cardExpenseTextView = (TextView) root.findViewById(R.id.f_home_overview_card_expense_amt);

		// set total text
		cardTotalTextView = (TextView) root.findViewById(R.id.f_home_overview_card_total_amt);

		refreshOverviewCardDetails(CURRENT_ACCOUNT_ID);

		// no transaction text
		noTransText = (TextView) root.findViewById(R.id.f_home_no_trans_text);
		if (myTransactions.length == 0) {
			noTransText.setVisibility(View.VISIBLE);
		} else {
			noTransText.setVisibility(View.GONE);
		}

		// list view
		transListView = (ListView) root.findViewById(R.id.f_home_trans_list);
		transListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(getContext(), AEditTransaction.class);
				intent.putExtra("trans_id", myTransactions[position].getId());
				startActivity(intent);

			}
		});
		refreshTransList(CURRENT_ACCOUNT_ID);

		return root;
	}

	public void refreshFragmentContent(int currentAccount) {
		refreshTransList(currentAccount);
		refreshOverviewCardDetails(currentAccount);
	}

	// Also refreshes Overview Card details
	private void refreshTransList(int currentAccount) {

		if (currentAccount == ALL_ACCOUNT_ID) {
			myTransactions = tTransactions.getTransactionsForDay(myDate);
		} else {
			myTransactions = tTransactions.getAccountSpecificTransactionsForDay(CURRENT_ACCOUNT_ID, myDate);
		}

		// hide the 'no transaction found' text if transaction found
		if (myTransactions.length == 0) {
			noTransText.setVisibility(View.VISIBLE);
		} else {
			noTransText.setVisibility(View.GONE);
		}

		final TransListAdapter tla = new TransListAdapter(getContext(), myTransactions);
		transListView.setAdapter(tla);

	}

	private void refreshOverviewCardDetails(int acc) {

		final TTransactions tTransactions = new TTransactions(getContext());

		double incomeAmt, expenseAmt;

		if (acc == ALL_ACCOUNT_ID) {

			incomeAmt = tTransactions.getSumOfTransactionTypeForDay(INCOME, myDate);
			expenseAmt = tTransactions.getSumOfTransactionTypeForDay(EXPENSE, myDate);

		} else {

			incomeAmt = tTransactions.getAccountSpecificSumOfTransactionTypeForDay(acc, INCOME, myDate);
			expenseAmt = tTransactions.getAccountSpecificSumOfTransactionTypeForDay(acc, EXPENSE, myDate);

		}

		final double totalAmt = incomeAmt - expenseAmt;

		cardIncomeTextView.setText(formatAmt(incomeAmt));
		cardExpenseTextView.setText(formatAmt(expenseAmt));
		cardTotalTextView.setText(formatAmt(totalAmt));

	}

	class TransListAdapter extends ArrayAdapter<Transaction> {


		TransListAdapter(Context context, Transaction[] objects) {
			super(context, -1, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final LayoutInflater inflater = getActivity().getLayoutInflater();

			final View rowView = inflater.inflate(R.layout.x_home_trans_row, parent, false);

			final TextView tCat = (TextView) rowView.findViewById(R.id.x_home_trans_row_cat);
			final TextView tAmt = (TextView) rowView.findViewById(R.id.x_home_trans_row_amt);
			final TextView tAcc = (TextView) rowView.findViewById(R.id.x_home_trans_row_acc);
			final TextView tInfo = (TextView) rowView.findViewById(R.id.x_home_trans_row_info);

			final Transaction transaction = getItem(position);
			final int catType = transaction.getCategory().getType();// 0 - income, 1- expense

			if (catType == Common.INCOME) {
				tAmt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
			} else {
				tAmt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
			}

			if (CURRENT_ACCOUNT_ID == ALL_ACCOUNT_ID) {
				tAcc.setText(transaction.getAccount().getName());
				tAcc.setVisibility(View.VISIBLE);
			} else {
				tAcc.setVisibility(View.GONE);
			}

			tCat.setText(transaction.getCategory().getName());
			tAmt.setText(transaction.getAmountString());
			tInfo.setText(transaction.getShortInfo());

			return rowView;
		}
	}


}
