// Created by Dhiraj on 24/02/17.

package com.moneymanager.entities;

import com.moneymanager.utilities.MyCalendar;

import java.util.Date;

import static com.moneymanager.Common.formatAmt;

public class Transfer {

	private int id;
	private Account toAccount, fromAccount;
	private double amount;
	private Date date;

	public Transfer(int id, Account toAccount, Account fromAccount, double amount, Date date) {
		this.id = id;
		this.toAccount = toAccount;
		this.fromAccount = fromAccount;
		this.amount = amount;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Account getToAccount() {
		return toAccount;
	}

	public void setToAccount(Account toAccount) {
		this.toAccount = toAccount;
	}

	public Account getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(Account fromAccount) {
		this.fromAccount = fromAccount;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getAmountString() {
		return formatAmt(amount);
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
