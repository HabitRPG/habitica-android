package com.habitrpg.android.habitica;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.prefs.scanner.IntentIntegrator;
import com.habitrpg.android.habitica.prefs.scanner.IntentResult;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Mickael Goubin
 */
public class LoginActivity extends AppCompatActivity
	implements Callback<UserAuthResponse>,HabitRPGUserCallback.OnUserReceived {
	private final static String TAG_ADDRESS="address";
	private final static String TAG_USERID="user";
	private final static String TAG_APIKEY="key";

	private Button mRegisterBtn, mLoginNormalBtn, mLoginTokensBtn;
	private ImageView mLoginBarcode;
	private LinearLayout mLoginNormalLayout, mLoginTokensLayout, mRegisterLayout, mLoginBtnContainer;
	private EditText mUserTokenET,mApiTokenET, mUsernameET, mPasswordET,mRegUsername,mRegEmail,mRegPassword,mRegConfirmPassword;
	private APIHelper mApiHelper;
	private Handler mMainHandler;
	private ProgressBar mProgressBar;
	public String mTmpUserToken;
	public String mTmpApiToken;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_screen);

		mRegisterBtn = (Button) this.findViewById(R.id.register_btn);
		mLoginNormalBtn = (Button) this.findViewById(R.id.login_normal_btn);
		mLoginTokensBtn = (Button) this.findViewById(R.id.login_tokens_btn);
		mLoginBarcode = (ImageView) this.findViewById(R.id.login_barcode);
		mProgressBar = (ProgressBar) this.findViewById(R.id.PB_AsyncTask);

		mLoginNormalLayout = (LinearLayout) this.findViewById(R.id.login_normal_layout);
		mLoginTokensLayout = (LinearLayout) this.findViewById(R.id.login_tokens_layout);
		mRegisterLayout = (LinearLayout) this.findViewById(R.id.register_layout);
		mLoginBtnContainer = (LinearLayout) this.findViewById(R.id.login_btn_container);

		mUserTokenET = (EditText) this.findViewById(R.id.userId);
		mApiTokenET = (EditText) this.findViewById(R.id.apiToken);

		mUsernameET = (EditText) this.findViewById(R.id.username);
		mPasswordET = (EditText) this.findViewById(R.id.password);

		mRegUsername = (EditText) this.findViewById(R.id.reg_username);
		mRegEmail = (EditText) this.findViewById(R.id.reg_email);
		mRegPassword = (EditText) this.findViewById(R.id.reg_password);
		mRegConfirmPassword = (EditText) this.findViewById(R.id.reg_confirm_password);

		mRegisterBtn.setOnClickListener(mRegisterClick);
		mLoginNormalBtn.setOnClickListener(mLoginNormalClick);
		mLoginTokensBtn.setOnClickListener(mLoginTokensClick);
		mLoginBarcode.setOnClickListener(mBarcodeClick);

		HostConfig hc= PrefsActivity.fromContext(this);
        if(hc ==null) {
            hc =  new HostConfig(getString(R.string.SP_address_default), "80", "", "");
        }
		mApiHelper = new APIHelper(this,hc);

	}

	private void resetLayout() {
		//expand(mLoginBarcode);
		if(mRegisterBtn.getVisibility() == View.GONE)
			expand(mRegisterBtn);
		if(mLoginTokensBtn.getVisibility() == View.GONE)
			expand(mLoginTokensBtn);
		if(mLoginNormalBtn.getVisibility() == View.GONE)
			expand(mLoginNormalBtn);
		if(mLoginBarcode.getVisibility()==View.GONE)
			expand(mLoginBarcode);
		if(mLoginBtnContainer.getVisibility()==View.GONE)
			expand(mLoginBtnContainer);
		collapse(mLoginNormalLayout);
		collapse(mLoginTokensLayout);
		collapse(mRegisterLayout);

	}
	private View.OnClickListener mBarcodeClick = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			IntentIntegrator integrator = new IntentIntegrator(LoginActivity.this);
			integrator.initiateScan();
		}
	};
	private View.OnClickListener mRegisterClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mLoginTokensBtn.getVisibility()==View.GONE && mLoginNormalBtn.getVisibility()==View.GONE) {
				String username, email,password,cpassword;
				username = String.valueOf(mRegUsername.getText());
				email = String.valueOf(mRegEmail.getText());
				password = String.valueOf(mRegPassword.getText());
				cpassword = String.valueOf(mRegConfirmPassword.getText());
				mApiHelper.registerUser(v,username,email,password,cpassword);
			}else {
				expand(mRegisterLayout);//.setVisibility(View.VISIBLE);
				collapse(mLoginNormalBtn);//.setVisibility(View.GONE);
				collapse(mLoginTokensBtn);//.setVisibility(View.GONE);
			}
		}
	};
	private View.OnClickListener mLoginNormalClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mRegisterBtn.getVisibility()==View.GONE && mLoginTokensBtn.getVisibility()==View.GONE) {
				String username,password;
				username = String.valueOf(mUsernameET.getText());
				password = String.valueOf(mPasswordET.getText());
				mApiHelper.connectUser(username,password, LoginActivity.this);
			} else {
				expand(mLoginNormalLayout);//.setVisibility(View.VISIBLE);
				collapse(mRegisterBtn);//.setVisibility(View.GONE);
				collapse(mLoginTokensBtn);//.setVisibility(View.GONE);
			}
		}
	};
	private View.OnClickListener mLoginTokensClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mRegisterBtn.getVisibility() == View.GONE && mLoginNormalBtn.getVisibility() == View.GONE) {
				mTmpUserToken = String.valueOf(mUserTokenET.getText());
				mTmpApiToken = String.valueOf(mApiTokenET.getText());
				HostConfig config = PrefsActivity.fromContext(LoginActivity.this);
                if(config==null) {
                    config = new HostConfig(getString(R.string.SP_address_default), "80", mTmpApiToken, mTmpUserToken);
                } else {
                    config.setApi(mTmpApiToken);
                    config.setUser(mTmpUserToken);
                }
				APIHelper secApiHelper = new APIHelper(LoginActivity.this, config);
				secApiHelper.retrieveUser(new HabitRPGUserCallback(LoginActivity.this));

			} else {
				expand(mLoginTokensLayout);
				collapse(mLoginNormalBtn);
				collapse(mRegisterBtn);
			}
		}
	};



	private boolean layoutHasChanged() {
		return mRegisterBtn.getVisibility() == View.GONE || mLoginNormalBtn.getVisibility() == View.GONE ||mLoginTokensBtn.getVisibility() == View.GONE;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && layoutHasChanged()) {
			resetLayout();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public static void expand(final View v) {
		v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		final int targtetHeight = v.getMeasuredHeight();
		final int targtetWidth = v.getMeasuredWidth();
		Log.v("expanding ", "w:" + targtetWidth + " h:"+targtetHeight);
		//v.getLayoutParams().height = 0;
		//v.getLayoutParams().width=0;
		v.setVisibility(View.VISIBLE);//works when the setVisibility is outside the animationlistener.

		Animation a = new Animation()
		{

			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				v.getLayoutParams().height = interpolatedTime == 1
						? ViewGroup.LayoutParams.WRAP_CONTENT
						: (int)(targtetHeight * interpolatedTime);
				v.getLayoutParams().width = interpolatedTime == 1
						? ViewGroup.LayoutParams.WRAP_CONTENT
						: (int)(targtetWidth * interpolatedTime);
				v.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		// 1dp/ms
		a.setDuration(500);
		v.startAnimation(a);
	}
 //

	public static void collapse(final View v) {
		final int initialHeight = v.getMeasuredHeight();
		final int initialWidth = v.getMeasuredWidth();

		Animation a = new Animation()
		{
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if(interpolatedTime == 1){
					v.setVisibility(View.GONE);
				}else{
					v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					v.getLayoutParams().width = initialWidth - (int) (initialWidth*interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 1dp/ms
		a.setDuration(500);//targtetHeight / (v.getContext().getResources().getDisplayMetrics().density)));
		v.startAnimation(a);
	}

	private void startMainActivity() {
		startActivity(new Intent(LoginActivity.this, MainActivity.class));
		finish();

	}
	private void saveTokens(String api, String user) throws Exception {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
		SharedPreferences.Editor editor = prefs.edit();
        Log.v("login", "saving tokens");
		boolean ans = editor.putString(getString(R.string.SP_APIToken), api)
				.putString(getString(R.string.SP_userID), user)
                .putString(getString(R.string.SP_address),getString(R.string.SP_address_default))
				.commit();
			if(!ans) {
				throw new Exception("PB_string_commit");
			}

	}
	private void showHelpMMessage() {
		final SpannableString mess = new SpannableString(getString(R.string.helpString));
		Linkify.addLinks(mess, Linkify.ALL);
		AlertDialog d =new AlertDialog.Builder(this)
		.setTitle(R.string.pref_dialog_title)
		.setMessage(mess).setCancelable(false)
		.setPositiveButton(R.string.string_pref_dialog_positive, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		}).create();
		d.show();
		((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		((TextView)d.findViewById(android.R.id.message)).setTextSize(13);

	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			try {
				Log.d("scanresult", scanResult.getContents());
				this.parse(scanResult.getContents());
			} catch(Exception e) {

			}
		}
	}

	private void parse(String contents) {
		String adr=null,user=null,key=null;
		try {
			JSONObject obj;

			obj = new JSONObject(contents);
			adr = obj.getString(TAG_ADDRESS);
			user = obj.getString(TAG_USERID);
			key = obj.getString(TAG_APIKEY);
			Log.d("", "adr" + adr + " user:" + user + " key" + key);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = prefs.edit();
			boolean ans = editor.putString(getString(R.string.SP_address), adr)
					.putString(getString(R.string.SP_APIToken), key)
					.putString(getString(R.string.SP_userID), user)
					.commit();
			if(ans != true) {
				throw new Exception("PB_string_commit");
			}
			startMainActivity();
		} catch (JSONException e) {
			showSnackbar(getString(R.string.ERR_pb_barcode));
			e.printStackTrace();
		} catch(Exception e) {
			if("PB_string_commit".equals(e.getMessage())) {
				showSnackbar(getString(R.string.ERR_pb_barcode));
			}
		}
	}

	private void showSnackbar(String content)
	{
		Snackbar snackbar = Snackbar
				.make(this.findViewById(R.id.login_linear_layout), content, Snackbar.LENGTH_LONG);

		View snackbarView = snackbar.getView();
		snackbarView.setBackgroundColor(getResources().getColor(R.color.red));//change Snackbar's background color;
		snackbar.show(); // Don’t forget to show!
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_info:
				showHelpMMessage();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


/*	@Override
	public void onNewUser(String apiToken, String apiUser) {
		final String api = apiToken;
		final String user = apiUser;
		mMainHandler = new Handler(getMainLooper());
		Runnable myRunnable = new Runnable(){
			public void run() {
				afterResults();
				try {
					saveTokens(api,user);
					startMainActivity();
					finish();
				} catch (Exception e) {
					if("PB_string_commit".equals(e.getMessage()))
						Crouton.makeText(LoginActivity.this, getString(R.string.ERR_pb_accountCreation), Style.ALERT).show();
				}
			}
		};
		mMainHandler.post(myRunnable);
	}
	@Override
	public void onUserConnected(String api_t, String user_t) {
		final String api = api_t;
		final String user = user_t;
		mMainHandler = new Handler(getMainLooper());
        Log.v("user connected", "api:" +api_t+ " us" + user_t);
		Runnable myRunnable = new Runnable(){
			public void run() {
				afterResults();
				try {
					saveTokens(api,user);
					startMainActivity();
				} catch (Exception e) {
					if("PB_string_commit".equals(e.getMessage()))
						Crouton.makeText(LoginActivity.this, getString(R.string.ERR_pb_connection), Style.ALERT).show();
				}
			}
		};
		mMainHandler.post(myRunnable);
	}


	@Override
	public void onUserReceived(User user) {
		try {
			saveTokens(mTmpApiToken,mTmpUserToken);
			startMainActivity();
			finish();
		} catch(Exception e) {
			if("PB_string_commit".equals(e.getMessage()))
				Crouton.makeText(LoginActivity.this, getString(R.string.ERR_pb_connection), Style.ALERT).show();
		}
	}

	@Override
	public void onUserItemsReceived(UserLook.UserItems userLook, Reward.SpecialReward itemBought) {

	}

	@Override
	public void onPostResult(double xp, double hp, double gold, double lvl, double delta) {

	}

	@Override
	public void onPreResult() {
		mProgressBar.setVisibility(View.VISIBLE);

	}

	@Override
	public void onError(HabitRPGException error) {
		final HabitRPGException err = error;
		mMainHandler = new Handler(getMainLooper());
		Runnable myRunnable = new Runnable(){
			public void run() {
				afterResults();
				String myMessage=err!=null ? err.getMessage() : null;
				if(myMessage == null)
					myMessage= getString(R.string.unknown_error);
				Crouton.makeText(LoginActivity.this, myMessage, Style.ALERT).show();
			}
		};
		mMainHandler.post(myRunnable);
	}*/


	private void afterResults() {
			mProgressBar.setVisibility(View.INVISIBLE);
	}

    @Override
    public void success(UserAuthResponse userAuthResponse, Response response) {
        try {
            saveTokens(userAuthResponse.getToken(), userAuthResponse.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.startMainActivity();
    }

    @Override
    public void failure(RetrofitError error) {
        Log.d("OMG", "omg" + error.getMessage() + " " + error.getUrl());
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        try {
            saveTokens(mTmpApiToken,mTmpUserToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.startMainActivity();
    }

    @Override
    public void onUserFail() {
		showSnackbar(getString(R.string.unknown_error));
    }
}
