package com.moneymanager.activities.search;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.MyBaseActivity;
import com.moneymanager.entities.Transaction;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;

import static com.moneymanager.Common.ALL_ACCOUNT_ID;
import static com.moneymanager.Common.CURRENT_ACCOUNT_ID;

public class ASearchResults extends MyBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_search_results);

		final String[] queries = getIntent().getStringArrayExtra("queries");

		final TextView textView = (TextView) findViewById(R.id.a_search_result_text);
		final ListView listView = (ListView) findViewById(R.id.a_search_result_listview);

		new AsyncTask<Void, Void, Transaction[]>() {

			@Override
			protected Transaction[] doInBackground(Void... params) {
				TTransactions tTransactions = new TTransactions(ASearchResults.this);
				return tTransactions.getsSearchedTransactions(queries);
			}

			@Override
			protected void onPostExecute(Transaction[] transactions) {
				super.onPostExecute(transactions);

				if (transactions == null) {
					listView.setVisibility(View.GONE);
					textView.setVisibility(View.VISIBLE);
					return;
				}

				listView.setVisibility(View.VISIBLE);
				textView.setVisibility(View.GONE);
				listView.setAdapter(new TransListAdapter(ASearchResults.this, transactions));

				showShortToast("Found " + transactions.length + (transactions.length == 1 ? " transaction" : " transactions"));

			}
		}.execute();


	}

	class TransListAdapter extends ArrayAdapter<Transaction> {


		TransListAdapter(Context context, Transaction[] objects) {
			super(context, -1, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final LayoutInflater inflater = getLayoutInflater();

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

			final String details =
					"Amount: " + transaction.getAmountString() + "\n" +
							"Account: " + transaction.getAccount().getName() + "\n" +
							"Category: " + transaction.getCategory().getName() + "\n" +
							"Date: " + MyCalendar.getNiceFormatedCompleteDateString(transaction.getDateTime()) + "\n" +
							"Info: " + transaction.getInfo();

			rowView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(ASearchResults.this)
							.setTitle("Transaction details:")
							.setMessage(details)
							.create()
							.show();
				}
			});

			return rowView;
		}
	}

}
