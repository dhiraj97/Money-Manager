// Created by Dhiraj on 07/01/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.Account;
import com.moneymanager.exceptions.AccountNameExistsException;
import com.moneymanager.exceptions.InsufficientBalanceException;
import com.moneymanager.exceptions.NoAccountsException;

public interface IAccount {

	long insertNewAccount(Account account);

	Account[] getAllAccounts(String column, String order) throws NoAccountsException;

	int countTransactions(int id);

	int countDebt(int id);

	int countLoan(int id);

	Account getAccount(int id);

	void removeAccount(int id);

	double getSumOfBalanceOfAllAccounts();

	double getSumOfBalanceOfAccount(int selectedAccount);

	void updateAccountBalance(int id, double amount, boolean add) throws InsufficientBalanceException;

	void updateAccount(Account account) throws AccountNameExistsException;
}
