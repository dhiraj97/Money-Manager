// Created by Dhiraj on 07/01/17.
package com.moneymanager.entities;

import com.moneymanager.utilities.MyCalendar;

import java.util.Date;

public class Account {

	private int id;
	private String name;
	private double balance;
	private double startingBalance;
	private Date createDate;
	private boolean exclude;

	public Account(int id, String name, double balance, double startingBalance, Date date, boolean exclude) {
		this.id = id;
		this.name = name;
		this.balance = balance;
		this.exclude = exclude;
		this.startingBalance = startingBalance;
		this.createDate = date;
	}

	public static String[] extractNameStringArrayFromArray(Account[] accounts) {
		final String[] str_arr = new String[accounts.length];
		int x = 0;
		for (Account c : accounts) {

			str_arr[x++] = c.getName();

		}
		return str_arr;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	public double getStartingBalance() {
		return startingBalance;
	}

	public void setStartingBalance(double startingBalance) {
		this.startingBalance = startingBalance;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String formatedDateTime() {
		return MyCalendar.stringFormatOfDate(createDate);
	}

}
