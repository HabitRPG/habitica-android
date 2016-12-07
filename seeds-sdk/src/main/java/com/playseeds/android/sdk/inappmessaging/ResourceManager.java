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
 *		Changes: moved from video sub-package; removed HttpClient
 */

package com.playseeds.android.sdk.inappmessaging;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.playseeds.android.sdk.BuildConfig;

public class ResourceManager {
	public static final int RESOURCE_LOADED_MSG = 100;
	public static boolean sDownloading = false;
	public static boolean sCancel = false;
	public static final int DEFAULT_TOPBAR_BG_RESOURCE_ID = -1;
	public static final int DEFAULT_BOTTOMBAR_BG_RESOURCE_ID = -2;
	public static final int DEFAULT_PLAY_IMAGE_RESOURCE_ID = -11;
	public static final int DEFAULT_PAUSE_IMAGE_RESOURCE_ID = -12;
	public static final int DEFAULT_REPLAY_IMAGE_RESOURCE_ID = -13;
	public static final int DEFAULT_BACK_IMAGE_RESOURCE_ID = -14;
	public static final int DEFAULT_FORWARD_IMAGE_RESOURCE_ID = -15;
	public static final int DEFAULT_RELOAD_IMAGE_RESOURCE_ID = -16;
	public static final int DEFAULT_EXTERNAL_IMAGE_RESOURCE_ID = -17;
	public static final int DEFAULT_SKIP_IMAGE_RESOURCE_ID = -18;
	public static final int DEFAULT_CLOSE_BUTTON_NORMAL_RESOURCE_ID = -29;
	public static final int DEFAULT_CLOSE_BUTTON_PRESSED_RESOURCE_ID = -30;
	public static final String PLAY_IMAGE_DRAWABLE = "video_play";
	public static final String PAUSE_IMAGE_DRAWABLE = "video_pause";
	public static final String REPLAY_IMAGE_DRAWABLE = "video_replay";
	public static final String BACK_IMAGE_DRAWABLE = "browser_back";
	public static final String FORWARD_IMAGE_DRAWABLE = "browser_forward";
	public static final String RELOAD_IMAGE_DRAWABLE = "browser_reload";
	public static final String EXTERNAL_IMAGE_DRAWABLE = "browser_external";
	public static final String SKIP_IMAGE_DRAWABLE = "skip";
	public static final String BAR_IMAGE_DRAWABLE = "bar";
	public static final String CLOSE_BUTTON_NORMAL_IMAGE_DRAWABLE = "close_button_normal";
	public static final String CLOSE_BUTTON_PRESSED_IMAGE_DRAWABLE = "close_button_pressed";
	private static HashMap<Integer, Drawable> sResources = new HashMap<>();
	private HashMap<Integer, Drawable> mResources = new HashMap<>();

	public ResourceManager() {
	}

