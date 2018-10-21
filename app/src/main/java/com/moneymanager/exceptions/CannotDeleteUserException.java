// Created by Dhiraj on 19/03/17.

package com.moneymanager.exceptions;

public class CannotDeleteUserException extends Throwable {

	public CannotDeleteUserException() {
		super("Cannot delete the user with active Debt or Loan");
	}

	public CannotDeleteUserException(String username, int debtCount, int loanCount) {


		super("Cannot delete the user with active Debt or Loan.\n" +
				username + " has\n" +
				((debtCount > 0) ? (debtCount == 1) ? debtCount + " active debt\n" : debtCount + " active debts\n" : "") +
				((loanCount > 0) ? (loanCount == 1) ? loanCount + " active loan\n" : loanCount + " active loans" : "")
		);
	}

}
