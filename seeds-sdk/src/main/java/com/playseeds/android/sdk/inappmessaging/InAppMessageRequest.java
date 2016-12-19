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
 *		Changes: 	removed video, MRAID and custom ad-specific code
 *					renamed from AdRequest
 */

package com.playseeds.android.sdk.inappmessaging;

import java.util.List;

import android.net.Uri;
import android.os.Build;

import com.playseeds.android.sdk.DeviceId;

public class InAppMessageRequest {
	private String userAgent;
	private String userAgent2;
	private String headers;
	private String listAds;
	private String requestURL;
	private String protocolVersion;
	private String appKey;
	private String deviceId;
	private DeviceId.Type idMode;
	private String messageId;

	private double longitude = 0.0;
	private double latitude = 0.0;
	private boolean adspaceStrict;
	private int adspaceWidth;
	private int adspaceHeight;
	private Gender gender;
	private int userAge;
	private List<String> keywords;
	private String ipAddress;
	private String connectionType;
	private long timestamp;
	private String orientation;
	private String androidAdId = "";
	private boolean adDoNotTrack = false;
	private static final String REQUEST_TYPE_ANDROID = "android_app";
	// sleep in ms if device_id not loaded yet
	private static final int DEVICE_ID_MS_DELAY = 500;

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public void setIdMode(DeviceId.Type idMode) {
		this.idMode = idMode;
	}

	public void setConnectionType(final String connectionType) {
		this.connectionType = connectionType;
	}

	public void setAdDoNotTrack(boolean adDoNotTrack) {
		this.adDoNotTrack = adDoNotTrack;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public void setHeaders(final String headers) {
		this.headers = headers;
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public void setListAds(final String listAds) {
		this.listAds = listAds;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	public void setProtocolVersion(final String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public void setAppKey(final String appKey) {
		this.appKey = appKey;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	public void setUserAgent2(final String userAgent) {
		this.userAgent2 = userAgent;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public void setAdspaceStrict(boolean adspaceStrict) {
		this.adspaceStrict = adspaceStrict;
	}

	public void setAdspaceWidth(int adspaceWidth) {
		this.adspaceWidth = adspaceWidth;
	}

	public void setAdspaceHeight(int adspaceHeight) {
		this.adspaceHeight = adspaceHeight;
	}

	public void setAndroidAdId(String androidAdId) {
		this.androidAdId = androidAdId;
	}

	public String countlyUriToString() {
		return this.toCountlyUri().toString();
	}

	//TODO: make sure "deviceCategory" gets to server code
	public Uri toCountlyUri() {
		String countlyURL = requestURL;
		String path = "/o/messages";
		final Uri.Builder b = Uri.parse((countlyURL + path)).buildUpon();

		b.appendQueryParameter("app_key", appKey);
		b.appendQueryParameter("orientation", orientation);

		if (deviceId == null || deviceId.isEmpty()) {
			deviceId = Util.getAndroidAdId();
			if (deviceId == null || deviceId.isEmpty()) {
				try {
					Thread.sleep(DEVICE_ID_MS_DELAY);
				} catch (InterruptedException e) {
					Log.e("Sleep interrupted: " + e);
				}
				deviceId = Util.getAndroidAdId();
			}
		}

		if (deviceId == null || deviceId.isEmpty()) {
			Log.e("Device Id could not be set");
		}
		b.appendQueryParameter("device_id", deviceId);
		//b.appendQueryParameter("device_id_type", idMode.toString()); //currently unused
		if (messageId != null)
			b.appendQueryParameter("message_id", messageId);

		return b.build();
	}

	public String getRequestURL() {
		return requestURL;
	}

	public boolean isAdspaceStrict() {
		return adspaceStrict;
	}

	public int getAdspaceWidth() {
		return adspaceWidth;
	}

	public int getAdspaceHeight() {
		return adspaceHeight;
	}

	public String getAndroidAdId() {
		return androidAdId;
	}

	public Boolean hasAdDoNotTrack() {
		return adDoNotTrack;
	}

	public String getOrientation() {
		return orientation;
	}

	public DeviceId.Type getIdMode() {
		return idMode;
	}

	public String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	public String getConnectionType() {
		return this.connectionType;
	}

	public String getDeviceMode() {
		return Build.MODEL;
	}

	public String getDeviceId() {
		return deviceId;
	}


	public String getHeaders() {
		if (this.headers == null)
			return "";
		return this.headers;
	}

	public String getIpAddress() {
		if (this.ipAddress == null)
			return "";
		return this.ipAddress;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public String getListAds() {
		if (this.listAds != null)
			return this.listAds;
		else
			return "";
	}

	public double getLongitude() {
		return this.longitude;
	}

	public String getProtocolVersion() {
		if (this.protocolVersion == null)
			return Const.VERSION;
		else
			return this.protocolVersion;
	}

	public String getAppKey() {
		if (this.appKey == null)
			return "";
		return this.appKey;
	}

	public String getRequestType() {
		return InAppMessageRequest.REQUEST_TYPE_ANDROID;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getUserAgent() {
		if (this.userAgent == null)
			return "";
		return this.userAgent;
	}

	public String getUserAgent2() {
		if (this.userAgent2 == null)
			return "";
		return this.userAgent2;
	}

	public Gender getGender() {
		return this.gender;
	}

	public int getUserAge() {
		return this.userAge;
	}

	public List<String> getKeywords() {
		return this.keywords;
	}
}