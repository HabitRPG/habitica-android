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
 *		Changes: Removed MRAID and video-specific code
 */

package com.playseeds.android.sdk.inappmessaging;

public interface Const {

	public static final String ENCODING = "UTF-8";
	public static final String RESPONSE_ENCODING = "ISO-8859-1";

	public static final String VERSION = "6.1.0";

	public static final String PROTOCOL_VERSION = "3.0";

	public static final int LIVE = 0;
	public static final int TEST = 1;

	public static final String IMAGE_BODY = "<body style='\"'margin: 0px; padding: 0px; text-align:center;'\"'><img src='\"'{0}'\"' width='\"'{1}'dp\"' height='\"'{2}'dp\"'/></body>";
	//	public static final String IMAGE_BODY = "<body style='\"'margin: 0px; padding: 0px; text-align:center;'\"'><div style=\"background-image:{0} width='\"'{1}'dp\"' height='\"'{2}'dp\"'></div></body>";
	public static final String REDIRECT_URI = "REDIRECT_URI";

	public static final String HIDE_BORDER = "<style>* { -webkit-tap-highlight-color: rgba(0,0,0,0);} img {width:100%;height:100%} body {margin: 0; padding: 0}</style>";
	public static final String INTERSTITIAL_HIDE_BORDER = "<style>* { -webkit-tap-highlight-color: rgba(0,0,0,0);} body {height:100%; width:100%;} img {max-width:100%; max-height:100%; width:auto; height:auto; position: absolute; margin: auto; top: 0; left: 0; right: 0; bottom: 0;}</style>";

	//	public static final String HIDE_BORDER = "<style>* { -webkit-tap-highlight-color: rgba(0,0,0,0) }</style>";

	public static final int TOUCH_DISTANCE = 30;

/*	public static final long VIDEO_LOAD_TIMEOUT = 1200000;*/
	public static final int CONNECTION_TIMEOUT = 10000; // = 15 sec
	public static final int SOCKET_TIMEOUT = 10000; // = 15 sec

	public static final String PREFS_DEVICE_ID = "device_id";

	public static final String USER_AGENT_PATTERN = "Mozilla/5.0 (Linux; U; Android %1$s; %2$s; %3$s Build/%4$s) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	public static final String AD_EXTRA = "RICH_AD_DATA";
	public static final String AD_TYPE_EXTRA = "RICH_AD_TYPE";
	public static final int MAX_NUMBER_OF_TRACKING_RETRIES = 5;

	public static final String CONNECTION_TYPE_UNKNOWN = "UNKNOWN";
	public static final String CONNECTION_TYPE_WIFI = "WIFI";
	public static final String CONNECTION_TYPE_WIMAX = "WIMAX";
	public static final String CONNECTION_TYPE_MOBILE_UNKNOWN = "MOBILE";
	public static final String CONNECTION_TYPE_MOBILE_1xRTT = "1xRTT";
	public static final String CONNECTION_TYPE_MOBILE_CDMA = "CDMA";
	public static final String CONNECTION_TYPE_MOBILE_EDGE = "EDGE";
	public static final String CONNECTION_TYPE_MOBILE_EHRPD = "EHRPD";
	public static final String CONNECTION_TYPE_MOBILE_EVDO_0 = "EVDO_0";
	public static final String CONNECTION_TYPE_MOBILE_EVDO_A = "EVDO_A";
	public static final String CONNECTION_TYPE_MOBILE_EVDO_B = "EVDO_B";
	public static final String CONNECTION_TYPE_MOBILE_GPRS = "GPRS";
	public static final String CONNECTION_TYPE_MOBILE_HSDPA = "HSDPA";
	public static final String CONNECTION_TYPE_MOBILE_HSPA = "HSPA";
	public static final String CONNECTION_TYPE_MOBILE_HSPAP = "HSPAP";
	public static final String CONNECTION_TYPE_MOBILE_HSUPA = "HSUPA";
	public static final String CONNECTION_TYPE_MOBILE_IDEN = "IDEN";
	public static final String CONNECTION_TYPE_MOBILE_LTE = "LTE";
	public static final String CONNECTION_TYPE_MOBILE_UMTS = "UMTS";

	public static final CharSequence LOADING = "Loading....";
	public static final long CACHE_DOWNLOAD_PERIOD = 10 * 60 * 1000;

	public final static int AD_FAILED = -1;
	public final static int IMAGE = 0;
	public final static int TEXT = 1;
	public final static int NO_AD = 2;

}
