// Created by Dhiraj on 01/02/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.Debt;
import com.moneymanager.exceptions.InsufficientBalanceException;

import java.util.Date;

public interface IDebt {

	void insertDebt(Debt debt) throws InsufficientBalanceException;

	Debt getDebt(int id);

	void updateDebtAmount(Debt new_debt) throws InsufficientBalanceException;

	Debt getVerySpecificDebt(int useId, int accID, int type, Date date);

	void removeDebt(Debt debt);

	Debt[] getDebts(int type);

	Debt[] getAllDebtsForUser(int id);

	void updateDebt(Debt updatedDebt) throws InsufficientBalanceException;

	Debt[] getAccountSpecificDebts(int selectedAccountId);

	void removeDebtsForAccount(int id);
}
