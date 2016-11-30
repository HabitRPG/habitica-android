/*
 *		Copyright 2015 MobFox
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *		Unless required by applicable law or agreed to in writing, software
 *		distributed under the License is distributed on an "AS IS" BASIS,
 *		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *		See the License for the specific language governing permissions and
 *		limitations under the License.
 *
 *		Changes: moved from video sub-package, removed video, MRAID and custome event
 *		code
 */

package com.playseeds.android.sdk.inappmessaging;

import java.lang.ref.WeakReference;

import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.FrameLayout;
import android.widget.ImageView;

import com.playseeds.android.sdk.inappmessaging.InAppMessageView.BannerAdViewListener;

public class RichMediaActivity extends Activity {
	private ResourceManager mResourceManager;
	private FrameLayout mRootLayout;
	private ImageView mSkipButton;
	private InAppMessageResponse mAd;
	private int mType;
	private boolean mResult;

	int skipButtonSizeLand = 40;
	int skipButtonSizePort = 40;
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_BROWSER = 0;
	public static final int TYPE_INTERSTITIAL = 2;

	DisplayMetrics metrics;

	static class ResourceHandler extends Handler {
		WeakReference<RichMediaActivity> richMediaActivity;

		public ResourceHandler(RichMediaActivity activity) {
			richMediaActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(final Message msg) {
			RichMediaActivity wRichMediaActivity = richMediaActivity.get();
			if (wRichMediaActivity != null) {
				wRichMediaActivity.handleMessage(msg);
			}
		}
	}

	public void handleMessage(final Message msg) {
		switch (msg.what) {
			case ResourceManager.RESOURCE_LOADED_MSG:
				switch (msg.arg1) {
					case ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID:
						if (RichMediaActivity.this.mSkipButton != null) {
							RichMediaActivity.this.mSkipButton.setImageDrawable(mResourceManager.getResource(this, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID));
						}
					break;
				}
			break;
		}
	}

