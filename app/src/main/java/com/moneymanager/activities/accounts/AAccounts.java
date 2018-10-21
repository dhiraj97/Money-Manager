package com.moneymanager.activities.accounts;
//Dhiraj Ramnani rocks
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.moneymanager.R;
import com.moneymanager.activities.MyBaseActivity;
import com.moneymanager.entities.Account;
import com.moneymanager.exceptions.AccountNameExistsException;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.utilities.BackupManager;
import com.moneymanager.utilities.ShrPref;

import static com.moneymanager.Common.*;

public class AAccounts extends MyBaseActivity {

	public static boolean noAccounts = true;
	private final int RESOLVE_CONNECTION_REQUEST_CODE = 234;
	private GoogleApiClient googleApiClient;
	private boolean checkForBackup = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_accounts);

		// setup toolbar
		setupToolbar(this, R.id.accounts_toolbar, "Accounts");

		// setting up fab
		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_account);
		fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AAccounts.this, AAddAccount.class));
			}
		});

		checkForBackup = getIntent().getBooleanExtra("checkForBackup", false);

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Drive.API)
				.addScope(Drive.SCOPE_FILE)
				.addScope(Drive.SCOPE_APPFOLDER)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(@Nullable Bundle bundle) {


						BackupManager bm = new BackupManager(AAccounts.this, googleApiClient);
						bm.restoreBackup();
						refreshAccountsList();

					}

					@Override
					public void onConnectionSuspended(int i) {

					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(@NonNull ConnectionResult result) {

						log_i("GoogleApiClient connection failed: " + result.getErrorMessage());

						if (!result.hasResolution()) {

							GoogleApiAvailability.getInstance().getErrorDialog(AAccounts.this, result.getErrorCode(), 0).show();
							return;

						}

						try {
							result.startResolutionForResult(AAccounts.this, RESOLVE_CONNECTION_REQUEST_CODE);
						} catch (IntentSender.SendIntentException e) {
							e.printStackTrace();
						}


					}
				})
				.build();

	}

	public void onResume() {
		super.onResume();
		if (checkForBackup) {

			if (!googleApiClient.isConnected()) {
				googleApiClient.connect();
			} else {
				BackupManager backupManager = new BackupManager(this, googleApiClient);
				backupManager.restoreBackup();
			}

			checkForBackup = false;

		} else {
			refreshAccountsList();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.accounts_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.accounts_Menu_transfer:

				if (noAccounts) {

					Toast.makeText(this, "No Accounts found", Toast.LENGTH_SHORT).show();

				} else {

					startActivity(new Intent(this, AAccountTransfer.class));

				}

				break;

			case R.id.accounts_Menu_restore:

				if (!googleApiClient.isConnected()) {
					googleApiClient.connect();
				} else {
					BackupManager backupManager = new BackupManager(this, googleApiClient);
					backupManager.restoreBackup();
				}

				break;

		}

		return true;

	}

	public void onBackPressed() {

		// if accounts exists close this activity
		if (!noAccounts) {

			finish();

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		switch (requestCode) {

			case RESOLVE_CONNECTION_REQUEST_CODE:

				if (resultCode == RESULT_OK) {
					googleApiClient.connect();
				}
				break;

		}


	}

	public void refreshAccountsList() {
		final TAccounts accTable = new TAccounts(this);

		final ListView accListView = (ListView) findViewById(R.id.account_accounts_list);
		final TextView accTxt = (TextView) findViewById(R.id.accounts_text);

		try {
			final Account[] accounts = accTable.getAllAccounts(TAccounts.ID, null);
			noAccounts = false;

			accListView.setAdapter(new AccListAdapter(this, accounts, accTable));
			accListView.setVisibility(View.VISIBLE);

			accTxt.setVisibility(View.GONE);

		} catch (NoAccountsException e) {

			accTxt.setVisibility(View.VISIBLE);
			accListView.setVisibility(View.GONE);


		}
	}

	class AccListAdapter extends ArrayAdapter<Account> {

		Activity myContext;
		TAccounts accTable;

		AccListAdapter(Activity myContext, Account[] accArr, TAccounts accTable) {
			super(myContext, -1, accArr);
			this.myContext = myContext;
			this.accTable = accTable;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			// inflate and setup the layout
			final LayoutInflater inflator = myContext.getLayoutInflater();
			View rowView;
			if (convertView == null) {
				rowView = inflator.inflate(R.layout.x_account_view, parent, false);
			} else {
				rowView = convertView;
			}


			// get the account and it's details
			final Account acc = getItem(position);
			final int id = acc.getId();
			final String name = acc.getName();
			final double bal = acc.getBalance();

			// fill up the layout elements
			final TextView nametv = (TextView) rowView.findViewById(R.id.account_view_name);
			nametv.setText(name);
			final TextView baltv = (TextView) rowView.findViewById(R.id.account_view_bal);
			baltv.setText(formatAmt(bal));
			final TextView infotv = (TextView) rowView.findViewById(R.id.account_view_info);

			TAccounts tAccounts = new TAccounts(myContext);
			String transactionSum, debtSum, loanSum;

			final int tC = tAccounts.countTransactions(id);
			final int dC = tAccounts.countDebt(id);
			final int lC = tAccounts.countLoan(id);

			if (tC <= 0 && dC <= 0 && lC <= 0) {
				infotv.setVisibility(View.GONE);
			} else {
				transactionSum = tC <= 0 ? "" : "transactions: " + tC + "\n";
				debtSum = dC <= 0 ? "" : "debt: " + dC + "\n";
				loanSum = lC <= 0 ? "" : "loan: " + lC + "\n";
				infotv.setVisibility(View.VISIBLE);
				String infoString = transactionSum + debtSum + loanSum;
				while (infoString.endsWith("\n")) {
					infoString = infoString.substring(0, infoString.length() - 1);
				}
				infotv.setText(infoString);
			}

			final LinearLayout popLayout = (LinearLayout) rowView.findViewById(R.id.x_account_spinner_layout);
			final ImageButton popUpButton = (ImageButton) popLayout.findViewById(R.id.x_account_popbutton);

			View.OnClickListener popListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PopupMenu popupMenu = new PopupMenu(getContext(), v);
					popupMenu.getMenuInflater().inflate(R.menu.x_account_actions_popup, popupMenu.getMenu());
					popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {

							switch (item.getItemId()) {
								case R.id.x_account_popup_edit_name:

									// set up insert dialog
									final View alertView = getLayoutInflater().inflate(R.layout.d_edit_account_name, null);
									final EditText acc_name_edittext = (EditText) alertView.findViewById(R.id.d_edit_account_name);

									final AlertDialog dialog = new AlertDialog.Builder(AAccounts.this)
											.setCancelable(true)
											.setView(alertView)
											.setPositiveButton("save", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {


													final Account newAcc = new Account(
															acc.getId(),
															acc_name_edittext.getText().toString(),
															acc.getBalance(),
															acc.getStartingBalance(),
															acc.getCreateDate(),
															false
													);

													TAccounts tAccount = new TAccounts(AAccounts.this);
													try {
														tAccount.updateAccount(newAcc);
														acc.setName(acc_name_edittext.getText().toString());
													} catch (AccountNameExistsException e) {
														showLongToast("Account name already exists.\nPlease choose another name.");
													}

												}
											})
											.create();
									dialog.show();

									break;
								case R.id.x_account_popup_delete:
									final Snackbar sb = Snackbar.make(findViewById(R.id.a_accounts_coordinate_layout),
											"Delete Account?",
											Snackbar.LENGTH_SHORT)
											.setAction("Yes", new OnClickListener() {
												@Override
												public void onClick(View v) {

													Toast.makeText(myContext, "Account deleted successfully", Toast.LENGTH_LONG).show();
													accTable.removeAccount(id);
													CURRENT_ACCOUNT_ID = ALL_ACCOUNT_ID;
													ShrPref.writeData(AAccounts.this, spCURRENT_ACCOUNT_ID, CURRENT_ACCOUNT_ID);

													// refresh account list
													final TAccounts accTable = new TAccounts(myContext);
													final ListView accListView = (ListView) myContext.findViewById(R.id.account_accounts_list);

													try {
														final Account[] accounts = accTable.getAllAccounts(TAccounts.ID, null);
														noAccounts = false;

														accListView.setAdapter(new AccListAdapter(myContext, accounts, accTable));


													} catch (NoAccountsException e) {

														noAccounts = true;
														final TextView accTxt = (TextView) myContext.findViewById(R.id.accounts_text);
														accTxt.setVisibility(View.VISIBLE);
														accListView.setVisibility(View.GONE);


													}


												}
											});
									View sbv = sb.getView();
									sbv.setBackgroundColor(getMyColor(AAccounts.this, R.color.colorRed));
									sb.setActionTextColor(getMyColor(AAccounts.this, R.color.colorPrimaryDark));
									sb.show();
									break;
							}
							return true;
						}
					});
					popupMenu.show();
				}
			};

			popLayout.setOnClickListener(popListener);
			popUpButton.setOnClickListener(popListener);


			rowView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(AAccounts.this, ALedger.class);
					intent.putExtra("acc_id", acc.getId());
					startActivity(intent);

				}
			});

			return rowView;

		}
	}

}
