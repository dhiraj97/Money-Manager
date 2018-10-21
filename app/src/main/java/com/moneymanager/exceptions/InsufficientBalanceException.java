// Created by Dhiraj on 31/01/17.

package com.moneymanager.exceptions;

public class InsufficientBalanceException extends Exception {

	public InsufficientBalanceException() {
		super("expense amount should not exceed balance");
	}

}
