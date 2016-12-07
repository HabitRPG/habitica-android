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
 *		Changes: TAG variable value
 */

package com.playseeds.android.sdk.inappmessaging;

public final class Log {

	/*
	 * Enable logging DEBUG logs on a device: 
	 * adb shell setprop log.tag.ADSDK DEBUG
	 */

	public static final String TAG = "Seeds IAM";
	public static final boolean LOG_AD_RESPONSES = false;

	public static boolean isLoggable(int logLevel) {
//		return android.util.Log.isLoggable(TAG, logLevel);
		return true;
	}

	public static void d(final String msg) {
		if (isLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(TAG, msg);
		}
	}

	public static void d(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.DEBUG)) {
			android.util.Log.d(TAG, msg, tr);
		}
	}

	public static void e(final String msg) {
		if (isLoggable(android.util.Log.ERROR)) {
			android.util.Log.e(TAG, msg);
		}
	}

	public static void e(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.ERROR)) {
			android.util.Log.w(TAG, msg, tr);
		}
	}

	public static void i(final String msg) {
		if (isLoggable(android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg);
		}
	}

	public static void i(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg, tr);
		}
	}

	public static void v(final String msg) {
		if (isLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg);
		}
	}

	public static void v(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg, tr);
		}
	}

	public static void w(final String msg) {
		if (isLoggable(android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg);
		}
	}

	public static void w(final String msg, final Throwable tr) {
		if (isLoggable(android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg, tr);
		}
	}
}