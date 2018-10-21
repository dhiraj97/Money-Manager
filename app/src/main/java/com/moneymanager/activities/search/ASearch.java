package com.moneymanager.activities.search;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import com.moneymanager.R;
import com.moneymanager.activities.MyBaseActivity;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Category;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.repo.TCategories;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;

import java.util.Calendar;
import java.util.Date;

import static com.moneymanager.Common.*;

public class ASearch extends MyBaseActivity {

	private static Date startDate = MyCalendar.dateToday();
	private static Date endDate = MyCalendar.dateToday();
	private final int ANY = 234;
	private final int GREATER = 235;
	private final int LESS = 236;
	private final int EQUALS = 237;
	private final int BETWEEN = 238;
	private final String[] queries = {"", "", "", "", "", ""};
	private final int AMT_QUETY_IDX = 0;
	private final int CAT_QUETY_IDX = 1;
	private final int ACC_QUETY_IDX = 2;
	private final int PERIOD_QUETY_IDX = 3;
	private final int INFO_QUETY_IDX = 4;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_search);

		setupToolbar(this, R.id.search_toolbar, "Search");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		final TextView infoText = (TextView) findViewById(R.id.a_search_trans_info);
		if (infoText.getText().toString().length() > 0) {
			final String query = TTransactions.INFO + " LIKE '%" + infoText.getText().toString().replace("'", "''") + "%'";
			queries[INFO_QUETY_IDX] = query;
		} else {
			queries[INFO_QUETY_IDX] = "";
		}

		Intent i = new Intent(this, ASearchResults.class);
		i.putExtra("queries", queries);
		startActivity(i);

		return true;
	}

	public void onAmtTextClick(View view) {

		final String[] options = {"any", "greater than", "less than", "equals", "between"};

		final TextView amtTextview = (TextView) findViewById(R.id.a_search_trans_amt);

		final int[] selectedOption = {ANY};

		final double[] selectedAmount = {0, 0};

		final String defaultText = "Any Amount";
		final String[] query = {"", defaultText};


		AlertDialog alert = new AlertDialog.Builder(this)
				.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedOption[0] = ANY + which;

						switch (selectedOption[0]) {

							case GREATER:
								query[0] = TTransactions.AMOUNT + " > ";
								query[1] = "greater than ";
								break;
							case LESS:
								query[0] = TTransactions.AMOUNT + " < ";
								query[1] = "less than ";
								break;
							case EQUALS:
								query[0] = TTransactions.AMOUNT + " = ";
								query[1] = "equals ";
								break;
							case BETWEEN:
								query[0] = TTransactions.AMOUNT + " BETWEEN ";
								query[1] = "between ";
								break;
							default:
								query[0] = "";
								query[1] = defaultText;
						}

					}
				})
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (selectedOption[0]) {
							case ANY:
								query[0] = "";
								query[1] = defaultText;
								queries[AMT_QUETY_IDX] = query[0];
								amtTextview.setText(query[1]);
								break;
							case GREATER:
							case LESS:
							case EQUALS:
								final View view = getLayoutInflater().inflate(R.layout.d_add_search_amt, null);
								final EditText amtText = (EditText) view.findViewById(R.id.d_search_amt);
								AlertDialog secondDialog = new AlertDialog.Builder(ASearch.this)
										.setView(view)
										.setTitle("Set amount")
										.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												selectedAmount[0] = Double.parseDouble(amtText.getText().toString());
												query[0] += selectedAmount[0];
												query[1] += formatAmt(selectedAmount[0]);
												queries[AMT_QUETY_IDX] = query[0];
												amtTextview.setText(query[1]);
											}
										})
										.create();
								secondDialog.show();
								break;
							case BETWEEN:
								final View viewBetween = getLayoutInflater().inflate(R.layout.d_add_search_amt_between, null);
								final EditText startAmt = (EditText) viewBetween.findViewById(R.id.d_search_start_amt);
								final EditText endAmt = (EditText) viewBetween.findViewById(R.id.d_search_end_amt);
								AlertDialog betweenDialog = new AlertDialog.Builder(ASearch.this)
										.setView(viewBetween)
										.setTitle("Set amount range")
										.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {

												// first validate inputs
												if (startAmt.getText().toString().equals("")) {
													startAmt.setError("Enter starting amount");
													return;
												}

												if (endAmt.getText().toString().equals("")) {
													endAmt.setError("Enter ending amount");
													return;
												}

												selectedAmount[0] = Double.parseDouble(startAmt.getText().toString());
												selectedAmount[1] = Double.parseDouble(endAmt.getText().toString());

												if (selectedAmount[0] > selectedAmount[1]) {
													showLongToast("Ending amount cannot be greater than starting amount");
													return;
												}

												dialog.dismiss();

												query[0] += selectedAmount[0] + " AND " + selectedAmount[1];
												query[1] += formatAmt(selectedAmount[0]) + " and " + formatAmt(selectedAmount[1]);
												queries[AMT_QUETY_IDX] = query[0];
												amtTextview.setText(query[1]);
											}
										})
										.create();
								betweenDialog.show();
								break;
						}
						dialog.dismiss();
					}
				})
				.create();
		alert.show();

	}

	public void onCategoryTextClick(View view) {

		final int opt_any = 100;
		final int opt_all_exp = 101;
		final int opt_all_inc = 102;
		final int opt_select_exp = 103;
		final int opt_select_inc = 104;

		final String[] options = {"any", "any expense category", "any income category", "expense category", "income category"};

		final String defaultText = "Any Category";
		final int[] selectedOption = {opt_any};
		final String[] query = {""};

		final TextView catTextview = (TextView) findViewById(R.id.a_search_trans_cat);

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedOption[0] = opt_any + which;
					}
				})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
						switch (selectedOption[0]) {

							case opt_all_exp:
							case opt_all_inc:
								final boolean exp = selectedOption[0] == opt_all_exp;
								query[0] = TCategories.TYPE + " = " + (exp ? EXPENSE : INCOME);
								catTextview.setText("Any " + (exp ? "expense" : "income") + " Category");
								queries[CAT_QUETY_IDX] = query[0];
								break;
							case opt_select_exp:
							case opt_select_inc:
								final boolean expense = selectedOption[0] == opt_select_exp;
								final TCategories tCategories = new TCategories(ASearch.this);
								final Category[] categories = tCategories.getTypeSpecificCategories(expense ? EXPENSE : INCOME);

								final String[] categoryNames = Category.extractNameStringArrayFromArray(categories);

								// new alert dialog
								AlertDialog dialog2 = new AlertDialog.Builder(ASearch.this)
										.setItems(categoryNames, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {

												query[0] = TTransactions.CATEGORY + " = " + categories[which].getId();
												catTextview.setText(categories[which].getName());
												queries[CAT_QUETY_IDX] = query[0];

											}
										})
										.create();
								dialog2.show();
								break;
							default:
								query[0] = "";
								catTextview.setText(defaultText);
								queries[CAT_QUETY_IDX] = query[0];
								break;

						}

					}
				}).create();
		dialog.show();

	}

	public void onAccountTextClick(View view) {

		final int opt_any = 100;
		final int opt_select_acc = 101;
		final String[] options = {"any", "select account"};

		final int[] selectedOption = {opt_any};
		final String defaultText = "All Accounts";
		final String[] query = {""};

		final TextView accTextview = (TextView) findViewById(R.id.a_search_trans_acc);

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedOption[0] = opt_any + which;
					}
				})
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (selectedOption[0]) {
							case opt_select_acc:

								final TAccounts tAccounts = new TAccounts(ASearch.this);
								try {
									final Account[] accounts = tAccounts.getAllAccounts(null, null);

									final String[] accountNames = Account.extractNameStringArrayFromArray(accounts);

									AlertDialog dialog2 = new AlertDialog.Builder(ASearch.this)
											.setItems(accountNames, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {

													query[0] = TTransactions.ACCOUNT + " = " + accounts[which].getId();
													accTextview.setText(accounts[which].getName());
													queries[ACC_QUETY_IDX] = query[0];

												}
											})
											.create();
									dialog2.show();

								} catch (NoAccountsException e) {
									e.printStackTrace();
								}


								break;
							default:
								query[0] = "";
								accTextview.setText(defaultText);
								queries[ACC_QUETY_IDX] = query[0];
								break;
						}

					}
				})
				.create();
		dialog.show();


	}

	public void onPeriodTextClick(View view) {

		final String[] options = {"any", "after", "before", "on", "between"};

		final TextView periodTextview = (TextView) findViewById(R.id.a_search_trans_period);

		final int[] selectedOption = {ANY};

		final String defaultText = "Any time period";
		final String[] query = {"", defaultText};

		AlertDialog alert = new AlertDialog.Builder(this)
				.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedOption[0] = ANY + which;

						switch (selectedOption[0]) {

							case GREATER:
								query[0] = TTransactions.DATETIME + " > ";
								break;
							case LESS:
								query[0] = TTransactions.DATETIME + " < ";
								break;
							case EQUALS:
								query[0] = TTransactions.DATETIME + " = ";
								break;
							case BETWEEN:
								query[0] = TTransactions.DATETIME + " BETWEEN ";
								break;
							default:
								query[0] = "";
								query[1] = defaultText;
						}

					}
				})
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						periodTextview.setText(query[1]);

						final View twoInputDialog = getLayoutInflater().inflate(R.layout.d_custom_period_picker, null);
						final TextView fromtext = (TextView) twoInputDialog.findViewById(R.id.d_custom_period_from_text);
						final TextView startDate = (TextView) twoInputDialog.findViewById(R.id.d_custom_period_starting_date);
						startDate.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								final ASearch.DatePickerFragment datePickerFragment = new ASearch.DatePickerFragment();
								datePickerFragment.setTextView(startDate);
								datePickerFragment.show(getSupportFragmentManager(), "pick a date");
							}
						});
						startDate.setText(MyCalendar.getNiceFormatedCompleteDateString(ASearch.startDate));
						final TextView toText = (TextView) twoInputDialog.findViewById(R.id.d_custom_period_to_text);
						final TextView endDate = (TextView) twoInputDialog.findViewById(R.id.d_custom_period_ending_date);
						endDate.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								final ASearch.DatePickerFragment datePickerFragment = new ASearch.DatePickerFragment();
								datePickerFragment.setTextView(endDate);
								datePickerFragment.show(getSupportFragmentManager(), "pick a date");
							}
						});
						endDate.setText(MyCalendar.getNiceFormatedCompleteDateString(ASearch.endDate));

						query[1] = options[selectedOption[0] - ANY] + " ";

						switch (selectedOption[0]) {
							case ANY:
								query[0] = "";
								queries[PERIOD_QUETY_IDX] = query[0];
								break;

							case GREATER:
							case LESS:
							case EQUALS:

								endDate.setVisibility(View.GONE);
								fromtext.setVisibility(View.GONE);
								toText.setVisibility(View.GONE);

								AlertDialog secondDialog = new AlertDialog.Builder(ASearch.this)
										.setView(twoInputDialog)
										.setTitle(query[1])
										.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												query[0] += "'" + MyCalendar.stringFormatOfDate(ASearch.startDate) + "'";
												query[1] += MyCalendar.getNiceFormatedCompleteDateString(ASearch.startDate);
												queries[PERIOD_QUETY_IDX] = query[0];
												periodTextview.setText(query[1]);
											}
										})
										.create();
								secondDialog.show();
								break;

							case BETWEEN:

								endDate.setVisibility(View.VISIBLE);
								fromtext.setVisibility(View.VISIBLE);
								toText.setVisibility(View.VISIBLE);

								AlertDialog betweenDialog = new AlertDialog.Builder(ASearch.this)
										.setView(twoInputDialog)
										.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												query[0] += "'" + MyCalendar.stringFormatOfDate(ASearch.startDate) + "'" +
														" AND " + "'" + MyCalendar.stringFormatOfDate(ASearch.endDate) + "'";
												query[1] += "\n" + MyCalendar.getNiceFormatedCompleteDateString(ASearch.startDate) +
														" and " + MyCalendar.getNiceFormatedCompleteDateString(ASearch.endDate);
												queries[PERIOD_QUETY_IDX] = query[0];
												periodTextview.setText(query[1]);
											}
										})
										.create();
								betweenDialog.show();
								break;
						}
						dialog.dismiss();
					}
				})
				.create();
		alert.show();

	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		TextView textView;

		void setTextView(TextView textView) {
			this.textView = textView;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar cal = Calendar.getInstance();
			int y = cal.get(Calendar.YEAR);
			int m = cal.get(Calendar.MONTH);
			int d = cal.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), this, y, m, d);
		}

		public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

			year -= 1900;//
			Date newDate = new Date(year, month, dayOfMonth);

			if (textView.getId() == R.id.d_custom_period_starting_date) {
				startDate = newDate;
			} else {
				endDate = newDate;
			}

			textView.setText(MyCalendar.getNiceFormatedCompleteDateString(newDate));

		}

	}


}
