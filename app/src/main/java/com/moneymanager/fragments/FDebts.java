package com.moneymanager.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.moneymanager.R;
import com.moneymanager.activities.debts.AEditDebt;
import com.moneymanager.activities.transaction.AAddTransaction;
import com.moneymanager.entities.Debt;
import com.moneymanager.repo.TDebt;
import com.moneymanager.utilities.MyCalendar;

import java.util.Date;

import static com.moneymanager.Common.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class FDebts extends Fragment {


	private Date previousDebtDate;
	private Debt[] debtArray;
	private boolean debt;

	// Views
	private ListView list;
	private TextView noDebtText;

	public FDebts() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		debt = getArguments().getBoolean("debt");

		refreshDebtArray();

	}

	@Override
	public void onResume() {
		super.onResume();
		refreshDebtArray();
		if (debtArray.length == 0) {
			list.setVisibility(View.GONE);

			noDebtText.setText("No " + (debt ? "Debts" : "Loans") + " found");
			noDebtText.setVisibility(View.VISIBLE);

		} else {
			noDebtText.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
			list.setAdapter(null);
			list.setAdapter(new DebtListAdapter(getContext(), debtArray));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f_debts, container, false);

		list = (ListView) rootView.findViewById(R.id.f_debt_listview);
		noDebtText = (TextView) rootView.findViewById(R.id.f_no_debts_text);

		return rootView;
	}

	private void refreshDebtArray() {

		TDebt tDebt = new TDebt(getContext());
		debtArray = null;
		debtArray = tDebt.getDebts(debt ? DEBT : LOAN);

		previousDebtDate = debtArray.length > 0 ? MyCalendar.dateBeforeDays(debtArray[0].getDate(), 2) : null;

		for (final Debt debt : debtArray) {


			if (debt.getDate().equals(previousDebtDate)) {

				debt.setDate(null);
			} else {
				previousDebtDate = debt.getDate();
			}


		}

	}

	class DebtListAdapter extends ArrayAdapter<Debt> {


		DebtListAdapter(Context context, Debt[] objects) {

			super(context, -1, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Debt debt = getItem(position);

			final LayoutInflater inf = getActivity().getLayoutInflater();
			final View rowView = inf.inflate(R.layout.x_debt_row, null);
			final LinearLayout dateLayout = (LinearLayout) rowView.findViewById(R.id.x_debt_date_layout);
			final LinearLayout popLayout = (LinearLayout) rowView.findViewById(R.id.x_debt_spinner_layout);
			final ImageButton popUpButton = (ImageButton) popLayout.findViewById(R.id.x_debt_popbutton);

			View.OnClickListener popListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PopupMenu popupMenu = new PopupMenu(getContext(), v);
					popupMenu.getMenuInflater().inflate(R.menu.x_debt_actions_popup, popupMenu.getMenu());
					popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {

							Intent intent;

							switch (item.getItemId()) {
								case R.id.x_debt_popup_edit:

									intent = new Intent(getContext(), AEditDebt.class);
									intent.putExtra("debt", debt.getId());
									startActivity(intent);

									break;
								case R.id.x_debt_popup_similar:
									intent = new Intent(getContext(), AAddTransaction.class);
									intent.putExtra("debt", debt.getId());
									startActivity(intent);
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
			dateLayout.setVisibility(View.INVISIBLE);


			if (debt.getDate() != null) {

				dateLayout.setVisibility(View.VISIBLE);

				TextView dateText = (TextView) dateLayout.findViewById(R.id.x_debt_date);
				TextView monthYearText = (TextView) dateLayout.findViewById(R.id.x_debt_month_year);

				dateText.setText(MyCalendar.dateToString(debt.getDate()));

				String monthYear = MyCalendar.monthToString(debt.getDate()) + ",\n" + MyCalendar.yearToString(debt.getDate());
				monthYearText.setText(monthYear);

			}

			TextView userText = (TextView) rowView.findViewById(R.id.x_debt_user);
			TextView typeText = (TextView) rowView.findViewById(R.id.x_debt_type);
			TextView amtText = (TextView) rowView.findViewById(R.id.x_debt_amt);
			TextView accText = (TextView) rowView.findViewById(R.id.x_debt_acc);
			TextView infoText = (TextView) rowView.findViewById(R.id.x_debt_info);

			userText.setText(debt.getUser().getName());
			typeText.setText(debt.getType() == DEBT ? "owes you" : "you owe");
			amtText.setText(formatAmt(debt.getAmount()));
			accText.setText(debt.getAccount().getName());
			infoText.setText(debt.getShortInfo());

			return rowView;
		}

	}

}
