// Created by Dhiraj on 07/01/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.Budget;
import com.moneymanager.entities.Category;
import com.moneymanager.entities.Transaction;
import com.moneymanager.exceptions.InsufficientBalanceException;

import java.util.Date;

public interface ITransaction {

	Transaction[] getAllTransactions(String column, String order);

	double getSumOfTransactionTypeForDay(int type, Date date);

	double getAccountSpecificSumOfTransactionTypeForDay(int acc, int type, Date date);

	Transaction[] getAccountSpecificTransactions(int accID);

	Transaction[] getAccountSpecificTransactionsForDay(int accId, Date date);

	Transaction[] getTransactionsForWeek(Date date);

	Transaction[] getBudgetSpecificTransactions(Budget budget);

	Transaction[] getAccountSpecificTransactionsForWeek(int accId, Date date);

	Transaction[] getTransactionsForMonth(Date date);

	Transaction[] getAccountSpecificTransactionsForMonth(int accID, Date date);

	double[] getPeriodSums(int period, int type, Date date);

	double[] getPeriodSumsForAccount(int period, int type, Date date, int accId);

	/* Custom Period Transactions */
	Transaction[] getTransactionsForCustomPeriod(Date startDate, Date endDate);

	Transaction[] getAccountSpecificTransactionsForCustomPeriod(int accId, Date startDate, Date endDate);

	void insertNewTransaction(Transaction transaction) throws InsufficientBalanceException;

	void removeTransaction(Transaction t);

	void shiftDeletedTransactions(Category cat);

	Transaction getTransaction(int selectedTransactionID);

	void removeTransactionsForAccount(int id);

	Transaction[] getsSearchedTransactions(String[] queries);

	Transaction[] getTransactionsForYear(Date date);

	Transaction[] getAccountSpecificTransactionsForYear(int selectedAccountID, Date date);

}
