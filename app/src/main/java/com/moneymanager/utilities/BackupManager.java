// Created by Dhiraj on 16/02/17.

package com.moneymanager.utilities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.moneymanager.Common;
import com.moneymanager.activities.MyBaseActivity;
import com.moneymanager.db.DBHelper;
import com.moneymanager.repo.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static com.moneymanager.Common.CURRENT_ACCOUNT_ID;
import static com.moneymanager.Common.spCURRENT_ACCOUNT_ID;

public class BackupManager {

	private MyBaseActivity activity;
	private GoogleApiClient apiClient;

	public BackupManager(MyBaseActivity activity, GoogleApiClient googleApiClient) {
		this.activity = activity;
		this.apiClient = googleApiClient;
	}

	public void startBackup() {
		DriveContentsCallback callback = new DriveContentsCallback();
		Drive.DriveApi.newDriveContents(apiClient).setResultCallback(callback);

	}

	private String createBackupScript() {

		final StringBuilder scriptBuilder = new StringBuilder();

		final String[] newTables = {
				TAccounts.q_CREATE_TABLE(),
				TCategories.q_CREATE_TABLE(),
				TTransactions.q_CREATE_TABLE(),
				TUser.q_CREATE_TABLE(),
				TDebt.q_CREATE_TABLE(),
				TBudget.q_CREATE_TABLE(),
				TTransfers.q_CREATE_TABLE()
		};

		final String[] tableNames = {
				TAccounts.TABLE_NAME,
				TCategories.TABLE_NAME,
				TTransactions.TABLE_NAME,
				TUser.TABLE_NAME,
				TDebt.TABLE_NAME,
				TBudget.TABLE_NAME,
				TTransfers.TABLE_NAME
		};

		for (String s : tableNames) {
			scriptBuilder.append("DROP TABLE ").append(s).append(";");
		}

		for (String s : newTables) {

			scriptBuilder.append(s);
			if (!s.endsWith(";")) {
				scriptBuilder.append(";");
			}

		}

		DBHelper dbHelper = new DBHelper(activity);

		for (String table : tableNames) {

			final String query = "SELECT * FROM " + table;

			Cursor c = dbHelper.select(query, null);

			if (c == null) {
				continue;
			}

			if (c.getCount() < 1) {
				continue;
			}

			scriptBuilder.append("INSERT INTO ").append(table).append(" VALUES ");

			while (c.moveToNext()) {

				scriptBuilder.append("(");
				for (int i = 0; i < c.getColumnCount(); i++) {

					switch (c.getType(i)) {

						case Cursor.FIELD_TYPE_INTEGER:
							scriptBuilder.append(c.getInt(i));
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							scriptBuilder.append(c.getFloat(i));
							break;
						default:
							scriptBuilder.append("'").append(c.getString(i).replace("'", "''")).append("'");
							break;

					}

					if (i < c.getColumnCount() - 1) {
						scriptBuilder.append(",");
					}

				}
				scriptBuilder.append(")");

				if (c.isLast()) {
					scriptBuilder.append(";");
				} else {
					scriptBuilder.append(",");
				}

			}


		}


		return scriptBuilder.toString();
	}

	public void restoreBackup() {

		// query for backup file
		new FileRestoreTask().execute();

	}

