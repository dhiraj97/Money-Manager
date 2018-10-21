// Created by Dhiraj on 02/02/17.

package com.moneymanager.exceptions;

public class UserExistsException extends Exception {

	public UserExistsException() {
		super("This user already exists");
	}

}
