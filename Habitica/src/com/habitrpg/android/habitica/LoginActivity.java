package com.habitrpg.android.habitica;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableRow;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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

	private Button mLoginNormalBtn;
	private LoginButton mFacebookLoginBtn;
	private EditText mUsernameET, mPasswordET,mEmail,mConfirmPassword;
    private TableRow mEmailRow, mConfirmPasswordRow;
	private APIHelper mApiHelper;
	private ProgressBar mProgressBar;
	public String mTmpUserToken;
	public String mTmpApiToken;
	public Boolean isRegistering;
    private Menu menu;

	private String apiAddress;
	//private String apiAddress = "http://192.168.2.155:8080/"; // local testing

    private CallbackManager callbackManager;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_screen);
		apiAddress = getString(R.string.SP_address_default);
		mLoginNormalBtn = (Button) this.findViewById(R.id.login_btn);
        mProgressBar = (ProgressBar) this.findViewById(R.id.PB_AsyncTask);


		mUsernameET = (EditText) this.findViewById(R.id.username);
		mPasswordET = (EditText) this.findViewById(R.id.password);


		mEmail = (EditText) this.findViewById(R.id.email);
		mConfirmPassword = (EditText) this.findViewById(R.id.confirm_password);

        mEmailRow = (TableRow) this.findViewById(R.id.email_row);
        mConfirmPasswordRow = (TableRow) this.findViewById(R.id.confirm_password_row);

		mLoginNormalBtn.setOnClickListener(mLoginNormalClick);


        mFacebookLoginBtn = (LoginButton) this.findViewById(R.id.login_button);
		mFacebookLoginBtn.setReadPermissions("user_friends");

        callbackManager = CallbackManager.Factory.create();

        mFacebookLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                mApiHelper.connectSocial(accessToken.getUserId(), accessToken.getToken(), LoginActivity.this);
            }

            @Override
            public void onCancel() {
                Log.d("FB Login", "Cancelled");
            }

			@Override
			public void onError(FacebookException exception) {
                Log.d("FB Login", "Error");
			}
		});

		HostConfig hc= PrefsActivity.fromContext(this);
        if(hc ==null) {
            hc =  new HostConfig(apiAddress, "80", "", "");
        }
		mApiHelper = new APIHelper(this,hc);

        this.isRegistering = true;
        this.setRegistering(this.isRegistering);
    }

	private void resetLayout() {
		if (this.isRegistering) {
            if (this.mEmailRow.getVisibility() == View.GONE) {
                expand(this.mEmailRow);
            }
            if (this.mConfirmPasswordRow.getVisibility() == View.GONE) {
                expand(this.mConfirmPasswordRow);
            }
        } else {
            if (this.mEmailRow.getVisibility() == View.VISIBLE) {
                collapse(this.mEmailRow);
            }
            if (this.mConfirmPasswordRow.getVisibility() == View.VISIBLE) {
                collapse(this.mConfirmPasswordRow);
            }
        }
	}

	private View.OnClickListener mLoginNormalClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            mProgressBar.setVisibility(View.VISIBLE);
            if (isRegistering) {
                String username, email,password,cpassword;
                username = String.valueOf(mUsernameET.getText());
                email = String.valueOf(mEmail.getText());
                password = String.valueOf(mPasswordET.getText());
                cpassword = String.valueOf(mConfirmPassword.getText());
				if (username.length() == 0 || password.length() == 0 || email.length() == 0 || cpassword.length() == 0) {
					showValidationError(R.string.login_validation_error_fieldsmissing);
					return;
				}
                mApiHelper.registerUser(username,email,password, cpassword, LoginActivity.this);
            } else {
                String username,password;
                username = String.valueOf(mUsernameET.getText());
                password = String.valueOf(mPasswordET.getText());
				if (username.length() == 0 || password.length() == 0) {
					showValidationError(R.string.login_validation_error_fieldsmissing);
					return;
				}
                mApiHelper.connectUser(username,password, LoginActivity.this);
            }
		}
	};


	public static void expand(final View v) {
		v.setVisibility(View.VISIBLE);
	}

	public static void collapse(final View v) {
        v.setVisibility(View.GONE);
    }

	private void startMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void toggleRegistering() {
        this.isRegistering = !this.isRegistering;
        this.setRegistering(this.isRegistering);
	}

	private void setRegistering(boolean registering) {
        MenuItem menuItem = menu.findItem(R.id.action_toggleRegistering);
        if (this.isRegistering) {
            this.mLoginNormalBtn.setText(getString(R.string.register_btn));
            menuItem.setTitle(getString(R.string.login_btn));
            mUsernameET.setHint(R.string.username);
            mPasswordET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        } else {
            this.mLoginNormalBtn.setText(getString(R.string.login_btn));
            menuItem.setTitle(getString(R.string.register_btn));
            mUsernameET.setHint(R.string.email_username);
            mPasswordET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        this.resetLayout();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        callbackManager.onActivityResult(requestCode, resultCode, intent);
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
		snackbarView.setBackgroundColor(Color.RED);//change Snackbar's background color;
		snackbar.show(); // Don’t forget to show!
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
        this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_toggleRegistering:
				toggleRegistering();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


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

    private void saveTokens(String api, String user) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        boolean ans = editor.putString(getString(R.string.SP_APIToken), api)
                .putString(getString(R.string.SP_userID), user)
                .putString(getString(R.string.SP_address),getString(R.string.SP_address_default))
                .commit();
        if(!ans) {
            throw new Exception("PB_string_commit");
        }
    }

    @Override
    public void failure(RetrofitError error) {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        try {
            saveTokens(mTmpApiToken, mTmpUserToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.startMainActivity();
    }

    @Override
    public void onUserFail() {
        mProgressBar.setVisibility(View.GONE);
        showSnackbar(getString(R.string.unknown_error));
    }

	private void showValidationError(int resourceMessageString) {
		mProgressBar.setVisibility(View.GONE);
		new android.support.v7.app.AlertDialog.Builder(this)
					.setTitle(R.string.login_validation_error_title)
					.setMessage(resourceMessageString)
					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.setIcon(R.drawable.ic_warning_black)
					.show();
	}
}
