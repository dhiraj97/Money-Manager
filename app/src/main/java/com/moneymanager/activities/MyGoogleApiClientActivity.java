// Created by Dhiraj on 16/02/17.

package com.moneymanager.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

public abstract class MyGoogleApiClientActivity extends MyBaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	protected final int RESOLVE_CONNECTION_REQUEST_CODE = 2204;
	private GoogleApiClient googleApiClient;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		if (googleApiClient == null) {

			googleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Drive.API)
					.addScope(Drive.SCOPE_FILE)
					.addScope(Drive.SCOPE_APPFOLDER)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();

		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		googleApiClient.connect();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		switch (requestCode) {

			case RESOLVE_CONNECTION_REQUEST_CODE:

				if (resultCode == RESULT_OK) {
					googleApiClient.connect();
				} else {

				}

				break;

		}


	}

	/**
	 * Called when activity gets invisible. Connection to Drive service needs to
	 * be disconnected as soon as an activity is invisible.
	 */
	@Override
	protected void onPause() {
		if (googleApiClient != null) {
			googleApiClient.disconnect();
		}
		super.onPause();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		log_i("GoogleApiClient connected");
	}

	@Override
	public void onConnectionSuspended(int i) {
		log_i("GoogleApiClient connection suspended");
	}

	/**
	 * Called when {@code mGoogleApiClient} is trying to connect but failed.
	 * Handle {@code result.getResolution()} if there is a resolution is
	 * available.
	 */
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result) {
		log_i("GoogleApiClient connection failed: " + result.getErrorMessage());

		if (!result.hasResolution()) {

			GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
			return;

		}

		try {
			result.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
		} catch (IntentSender.SendIntentException e) {
			e.printStackTrace();
		}

	}

	public GoogleApiClient getGoogleApiClient() {
		return googleApiClient;
	}
}
