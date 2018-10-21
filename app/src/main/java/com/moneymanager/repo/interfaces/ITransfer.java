// Created by Dhiraj on 24/02/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.Transfer;
import com.moneymanager.exceptions.InsufficientBalanceException;

public interface ITransfer {

	long addTransfer(Transfer transfer) throws InsufficientBalanceException;

	Transfer[] getAllTransfers();

	Transfer[] getAccountTransfers(int accountID);

	void removeTransfersForAccount(int id);
}
