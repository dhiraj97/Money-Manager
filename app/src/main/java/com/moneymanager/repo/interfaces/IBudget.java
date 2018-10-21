// Created by Dhiraj on 05/02/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.Budget;

public interface IBudget {


	long insertBudget(Budget budget);

	Budget[] getAllBudgets();

	void removeBudget(int id);

	void removeBudgetsForAccount(int id);
}
