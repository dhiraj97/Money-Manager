// Created by Dhiraj on 14/03/17.

package com.moneymanager.utilities;

import com.moneymanager.entities.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TransStats {

	public Date date;
	public ArrayList<Transaction> allTransactions;
	public ArrayList<Transaction> incomeTransactions;
	public ArrayList<Transaction> expenseTransactions;
	public int noofAllTransactions;
	public int noofIncomeTransactions;
	public int noofExpenseTransactions;
	public double allTransSum;
	public double incomeTransSum;
	public double expenseTransSum;
	public double netSum;
	public double[] periodicIncomeSums;
	public double[] periodicExpenseSums;
	public int[] periodicNoofIncome;
	public int[] periodicNoofExpense;
	public Transaction firstTransaction = null;
	public Transaction firstIncomeTransaction = null;
	public Transaction firstExpenseTransaction = null;
	Date seconDate;// secondDate for custom period only

	public TransStats() {
		incomeTransactions = new ArrayList<>();
		expenseTransactions = new ArrayList<>();
	}

	public void setAllTransctions(Transaction[] trans) {
		allTransactions = new ArrayList<>(Arrays.asList(trans));
		noofAllTransactions = trans.length;
	}

	public void setIncomeTransSum(double incomeTransSum) {
		this.incomeTransSum = incomeTransSum;
		netSum = incomeTransSum - expenseTransSum;
	}

	public void setExpenseTransSum(double expenseTransSum) {
		this.expenseTransSum = expenseTransSum;
		netSum = incomeTransSum - expenseTransSum;
	}

}
