// Created by Dhiraj on 15/03/17.

package com.moneymanager.workers;

import android.content.Context;
import android.content.DialogInterface;
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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.moneymanager.Common;
import com.moneymanager.R;
import com.moneymanager.activities.stats.AStats;
import com.moneymanager.entities.Transaction;
import com.moneymanager.repo.TTransactions;
import com.moneymanager.utilities.MyCalendar;
import com.moneymanager.utilities.TransStats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.moneymanager.Common.*;

public class YearStatsLoader extends AsyncTask<Bundle, Void, TransStats> {

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

	public YearStatsLoader(AStats statsActivity) {

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
		yearLineChart.setVisibility(View.VISIBLE);
		yearLineChart.setNoDataText("loading date...");
		weekBarChart.setVisibility(View.GONE);
		monthBarChart.setVisibility(View.GONE);

		// hide transactions list
		income_trans_container.removeAllViews();
		expense_trans_container.removeAllViews();
		income_trans_container_card.setVisibility(View.GONE);
		expense_trans_container_card.setVisibility(View.GONE);
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

		// first get all the transactions for the year
		if (selectedAccountID == ALL_ACCOUNT_ID) {
			stats.setAllTransctions(tTransactions.getTransactionsForYear(date));
			stats.periodicExpenseSums = tTransactions.getPeriodSums(AStats.YEAR, EXPENSE, date);
			stats.periodicIncomeSums = tTransactions.getPeriodSums(AStats.YEAR, INCOME, date);
		} else {
			stats.setAllTransctions(tTransactions.getAccountSpecificTransactionsForYear(selectedAccountID, date));
			stats.periodicExpenseSums = tTransactions.getPeriodSumsForAccount(AStats.YEAR, EXPENSE, date, selectedAccountID);
			stats.periodicIncomeSums = tTransactions.getPeriodSumsForAccount(AStats.YEAR, INCOME, date, selectedAccountID);
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
				stats.periodicNoofExpense[transaction.getDateTime().getMonth() + 1]++;
			} else {
				noofIncomeTrans++;
				sumOfIncomeTrans += transaction.getAmount();
				stats.incomeTransactions.add(transaction);
				if (stats.firstIncomeTransaction == null) {
					stats.firstIncomeTransaction = transaction;
				}
				stats.periodicNoofIncome[transaction.getDateTime().getMonth() + 1]++;
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
			mainPieChart.setNoDataText("No Transactions found for year " + MyCalendar.yearToString(stats.date));
			yearLineChart.setVisibility(View.GONE);
			return;
		}

		// setting up overview card
		TextView cardIncomeTextView = (TextView) activity.findViewById(R.id.a_stats_overview_card_income_amt);
		TextView cardExpenseTextView = (TextView) activity.findViewById(R.id.a_stats_overview_card_expense_amt);
		TextView cardTotalTextView = (TextView) activity.findViewById(R.id.a_stats_overview_card_total_amt);
		TextView title = (TextView) activity.findViewById(R.id.a_stats_overview_card_title);
		title.setText(MyCalendar.yearToString(stats.date) + "'s Overview");
		cardExpenseTextView.setText(formatAmt(stats.expenseTransSum));
		cardIncomeTextView.setText(formatAmt(stats.incomeTransSum));
		cardTotalTextView.setText(formatAmt(stats.netSum));


		setUpMainPieChart(stats);
		setUpLineChart(stats);

	}

