// Created by Dhiraj on 17/02/17.

package com.moneymanager.activities;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import static com.moneymanager.Common.mylog;

public abstract class MyBaseActivity extends AppCompatActivity {

	public void showShortToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	public void showLongToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	public void log_i(String message) {
		Log.i(mylog, message);
	}

}
