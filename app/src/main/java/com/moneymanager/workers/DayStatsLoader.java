// Created by Dhiraj on 14/03/17.

package com.moneymanager.workers;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.stats.AStats;
import com.moneymanager.activities.transaction.AEditTransaction;
import com.moneymanager.entities.Transaction;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;
import com.moneymanager.utilities.TransStats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.moneymanager.Common.*;

public class DayStatsLoader extends AsyncTask<Bundle, Void, TransStats> {

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

	public DayStatsLoader(AStats statsActivity) {

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
			stats.setAllTransctions(tTransactions.getTransactionsForDay(date));
			stats.periodicExpenseSums = tTransactions.getPeriodSums(AStats.DAY, EXPENSE, date);
			stats.periodicIncomeSums = tTransactions.getPeriodSums(AStats.DAY, INCOME, date);
		} else {
			stats.setAllTransctions(tTransactions.getAccountSpecificTransactionsForDay(selectedAccountID, date));
			stats.periodicExpenseSums = tTransactions.getPeriodSumsForAccount(AStats.DAY, EXPENSE, date, selectedAccountID);
			stats.periodicIncomeSums = tTransactions.getPeriodSumsForAccount(AStats.DAY, INCOME, date, selectedAccountID);
		}

		stats.periodicNoofIncome = new int[stats.periodicIncomeSums.length];
		stats.periodicNoofExpense = new int[stats.periodicExpenseSums.length];

		// then get the income and expense transactions from all transaction array
		int noofIncomeTrans = 0, noofExpenseTrans = 0;
		double sumOfIncomeTrans = 0, sumOfExpenseTrans = 0, allTransSum = 0;
		for (Transaction transaction : stats.allTransactions) {

			if (stats.firstTransaction == null) {
				stats.firstTransaction = transaction;
			}

			if (transaction.getCategory().getType() == EXPENSE) {
				stats.expenseTransactions.add(transaction);
				noofExpenseTrans++;
				sumOfExpenseTrans += transaction.getAmount();
				if (stats.firstExpenseTransaction == null) {
					stats.firstExpenseTransaction = transaction;
				}
				stats.periodicNoofExpense[0]++;
			} else {
				noofIncomeTrans++;
				sumOfIncomeTrans += transaction.getAmount();
				stats.incomeTransactions.add(transaction);
				if (stats.firstIncomeTransaction == null) {
					stats.firstIncomeTransaction = transaction;
				}
				stats.periodicNoofIncome[0]++;
			}

			allTransSum += transaction.getAmount();
		}

		// fill other metadata about transactions
		stats.noofExpenseTransactions = noofExpenseTrans;
		stats.noofIncomeTransactions = noofIncomeTrans;
		stats.setExpenseTransSum(sumOfExpenseTrans);
		stats.setIncomeTransSum(sumOfIncomeTrans);
		stats.allTransSum = allTransSum;

