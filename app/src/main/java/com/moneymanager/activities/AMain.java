package com.moneymanager.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.accounts.AAccounts;
import com.moneymanager.activities.budget.ABudgets;
import com.moneymanager.activities.category.ACategories;
import com.moneymanager.activities.debts.ADebts;
import com.moneymanager.activities.search.ASearch;
import com.moneymanager.activities.stats.AStats;
import com.moneymanager.activities.transaction.AAddTransaction;
import com.moneymanager.adapters.HomePagerAdapter;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Budget;
import com.moneymanager.entities.Transaction;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.fragments.FHomePage;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.repo.TBudget;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;
import com.moneymanager.utilities.ShrPref;

import static com.moneymanager.Common.*;

public class AMain extends MyBaseActivity {

	private final String[] nav_places = {
			"Stats", "Accounts", "Budgets", "Debts & Loans", "Categories", "Users"
	};
	private Account[] accounts;
	private String[] acc_names;
	private int[] acc_ids;
	private double[] acc_bals;
	private ViewPager viewPager;
	private DrawerLayout navD;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		final Toolbar home_toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		home_toolbar.setTitle("");
		setSupportActionBar(home_toolbar);

		// setting up Navigation drawer stuff

		navD = (DrawerLayout) findViewById(R.id.a_home_nav_drawer);
		navD.setScrimColor(getMyColor(this, R.color.fadeBlack));

