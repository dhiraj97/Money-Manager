// Created by Dhiraj on 07/01/17.

package com.moneymanager.repo.interfaces;

import com.moneymanager.entities.Category;
import com.moneymanager.exceptions.CategoryExistsException;

public interface ICategory {

	Category getCategory(int id);

	Category[] getAllCategories();

	/**
	 * Retures Category list for income or expense type
	 */
	Category[] getTypeSpecificCategories(int type);

	void insertNewCategory(Category category) throws CategoryExistsException;

	void updateCategory(Category category) throws CategoryExistsException;

	void removeCategory(Category cat);

	Category[] getTypeSpecificSearchedCategories(int i, String searchText);
}