		return stats;
	}

	@Override
	protected void onPostExecute(TransStats stats) {
		super.onPostExecute(stats);

		if (stats.noofAllTransactions == 0) {

			if (stats.date.equals(MyCalendar.dateToday())) {
				mainPieChart.setNoDataText("No Transactions found for today");
			} else {
				mainPieChart.setNoDataText("No Transactions found for " + MyCalendar.dateToString(myDate) + " " + MyCalendar.monthToFullString(myDate));
			}
			mainPieChart.invalidate();
			return;
		}


		// setting up overview card

		TextView cardIncomeTextView = (TextView) activity.findViewById(R.id.a_stats_overview_card_income_amt);
		TextView cardExpenseTextView = (TextView) activity.findViewById(R.id.a_stats_overview_card_expense_amt);
		TextView cardTotalTextView = (TextView) activity.findViewById(R.id.a_stats_overview_card_total_amt);

		TextView title = (TextView) activity.findViewById(R.id.a_stats_overview_card_title);
		title.setText(MyCalendar.dateToString(myDate) + " " + MyCalendar.monthToFullString(myDate) + "'s Overview");
		cardExpenseTextView.setText(formatAmt(stats.expenseTransSum));
		cardIncomeTextView.setText(formatAmt(stats.incomeTransSum));
		cardTotalTextView.setText(formatAmt(stats.netSum));

		// setup main piechart
		setupMainPieChart(stats);

		// setting up transaction list for the selected day

		// income transactions
		LinearLayout incomeContainer = (LinearLayout) activity.findViewById(R.id.a_stats_income_trans_list_container);
		fillupTransactionInContainer(incomeContainer, stats.incomeTransactions);
		// expense transactions
		LinearLayout expenseContainer = (LinearLayout) activity.findViewById(R.id.a_stats_expense_trans_list_container);
		fillupTransactionInContainer(expenseContainer, stats.expenseTransactions);

	}

	private void setupMainPieChart(TransStats stats) {

		// initialize alert dialog
		piechart_dialog = new AlertDialog.Builder(activity)
				.setView(R.layout.d_stats_piechart)
				.setCancelable(true)
				.create();

		mainPieChart.setVisibility(View.VISIBLE);
		mainPieChart.setHoleColor(colorTransparent);
		mainPieChart.setHoleRadius(Common.HOLE_RADIUS);
		mainPieChart.setTransparentCircleRadius(0);
		mainPieChart.setDrawCenterText(false);
		mainPieChart.setRotationEnabled(false);
		mainPieChart.getLegend().setEnabled(false);
		mainPieChart.getDescription().setEnabled(false);
		mainPieChart.setOnChartValueSelectedListener(new MyOnValueSelectedListener(stats));
		final int top_bottom_offset = 10;
		final int right_left_offset = 0;
		mainPieChart.setExtraOffsets(
				right_left_offset,
				top_bottom_offset,
				right_left_offset,
				top_bottom_offset
		);

		ArrayList<PieEntry> yE = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();
		if (stats.expenseTransSum > 0) {
			yE.add(new PieEntry((float) stats.expenseTransSum, "Expense", EXPENSE));
			colors.add(getMyColor(activity, R.color.colorRed));
		}
		if (stats.incomeTransSum > 0) {
			yE.add(new PieEntry((float) stats.incomeTransSum, "Income", INCOME));
			colors.add(getMyColor(activity, R.color.colorGreen));
		}

		PieDataSet pieDataSet = new PieDataSet(yE, "this is some label");
		pieDataSet.setSliceSpace(Common.SLICE_SPACE);
		pieDataSet.setValueTextSize(14);
		pieDataSet.setColors(colors);
		pieDataSet.setValueTextColor(colorPrimaryDark);
		pieDataSet.setValueLineColor(colorTransparent);
		pieDataSet.setValueLinePart1OffsetPercentage(100.f);
		pieDataSet.setValueLinePart1Length(0.5f);
		pieDataSet.setValueLinePart2Length(0f);
		pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

		mainPieChart.setData(new PieData(pieDataSet));
		mainPieChart.invalidate();

	}


	private void fillupTransactionInContainer(LinearLayout container, ArrayList<Transaction> transactions) {

		container.removeAllViews();

		// filling up expense container
		for (final Transaction t : transactions) {

			// get row layout reference
			final View rowView = activity.getLayoutInflater().inflate(R.layout.x_stats_row, null);

			rowView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					Intent intent = new Intent(activity, AEditTransaction.class);
					intent.putExtra("trans_id", t.getId());
					activity.startActivity(intent);

					if (piechart_dialog != null) {
						if (piechart_dialog.isShowing()) {
							piechart_dialog.dismiss();
						}
					}

				}
			});

			final TextView tCat = (TextView) rowView.findViewById(R.id.x_stats_row_cat);
			final TextView tAmt = (TextView) rowView.findViewById(R.id.x_stats_row_amt);
			final TextView tAcc = (TextView) rowView.findViewById(R.id.x_stats_row_acc);
			final TextView tDate = (TextView) rowView.findViewById(R.id.x_stats_row_date);
			final TextView tInfo = (TextView) rowView.findViewById(R.id.x_stats_row_info);
			tInfo.setText(t.getShortInfo());
			tDate.setVisibility(View.GONE);
			final String dateText = MyCalendar.dateToString(t.getDateTime()) + "\n" + MyCalendar.monthToString(t.getDateTime());

			if (selectedAccountID == ALL_ACCOUNT_ID) {
				tAcc.setVisibility(View.VISIBLE);
				tAcc.setText(t.getAccount().getName());
			} else {
				tAcc.setVisibility(View.GONE);
			}

			tCat.setText(t.getCategory().getName());
			tAmt.setText(t.getAmountString());

			container.addView(rowView);

		}

	}

	class MyOnValueSelectedListener implements OnChartValueSelectedListener {

		TransStats stats;

		public MyOnValueSelectedListener(TransStats stats) {
			this.stats = stats;
		}

		@Override
		public void onValueSelected(Entry e, Highlight h) {

			final int type = (int) e.getData();
			piechart_dialog.setOnShowListener(new DialogInterface.OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {

					final LinearLayout container_layout = (LinearLayout) piechart_dialog.findViewById(R.id.d_stats_piechart_list_container);
					if (container_layout != null) {
						container_layout.removeAllViews();
					}

					final int color = type == INCOME ? colorGreen : colorRed;
					final String period = "on " + MyCalendar.dateToString(myDate) + " " + MyCalendar.monthToString(myDate);
					final String centerText = (type == INCOME ? "Income" : "Expense") + " " + period;

					ArrayList<PieEntry> ye = new ArrayList<>();

					final HashMap<String, Double> cat_stats_map = new HashMap<>();

					final ArrayList<Transaction> transactions = type == EXPENSE ? stats.expenseTransactions : stats.incomeTransactions;

					double highest = 0;
					double lowest = transactions.get(0).getAmount();

					for (Transaction t : transactions) {

						final double amt = t.getAmount();
						final String cat_name = t.getCategory().getName();
						if (cat_stats_map.containsKey(cat_name)) {
							cat_stats_map.put(cat_name, (cat_stats_map.get(cat_name) + amt));
						} else {
							cat_stats_map.put(cat_name, amt);
						}

						if (amt >= highest) {
							highest = amt;
						}
						if (amt <= lowest) {
							lowest = amt;
						}

					}

					final int count = cat_stats_map.size();

					int max_color = type == EXPENSE ? colorRed : colorGreen;
					int min_color = type == EXPENSE ? colorLightRed : colorLightGreen;

					float[] max_hsv = new float[3];
					Color.colorToHSV(max_color, max_hsv);
					float[] min_hsv = new float[3];
					Color.colorToHSV(min_color, min_hsv);

					final float max_s = max_hsv[1] * 100;
					final float min_s = min_hsv[1] * 100;
					final float s_diff = max_s - min_s;
					final float step = s_diff / count;


					ArrayList<Integer> colors = new ArrayList<>();
					for (Map.Entry e : cat_stats_map.entrySet()) {

						final double value = (Double) e.getValue();

						ye.add(
								new PieEntry(
										(float) value,
										((String) e.getKey()),
										e.getKey())
						);

						final float c_sat = (float) (((value * count * step / highest) + min_s) / 100);
						final float[] c_hsv = new float[3];
						Color.colorToHSV(max_color, c_hsv);
						c_hsv[1] = c_sat;
						final int c = Color.HSVToColor(c_hsv);

						colors.add(c);

					}

					final PieChart dialog_piechart = (PieChart) piechart_dialog.findViewById(R.id.d_stats_piechart);
					dialog_piechart.setHoleColor(Common.getMyColor(activity, R.color.transparent));
					dialog_piechart.setHoleRadius(60);
					dialog_piechart.setTransparentCircleRadius(0);
					final int top_bottom_offset = 0;
					final int right_left_offset = 25;
					dialog_piechart.setExtraOffsets(
							right_left_offset,
							top_bottom_offset,
							right_left_offset,
							top_bottom_offset
					);
					dialog_piechart.setCenterTextSize(16);
					dialog_piechart.setCenterTextRadiusPercent(80);
					dialog_piechart.setCenterTextColor(color);
					dialog_piechart.setCenterText(centerText);
					dialog_piechart.getLegend().setEnabled(false);
					dialog_piechart.getDescription().setEnabled(false);

					final PieDataSet pieDataSet = new PieDataSet(ye, "");
					pieDataSet.setColor(color);
					pieDataSet.setSliceSpace(0);
					pieDataSet.setValueTextSize(14);
					pieDataSet.setColors(colors);
					pieDataSet.setValueLineColor(color);
					pieDataSet.setValueLinePart1OffsetPercentage(100.f);
					pieDataSet.setValueLinePart1Length(0.5f);
					pieDataSet.setValueLinePart2Length(0f);
					pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
					dialog_piechart.setData(new PieData(pieDataSet));
					dialog_piechart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

						@Override
						public void onValueSelected(Entry e, Highlight h) {

							dialog_piechart.setCenterText(((PieEntry) e).getLabel() + " Stats");

							final String cat_name = ((String) e.getData());

							// create a transaction list of the selected category
							final ArrayList<Transaction> categoryTransactions = new ArrayList<>();
							for (Transaction t : transactions) {

								if (t.getCategory().getName().equals(cat_name)) {
									categoryTransactions.add(t);
								}
							}

							fillupTransactionInContainer(container_layout, categoryTransactions);
						}

						@Override
						public void onNothingSelected() {
							dialog_piechart.setCenterText(centerText);
							container_layout.removeAllViews();
						}
					});
					dialog_piechart.invalidate();

				}
			});

			piechart_dialog.show();
		}

		@Override
		public void onNothingSelected() {

		}
	}

}