		ListView navigationList = (ListView) findViewById(R.id.a_home_nav_list);
		navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
					case 0:
						final Intent intent = new Intent(AMain.this, AStats.class);
						intent.putExtra("date", MyCalendar.getSimpleDateFormat().format(MyCalendar.dateToday()));
						startActivity(intent);
						break;
					case 1:
						startActivity(new Intent(AMain.this, AAccounts.class));
						break;
					case 2:
						startActivity(new Intent(AMain.this, ABudgets.class));
						break;
					case 3:
						startActivity(new Intent(AMain.this, ADebts.class));
						break;
					case 4:
						startActivity(new Intent(AMain.this, ACategories.class));
						break;
					case 5:
						startActivity(new Intent(AMain.this, AUser.class));
						break;

				}

			}
		});

		FrameLayout hamLayout = (FrameLayout) findViewById(R.id.home_hamburger_layout);

		hamLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (navD.isDrawerOpen(GravityCompat.START)) {
					navD.closeDrawer(GravityCompat.START);
				} else {
					navD.openDrawer(GravityCompat.START);
				}
			}
		});


		final ImageButton imgBtn = (ImageButton) findViewById(R.id.a_home_nav_settings);
		imgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AMain.this, ASettings.class));
			}
		});

		final ImageButton aboutBtn = (ImageButton) findViewById(R.id.a_home_nav_about);
		aboutBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final AlertDialog dialog = new AlertDialog.Builder(AMain.this)
						.setCancelable(true)
						.setTitle("About")
						.setMessage("Hi! I am Dhiraj Ramnani, developer of this app. If you have any issues, suggestions, feedback, please feel free to contact me. Anykind of feedback is really appreciated. " +
								"\n\nIf you like this app then please review it on the Play Store. Thank You :)")
						.setNegativeButton("Rate it", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						})
						.setPositiveButton("get in touch", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

								final String[] TO = {"dhirajramnani86@gmail.com"};
								Intent emailIntent = new Intent(Intent.ACTION_SEND);

								emailIntent.setData(Uri.parse("mailto:"));
								emailIntent.setType("text/plain");
								emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
								emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Money Manager feedback");

								try {
									startActivity(Intent.createChooser(emailIntent, "Send Email"));
								} catch (android.content.ActivityNotFoundException ex) {
									Toast.makeText(AMain.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
								}

							}
						})
						.create();
				dialog.show();

			}
		});

		// set up fab button to add new transaction
		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_transaction);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AMain.this, AAddTransaction.class));
			}
		});
		fab.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				return true;
			}
		});

		final HomePagerAdapter hmp = new HomePagerAdapter(getSupportFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.home_viewpager);
		viewPager.setAdapter(hmp);
		viewPager.setCurrentItem(6);

		// setup curreny symbol
		final int currentSymbolIdx = ShrPref.readData(this, spCURRENCY_SYMBOL, 0);
		Common.CURRENCY_FORMAT = String.valueOf(currenySymbols[currentSymbolIdx].charAt(0));

	}

	@Override
	protected void onStart() {

		super.onStart();


	}

	@Override
	protected void onResume() {
		super.onResume();

		// check if accounts exists else redirect to accounts page
		final TAccounts accTable = new TAccounts(this);
		try {

			// query for account list, throw NoAccountsException if none found
			accounts = accTable.getAllAccounts(TAccounts.NAME, null);

			// get the current account id
			CURRENT_ACCOUNT_ID = ShrPref.readData(this, spCURRENT_ACCOUNT_ID, ALL_ACCOUNT_ID);

			if (CURRENT_ACCOUNT_ID == ALL_ACCOUNT_ID) {
				CURRENT_ACCOUNT_NAME = "All Accounts";
			}

			// setup Account related Arrays
			acc_names = new String[accounts.length + 1];
			acc_names[0] = "All Accounts";
			acc_ids = new int[accounts.length + 1];
			acc_ids[0] = ALL_ACCOUNT_ID;
			acc_bals = new double[accounts.length + 1];
			acc_bals[0] = -1;
			for (int i = 1; i < acc_names.length; i++) {
				acc_names[i] = accounts[i - 1].getName();
				acc_ids[i] = accounts[i - 1].getId();
				acc_bals[i] = accounts[i - 1].getBalance();
				if (acc_ids[i] == CURRENT_ACCOUNT_ID) {
					CURRENT_ACCOUNT_NAME = acc_names[i];
				}
			}

			refreshToolbar();


		} catch (NoAccountsException e) {
			final Intent intent = new Intent(this, AAccounts.class);
			intent.putExtra("checkForBackup", true);
			startActivity(intent);
		}

		if (getSupportFragmentManager().getFragments() != null) {
			for (Fragment f : getSupportFragmentManager().getFragments()) {
				((FHomePage) f).refreshFragmentContent(CURRENT_ACCOUNT_ID);
			}
		}

		// check for budget overspendings
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {

				final int budgetLimit = ShrPref.readData(AMain.this, spBUDGET_LIMIT, 10);
				final TBudget tBudget = new TBudget(AMain.this);
				final Budget[] budgets = tBudget.getAllBudgets();

				for (Budget budget : budgets) {


					final Transaction[] trans = new TTransactions(AMain.this).getBudgetSpecificTransactions(budget);

					final double set = budget.getAmount();
					double spent = 0;
					for (Transaction t : trans) {
						spent += t.getAmount();
					}

					final double rem = set - spent;

					final double expectedRem = (budgetLimit * set) / 100;

					if (rem <= expectedRem) {
						return true;
					}

				}

				return false;
			}

			@Override
			protected void onPostExecute(final Boolean overpsent) {
				super.onPostExecute(overpsent);


				ListView navigationList = (ListView) findViewById(R.id.a_home_nav_list);

				final ColorStateList defColor = ((TextView) (getLayoutInflater().inflate(R.layout.x_list_item, null).findViewById(R.id.x_list_item_name))).getTextColors();

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(AMain.this, R.layout.x_list_item, R.id.x_list_item_name, nav_places) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						LinearLayout linearLayout = (LinearLayout) super.getView(position, convertView, parent);
						TextView textView = (TextView) linearLayout.findViewById(R.id.x_list_item_name);

						final ImageView hamBob = (ImageView) findViewById(R.id.home_hamburger_bob);
						if (overpsent) {
							hamBob.setVisibility(View.VISIBLE);
						} else {
							hamBob.setVisibility(View.INVISIBLE);
						}

						if (textView.getText().equals(nav_places[2]) && overpsent) {
							textView.setTextColor(getMyColor(AMain.this, R.color.colorRed));
						} else {
							textView.setTextColor(defColor);
						}
						return linearLayout;
					}
				};
				navigationList.setAdapter(null);
				navigationList.setAdapter(adapter);

			}
		}.execute();


	}

	@Override
	protected void onPause() {
		super.onPause();

		// store current account id
		ShrPref.writeData(this, spCURRENT_ACCOUNT_ID, CURRENT_ACCOUNT_ID);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.home_menu_search: {

				startActivity(new Intent(this, ASearch.class));

				return true;
			}

			default: {
				return super.onOptionsItemSelected(item);
			}

		}
	}

	@Override
	public void onBackPressed() {

		if (navD.isDrawerOpen(GravityCompat.START)) {
			navD.closeDrawer(GravityCompat.START);
		} else {

			final AlertDialog alertDialog = new AlertDialog.Builder(this)
					.setTitle("Exit the app?")
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							finish();
						}
					})
					.create();
			alertDialog.show();
		}

	}

	private void refreshToolbar() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.home_toolbar_layout);
		final LinearLayout balLayout = (LinearLayout) findViewById(R.id.home_toolbar_bal_layout);

		final TextView balText = (TextView) balLayout.findViewById(R.id.home_toolbar_bal_textview);

		new ToolbarRefresherThread(balText).execute();

		final TextView toolbar_text = (TextView) layout.findViewById(R.id.home_toolbar_textview);
		toolbar_text.setText(CURRENT_ACCOUNT_NAME);

		layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(AMain.this);
				builder.setCancelable(true);
				builder.setTitle("Select an Account");
				builder.setPositiveButton("Manage Accounts", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(AMain.this, AAccounts.class));
					}
				});
				builder.setItems(acc_names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Log.i(mylog, "Current account: " + acc_names[i]);
						CURRENT_ACCOUNT_ID = acc_ids[i];
						toolbar_text.setText(acc_names[i]);
						CURRENT_ACCOUNT_NAME = acc_names[i];
						ShrPref.writeData(AMain.this, spCURRENT_ACCOUNT_ID, CURRENT_ACCOUNT_ID);
						dialogInterface.dismiss();

						// update balance
						if (CURRENT_ACCOUNT_ID == ALL_ACCOUNT_ID) {
							new ToolbarRefresherThread(balText).execute();
						} else {
							balText.setText(formatAmt(acc_bals[i]));
						}

						for (Fragment f : getSupportFragmentManager().getFragments()) {
							((FHomePage) f).refreshFragmentContent(CURRENT_ACCOUNT_ID);
						}

					}
				});
				builder.create().show();
			}
		});
	}

	private class ToolbarRefresherThread extends AsyncTask<Void, Void, Double> {


		private final TextView balText;

		ToolbarRefresherThread(TextView balText) {
			this.balText = balText;
		}

		@Override
		protected Double doInBackground(Void... params) {
			TAccounts tAccounts = new TAccounts(AMain.this);
			if (CURRENT_ACCOUNT_ID == ALL_ACCOUNT_ID) {
				double bal = 0;
				try {
					Account[] accounts = tAccounts.getAllAccounts(null, null);
					for (Account acc : accounts) {
						bal += acc.getBalance();
					}
				} catch (NoAccountsException e) {
					e.printStackTrace();
				}
				return bal;
			} else {
				return tAccounts.getSumOfBalanceOfAccount(CURRENT_ACCOUNT_ID);
			}
		}

		@Override
		protected void onPostExecute(Double aDouble) {
			super.onPostExecute(aDouble);

			balText.setText(formatAmt(aDouble));

		}
	}

}
