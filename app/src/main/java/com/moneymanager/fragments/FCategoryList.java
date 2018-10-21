package com.moneymanager.fragments;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.category.ACategories;
import com.moneymanager.entities.Category;
import com.moneymanager.exceptions.CategoryExistsException;
import com.moneymanager.repo.TCategories;

import static com.moneymanager.Common.*;

public class FCategoryList extends Fragment {

	private Category[] myCategories;
	private String[] myCategoriesNameArray;
	private int position;

	private ListView listView;

	private int selectedCategoryIndex = -1;
	private String searchText = "";

	public FCategoryList() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		position = getArguments().getInt("pos");

		refreshCatArrays();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.f_category_list, container, false);

		// setup update category dialog
		final View alertView = inflater.inflate(R.layout.d_add_category, null);
		final ToggleButton cat_type_toggle = (ToggleButton) alertView.findViewById(R.id.x_add_cat_type);
		final EditText cat_name_edittext = (EditText) alertView.findViewById(R.id.x_add_cat_name);

		final AlertDialog dialog = new AlertDialog.Builder(getContext())
				.setCancelable(true)
				.setTitle("Update Category")
				.setView(alertView)
				.setNegativeButton("delete", null)
				.setPositiveButton("update", null)
				.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialogX) {

				// setup delete button
				final Button deleteButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				deleteButton.setTextColor(Common.getMyColor(getContext(), R.color.colorRed));
				deleteButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						final Category dCat = myCategories[selectedCategoryIndex];

						((ACategories) getActivity()).showUndoSnackbar(FCategoryList.this, dCat);
						dialogX.dismiss();


					}

				});

				// setup update button
				final Button updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				updateButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// update the Category
						final String cat_name = cat_name_edittext.getText().toString();
						final int type = cat_type_toggle.isChecked() ? EXPENSE : INCOME;

						Log.i(mylog, "selected category index: " + selectedCategoryIndex);
						if (cat_name.equals("")) {
							cat_name_edittext.setError("Name cannot be empty");
						} else {

							Category cat = myCategories[selectedCategoryIndex];
							cat.setName(cat_name);
							cat.setType(type);

							TCategories tCategories = new TCategories(getContext());
							try {
								tCategories.updateCategory(cat);
								Toast.makeText(getContext(), "Updated category " + cat.getName(), Toast.LENGTH_LONG).show();
								dialogX.dismiss();
							} catch (CategoryExistsException e) {
								cat_name_edittext.setError(e.getMessage());
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
				refreshCatList();
			}
		});


		listView = (ListView) rootView.findViewById(R.id.f_category_listview);
		ArrayAdapter<String> adp = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myCategoriesNameArray);
		listView.setAdapter(adp);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				selectedCategoryIndex = position;
				Category cat = myCategories[position];

				if (cat.getName().toLowerCase().equals(Category.OTHER_EXPENSE.toLowerCase()) ||
						cat.getName().toLowerCase().equals(Category.OTHER_INCOME.toLowerCase())) {
					Toast.makeText(getContext(), "Cannot modify this Category", Toast.LENGTH_LONG).show();
					return;
				}

				cat_name_edittext.setText(cat.getName());
				cat_type_toggle.setChecked(cat.getType() == EXPENSE);
				dialog.show();


			}
		});

		return rootView;
	}

	public void refreshCatList() {

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				refreshCatArrays();
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myCategoriesNameArray));
			}
		}.execute();


	}

	private void refreshCatArrays() {

		TCategories tc = new TCategories(getContext());
		// 0- expense, 1- income
		myCategories = null;

		if (searchText == null || searchText.equals("")) {
			myCategories = tc.getTypeSpecificCategories(position == 0 ? EXPENSE : INCOME);
		} else {
			myCategories = tc.getTypeSpecificSearchedCategories(position == 0 ? EXPENSE : INCOME, searchText);
		}

		myCategoriesNameArray = null;
		myCategoriesNameArray = Category.extractNameStringArrayFromArray(myCategories);

	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

}