	private void setUpMainPieChart(TransStats stats) {

		// initialize alert dialog
		piechart_dialog = new AlertDialog.Builder(activity)
				.setView(R.layout.d_stats_piechart)
				.setCancelable(true)
				.create();

		mainPieChart.setVisibility(View.VISIBLE);
		mainPieChart.setHoleColor(colorTransparent);
		mainPieChart.setHoleRadius(Common.HOLE_RADIUS);
		mainPieChart.setDrawCenterText(false);
		mainPieChart.setRotationEnabled(false);
		mainPieChart.getLegend().setEnabled(false);
		mainPieChart.getDescription().setEnabled(false);
		mainPieChart.setOnChartValueSelectedListener(new MyOnValueSelectedListener(stats));

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

	private void setUpLineChart(TransStats stats) {

		yearLineChart.fitScreen();

		final List<Entry> incomeGroup = new ArrayList<>();
		final List<Entry> expenseGroup = new ArrayList<>();

		for (int i = 0; i < 14; i++) {
			if (i < stats.periodicIncomeSums.length) {
				incomeGroup.add(new Entry(i, (float) stats.periodicIncomeSums[i], stats));
			} else {
				incomeGroup.add(new Entry(i, 0f));
			}
		}
		for (int i = 0; i < 14; i++) {
			if (i < stats.periodicExpenseSums.length) {
				expenseGroup.add(new Entry(i, (float) stats.periodicExpenseSums[i], stats));
			} else {
				expenseGroup.add(new Entry(i, 0f));
			}
		}

		final LineDataSet inSet = new LineDataSet(incomeGroup, "income");
		inSet.setColor(colorGreen);
		inSet.setValueTextColor(colorGreen);
		inSet.setValueTextSize(10);
		inSet.setCircleColor(colorGreen);
		inSet.setCircleColorHole(colorGreen);
		inSet.setHighLightColor(colorTransparent);
		final LineDataSet exSet = new LineDataSet(expenseGroup, "expense");
		exSet.setColor(colorRed);
		exSet.setValueTextColor(colorRed);
		exSet.setValueTextSize(10);
		exSet.setCircleColor(colorRed);
		exSet.setCircleColorHole(colorRed);
		exSet.setHighLightColor(colorTransparent);

		final LineData lineData = new LineData(inSet, exSet);
		yearLineChart.setData(lineData);
		yearLineChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
		yearLineChart.getAxisRight().setEnabled(false);
		yearLineChart.getAxisLeft().setTextColor(colorWhite);
		yearLineChart.getAxisLeft().setDrawGridLines(false);
		yearLineChart.getXAxis().setDrawGridLines(true);
		yearLineChart.getDescription().setText(MyCalendar.yearToString(stats.date) + " (pinch out horizontally for more precision)");
		yearLineChart.getDescription().setTextColor(colorWhite);
		yearLineChart.getLegend().setEnabled(false);
		yearLineChart.setMarker(new CustomMarkerView(activity, R.layout.x_linechart_marker));
		yearLineChart.setVisibleXRangeMaximum(6);
		yearLineChart.setVerticalFadingEdgeEnabled(true);

		final XAxis xAxis = yearLineChart.getXAxis();
		xAxis.setGranularity(1f);
		xAxis.setTextColor(colorWhite);

		final String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", ""};
		xAxis.setValueFormatter(new IAxisValueFormatter() {
			@Override
			public String getFormattedValue(float value, AxisBase axis) {
				return months[(int) value];
			}
		});
		yearLineChart.invalidate();

	}

	public class CustomMarkerView extends MarkerView {

		private TextView mText, inText, exText;

		public CustomMarkerView(Context context, int layoutResource) {
			super(context, layoutResource);
			// this markerview only displays a textview
			mText = (TextView) findViewById(R.id.x_linechart_marker_month);
			inText = (TextView) findViewById(R.id.x_linechart_marker_income);
			exText = (TextView) findViewById(R.id.x_linechart_marker_expense);

		}

		// callbacks everytime the MarkerView is redrawn, can be used to update the
		// content (user-interface)
		@Override
		public void refreshContent(Entry e, Highlight highlight) {
			final TransStats stats = (TransStats) e.getData();
			final int idx = (int) e.getX() - 1;
			final double inNet = stats.periodicIncomeSums[idx + 1];
			final double exNet = stats.periodicExpenseSums[idx + 1];
			final double mNet = inNet - exNet;
			mText.setText(MyCalendar.monthToFullString(new Date(117, idx, 1)) + ", Net: " + mNet);
			inText.setText("Income Transactions: " + stats.periodicNoofIncome[idx + 1] + ", Net: " + inNet);
			exText.setText("Expense Transactions: " + stats.periodicNoofExpense[idx + 1] + ", Net: " + exNet);
		}

		@Override
		public MPPointF getOffset() {
			return new MPPointF(
					(getWidth() / 20),
					-getHeight() - 20
			);
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
					final String period = "for " + MyCalendar.yearToString(stats.date);
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

							final String cat_name = ((String) e.getData());

							dialog_piechart.setCenterText(cat_name);

						}

						@Override
						public void onNothingSelected() {
							dialog_piechart.setCenterText(centerText);
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