	public void releaseInstance(){
		Iterator<Entry<Integer, Drawable>> it = mResources.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Drawable> pairsEntry = (Entry<Integer, Drawable>)it.next();
			it.remove();
			BitmapDrawable d = (BitmapDrawable) pairsEntry.getValue();

		}
		assert(mResources.size()==0);
		System.gc();
	}

	private static void initDefaultResource(Context ctx, int resource) {
		switch (resource) {
		case DEFAULT_PLAY_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_PLAY_IMAGE_RESOURCE_ID, PLAY_IMAGE_DRAWABLE);
			break;
		case DEFAULT_PAUSE_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_PAUSE_IMAGE_RESOURCE_ID, PAUSE_IMAGE_DRAWABLE);
			break;
		case DEFAULT_REPLAY_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_REPLAY_IMAGE_RESOURCE_ID, REPLAY_IMAGE_DRAWABLE);
			break;
		case DEFAULT_BACK_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_BACK_IMAGE_RESOURCE_ID, BACK_IMAGE_DRAWABLE);
			break;
		case DEFAULT_FORWARD_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_FORWARD_IMAGE_RESOURCE_ID, FORWARD_IMAGE_DRAWABLE);
			break;
		case DEFAULT_RELOAD_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_RELOAD_IMAGE_RESOURCE_ID, RELOAD_IMAGE_DRAWABLE);
			break;
		case DEFAULT_EXTERNAL_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_EXTERNAL_IMAGE_RESOURCE_ID, EXTERNAL_IMAGE_DRAWABLE);
			break;
		case DEFAULT_SKIP_IMAGE_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_SKIP_IMAGE_RESOURCE_ID, SKIP_IMAGE_DRAWABLE);
			break;
		case DEFAULT_TOPBAR_BG_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_TOPBAR_BG_RESOURCE_ID, BAR_IMAGE_DRAWABLE);
			break;
		case DEFAULT_BOTTOMBAR_BG_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_BOTTOMBAR_BG_RESOURCE_ID, BAR_IMAGE_DRAWABLE);
			break;
		case DEFAULT_CLOSE_BUTTON_NORMAL_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_CLOSE_BUTTON_NORMAL_RESOURCE_ID,
					CLOSE_BUTTON_NORMAL_IMAGE_DRAWABLE);
			break;
		case DEFAULT_CLOSE_BUTTON_PRESSED_RESOURCE_ID:
			registerImageResource(ctx, DEFAULT_CLOSE_BUTTON_PRESSED_RESOURCE_ID,
					CLOSE_BUTTON_PRESSED_IMAGE_DRAWABLE);
			break;
		}
	}

	private static void registerImageResource(Context ctx, int resId,
											  String drawableName) {
		Drawable d = buildDrawable(ctx, drawableName);
		if (d != null) {
			sResources.put(resId, d);
		} else {
			Log.i("registerImageResource: drawable was null " + drawableName);
			Log.i("Context was " + ctx);
		}
	}

	private static Drawable buildDrawable(Context ctx, String drawableName) {
		try {
			int resourceId = ctx.getResources().getIdentifier(drawableName, "drawable",
					BuildConfig.APPLICATION_ID);
			if (resourceId == 0) {
				resourceId = ctx.getResources().getIdentifier(drawableName, "drawable",
						ctx.getApplicationContext().getPackageName());
			}

			Bitmap b = BitmapFactory.decodeResource(ctx.getResources(), resourceId);
			if (b != null) {

				DisplayMetrics m = ctx.getResources().getDisplayMetrics();
				int w = b.getWidth();
				int h = b.getHeight();
				int imageWidth = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, w, m);
				int imageHeight = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, h, m);
				if ((imageWidth != w) || (imageHeight != h)) {
					b = Bitmap.createScaledBitmap(b, imageWidth, imageHeight,
							false);
				}
				return new BitmapDrawable(ctx.getResources(), b);
			} else {
				throw new FileNotFoundException();
			}
		} catch (Exception e) {
			Log.i("ResourceManager cannot find resource " + drawableName);
		}
		return null;
	}

	public static boolean isDownloading() {
		return sDownloading;
	}

	public static void cancel() {
		sCancel = true;
		sResources.clear();
	}

	public Drawable getResource(Context ctx, int resourceId) {
		BitmapDrawable d;
		d = (BitmapDrawable) mResources.get(resourceId);
		if(d!=null){
			return d;
		}
		return ResourceManager.getStaticResource(ctx, resourceId);
	}

	public static Drawable getStaticResource(Context ctx, int resourceId) {
		BitmapDrawable d = (BitmapDrawable) sResources.get(resourceId);
		boolean isNull = d == null;
		if (isNull || d.getBitmap().isRecycled()) {

			initDefaultResource(ctx, resourceId);

			d = (BitmapDrawable) sResources.get(resourceId);
		}
		return d;
	}

	protected HashMap<Integer, Drawable> getsResources() {
		return sResources;
	}
}
