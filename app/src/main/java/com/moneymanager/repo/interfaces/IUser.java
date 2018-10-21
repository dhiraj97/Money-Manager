// Created by Dhiraj on 01/02/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.User;
import com.moneymanager.exceptions.CannotDeleteUserException;
import com.moneymanager.exceptions.UserExistsException;

public interface IUser {

	long addUser(User user) throws UserExistsException;

	User getUser(int id);

	User[] getAllUsers();

	User[] getSpecificUsers(int selectedAccountID, int type);

	User[] getSpecificUsersFromAllAccounts(int type);

	void removeUser(User user) throws CannotDeleteUserException;

	User[] getSearchedUsers(String searchText);
}
