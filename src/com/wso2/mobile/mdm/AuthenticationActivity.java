/*
 ~ Copyright (c) 2014, WSO2 Inc. (http://wso2.com/) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
*/
package com.wso2.mobile.mdm;

import java.util.Map;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import com.wso2.mobile.mdm.api.DeviceInfo;
import com.wso2.mobile.mdm.utils.CommonUtilities;
import com.wso2.mobile.mdm.utils.ServerUtilities;
import com.wso2.mobile.mdm.R;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;

public class AuthenticationActivity extends SherlockActivity {
	AsyncTask<Void, Void, Void> mRegisterTask;
	String regId = "";
	Button authenticate;
	EditText username;
	EditText txtDomain;
	EditText password;
	//TextView txtLoadingEULA;
	RadioButton radioBYOD, radioCOPE;
	String deviceType;
	Activity activity;
	Context context;
	String isAgreed = "";
	String eula = "";
	ProgressDialog progressDialog;
	AsyncTask<Void, Void, String> mLicenseTask;
	private final int TAG_BTN_AUTHENTICATE = 0;
	private final int TAG_BTN_OPTIONS = 1;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setCustomView(R.layout.custom_sherlock_bar);
		View homeIcon = findViewById(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.id.home
				: R.id.abs__home);
		((View) homeIcon.getParent()).setVisibility(View.GONE);

		this.activity = AuthenticationActivity.this;
		this.context = AuthenticationActivity.this;
		deviceType = getResources().getString(R.string.device_enroll_type_byod);
		//txtLoadingEULA = (TextView)findViewById(R.id.txtLoadingEULA);
		txtDomain = (EditText) findViewById(R.id.txtDomain);
		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		radioBYOD = (RadioButton)findViewById(R.id.radioBYOD);
		radioCOPE = (RadioButton)findViewById(R.id.radioCOPE);
		txtDomain.setFocusable(true);
		txtDomain.requestFocus();
		if(CommonUtilities.DEBUG_MODE_ENABLED){
			Log.v("check first username", username.getText().toString());
			Log.v("check first password", password.getText().toString());
		}
		AsyncTask<Void, Void, Void> mRegisterTask;
		authenticate = (Button) findViewById(R.id.btnRegister);
		authenticate.setEnabled(false);
		authenticate.setTag(TAG_BTN_AUTHENTICATE);
		authenticate.setOnClickListener(onClickListener_BUTTON_CLICKED);
		authenticate.setBackground(getResources().getDrawable(R.drawable.btn_grey));
		authenticate.setTextColor(getResources().getColor(R.color.black));
		/*txtLoadingEULA.setVisibility(View.VISIBLE);
		username.setVisibility(View.GONE);
		password.setVisibility(View.GONE);
		authenticate.setVisibility(View.GONE);*/
		

		DeviceInfo deviceInfo = new DeviceInfo(AuthenticationActivity.this);

