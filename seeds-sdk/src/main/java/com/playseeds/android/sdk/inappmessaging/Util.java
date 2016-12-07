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
 *
 */

package com.playseeds.android.sdk.inappmessaging;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Util {
	private static String androidAdId;
	private static boolean adDoNotTrack = false;
	private static final float MINIMAL_ACCURACY = 1000;
	private static final long MINIMAL_TIME_FROM_FIX = 1000 * 60 * 20;

	public static boolean isNetworkAvailable(Context ctx) {
		int networkStatePermission = ctx.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);

		if (networkStatePermission == PackageManager.PERMISSION_GRANTED) {

			ConnectivityManager mConnectivity = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

			// Skip if no connection, or background data disabled
			NetworkInfo info = mConnectivity.getActiveNetworkInfo();
			if (info == null) {
				return false;
			}
			// Only update if WiFi
			int netType = info.getType();
			// int netSubtype = info.getSubtype();
			if ((netType == ConnectivityManager.TYPE_WIFI) || (netType == ConnectivityManager.TYPE_MOBILE)) {
				return info.isConnected();
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static String getConnectionType(Context context) {
		int networkStatePermission = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);

		if (networkStatePermission == PackageManager.PERMISSION_GRANTED) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info == null) {
				return Const.CONNECTION_TYPE_UNKNOWN;
			}
			int netType = info.getType();
			int netSubtype = info.getSubtype();
			if (netType == ConnectivityManager.TYPE_WIFI) {
				return Const.CONNECTION_TYPE_WIFI;
			} else if (netType == 6) {
				return Const.CONNECTION_TYPE_WIMAX;
			} else if (netType == ConnectivityManager.TYPE_MOBILE) {
				switch (netSubtype) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					return Const.CONNECTION_TYPE_MOBILE_1xRTT;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return Const.CONNECTION_TYPE_MOBILE_CDMA;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return Const.CONNECTION_TYPE_MOBILE_EDGE;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					return Const.CONNECTION_TYPE_MOBILE_EVDO_0;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					return Const.CONNECTION_TYPE_MOBILE_EVDO_A;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return Const.CONNECTION_TYPE_MOBILE_GPRS;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return Const.CONNECTION_TYPE_MOBILE_UMTS;
				case 14:
					return Const.CONNECTION_TYPE_MOBILE_EHRPD;
				case 12:
					return Const.CONNECTION_TYPE_MOBILE_EVDO_B;
				case 8:
					return Const.CONNECTION_TYPE_MOBILE_HSDPA;
				case 10:
					return Const.CONNECTION_TYPE_MOBILE_HSPA;
				case 15:
					return Const.CONNECTION_TYPE_MOBILE_HSPAP;
				case 9:
					return Const.CONNECTION_TYPE_MOBILE_HSUPA;
				case 11:
					return Const.CONNECTION_TYPE_MOBILE_IDEN;
				case 13:
					return Const.CONNECTION_TYPE_MOBILE_LTE;
				default:
					return Const.CONNECTION_TYPE_MOBILE_UNKNOWN;
				}
			} else {
				return Const.CONNECTION_TYPE_UNKNOWN;
			}
		} else {
			return Const.CONNECTION_TYPE_UNKNOWN;
		}
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(ex.toString());
		}
		return null;
	}

	public static Location getLocation(Context context) {
		boolean HasFineLocationPermission = false;
		boolean HasCoarseLocationPermission = false;

		if (context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			HasFineLocationPermission = true;
			HasCoarseLocationPermission = true;
		} else if (context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			HasCoarseLocationPermission = true;
		}

		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		if (locationManager != null && HasFineLocationPermission && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			if (location == null) {
				return null;
			}

			long timeFromFix = Math.abs(System.currentTimeMillis() - location.getTime());
			if (location.hasAccuracy() && location.getAccuracy() < MINIMAL_ACCURACY && timeFromFix < MINIMAL_TIME_FROM_FIX) {
				return location;
			}
		}
		if (locationManager != null && HasCoarseLocationPermission && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			if (location == null) {
				return null;
			}
			long timeFromFix = Math.abs(System.currentTimeMillis() - location.getTime());
			if (location.hasAccuracy() && location.getAccuracy() < MINIMAL_ACCURACY && timeFromFix < MINIMAL_TIME_FROM_FIX) {
				return location;
			}
		}
		return null;
	}

	public static String getDefaultUserAgentString() {
		String userAgent = System.getProperty("http.agent");
		return userAgent;
	}

	@SuppressLint("DefaultLocale")
	public static String buildUserAgent() {
		String androidVersion = Build.VERSION.RELEASE;
		String model = Build.MODEL;
		String androidBuild = Build.ID;
		final Locale l = Locale.getDefault();
		final String language = l.getLanguage();
		String locale = "en";
		if (language != null) {
			locale = language.toLowerCase();
			final String country = l.getCountry();
			if (country != null) {
				locale += "-" + country.toLowerCase();
			}
		}

		String userAgent = String.format(Const.USER_AGENT_PATTERN, androidVersion, locale, model, androidBuild);
		return userAgent;
	}

	public static int getMemoryClass(Context context) {
		try {
			Method getMemoryClassMethod = ActivityManager.class.getMethod("getMemoryClass");
			ActivityManager ac = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			return (Integer) getMemoryClassMethod.invoke(ac, new Object[] {});
		} catch (Exception ex) {
			return 16;
		}
	}

	public static void prepareAndroidAdId(final Context context) {
		if (androidAdId == null && GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
			Log.d("GooglePlayServices connected");
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					Info adInfo = null;
					try {
						adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
						Log.d("adInfo " + adInfo);
						androidAdId = adInfo.getId();
						Log.d("adId " + androidAdId);
						adDoNotTrack = adInfo.isLimitAdTrackingEnabled();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (GooglePlayServicesNotAvailableException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (GooglePlayServicesRepairableException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

			};
			task.execute();
		}
	}

	public static String getAndroidAdId() {
		if (androidAdId == null) {
			return "";
		}
		return androidAdId;
	}

	public static Bitmap loadBitmap (String url) {
		Bitmap bitmap = null;
		try {
			InputStream in = new URL(url).openStream();
			bitmap = BitmapFactory.decodeStream(in);
		} catch (Throwable t) { //to catch also out of memory error when decoding bitmap.
			bitmap = null;
			Log.e("Decoding bitmap failed!");
		}
		
		return bitmap;
	}
}
