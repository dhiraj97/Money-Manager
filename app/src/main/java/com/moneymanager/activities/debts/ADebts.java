package com.moneymanager.activities.debts;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.moneymanager.R;
import com.moneymanager.activities.transaction.AAddTransaction;
import com.moneymanager.adapters.DebtAdapter;

public class ADebts extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_debt);

//		setupToolbar(this, R.id.a_debt_toolbar, "Debts and Loans");

		// View Pager Stuff
		ViewPager viewPager = (ViewPager) findViewById(R.id.debt_viewpager);
		viewPager.setAdapter(new DebtAdapter(getSupportFragmentManager()));

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.a_debt_add_debt_fab);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ADebts.this, AAddTransaction.class);
				intent.putExtra("debt", 0);
				startActivity(intent);
			}
		});

	}
}
