// Created by Dhiraj on 09/01/17.

package com.moneymanager.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.moneymanager.Common.DATE_FORMAT;

/**
 * Utility class to handle all Calendar related functions
 */
public class MyCalendar {

	public static SimpleDateFormat getSimpleDateFormat() {
		return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	}

	public static int daysBetween(Date startDate, Date endDate) {


		long diff = (Math.abs(startDate.getTime() - endDate.getTime()));

		return (int) (diff / (24 * 60 * 60 * 1000));

	}

	public static String timeToString(Date date) {

		int hours = date.getHours();
		int minutes = date.getMinutes();
		String am = (hours >= 12) ? "pm" : "am";

		hours = (hours > 12) ? hours - 12 : hours;

		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes) + " " + am;

	}

	/* Day stuff */

	public static Date dateToday() {
		return new Date();
	}

	public static String dateTodayString() {
		int lastInt = dateToday().getDate() % 10;
		switch (lastInt) {
			case 1:
				return dateToday().getDate() + "st";
			case 2:
				return dateToday().getDate() + "nd";
			case 3:
				return dateToday().getDate() + "rd";
			default:
				return dateToday().getDate() + "th";
		}

	}

	public static String dateToString(Date date) {
		final int lastInt = date.getDate() % 10;
		int secondLastInt = (date.getDate() - lastInt) / 10;
		if (secondLastInt == 1) {
			return date.getDate() + "th";
		} else {
			switch (lastInt) {
				case 1:
					return date.getDate() + "st";
				case 2:
					return date.getDate() + "nd";
				case 3:
					return date.getDate() + "rd";
				default:
					return date.getDate() + "th";
			}
		}
	}

	public static String stringFormatOfDate(Date date) {

		return getSimpleDateFormat().format(date);

	}

	public static String getShortDateString(Date date) {

		int d = date.getDate();
		int m = date.getMonth() + 1;
		int y = date.getYear() - 100;

		return (d < 10 ? "0" + d : d) + "/" + (m < 10 ? "0" + m : m) + "/" + y;

	}

	public static Date dateBeforeDays(int noofDays) {
		final Date dateToday = dateToday();

		int date = dateToday.getDate();
		final Date newDate = new Date(dateToday.getYear(), dateToday.getMonth(), (-noofDays));
		return new Date(newDate.getYear(), newDate.getMonth(), newDate.getDate() + date);
	}

	public static Date dateBeforeDays(Date d, int noofDays) {
		final Date dateToday = d;

		int date = dateToday.getDate();
		final Date newDate = new Date(dateToday.getYear(), dateToday.getMonth(), (-noofDays));
		return new Date(newDate.getYear(), newDate.getMonth(), newDate.getDate() + date);
	}

	public static Date dateAfterDays(int noofDays) {
		final Date dateToday = dateToday();

		int date = dateToday.getDate();
		final Date newDate = new Date(dateToday.getYear(), dateToday.getMonth(), (noofDays));
		return new Date(newDate.getYear(), newDate.getMonth(), newDate.getDate() + date);
	}

	public static Date dateAfterDays(Date d, int noofDays) {
		final Date dateToday = d;

		int date = dateToday.getDate();
		final Date newDate = new Date(dateToday.getYear(), dateToday.getMonth(), (noofDays));
		return new Date(newDate.getYear(), newDate.getMonth(), newDate.getDate() + date);
	}

	/* Week stuff */

	// returns specified date's week starting and ending dates
	public static Date[] weekEndandStartDatesforDate(Date date) {

		Date[] dates = new Date[2];

		int week_day = date.getDay() - 1;

		if (week_day < 0) {
			week_day = 6;
		}

		System.out.println(week_day);

		dates[0] = dateBeforeDays(date, week_day);
		dates[1] = dateAfterDays(date, 6 - week_day);

		return dates;
	}




	/* Month stuff */

	public static String thisMonthString() {
		return String.format("%tb", dateToday());
	}

	public static String monthToString(Date date) {
		return String.format("%tb", date);
	}

	public static String monthToFullString(Date date) {
		return String.format("%tB", date);
	}

	public static String monthToStringDigits(Date date) {
		return String.format("%tm", date);
	}

	public static Date firstDateOfMonth(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();

	}

	public static Date lastDateOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		return c.getTime();
	}

	public static int prevMonth() {
		int prevMonth = dateToday().getMonth() - 1;
		if (prevMonth < 0) {
			prevMonth = 11;
		}
		return prevMonth;

	}

	public static int nextMonth() {
		int nxtMon = dateToday().getMonth() + 1;
		if (nxtMon > 11) {
			nxtMon = 0;
		}
		return nxtMon;
	}



	/* Year stuff */

	public static int thisYear() {
		return new Date().getYear();
	}

	public static String yearToString(Date date) {
		return String.format("%tY", date);
	}

	public static String getNiceFormatedCompleteDateString(Date date) {
		final String dateStr = MyCalendar.dateToString(date);
		final String monthStr = MyCalendar.monthToString(date);
		final String yearStr = MyCalendar.yearToString(date);
		return dateStr + " " + monthStr + " " + yearStr;
	}

	public static String getNiceFormatedCompleteDateTimeString(Date date) {
		final String dateStr = MyCalendar.dateToString(date);
		final String monthStr = MyCalendar.monthToString(date);
		final String yearStr = MyCalendar.yearToString(date);
		return dateStr + " " + monthStr + " " + yearStr + ", " + timeToString(date);
	}

}