	private String readFromFile(InputStream in) {

		final StringBuilder stringBuilder = new StringBuilder();

		try {
			final DataInputStream dis = new DataInputStream(in);

			byte x;
			while ((x = dis.readByte()) != -1) {
				stringBuilder.append((char) x);
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	private void writeToFile(String data, OutputStream os) {
		try {
			if (data != null) {

				Writer writer = new OutputStreamWriter(os);
				writer.write(data);
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class DriveContentsCallback implements ResultCallback<DriveApi.DriveContentsResult> {

		@Override
		public void onResult(@NonNull DriveApi.DriveContentsResult result) {

			if (!result.getStatus().isSuccess()) {
				activity.showShortToast("Error while trying to create new file contents");
				return;
			}

			final DriveContents contents = result.getDriveContents();

			new FileUploadTask(contents).execute();


		}
	}

	private class FileUploadTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog;
		private DriveContents contents;

		FileUploadTask(DriveContents contents) {
			this.contents = contents;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog = new ProgressDialog(activity);
			progressDialog.setMessage("Backing up your stuff...");
			progressDialog.setCancelable(false);
			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {

			// delete all remote backup files first
			final Query query = new Query.Builder()
					.addFilter(Filters.eq(SearchableField.TITLE, DBHelper.DB_NAME))
					.build();

			final DriveApi.MetadataBufferResult metadataBufferResult = Drive.DriveApi
					.getAppFolder(apiClient)
					.queryChildren(apiClient, query)
					.await();
			final MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

			for (final Metadata m : metadataBuffer) {

				DriveFile file = m.getDriveId().asDriveFile();
				file.delete(apiClient).setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
					@Override
					public void onResult(@NonNull com.google.android.gms.common.api.Status status) {

						if (status.isSuccess()) {
							activity.log_i(m.getTitle() + " created on " + MyCalendar.getNiceFormatedCompleteDateTimeString(m.getCreatedDate()) + " deleted");
						} else {
							activity.log_i(m.getTitle() + " created on " + MyCalendar.getNiceFormatedCompleteDateTimeString(m.getCreatedDate()) + " not deleted");
						}

					}
				});
			}


			// first get the backup script
			final String script = createBackupScript();
			OutputStream outputStream = contents.getOutputStream();

			activity.log_i("backup script:\n" + script);

			// write the contents to the DriveContent
			writeToFile(script, outputStream);

			final MetadataChangeSet fileMetadata = new MetadataChangeSet.Builder()
					.setTitle(DBHelper.DB_NAME)
					.setMimeType("text/plain")
					.build();

			Drive.DriveApi.getAppFolder(apiClient).createFile(apiClient, fileMetadata, contents)
					.setResultCallback(new ResultCallbacks<DriveFolder.DriveFileResult>() {
						@Override
						public void onSuccess(@NonNull DriveFolder.DriveFileResult driveFileResult) {
							activity.showShortToast("Backup successful");
						}

						@Override
						public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
							activity.showShortToast("Backup failed: " + status.getStatusMessage());
						}
					});


			return null;

		}

		@Override
		protected void onPostExecute(Void v) {
			super.onPostExecute(v);

			progressDialog.dismiss();


		}
	}

	private class FileRestoreTask extends AsyncTask<Void, Void, String> {

		ProgressDialog progressDialog;
		private Metadata metadata;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog = new ProgressDialog(activity);
			progressDialog.setMessage("Searching for Backup");
			progressDialog.setCancelable(false);
			progressDialog.show();

		}

		@Override
		protected String doInBackground(Void[] params) {

			Drive.DriveApi.requestSync(apiClient).await(5, TimeUnit.SECONDS);

			final Query query = new Query.Builder()
					.addFilter(Filters.eq(SearchableField.TITLE, DBHelper.DB_NAME))
					.build();

			final DriveApi.MetadataBufferResult metadataBufferResult = Drive.DriveApi
					.getAppFolder(apiClient)
					.queryChildren(apiClient, query)
					.await();
			final MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

			for (Metadata m : metadataBuffer) {
				activity.log_i(m.getTitle() + " created on " + MyCalendar.getNiceFormatedCompleteDateTimeString(m.getCreatedDate()));
			}

			if (metadataBuffer.getCount() > 0) {

				metadata = metadataBuffer.get(0);
				activity.log_i(metadata.getTitle() + "->" + metadata.getCreatedDate());

				DriveContents contents = metadata
						.getDriveId()
						.asDriveFile()
						.open(apiClient, DriveFile.MODE_READ_ONLY, null)
						.await()
						.getDriveContents();


				return readFromFile(contents.getInputStream());

			} else {
				return null;
			}


		}

		@Override
		protected void onPostExecute(final String script) {

			progressDialog.dismiss();

			if (script == null) {
				AlertDialog alertDialog = new AlertDialog.Builder(activity)
						.setCancelable(true)
						.setMessage("No Backup Found :(")
						.setPositiveButton("okay", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();

				alertDialog.show();
			} else {

				activity.log_i("retrieved script:\n" + script);

				final AlertDialog alertDialog = new AlertDialog.Builder(activity)
						.setCancelable(true)
						.setTitle("Backup Found")
						.setMessage("created on:\n" + MyCalendar.getNiceFormatedCompleteDateTimeString(metadata.getCreatedDate()))
						.setPositiveButton("restore", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

								new AsyncTask<Void, Void, Void>() {

									ProgressDialog pd;

									@Override
									protected void onPreExecute() {
										super.onPreExecute();
										pd = new ProgressDialog(activity);
										pd.setMessage("Restoring Backup...");
										pd.setCancelable(false);
										pd.show();
									}

									@Override
									protected Void doInBackground(Void... params) {

										DBHelper dbHelper = new DBHelper(activity);
										String[] scripts = script.split(";");
										for (String s : scripts) {
											if (!s.equals("")) {
												try {
													dbHelper.execute(s);
												} catch (SQLException e) {
													activity.log_i("SQL error: " + e.getMessage() + " for " + s);
												}
											}
										}

										return null;
									}

									@Override
									protected void onPostExecute(Void aVoid) {
										super.onPostExecute(aVoid);

										activity.finish();

										Common.CURRENT_ACCOUNT_ID = Common.ALL_ACCOUNT_ID;
										ShrPref.writeData(activity, spCURRENT_ACCOUNT_ID, CURRENT_ACCOUNT_ID);
										pd.dismiss();

									}
								}.execute();


							}
						})
						.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();

				alertDialog.show();
			}


		}

	}

}
