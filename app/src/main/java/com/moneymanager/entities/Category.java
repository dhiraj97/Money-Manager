// Created by Dhiraj on 07/01/17.

package com.moneymanager.entities;

public class Category {

	public static final String OTHER_EXPENSE = "other expense";
	public static final String OTHER_INCOME = "other income";
	private int id;
	private String name;
	private int type; // 0- income, 1- expense
	private boolean exclude;

	public Category(int id, String name, int type, boolean exclude) {
		this.id = id;
		name = name.toLowerCase();
		this.name = name.replaceFirst(String.valueOf(name.charAt(0)), String.valueOf(name.charAt(0)).toUpperCase());
		this.type = type;
		this.exclude = exclude;
	}

	public static String[] extractNameStringArrayFromArray(Category[] cat_arr) {

		final String[] str_arr = new String[cat_arr.length];
		int x = 0;
		for (Category c : cat_arr) {

			str_arr[x++] = c.getName();

		}
		return str_arr;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTypeString() {
		return type == 0 ? "income" : "expense";
	}

	public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	@Override
	public String toString() {
		String t = type == 0 ? "income" : "expense";
		t += " (" + type + ")";
		return "id: " + id + "\n" +
				"name: " + name + "\n" +
				"type: " + t;
	}
}
