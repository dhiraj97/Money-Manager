package com.moneymanager.activities.accounts;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.moneymanager.R;
import com.moneymanager.entities.Account;
import com.moneymanager.entities.Transfer;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.exceptions.NoAccountsException;
import com.moneymanager.repo.TAccounts;
import com.moneymanager.repo.TTransfers;
import com.moneymanager.utilities.MyCalendar;

import static com.moneymanager.Common.setupToolbar;

public class AAccountTransfer extends AppCompatActivity {

	int toAccount = -1;
	int fromAccount = -1;

	Account[] accounts = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_account_transfer);

		setupToolbar(this, R.id.a_acc_transfer_toolbar, "Transfer Amount");

		final TextView fAccountTxt = (TextView) findViewById(R.id.a_transfer_acc_from);
		final TextView tAccountTxt = (TextView) findViewById(R.id.a_transfer_acc_to);

		try {
			TAccounts tAccounts = new TAccounts(this);
			accounts = tAccounts.getAllAccounts(null, null);
		} catch (NoAccountsException e) {
			e.printStackTrace();
		}

		final String[] acc_names = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) {
			acc_names[i] = accounts[i].getName();
		}

		fAccountTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {


				final AlertDialog.Builder builder = new AlertDialog.Builder(AAccountTransfer.this);
				builder.setCancelable(true);
				builder.setTitle("Transfer From");
				builder.setItems(acc_names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

						fromAccount = accounts[i].getId();
						fAccountTxt.setText("account: " + acc_names[i]);
						dialogInterface.dismiss();
					}
				});
				builder.create().show();

			}
		});
		tAccountTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				AlertDialog accDialog = new AlertDialog.Builder(AAccountTransfer.this)
						.setItems(acc_names, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

								toAccount = accounts[which].getId();
								tAccountTxt.setText("account: " + acc_names[which]);
								dialog.dismiss();

							}
						})
						.create();
				accDialog.show();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.accounts_transfer_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		EditText amt = (EditText) findViewById(R.id.d_transfer_amt);

		if (amt.getText().toString().equals("")) {
			amt.setError("Enter amount");
		} else if (fromAccount <= 0) {
			Toast.makeText(this, "Select account from which transfer starts", Toast.LENGTH_LONG).show();
		} else if (toAccount <= 0) {
			Toast.makeText(this, "Select target account", Toast.LENGTH_LONG).show();
		} else if (fromAccount == toAccount) {
			Toast.makeText(this, "Transfering into same account makes no sense", Toast.LENGTH_LONG).show();
		} else {
			final double amount = Double.valueOf(amt.getText().toString());

			try {
				transferAmount(fromAccount, toAccount, amount);
				finish();
			} catch (InsufficientBalanceException e) {
				amt.setError(e.getMessage());
			}
		}

		return true;
	}

	private void transferAmount(int fromAccount, int toAccount, double amt) throws InsufficientBalanceException {

		final TAccounts tAccounts = new TAccounts(this);

		final Account toAcc = tAccounts.getAccount(toAccount);
		final Account fromAcc = tAccounts.getAccount(fromAccount);

		final Transfer transfer = new Transfer(-1, toAcc, fromAcc, amt, MyCalendar.dateToday());

		final TTransfers tTransfers = new TTransfers(this);
		tTransfers.addTransfer(transfer);

	}

}
