// Created by Dhiraj on 01/02/17.

package com.moneymanager.exceptions;

public class CategoryExistsException extends Exception {

	public CategoryExistsException() {
		super("Category already exists");
	}

}
