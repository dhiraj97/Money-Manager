package com.moneymanager.activities.category;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.adapters.CategoriesAdapter;
import com.moneymanager.entities.Category;
import com.moneymanager.exceptions.CategoryExistsException;
import com.moneymanager.fragments.FCategoryList;
import com.moneymanager.repo.TCategories;

import static com.moneymanager.Common.*;

public class ACategories extends AppCompatActivity {

	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_categories);

		Common.setupToolbar(this, R.id.categories_toolbar, "Categories");

		// set up insert dialog
		final View alertView = getLayoutInflater().inflate(R.layout.d_add_category, null);
		final ToggleButton cat_type_toggle = (ToggleButton) alertView.findViewById(R.id.x_add_cat_type);
		final EditText cat_name_edittext = (EditText) alertView.findViewById(R.id.x_add_cat_name);

		final AlertDialog dialog = new AlertDialog.Builder(ACategories.this)
				.setCancelable(true)
				.setTitle("Create new Category")
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
						final String cat_name = cat_name_edittext.getText().toString();
						final int type = cat_type_toggle.isChecked() ? EXPENSE : INCOME;

						if (cat_name.equals("")) {
							cat_name_edittext.setError("Enter a valid name");
						} else {

							Category cat = new Category(-1, cat_name, type, false);

							TCategories tCategories = new TCategories(ACategories.this);
							try {
								tCategories.insertNewCategory(cat);
								Toast.makeText(ACategories.this, "New category " + cat.getName() + " added in " + cat.getTypeString(), Toast.LENGTH_LONG).show();
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
				for (Fragment f : getSupportFragmentManager().getFragments()) {
					((FCategoryList) f).refreshCatList();
				}
			}
		});

		// setup fab
		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_category);
		fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ACategories.this, R.color.colorRed)));
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});

		// setting up ViewPager Stuff
		final CategoriesAdapter ca = new CategoriesAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.categories_viewpager);
		viewPager.setAdapter(ca);

		final int currentPage = (getIntent().getIntExtra("type", EXPENSE) == INCOME) ? 1 : 0;
		viewPager.setCurrentItem(currentPage);


		final TabLayout tl = (TabLayout) viewPager.findViewById(R.id.categories_tab_layout);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {

				int newcolor;
				if (position == 0) {
					newcolor = R.color.colorRed;
					cat_type_toggle.setChecked(true);
				} else {
					newcolor = R.color.colorGreen;
					cat_type_toggle.setChecked(false);
				}
				tl.setSelectedTabIndicatorColor(ContextCompat.getColor(ACategories.this, newcolor));
				fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ACategories.this, newcolor)));
				for (Fragment f : getSupportFragmentManager().getFragments()) {

					((FCategoryList) f).refreshCatList();

				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		int newcolor;
		if (currentPage == 0) {
			newcolor = R.color.colorRed;
			cat_type_toggle.setChecked(true);
		} else {
			newcolor = R.color.colorGreen;
			cat_type_toggle.setChecked(false);
		}
		tl.setSelectedTabIndicatorColor(ContextCompat.getColor(ACategories.this, newcolor));
		fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ACategories.this, newcolor)));


	}

	public void showUndoSnackbar(final FCategoryList fCategoryList, final Category dCat) {

		final Snackbar sb = Snackbar.make(findViewById(R.id.a_categories_coordinate_layout),
				"Delete category " + dCat.getName() + "?",
				Snackbar.LENGTH_SHORT)
				.setAction("Sure", new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						TCategories tCategories = new TCategories(ACategories.this);
						tCategories.removeCategory(dCat);
						fCategoryList.refreshCatList();
					}
				});
		View sbv = sb.getView();
		sbv.setBackgroundColor(getMyColor(ACategories.this, R.color.colorRed));
		sb.setActionTextColor(getMyColor(ACategories.this, R.color.colorPrimaryDark));
		sb.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.categories_menu, menu);
		MenuItem menuItem = menu.findItem(R.id.category_search);

		SearchView search = (SearchView) menuItem.getActionView();
		search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				for (Fragment f : getSupportFragmentManager().getFragments()) {

					((FCategoryList) f).setSearchText(newText);
					((FCategoryList) f).refreshCatList();

				}

				return true;
			}
		});
		return true;
	}
}
