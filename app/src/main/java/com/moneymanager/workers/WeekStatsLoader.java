// Created by Dhiraj on 15/03/17.

package com.moneymanager.workers;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.stats.AStats;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;
import com.moneymanager.utilities.TransStats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.moneymanager.Common.*;

public class WeekStatsLoader extends AsyncTask<Bundle, Void, TransStats> {

	// colors
	private final int colorGreen;
	private final int colorLightGreen;
	private final int colorFaintGreen;
	private final int colorRed;
	private final int colorLightRed;
	private final int colorFaintRed;
	private final int colorWhite;
	private final int colorTransparent;
	private final int colorPrimaryDark;
	private AStats activity;
	private PieChart mainPieChart;
	private LineChart yearLineChart;
	private BarChart weekBarChart, monthBarChart;
	private AlertDialog piechart_dialog;
	private LinearLayout income_trans_container;
	private LinearLayout expense_trans_container;
	private CardView income_trans_container_card;
	private CardView expense_trans_container_card;
	private int selectedAccountID;
	private SimpleDateFormat sdf;
	private Date myDate;

	public WeekStatsLoader(AStats statsActivity) {

		activity = statsActivity;
		// reseting main piechart
		mainPieChart = (PieChart) activity.findViewById(R.id.a_stats_piechart);
		mainPieChart.clear();
		mainPieChart.setOnChartValueSelectedListener(null);
		yearLineChart = activity.yearLineChart;
		weekBarChart = activity.weekBarChart;
		monthBarChart = activity.monthBarChart;

		income_trans_container = activity.income_trans_container;
		expense_trans_container = activity.expense_trans_container;
		income_trans_container_card = activity.income_trans_container_card;
		expense_trans_container_card = activity.expense_trans_container_card;

		sdf = new SimpleDateFormat(Common.DATE_FORMAT);
		selectedAccountID = activity.selectedAccountID;
		myDate = activity.myDate;

		// setup COlotrs
		colorGreen = getMyColor(activity, R.color.colorGreen);
		colorLightGreen = getMyColor(activity, R.color.colorLightGreen);
		colorFaintGreen = getMyColor(activity, R.color.colorFaintGreen);
		colorRed = getMyColor(activity, R.color.colorRed);
		colorLightRed = getMyColor(activity, R.color.colorLightRed);
		colorFaintRed = getMyColor(activity, R.color.colorFaintRed);
		colorWhite = getMyColor(activity, R.color.colorWhite);
		colorTransparent = getMyColor(activity, R.color.transparent);
		colorPrimaryDark = getMyColor(activity, R.color.colorPrimaryDark);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		mainPieChart.setVisibility(View.VISIBLE);
		mainPieChart.setNoDataText("loading data...");
		yearLineChart.setVisibility(View.GONE);
		yearLineChart.setNoDataText("loading date...");
		weekBarChart.setVisibility(View.GONE);
		monthBarChart.setVisibility(View.GONE);

		// transactions lists
		income_trans_container.removeAllViews();
		expense_trans_container.removeAllViews();
		income_trans_container_card.setVisibility(View.VISIBLE);
		expense_trans_container_card.setVisibility(View.VISIBLE);

	}

	@Override
	protected TransStats doInBackground(Bundle... params) {
		Bundle bundle = params[0];
		TransStats stats = new TransStats();

		TTransactions tTransactions = new TTransactions(activity);
		Date date;
		try {
			date = sdf.parse(bundle.getString("date"));
		} catch (ParseException e) {
			date = MyCalendar.dateToday();
		}

		stats.date = date;

		// first get all the transactions for the day
		if (selectedAccountID == ALL_ACCOUNT_ID) {
			stats.setAllTransctions(tTransactions.getTransactionsForWeek(date));
			stats.periodicExpenseSums = tTransactions.getPeriodSums(AStats.DAY, EXPENSE, date);
			stats.periodicIncomeSums = tTransactions.getPeriodSums(AStats.DAY, INCOME, date);
		} else {
			stats.setAllTransctions(tTransactions.getAccountSpecificTransactionsForWeek(selectedAccountID, date));
			stats.periodicExpenseSums = tTransactions.getPeriodSumsForAccount(AStats.DAY, EXPENSE, date, selectedAccountID);
			stats.periodicIncomeSums = tTransactions.getPeriodSumsForAccount(AStats.DAY, INCOME, date, selectedAccountID);
		}

		return stats;
	}


}