		/*
		 * optionBtn = (ImageView) findViewById(R.id.option_button);
		 * optionBtn.setTag(TAG_BTN_OPTIONS);
		 * optionBtn.setOnClickListener(onClickListener_BUTTON_CLICKED);
		 */

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(getResources().getString(R.string.intent_extra_regid))) {
				regId = extras.getString(getResources().getString(R.string.intent_extra_regid));
			}
		}
		if (regId == null || regId.equals("")) {
			regId = GCMRegistrar.getRegistrationId(this);
		}

		username.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				enableSubmitIfReady();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				enableSubmitIfReady();
			}
		});

		password.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				enableSubmitIfReady();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				enableSubmitIfReady();
			}
		});

	}

	OnClickListener onClickListener_BUTTON_CLICKED = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub

			int iTag = (Integer) view.getTag();

			switch (iTag) {

			case TAG_BTN_AUTHENTICATE:
				if(username.getText()!=null && !username.getText().toString().trim().equals("") && password.getText()!=null && !password.getText().toString().trim().equals("")){
					if(radioBYOD.isChecked()){
						deviceType = getResources().getString(R.string.device_enroll_type_byod);
					}else{
						deviceType = getResources().getString(R.string.device_enroll_type_cope);
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
							AuthenticationActivity.this);
					builder.setMessage(getResources().getString(R.string.dialog_init_middle) + " " + deviceType + " " + getResources().getString(R.string.dialog_init_end))
							.setNegativeButton(getResources().getString(R.string.info_label_rooted_answer_yes), dialogClickListener)
							.setPositiveButton(getResources().getString(R.string.info_label_rooted_answer_no), dialogClickListener).show();
				}else{
					if(username.getText()!=null && !username.getText().toString().trim().equals("")){
						Toast.makeText(context, getResources().getString(R.string.toast_error_password), Toast.LENGTH_LONG).show();					
					}else{
						Toast.makeText(context, getResources().getString(R.string.toast_error_username), Toast.LENGTH_LONG).show();
					}
				}
				break;

			case TAG_BTN_OPTIONS:
				// startOptionActivity();
				break;
			default:
				break;
			}

		}
	};

	public void showErrorMessage(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setCancelable(true);
		builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				cancelEntry();
				dialog.dismiss();
			}
		});
		/*
		 * builder1.setNegativeButton("No", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int id) { dialog.cancel(); } });
		 */

		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showAlert(String message, String title) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.custom_terms_popup);
		dialog.setTitle(CommonUtilities.EULA_TITLE);
		dialog.setCancelable(false);
		// TextView text = (TextView) dialog.findViewById(R.id.text);
		WebView web = (WebView) dialog.findViewById(R.id.webview);
		String html = "<html><body>" + message + "</body></html>";
		String mime = "text/html";
		String encoding = "utf-8";
		web.getSettings().setJavaScriptEnabled(true);
		web.loadDataWithBaseURL(null, html, mime, encoding, null);
		// text.setText(message+ "/n/n" +message +"/n/n/n"+message);
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		Button cancelButton = (Button) dialog
				.findViewById(R.id.dialogButtonCancel);

		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences mainPref = AuthenticationActivity.this
						.getSharedPreferences(getResources().getString(R.string.shared_pref_package), Context.MODE_PRIVATE);
				Editor editor = mainPref.edit();
				editor.putString(getResources().getString(R.string.shared_pref_isagreed), "1");
				editor.commit();
				dialog.dismiss();
				Intent intent = new Intent(AuthenticationActivity.this,
						PinCodeActivity.class);
				intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if(txtDomain.getText()!=null && txtDomain.getText().toString().trim()!=""){
					intent.putExtra(getResources().getString(R.string.intent_extra_email), username.getText().toString().trim()+"@"+txtDomain.getText().toString().trim());
				}else{
					intent.putExtra(getResources().getString(R.string.intent_extra_email), username.getText().toString().trim());
				}
				startActivity(intent);
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelEntry();
				dialog.dismiss();
			}
		});

		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}
		});

		dialog.show();
	}

	public void cancelEntry() {
		SharedPreferences mainPref = context.getSharedPreferences(getResources().getString(R.string.shared_pref_package),
				Context.MODE_PRIVATE);
		Editor editor = mainPref.edit();
		editor.putString(getResources().getString(R.string.shared_pref_policy), "");
		editor.putString(getResources().getString(R.string.shared_pref_isagreed), "0");
		editor.putString(getResources().getString(R.string.shared_pref_registered), "0");
		editor.putString(getResources().getString(R.string.shared_pref_ip), "");
		editor.commit();
		// finish();

		Intent intentIP = new Intent(AuthenticationActivity.this,
				SettingsActivity.class);
		intentIP.putExtra(getResources().getString(R.string.intent_extra_from_activity),
				AuthenticationActivity.class.getSimpleName());
		intentIP.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intentIP);

	}
	
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				dialog.dismiss();
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				startAuthentication();
				break;
			}
		}
	};
	
	public void showAlertSingle(String message, String title){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	//cancelEntry();
                dialog.cancel();
            }
        });
        /*builder1.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });*/

        AlertDialog alert = builder.create();
        alert.show();
	}

	
	public void fetchLicense(){
		SharedPreferences mainPref = context.getSharedPreferences(getResources().getString(R.string.shared_pref_package),
				Context.MODE_PRIVATE);
		isAgreed = mainPref.getString(getResources().getString(R.string.shared_pref_isagreed), "");
		String eula = mainPref.getString(getResources().getString(R.string.shared_pref_eula), "");
		String type = mainPref.getString(getResources().getString(R.string.shared_pref_reg_type), "");
		
		if(type.trim().equals(getResources().getString(R.string.device_enroll_type_byod))){
			if (!isAgreed.equals("1")) {
				/*username.setVisibility(View.GONE);
				password.setVisibility(View.GONE);
				txtDomain.setVisibility(View.GONE);
				authenticate.setVisibility(View.GONE);
				txtLoadingEULA.setVisibility(View.VISIBLE);*/
					mLicenseTask = new AsyncTask<Void, Void, String>() {
	
						@Override
						protected String doInBackground(Void... params) {
							// boolean registered =
							// ServerUtilities.register(context, regId);
							String response = "";
							try {
								response = ServerUtilities.getEULA(context, txtDomain.getText().toString().trim());
							} catch (Exception e) {
								e.printStackTrace();
							}
							return response;
						}
	
						
						@Override
						protected void onPreExecute() {
							progressDialog = ProgressDialog.show(
									AuthenticationActivity.this,
									getResources().getString(R.string.dialog_license_agreement),
									getResources().getString(R.string.dialog_please_wait), true);
							progressDialog.setCancelable(true);
							progressDialog.setOnCancelListener(cancelListener);
						};

						OnCancelListener cancelListener = new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface arg0) {
								showAlertSingle(
										getResources().getString(R.string.error_enrollment_failed_detail),
										getResources().getString(R.string.error_enrollment_failed));
								//finish();
							}
						};
						 
	
						@Override
						protected void onPostExecute(String result) {
							if (progressDialog != null
									&& progressDialog.isShowing()) {
								progressDialog.dismiss();
							}

							if (result != null) {
								SharedPreferences mainPref = AuthenticationActivity.this
										.getSharedPreferences(getResources().getString(R.string.shared_pref_package),
												Context.MODE_PRIVATE);
								Editor editor = mainPref.edit();
								editor.putString(getResources().getString(R.string.shared_pref_eula), result);
								editor.commit();
	
								isAgreed = mainPref.getString(getResources().getString(R.string.shared_pref_isagreed), "");
								String eula = mainPref.getString(getResources().getString(R.string.shared_pref_eula), "");
								if (!isAgreed.equals("1")) {
									if (eula != null && eula != "") {
										showAlert(eula, CommonUtilities.EULA_TITLE);
									} else {
										showErrorMessage(
												getResources().getString(R.string.error_enrollment_failed_detail),
												getResources().getString(R.string.error_enrollment_failed));
									}
								}
							} else {
								showErrorMessage(
										getResources().getString(R.string.error_enrollment_failed_detail),
										getResources().getString(R.string.error_enrollment_failed));
							}
							mLicenseTask = null;
						}
	
					};
	
					mLicenseTask.execute();
			}else{
				Intent intent = new Intent(AuthenticationActivity.this,
						PinCodeActivity.class);
				intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if(txtDomain.getText()!=null && txtDomain.getText().toString().trim()!=""){
					intent.putExtra(getResources().getString(R.string.intent_extra_email), username.getText().toString().trim()+"@"+txtDomain.getText().toString().trim());
				}else{
					intent.putExtra(getResources().getString(R.string.intent_extra_email), username.getText().toString().trim());
				}
				
				startActivity(intent);
			}
		}else{
			Intent intent = new Intent(AuthenticationActivity.this,
					PinCodeActivity.class);
			intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if(txtDomain.getText()!=null && txtDomain.getText().toString().trim()!=""){
				intent.putExtra(getResources().getString(R.string.intent_extra_email), username.getText().toString().trim()+"@"+txtDomain.getText().toString().trim());
			}else{
				intent.putExtra(getResources().getString(R.string.intent_extra_email), username.getText().toString().trim());
			}
			startActivity(intent);
		}
	}
	
	public void showAuthErrorMessage(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setCancelable(true);
		builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		/*
		 * builder1.setNegativeButton("No", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int id) { dialog.cancel(); } });
		 */

		AlertDialog alert = builder.create();
		alert.show();
	}


	public void startAuthentication() {
		final Context context = AuthenticationActivity.this;
		SharedPreferences mainPref = context.getSharedPreferences(
    			getResources().getString(R.string.shared_pref_package), Context.MODE_PRIVATE);
    	Editor editor = mainPref.edit();
		editor.putString(getResources().getString(R.string.shared_pref_reg_type), deviceType);
		editor.commit();
		
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			Map<String, String> state = null;

			@Override
			protected Void doInBackground(Void... params) {
				if(txtDomain.getText()!=null && !txtDomain.getText().toString().trim().equals("")){
					state = ServerUtilities.isAuthenticate(username.getText().toString().trim()+"@"+txtDomain.getText().toString()
							.trim(), password.getText().toString().trim(),
							AuthenticationActivity.this);
				}else{
					state = ServerUtilities.isAuthenticate(username.getText()
							.toString().trim(), password.getText().toString().trim(),
							AuthenticationActivity.this);
				}
				
				return null;
			}

			ProgressDialog progressDialog;

			// declare other objects as per your need
			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(
						AuthenticationActivity.this, getResources().getString(R.string.dialog_authenticate),
						getResources().getString(R.string.dialog_please_wait), true);

				// do initialization of required objects objects here
			};

			protected void onPostExecute(Void result) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				if (state!=null && state.get("status").trim().equals("1")) {
					fetchLicense();
					
				} else {
					if(state!=null && !state.get("message").trim().equals("")){
						if(state.get("status").trim().equals("0")){
							AlertDialog.Builder builder = new AlertDialog.Builder(
									AuthenticationActivity.this);
							builder.setTitle(getResources().getString(R.string.title_head_authentication_error));
							builder.setMessage(state.get("message"))
									.setNegativeButton(getResources().getString(R.string.info_label_rooted_answer_yes), dialogClickListener)
									.setPositiveButton(getResources().getString(R.string.info_label_rooted_answer_no), dialogClickListener).show();
						}else{
							showAuthErrorMessage(state.get("message"), getResources().getString(R.string.title_head_authentication_error));
						}
					}else{
						AlertDialog.Builder builder = new AlertDialog.Builder(
								AuthenticationActivity.this);
						builder.setTitle(getResources().getString(R.string.title_head_authentication_error));
						builder.setMessage(getResources().getString(R.string.error_auth_failed_detail))
								.setNegativeButton(getResources().getString(R.string.info_label_rooted_answer_yes), dialogClickListener)
								.setPositiveButton(getResources().getString(R.string.info_label_rooted_answer_no), dialogClickListener).show();
					}

				}
				mRegisterTask = null;
				progressDialog.dismiss();
			}

			/*
			 * protected Void doInBackground(Void... params) { // TODO
			 * Auto-generated method stub return null; }
			 */

		};
		mRegisterTask.execute(null, null, null);
	}

	public void startOptionActivity() {
		Intent intent = new Intent(AuthenticationActivity.this,
				DisplayDeviceInfoActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
				AuthenticationActivity.class.getSimpleName());
		intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
		startActivity(intent);
	}

	@SuppressLint("NewApi")
	public void enableSubmitIfReady() {

		boolean isReady = false;

		if (username.getText().toString().length() >= 1
				&& password.getText().toString().length() >= 1) {
			isReady = true;
		}

		if (isReady) {			
			authenticate.setBackground(getResources().getDrawable(R.drawable.btn_orange));
			authenticate.setTextColor(getResources().getColor(R.color.white));
			authenticate.setEnabled(true);
		} else {
			authenticate.setBackground(getResources().getDrawable(R.drawable.btn_grey));
			authenticate.setTextColor(getResources().getColor(R.color.black));
			authenticate.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.auth_sherlock_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ip_setting:
			SharedPreferences mainPref = AuthenticationActivity.this
					.getSharedPreferences(getResources().getString(R.string.shared_pref_package), Context.MODE_PRIVATE);
			Editor editor = mainPref.edit();
			editor.putString(getResources().getString(R.string.shared_pref_ip), "");
			editor.commit();

			Intent intentIP = new Intent(AuthenticationActivity.this,
					SettingsActivity.class);
			intentIP.putExtra(getResources().getString(R.string.intent_extra_from_activity),
					AuthenticationActivity.class.getSimpleName());
			intentIP.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
			startActivity(intentIP);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			this.startActivity(i);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			/*
			 * Intent i = new Intent(); i.setAction(Intent.ACTION_MAIN);
			 * i.addCategory(Intent.CATEGORY_HOME); this.startActivity(i);
			 */
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mRegisterTask = new AsyncTask<Void, Void, Void>() {
			boolean state = false;

			@Override
			protected Void doInBackground(Void... params) {
				try {
					state = ServerUtilities.isRegistered(regId, context);
				} catch (Exception e) {
					e.printStackTrace();
					// HandleNetworkError(e);
					// Toast.makeText(getApplicationContext(), "No Connection",
					// Toast.LENGTH_LONG).show();
				}
				return null;
			}

			// declare other objects as per your need
			@Override
			protected void onPreExecute() {
				/*
				 * progressDialog=
				 * ProgressDialog.show(AuthenticationActivity.this,
				 * "Checking Registration Info","Please wait", true);
				 * progressDialog.setCancelable(true);
				 * progressDialog.setOnCancelListener(cancelListener);
				 */
				// do initialization of required objects objects here
			};

			/*
			 * OnCancelListener cancelListener=new OnCancelListener(){
			 * 
			 * @Override public void onCancel(DialogInterface arg0){
			 * showErrorMessage(
			 * "Could not connect to server please check your internet connection and try again"
			 * , "Connection Error"); } };
			 */

			@Override
			protected void onPostExecute(Void result) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				SharedPreferences mainPref = context.getSharedPreferences(
						getResources().getString(R.string.shared_pref_package), Context.MODE_PRIVATE);
				String success = mainPref.getString(getResources().getString(R.string.shared_pref_registered), "");
				if (success.trim().equals("1")) {
					state = true;
				}

				if (state) {
					Intent intent = new Intent(AuthenticationActivity.this,
							AlreadyRegisteredActivity.class);
					intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					// finish();
				} else {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					// finish();
				}
				mRegisterTask = null;

			}

		};

		mRegisterTask.execute(null, null, null);

		super.onResume();
	}

}
