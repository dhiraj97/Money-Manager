// Created by Dhiraj on 05/02/17.

package com.moneymanager.entities;

import java.util.Date;

public class Budget {

	private int id;
	private Category category;
	private Account account;
	private double amount;
	private Date startDate;
	private int period;// noof days

	public Budget(int id, Category category, Account account, double amount, Date startDate, int period) {
		this.id = id;
		this.category = category;
		this.account = account;
		this.amount = amount;
		this.startDate = startDate;
		this.period = period;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}
}
