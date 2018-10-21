package com.moneymanager.activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.moneymanager.R;
import com.moneymanager.entities.User;
import com.moneymanager.exceptions.CannotDeleteUserException;
import com.moneymanager.exceptions.UserExistsException;
import com.moneymanager.repo.TUser;

import java.util.ArrayList;

import static com.moneymanager.Common.getMyColor;
import static com.moneymanager.Common.setupToolbar;

public class AUser extends MyBaseActivity {

	private ArrayList<String> user_name_list = new ArrayList<>();
	private ArrayList<Integer> user_name_id = new ArrayList<>();

	private ListView listView;
	private String searchText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_user);
		setupToolbar(this, R.id.a_user_toolbar, "Manage Users");

		// Setup List view
		listView = (ListView) findViewById(R.id.a_user_listview);
		listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, user_name_list));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final TUser tUser = new TUser(AUser.this);
				final User rmUser = new User(user_name_id.get(position), user_name_list.get(position));

				final Snackbar sb = Snackbar.make(findViewById(R.id.a_user_coordinate_layout),
						"Delete category " + rmUser.getName() + "?",
						Snackbar.LENGTH_SHORT)
						.setAction("Sure", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								try {
									tUser.removeUser(rmUser);
								} catch (CannotDeleteUserException e) {
									showLongToast(e.getMessage());
								}
								refreshUserList();

							}
						});
				View sbv = sb.getView();
				sbv.setBackgroundColor(getMyColor(AUser.this, R.color.colorRed));
				sb.setActionTextColor(getMyColor(AUser.this, R.color.colorPrimaryDark));
				sb.show();



			}
		});

		// set up insert dialog
		final View alertView = getLayoutInflater().inflate(R.layout.d_add_user, null);
		final EditText user_name_edittext = (EditText) alertView.findViewById(R.id.d_add_user_name);


		final AlertDialog dialog = new AlertDialog.Builder(AUser.this)
				.setView(alertView)
				.setPositiveButton("add", null)
				.create();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialogX) {
				final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// insert new Category
						final String user_name = user_name_edittext.getText().toString();

						if (user_name.equals("")) {
							user_name_edittext.setError("Enter a valid name");
						} else {

							User user = new User(-1, user_name);

							TUser tUser = new TUser(AUser.this);
							try {
								tUser.addUser(user);
								Toast.makeText(AUser.this, user.getName() + " added", Toast.LENGTH_LONG).show();
								dialogX.dismiss();
							} catch (UserExistsException e) {
								user_name_edittext.setError(e.getMessage());
							}

						}


					}

				});

			}
		});
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				// refresh category list
				refreshUserList();

			}
		});

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.a_add_user_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dialog.show();
			}
		});

		refreshUserList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.users_menu, menu);
		MenuItem menuItem = menu.findItem(R.id.user_search);

		SearchView search = (SearchView) menuItem.getActionView();
		search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				searchText = newText;
				refreshUserList();
				return true;
			}
		});

		return true;
	}

	private void refreshUserList() {

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				final TUser tUser = new TUser(AUser.this);

				final User[] user_array = (searchText == null || searchText.equals("")) ? tUser.getAllUsers() : tUser.getSearchedUsers(searchText);

				user_name_list.clear();
				user_name_id.clear();

				for (User user : user_array) {
					user_name_list.add(user.getName());
					user_name_id.add(user.getId());
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				listView.setAdapter(new ArrayAdapter<String>(AUser.this, android.R.layout.simple_list_item_1, user_name_list));
			}
		}.execute();

	}

}
