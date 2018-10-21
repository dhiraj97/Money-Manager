// Created by Dhiraj on 01/02/17.

package com.moneymanager.entities;

import com.moneymanager.utilities.MyCalendar;

import java.util.Date;

public class Debt {

	private int id;
	private int type;
	private User user;
	private double amount;
	private Account account;
	private String info;
	private Date date;

	public Debt(int id, int type, User user, double amount, Account account, String info, Date date) {
		this.id = id;
		this.type = type;
		this.user = user;
		this.amount = amount;
		this.account = account;
		this.info = info;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getShortInfo() {
		final int characterLimit = 20;
		if (info.length() > characterLimit) {
			return info.substring(0, characterLimit) + "...";
		} else {
			return info;
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String formatedDateTime() {
		return MyCalendar.stringFormatOfDate(date);
	}
}
