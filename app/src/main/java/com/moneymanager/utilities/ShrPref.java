// Created by Dhiraj on 08/01/17.

package com.moneymanager.utilities;


import android.content.Context;

import static com.moneymanager.Common.spFILE_NAME;


public class ShrPref {

	public static void writeData(Context c, String key, String data) {

		c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).edit().putString(key, data).apply();

	}

	public static void writeData(Context c, String key, int data) {
		c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).edit().putInt(key, data).apply();
	}

	public static void writeData(Context c, String key, float data) {
		c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).edit().putFloat(key, data).apply();
	}

	public static void writeData(Context c, String key, boolean data) {
		c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, data).apply();
	}

	public static String readData(Context c, String key, String def) {

		return c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).getString(key, def);

	}

	public static int readData(Context c, String key, int def) {
		return c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).getInt(key, def);
	}

	public static float readData(Context c, String key, float def) {

		return c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).getFloat(key, def);
	}

	public static boolean readData(Context c, String key, boolean def) {
		return c.getSharedPreferences(spFILE_NAME, Context.MODE_PRIVATE).getBoolean(key, def);
	}

}