	OnClickListener mOnInterstitialSkipListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			Log.v("###########TRACKING SKIP INTERSTITIAL");
			RichMediaActivity.this.close();
		}
	};


	@Override
	public void finish() {
		super.finish();

		if (this.mAd != null) {
			Log.d("Finish Activity type:" + this.mType + " ad Type:" + this.mAd.getType());
			InAppMessageManager.closeRunningInAppMessage(this.mAd);
		}
	}

	public void goBack() {
		switch (this.mType) {
			case TYPE_INTERSTITIAL:
				this.mResult = true;
				this.setResult(Activity.RESULT_OK);
				this.finish();
				break;
			case TYPE_BROWSER:
				this.finish();
				break;
		}
	}

	private void initInterstitialFromBannerView() {
		final FrameLayout layout = new FrameLayout(this);
		if (mAd.getType() == Const.TEXT || mAd.getType() == Const.IMAGE) {
			final DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
			final float scale = displayMetrics.density;

			int adWidth, adHeight;
			if (mAd.isHorizontalOrientationRequested()) {
				adWidth = 1080;
				adHeight = 720;
			} else {
				adWidth = 720;
				adHeight = 1080;
			}

			int width = (int)(displayMetrics.widthPixels / displayMetrics.density);
			int height = (int)(displayMetrics.heightPixels / displayMetrics.density);

			final float adAspectRatio = adWidth / (float) adHeight;
			if (adAspectRatio >= 1.0f)
				width = (int)(height * adAspectRatio + 0.5f);
			else
				height = (int)(width / adAspectRatio + 0.5f);

			InAppMessageView banner = new InAppMessageView(this, mAd, width, height, false, createLocalAdListener());

			if (mAd != null && mAd.getClickUrl() != null) {
				banner.setLayoutParams(new FrameLayout.LayoutParams(
						(int) (width * scale + 0.5f),
						(int) (height * scale + 0.5f),
						Gravity.CENTER));
			} else {
				banner.setLayoutParams(new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT,
						Gravity.CENTER));
			}
			banner.showContent();
			layout.addView(banner);
		}

		if (mAd.getClickUrl() != null) {
			this.mSkipButton = new ImageView(this);
			this.mSkipButton.setAdjustViewBounds(false);

			int buttonSize;
			if (mAd.isHorizontalOrientationRequested()) {
				buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.skipButtonSizeLand, this.getResources().getDisplayMetrics());
			} else {
				buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.skipButtonSizePort, this.getResources().getDisplayMetrics());
			}

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.TOP | Gravity.RIGHT);

			final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources().getDisplayMetrics());
			params.topMargin = margin;
			params.rightMargin = margin;

			this.mSkipButton.setImageDrawable(mResourceManager.getResource(this, ResourceManager.DEFAULT_SKIP_IMAGE_RESOURCE_ID));

			this.mSkipButton.setOnClickListener(this.mOnInterstitialSkipListener);

			this.mSkipButton.setVisibility(View.VISIBLE);

			layout.addView(this.mSkipButton, params);
		}

		this.mRootLayout.addView(layout);
	}

	private BannerAdViewListener createLocalAdListener() {
		return new BannerAdViewListener() {

			@Override
			public void onLoad() {
			}

			@Override
			public void onClick() {
				InAppMessageManager.notifyInAppMessageClick(mAd);
			}

			@Override
			public void onError() {
				finish();
			}
		};
	}

	private void initRootLayout() {
		this.mRootLayout = new FrameLayout(this);
		this.mRootLayout.setBackgroundColor(Color.argb(128, 0, 0, 0));
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d("RichMediaActivity onConfigurationChanged");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		try {
			final Window win = this.getWindow();
			final WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
			final Display display = this.getWindowManager().getDefaultDisplay();
			int mWindowWidth = display.getWidth();
			int mWindowHeight = display.getHeight();
			this.metrics = new DisplayMetrics();

			this.mResult = false;
			this.setResult(Activity.RESULT_CANCELED);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			win.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			wm.getDefaultDisplay().getMetrics(this.metrics);
			win.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

			Log.d("RichMediaActivity Window Size:(" + mWindowWidth + "," + mWindowHeight + ")");
/*
			this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
*/
			this.mType = RichMediaActivity.TYPE_UNKNOWN;
			final Intent intent = this.getIntent();
			final Bundle extras = intent.getExtras();
			if (extras == null || extras.getSerializable(Const.AD_EXTRA) == null) {
				Uri uri = intent.getData();
				Log.d("uri " + uri);

				if (uri == null) {
					this.finish();
					return;
				}

				this.mType = RichMediaActivity.TYPE_BROWSER;
			} else {
				this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			}

			ResourceHandler mHandler = new ResourceHandler(this);
			this.mResourceManager = new ResourceManager();
			this.initRootLayout();

				this.mAd = (InAppMessageResponse) extras.getSerializable(Const.AD_EXTRA);

				this.mType = extras.getInt(Const.AD_TYPE_EXTRA, -1);
				if (this.mType == -1) {
					switch (this.mAd.getType()) {
						case Const.TEXT:
						case Const.IMAGE:
							if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
								setOrientationOldApi();
							} else {
								setOrientation();
							}
							this.mType = TYPE_INTERSTITIAL;
							break;
					}
				}
			switch (this.mType) {
				case TYPE_INTERSTITIAL:
					Log.v("Type interstitial like banner");
					this.initInterstitialFromBannerView();
					break;
			}

			this.setContentView(this.mRootLayout);
			Log.d("RichMediaActivity onCreate done");

		} catch (Exception e) {
			// in unlikely case something goes terribly wrong
			finish();
		}
	}

	private void setOrientationOldApi() {
		if (mAd.isHorizontalOrientationRequested()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void setOrientation() {
		if (mAd.isHorizontalOrientationRequested()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mResourceManager != null) {
			mResourceManager.releaseInstance();
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("RichMediaActivity onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d("RichMediaActivity onResume");
		switch (this.mType) {
			case TYPE_BROWSER:
			break;
		}
	}

	public void close() {
		mResult = true;
		setResult(Activity.RESULT_OK);
		finish();
	}

	protected void setTypeInterstitial(int type) {
		mType = type;
	}
}
